package com.ibm.cpp.miners.parser.preprocessor;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import java.lang.*;
import java.util.*;


public class Macro
{
 private String        _name = null;
 private StringBuffer  _replacement = new StringBuffer();
 private int           _parameters;
 private boolean       _isFunctionStyle = false;
 
 //Construct a macro object by sending in the whole line after the #define
 //There are 2 kinds of macro defintions:
 // OBJECT-LIKE   =>  #define <identifier> <token-string>
 // FUNCTION-LIKE =>  #define <identifier>( <identifier>, ... ) <token-string>
 public Macro(String macroLine)
 {
  _name = getIdentifier(macroLine.trim());
    
  //Check to make sure there is a name, and also if there a replacement 
  if ( (_name == null) || (_name.length() == macroLine.length()))
   return;
   
  //If there is no space or open paren following the identifier, just return
  char c = macroLine.charAt(_name.length());
    
  //If there's a space following the identifier, we have an object-style macro
  if (Character.isWhitespace(c))
   _replacement.append(macroLine.substring(_name.length(), macroLine.length()).trim());
    
  //Finally, if we get here, we are dealing with an function-style macro
  else if (c == '(')
  {
   _isFunctionStyle = true;
   int closingparen = macroLine.indexOf(')',_name.length());
   String parameterList = macroLine.substring(_name.length()+1,closingparen).trim();
   ArrayList parameters = getParameters(parameterList);
   _parameters  = parameters.size();
   _replacement.append(macroLine.substring(closingparen + 1, macroLine.length()).trim());
   augmentReplacement(parameters);
  }
 }
 
 public String getName()
 {
  return _name;
 }
 
 public StringBuffer getReplacement()
 {
  return _replacement;
 }
 
 public String expand(String parameterList)
 {
  if (_replacement == null)
   return "";
  if (parameterList == null)
   return _replacement.toString();
   
  ArrayList theParams = getParameters(parameterList);
  StringBuffer expansion = new StringBuffer(_replacement.toString());
  
  for (int i = theParams.size()-1; i>=0; i--)
  {
   int index = 0;
   String theParam = (String)theParams.get(i);
   if (theParam != null)
    while ( (index = expansion.toString().indexOf("%%" + i + "%%")) >=0)
     expansion.replace(index, index + 5, theParam);
  }
  return expansion.toString();
 }

 //Parse out the first identifier, and return it
 //Assume the string comes in "trimmed"
 private String getIdentifier(String macroLine)
 {
  StringBuffer theIdentifier = new StringBuffer();
  
  int index = 0;
  int lineLength = macroLine.length();
  char c;
  
  while ((index < lineLength) && Character.isJavaIdentifierStart(c=macroLine.charAt(index++)))
   theIdentifier.append(c);
  if (theIdentifier.length() == 0)
   return null;
  return theIdentifier.toString();
 }
 
 private ArrayList getParameters(String theList)
 {
  int nextComma;
  ArrayList theParams = new ArrayList();
  if (theList.length() == 0)
   return theParams;
  
  while ((nextComma=theList.indexOf(',')) >= 0)
  {
   theParams.add(theList.substring(0,nextComma).trim());
   theList = theList.substring(nextComma + 1, theList.length());
  }
  theParams.add(theList.trim());
  return theParams;
 }
 
 private void augmentReplacement(ArrayList theParams)
 {
  for (int i = theParams.size()-1; i >=0 ; i--)
  {
   String theParam = (String)theParams.get(i);
   int index = 0;
   int startAt = 0;
   while ( (index = _replacement.toString().indexOf(theParam,startAt)) >=0)
   {
    if (isValidLocation(index, index + theParam.length() - 1))
    {
     _replacement.replace(index, index + theParam.length(), "%%"+i+"%%");
     index += 4;
    }
    startAt = index + 1;
   }
  }
  //Let's replace all the tabs, so they don't show up in the UI
  _replacement = new StringBuffer( _replacement.toString().replace('\t', ' '));
 }
 
 //Check to make sure that there are no identifier characters before or after the match...The intent is to 
 //check that we didn't just find a superstring...ie found 'abc' when looking for just 'b'
 private boolean isValidLocation(int begin, int end)
 {
  boolean result = false;
  if ((begin == 0) || (!Character.isJavaIdentifierStart(_replacement.charAt(begin-1))))
   if ((_replacement.length() == (end+1)) || (!Character.isJavaIdentifierStart(_replacement.charAt(end+1))))
    return true;
  return false;
 }
}

