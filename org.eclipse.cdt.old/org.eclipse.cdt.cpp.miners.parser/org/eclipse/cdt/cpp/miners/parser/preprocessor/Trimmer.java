package com.ibm.cpp.miners.parser.preprocessor;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.*;
import java.lang.*;

/*
 * This class is responsible for reading the file, trimming comments and extraneous 
 * whitespace from the code.  Also line continuation (e.g \ followed directly by 
 * either \n or \r\n) is handled here...If there is line continuation, the Trimmer 
 * concats it all into one line and trims it.  The next call to readLine() will 
 * return a #line directive to let the parser know what the actual current line 
 * of the file is. 
 */
public class Trimmer
{
 public  String           fileName    = null;;
 public  int              lineNumber  = 1;
 private boolean          _inString   = false;
 private boolean          _inComment  = false;
 private boolean          _emitLine   = false;
 private BufferedReader   _reader     = null;

 public Trimmer(File theFile)
 {
  
  try
  {
   fileName = theFile.getCanonicalPath();
   _reader = new BufferedReader(new FileReader (theFile));
   lineNumber = 1;
  }
  catch (IOException e) {}
 }
 
 public String readLine()
 {
  if (_emitLine)
  {
   _emitLine = false;
   return "#line " + lineNumber;
  }
  return getNextLine();
 }
  
 private StringBuffer getContinuedLine()
 {
  StringBuffer continuedLine = new StringBuffer();
  try
  {
   String theLine = _reader.readLine();
   if (theLine == null)
    return null;
   if (theLine.length() == 0)
    return continuedLine;
   continuedLine.append(theLine);
   while (continuedLine.charAt(continuedLine.length()-1) == '\\')
   {
    _emitLine = true;
    try
    {
     String nextnextLine = _reader.readLine();
     if (nextnextLine == null)
      return continuedLine;
     continuedLine.deleteCharAt(continuedLine.length()-1);
     continuedLine.append(nextnextLine);
     lineNumber++;
    }
    catch (IOException f) 
    {
     return continuedLine;
    }
   }
  }
  catch (IOException e) { return null; }
  return continuedLine;
  
 }
 

 private String getNextLine()
 {
  StringBuffer nextLine = getContinuedLine();
  if (nextLine == null)
   return null;
  
  StringBuffer theLine = new StringBuffer("");
  
  char    ch         = '\n';
  char    prev_ch    = '\n';
  int     curPos     = -1;    //It's -1 since we're going to increment at the start of the loop below.
  
  int     lineLength = nextLine.length();
    
  while (++curPos < lineLength) 
  {
   prev_ch = ch;
   ch = nextLine.charAt(curPos);
  
   //Check to see if we are in a Comment
   if (_inComment)
   {
    if ( (ch == '/') && (prev_ch == '*'))  
    {  
     _inComment = false;
     //reset ch and prev_ch since it is like we are starting fresh on this line.
     ch = (prev_ch = '\n');
     continue;
    }
    else
     continue;
   }
    
   //Check to see if we are entering or leaving a string literal
   if ((ch == '"') && (prev_ch != '\\')) 
   {
    _inString = !_inString;
    theLine.append('"');
    continue;
   }
   
   //Check for start of line comment...if we find one, we need to remove the first / from the output string.
   if ((ch == '/') && (prev_ch == '/'))
   { 
    curPos = lineLength;
    theLine.deleteCharAt(theLine.length()-1);
    continue;
   }
    
   //Check for start of comment...if we find one, we need to remove the / from the output string.
   if ((ch == '*') && (prev_ch == '/'))
   {
    _inComment = true; 
    theLine.deleteCharAt(theLine.length()-1);
    continue;
   }
    
   //Check for extra spaces as long as we're not in a string
   if (!_inString)
   {
    if ( ((ch == ' ') || (ch == '\t')) && ((prev_ch == ' ') || (prev_ch == '\t')) )
     continue;  
   }
     
   //If we get here, then we probably want to append the character to the line.
   theLine.append(ch);
  }
  lineNumber++;
  return theLine.toString();
 }

}






