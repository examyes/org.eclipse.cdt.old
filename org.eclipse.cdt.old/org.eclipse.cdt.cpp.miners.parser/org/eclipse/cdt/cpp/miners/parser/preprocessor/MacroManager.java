package com.ibm.cpp.miners.parser.preprocessor;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import com.ibm.dstore.core.model.*;

class MacroManager
{
 private Hashtable _macros;
 
 public MacroManager ()
 {
  _macros = new Hashtable();
 }
 
 public void reset() 
 {
  _macros.clear();
 }
 
 public Hashtable getMacros () 
 { 
  return _macros; 
 }

 public void rememberMacro(String macroLine, String location)
 {
  Macro theMacro = new Macro(macroLine);
  String theName = theMacro.getName();
    
  if ( (theName == null) || (theName.length() < 1))
   return;
  
  if (_macros.containsKey(theName))
   {
    Macro m = (Macro)_macros.get(theName);
    _macros.remove(m);
   }
  
  _macros.put(theName, theMacro);
 }
					 
 public void forgetMacro(String macroLine) 
 {
  String theName = getIdentifier(macroLine,0);
  if ( (theName == null) || (theName.length() < 1))
   return;

  //Just going to blindly remove a macro with the same name...should check number of arguments among other things
  _macros.remove(theName);
 }
 
 private String getIdentifier(String currentLine, int startAt)
 {
    
  int lineLength = currentLine.length();
  
  //Skip to start of next identifier
  while ( (startAt < lineLength) && !Character.isJavaIdentifierStart(currentLine.charAt(startAt)))
  {
   startAt++;
  }

  //Check to make sure we're not at the end of the line
  if (startAt >= lineLength)
   return null;
    
  int endAt = startAt;
 
  while ( (endAt < lineLength) && Character.isJavaIdentifierStart(currentLine.charAt(endAt)))
   endAt++;
 
  return currentLine.substring(startAt, endAt);
  
 }
 
 public String expandMacros(String sourceLine) 
 {
  StringBuffer currentLine = new StringBuffer(sourceLine);
  int    startIndex = 0;
  int    endIndex   = 0;
  
  String curId;
  String params = null;
  Macro  theMacro;
    
  while ( (curId = getIdentifier(currentLine.toString(), startIndex)) != null)
  {
   if (curId.length() == 0)
    break;
   
   startIndex = currentLine.toString().indexOf(curId,startIndex);
   endIndex   = startIndex + curId.length();
   
   //Check to see if it's a known macro:
   theMacro = (Macro)_macros.get(curId);
   if (theMacro != null)
   {
    //Check to see if it's function-style:
    if ( (endIndex < currentLine.length()) && (currentLine.charAt(endIndex) == '('))
    {
     int closeParen = getClosingParen(currentLine.toString(), endIndex+1);
     if (closeParen > (endIndex + 1))
     {
      params = currentLine.toString().substring(endIndex + 1, closeParen);
      endIndex = closeParen+1;
     }
    }
     
    String theExpansion = theMacro.expand(params);
       
    if (theExpansion == null)
     theExpansion = "";
    
    currentLine.replace(startIndex, endIndex, theExpansion);
    startIndex = startIndex + theExpansion.length();
   }
   else
    startIndex = endIndex;
  }
   
  return currentLine.toString();
 }
 
 //Start searching in theLine for a ), being careful to match inner parentheses
 public int getClosingParen(String theLine, int start)
 {
  int depth = 1;
  int iter  = start;
  boolean done = false;
  
  char c;
  
  while ( (!done) && (iter < theLine.length()) )
  {
   c = theLine.charAt(iter);
   if (c == '(')
    depth++;
   else if ( c == ')')
    depth--;
   
   if (depth == 0)
    done = true;
   else 
    iter++;
  }
  if (depth != 0)
   iter = -1;
  return iter;
 }
 
 public String toString()
 {
 
  StringBuffer out = new StringBuffer();
  for (Enumeration e = _macros.elements(); e.hasMoreElements() ;) 
  {
   Macro m = (Macro)_macros.get(e.nextElement());
   StringBuffer rep = m.getReplacement();
   if (rep == null)
    rep = new StringBuffer("JEFF");
   
   out.append(m.getName() + "====" + rep + "\n");
  }
  return out.toString();
 }
 
 
 public static void main (String args[])
 {
  try
   {
    
   MacroManager mm = new MacroManager();
   String m = "_IMPORT _Import";
   mm.rememberMacro(m,"");
   m = "_LNK_CONV _Optlink";
   mm.rememberMacro(m,"");
   
   String s = mm.expandMacros("     extern int      _IMPORT _LNK_CONV abs (int);");
   
   System.out.println(m + "\n" + s);
  }
  catch (Throwable e) 
   {
    e.printStackTrace();
    
    System.out.println(e.getMessage());
   }
 }
 
  
}

  














