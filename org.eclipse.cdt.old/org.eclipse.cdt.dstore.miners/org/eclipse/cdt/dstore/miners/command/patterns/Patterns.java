package com.ibm.dstore.miners.command.patterns;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.util.regex.text.regex.*;

import java.util.*;
import java.lang.*;
import java.text.*;
import java.io.*;

public class Patterns
{
 private ArrayList         _theCommands;
 private DataStore         _dataStore;
 private PatternCompiler   _compiler;
 private PatternMatcher    _matcher;
 private String            _currentCommand;
 private long              _timeStamp = 0;
 public Patterns(DataStore ds)
 { 
  _dataStore   = ds;
  _theCommands = new ArrayList();
  _compiler    = new Perl5Compiler();
  _matcher     = new Perl5Matcher();
  parsePatternsFile();
 }
 
 

 public void refresh(String theCommand)
 {
  _currentCommand = theCommand;
  parsePatternsFile();
 }
 
 private void parsePatternsFile()
 {
  //Check the timestamp of the patterns.dat file to make sure we need to read it.
  
  File thePatternsFile;
  if (_dataStore == null)
   return;
  thePatternsFile = new File(_dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + "/com.ibm.dstore.miners/patterns.dat");
  
  long newTimeStamp = 0;
  if (!thePatternsFile.exists() || ((newTimeStamp = thePatternsFile.lastModified()) == _timeStamp))
   return;
  _timeStamp = newTimeStamp;
  
  //If we get here, we are actually going to read\parse the file. 
  BufferedReader reader = null;
  try 
  {
   reader =  new BufferedReader(new FileReader(thePatternsFile));
   _theCommands.clear();
 
   String         curLine;
   CommandPattern curCommand = null;
 
   //Main Loop that reads each line.
   while ((curLine = reader.readLine()) != null)
   {
    curLine = curLine.trim();
    //Skip the current line if it is empty or starts with a #
    if ( (curLine.length() == 0) || (curLine.charAt(0) == '#'))
     continue;
            
    //Check if this line is the start of a new command section
    if (curLine.startsWith("command"))
    {
     int colon = curLine.indexOf(":");
     //Check that there is something after the colon
     if (colon == (curLine.length()-1))
      continue;
     curCommand = new CommandPattern(_compiler.compile(curLine.substring(colon+1, curLine.length()).trim()));
     _theCommands.add(curCommand);
    }
   
    //If we get here, the line must be an output pattern 
    else
    {
     int firstSpace  = curLine.indexOf(" ");
     int patternWord = curLine.indexOf("pattern");
     int firstEquals = curLine.indexOf("=");
     if ( (firstEquals == -1) || (firstEquals == (curLine.length()-1)))
      continue;
     String  objType       = curLine.substring(0, firstSpace);
     String  matchOrder    = curLine.substring(firstSpace+1,patternWord).trim();
     String  patternString = curLine.substring(firstEquals+1, curLine.length());
     Pattern thePattern    = _compiler.compile(patternString.trim());
         
     if (curCommand != null)
      curCommand.addOutputPattern(new OutputPattern(objType, matchOrder, thePattern));
    }
   }
  }
  catch (FileNotFoundException e) 
  {
   System.out.println(e.getMessage());
   return;
  }
  catch (MalformedPatternException e)
  {
   System.out.println(e.getMessage());
   return;
  }
  catch (IOException e)
  {
   System.out.println(e.getMessage());
   return;
  }
 }

 public ParsedOutput matchLine(String theLine)
 {
  CommandPattern curCommand;
  ParsedOutput   matchedOutput = null;
  int commands = _theCommands.size();
  
  for (int i=0; i<commands; i++)
  {
   curCommand = (CommandPattern)_theCommands.get(i);
   if (curCommand.matchCommand(_currentCommand, _matcher))
    matchedOutput = curCommand.matchLine(theLine, _matcher);
     if (matchedOutput != null)
      return matchedOutput;
  }
  return null;
 }
}
 

 

 









