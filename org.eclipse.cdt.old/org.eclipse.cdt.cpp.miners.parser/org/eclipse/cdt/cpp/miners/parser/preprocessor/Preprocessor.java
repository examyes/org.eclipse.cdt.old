package org.eclipse.cdt.cpp.miners.parser.preprocessor;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

/*
Following the description of preprocessing in chapter 16 of the Annotated C++ Reference Manual,
I do preprocessing here in different stages...Here's how the responsibilites are divided:
SourceReader.java:
 1. Replace tri-graphs
 2. Handle line concatenation (via \)
 3. Replace both style of comments with a single space

Preprocessor.java:
 4. Locate and execute preprocesor directives
 5. Expand Macros
 6. Concatenate adjacent string literals (ie "foo" "bar" => "foobar")
*/

import java.io.*;
import java.lang.*;
import java.util.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.miners.parser.invocation.*;

public class Preprocessor
{
 //Objects with state
 private SourceReader           _sourceReader;
 private MacroManager           _macroManager;
 private TruthStack             _truthStack;
 private HashMap                _directives;
 private Vector                 _includes;
 private StringBuffer           _emit = null;
 private Stack                  _emitStack; 
 private Stack                  _fileStack;
 private File                   _theFile;
 private PreprocessWorker       _ppWorker;


 public Preprocessor(PreprocessWorker ppWorker)
 {
  _ppWorker               = ppWorker;
  _sourceReader           = new SourceReader(this);
  _macroManager           = new MacroManager();
  //_evaluator              = new Evaluator();
  _truthStack             = new TruthStack();
  _includes               = new Vector();
  _emitStack              = new Stack();
  _fileStack              = new Stack();

  //Set up a Hashtable for easy access to the defined preprocessor directives
  _directives = new HashMap(); 
  _directives.put(""        ,new Integer(0));
  _directives.put("if"      ,new Integer(1));
  _directives.put("ifdef"   ,new Integer(2));
  _directives.put("ifndef"  ,new Integer(3));
  _directives.put("elif"    ,new Integer(4));
  _directives.put("else"    ,new Integer(5));
  _directives.put("endif"   ,new Integer(6));
  _directives.put("include" ,new Integer(7));
  _directives.put("define"  ,new Integer(8));
  _directives.put("undef"   ,new Integer(9));
  _directives.put("line"    ,new Integer(10));
  _directives.put("error"   ,new Integer(11));
  _directives.put("pragma"  ,new Integer(12));
 }

 public void reset()
 {
  _sourceReader.reset();
  _macroManager.reset();
  _truthStack.reset();
 }
 
 public void pushState()
 {
  _sourceReader.pushState();
  if (_theFile != null)
   _fileStack.push(_theFile);
  if (_emit != null)
   _emitStack.push(_emit);
 }
 
 
 public void popState()
 {
  _sourceReader.popState();
  if (!_emitStack.empty())
  {
   _emit = (StringBuffer)_emitStack.peek();
   _emitStack.pop();
  }
  if (!_fileStack.empty())
  {
   _theFile = (File)_fileStack.peek();
   _fileStack.pop();
  }
 }
   
 public void setIncludes(DataElement includes)
 {
  if (includes == null) 
   return;
  _includes.clear();
  ArrayList incs = includes.getNestedData();
  if (incs != null)
  {
   for(int i=0; i<incs.size(); i++)
   { 
    String path = new String(((DataElement)incs.get(i)).getName());
    path = path.replace('/', java.io.File.separator.charAt(0));
    _includes.add(path);
   }
  }
 } 
 
 public void setFile(File theFile)
 {
  _theFile = theFile;
 }

 public StringBuffer preprocess()
 {
  _emit = new StringBuffer();
  if (_theFile.length() == 0)
   return _emit; 
  _sourceReader.setFile(_theFile);
  String curLine = null;
  while ( (curLine = _sourceReader.getNextLine()) != null)
   _emit.append(process(curLine) + "\n");
  return _emit;
 }
 
 private String process(String line)
 {
  //First we need to replace the trigraph sequences:
  line = replaceTrigraphs(line);
  int loc = line.indexOf("#");

  if (loc == -1)
   return scan(line);

  if (line.trim().charAt(0) != '#') //Directive is not "alone"...there is something before it
   return scan(line.substring(0,loc)) + processDirective(line.substring(loc,line.length()),false);
  return processDirective(line.substring(loc,line.length()),true); //Directive is alone on the line
 }

 private String scan(String line)
 {
  if (line == null)  return "";
  if (_truthStack.peek() == _truthStack.T) return _macroManager.expandMacros(line);
  return "";
 }

 private String replaceTrigraphs(String line)
 {
  int       lineLength = line.length();
  if (lineLength < 3)
   return line;
  
  StringBuffer theLine = new StringBuffer();
  char               a;
  char               b;
  char               c;
  int             iter = 2;
  while (iter < lineLength)
  {
   a = line.charAt(iter-2);
   b = line.charAt(iter-1);
   c = line.charAt(iter);
   
   if ( ( a == '?') && ( b == '?') )
    switch (c)
    {
     case '=' : theLine.append('#');  iter += 3; break;
     case '/' : theLine.append('\\'); iter += 3; break;
     case '\\' : theLine.append('^');  iter += 3; break;
     case '(' : theLine.append('[');  iter += 3; break;
     case ')' : theLine.append(']');  iter += 3; break;
     case '!' : theLine.append('|');  iter += 3; break;
     default  : theLine.append(a);    iter++;    break;
    }
   else
   {
    theLine.append(a);
    iter++;
    if (iter >= lineLength)
    {
     theLine.append(b);
     theLine.append(c);
    }
   }
  }
  return theLine.toString();
 }
 
 private String stripComments (String line)
 {
  int    cComment     = line.indexOf("/*");
  int    cppComment   = line.indexOf("//");
  String strippedLine = line.trim();
  
  if (cComment > -1)  
   strippedLine = line.substring(0,cComment).trim();
  if ( (cppComment > -1) && (cppComment < strippedLine.length()) ) 
   strippedLine = line.substring(0,cppComment).trim();
  
  return strippedLine;
 }
 private String processDirective(String directive,boolean alone)
 {
  //alone tells whether the directive needs to take care of spitting out a blank line
  //or whether there was other text that will take care of that, ie:  int x = 3;  #pragma foo

  //Assuming directive[0] is "#", trim following whitespace  
  directive = directive.substring(1,directive.length());
  
  //Search for the end of the directive by checking for whitespace
  int endOfDirective;
  for (endOfDirective=0; endOfDirective<directive.length(); endOfDirective++)
   if (Character.isWhitespace(directive.charAt(endOfDirective)))
    break;
 
 //Store the directive arguments (ie the rest of the line after the directive)
 String dArgs = stripComments( directive.substring(endOfDirective,directive.length() ) );

  //Get the Integer corresponding to the directive name, so we can do a switch below
 Integer dInteger = (Integer)_directives.get(directive.substring(0,endOfDirective));
 if (dInteger == null)  //If this is true...we have a bogus directive
   dInteger = new Integer(-1);
 
  switch (dInteger.intValue()) 
  {
   case 0:  handleNullDirective(dArgs);     if (alone) return ("");   break;
   case 1:  handleIfDirective(dArgs);       if (alone) return ("");   break;
   case 2:  handleIfdefDirective(dArgs);    if (alone) return ("");   break; 
   case 3:  handleIfndefDirective(dArgs);   if (alone) return ("");   break; 
   case 4:  handleElifDirective(dArgs);     if (alone) return ("");   break;
   case 5:  handleElseDirective(dArgs);     if (alone) return ("");   break; 
   case 6:  handleEndifDirective(dArgs);    if (alone) return ("");   break;
   case 7:  handleIncludeDirective(dArgs);  if (alone) return ("");   break; 
   case 8:  handleDefineDirective(dArgs);   if (alone) return ("");   break;
   case 9:  handleUndefDirective(dArgs);    if (alone) return ("");   break;
   case 10: handleLineDirective(dArgs);     if (alone) return ("");   break;
   case 11: handleErrorDirective(dArgs);    if (alone) return ("");   break;
   case 12: handlePragmaDirective(dArgs);   if (alone) return ("");   break;
   default: handleBogusDirective(dArgs);    if (alone) return ("");   break;
  }
  return null;
 }

 //For now I do nothing with a Null, Error, Pragma or Bogus Directive
 //At some point I should handle Line and Error. 
 private void handleNullDirective(String dArgs) {}
 private void handleErrorDirective(String dArgs) {}
 private void handlePragmaDirective(String dArgs) {}
 private void handleBogusDirective(String dArgs) {}
 
 //The following methods handle conditional inclusion (16.1)
 private boolean evaluateExpression(String expression)
 {
  return true;
 }
  
 private void handleIfDirective(String expression)
 {
  if (_truthStack.peek() == _truthStack.T)
   if (evaluateExpression(expression))
    _truthStack.push(_truthStack.T);
   else
    _truthStack.push(_truthStack.F);
  else
   _truthStack.push(_truthStack.I);
 }
 
 private void handleElifDirective(String expression)
 {
  if (_truthStack.empty())
  {
   //addError("Found #elif with no matching #if");
   return;
  }
  if (_truthStack.peek() == _truthStack.T)
  {
   _truthStack.pop();
   _truthStack.push(_truthStack.F);
  }
  else if ( (_truthStack.peek()  == _truthStack.F) && (evaluateExpression(expression)) )
  {
   _truthStack.pop();
   _truthStack.push(_truthStack.T);
  }
 }
 
 private void handleEndifDirective(String line)
 {
  if (_truthStack.empty())
   {}//addError("Found #endif with no matching #if");
  else 
   _truthStack.pop();
 }
 
 //Handle the #ifdef directive.  "#ifdef x" is equivalent to "#if defined x",
 //so we'll prepend "defined " to identifier, and let handleIfDirective() do the work
 private void handleIfdefDirective(String identifier)
 {
  handleIfDirective("defined " + identifier);
 }
  
 //Handle the #ifndef directive.  "#ifndef x" is equivalent to "#if !defined x",
 //so we'll prepend "!defined " to identifier, and let handleIfDirective() do the work
 private void handleIfndefDirective(String identifier)
 {
  handleIfDirective("nope");
  //handleIfDirective("!defined " + identifier);  
 }
 
 //Handle the #else directive.  "#else" is equivalent to "elif 1", so we'll
 //just call handleElifDirective("1")
 private void handleElseDirective(String line)
 {
  if (_truthStack.empty())
   {}//addError("Found #else with no matching #if");
  else
   handleElifDirective("1");
 }
  
 //The following method handles Source File Inclusion
 private void handleIncludeDirective(String fileString)
 {
  if (fileString.length() < 3)
   return;
  
  //First expand all macros and trim Whitespace.
  fileString = _macroManager.expandMacros(fileString).trim();
  
  //Determine whether angle brackets (<>) or quotes are used (otherwise the line is invalid)
  char    firstChar       = fileString.charAt(0);
  char    lastChar        = fileString.charAt(fileString.length()-1);
  boolean quoteDelimiters = false;
  if ( (firstChar == '"') && (lastChar == '"') )
   quoteDelimiters = true; 
  else if ((firstChar != '<') || (lastChar != '>') )
  {
   //addError("FileName must be enclosed by matching <...> or \"...\"");
   return;
  }
  
  //Chop off the delimiters 
  fileString = fileString.substring(1,fileString.length()-1);

  //FIX THIS SHould look at the current directory If quote Delimiters were used, then first search the current directory
  File theFile = findFile(fileString);
  
  if (theFile == null)
   _emit.append("#include \"" + fileString + "\""); 
  
  else 
   {
    _emit.append("#include \"" + theFile.getPath().replace('\\','?') + "\""); 
    _ppWorker.preprocessIncludeFile(theFile);
   //System.out.println(_emit.toString());
   }
 }
 
 private void handleLineDirective(String dArgs) 
 {
  //For now we just handle our own #line directives from the Trimmer...we just emit them, and let the parser deal
  //with them
  _emit.append("#line " + dArgs);
 }

 //The following methods handle Macro Replacement (16.3)
 private void handleDefineDirective(String line)
 {
  //Let the MacroManager parse the line
  _macroManager.rememberMacro(line.trim(), _sourceReader.currentFile() + ":" + _sourceReader.currentLine());
  _emit.append("#define " + line.replace('\t', ' ')); 
 }
 
 private void handleUndefDirective(String identifier)
 {
  //Let the MacroManager parse the line and maybe spit out warnings if there 
  //is extra text.
  _macroManager.forgetMacro(identifier);
 }


 public void addError(String theText)
 {
 
 }

 //POSSIBLY PLATFORM DEPENDENT (ie check 390, 400)
 private File findFile (String fileName)
 {
  File theFile;
  String currentFile = _sourceReader.currentFile();
  
  //First look in the current directory.
  int lastSep      = currentFile.lastIndexOf("/");
  int lastBackSep  = currentFile.lastIndexOf("\\");
  if (lastBackSep > lastSep) lastSep = lastBackSep;
  
  theFile = new File (currentFile.substring(0,lastSep) + File.separator + fileName);
  if (theFile.exists())
   return theFile;
  
  //If we get here the file is not in the current directory
  for (int i = 0; i < _includes.size(); i++)
  {
   theFile  = new File((String)_includes.get(i) + File.separator + fileName);
   if (theFile.exists())
    return theFile;
  }
  //addError("Could not find " + fileName);
  return null;
 }
 
 //Standalone preprocessor driver
 public static void main(String args[])
 {
  // Preprocessor _thePP= new Preprocessor();
  //String huge = _thePP.preprocess(args[0]);
  //System.out.println(huge);
 }
}














