package com.ibm.cpp.miners.parser.preprocessor;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.*;
import java.lang.*;

public class Trimmer
{
 public  String           fileName    = null;;
 public  int              lineNumber  = 0;
 private BufferedReader   _reader     = null;
 private String           nextLine    = null;

 public Trimmer(String theFile)
 {
  fileName = theFile;
  try
  {
   _reader = new BufferedReader( new FileReader (new File (theFile)));
   lineNumber = 0;
  }
  catch (IOException e) {}
 }
 
 public String readLine()
 {
  return getNextLine();
 }
  
 private boolean inString   = false;
 private boolean inComment  = false;
 
 private String getNextLine()
 {
  try
  {
   nextLine = _reader.readLine();
  }
  catch (IOException e)
  {
   return null;
  }
  
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
   if (inComment)
   {
    if ( (ch == '/') && (prev_ch == '*'))  
    {  
     inComment = false;
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
    inString = !inString;
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
    inComment = true; 
    theLine.deleteCharAt(theLine.length()-1);
    continue;
   }
    
   //Check for extra spaces as long as we're not in a string
   if (!inString)
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






