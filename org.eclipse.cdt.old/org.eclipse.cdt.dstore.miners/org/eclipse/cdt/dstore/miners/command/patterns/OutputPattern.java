package org.eclipse.cdt.dstore.miners.command.patterns;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.util.regex.text.regex.*;
import java.util.*;

public class OutputPattern
{
 private Pattern   _pattern;
 private String    _objType;
 private ArrayList _matchOrder;
 
 public OutputPattern(String objType, String matchOrder, Pattern thePattern)
 {
  _objType = objType;
  _pattern = thePattern;
  
  _matchOrder = new ArrayList();
  //Here we add a dummy first element to the ArrayList, to mimick how the PatternMatcher stores it's 
  //matches (starting with group 1).
  _matchOrder.add(null);
  
  int index     =0;
  int nextSpace =0;
  //Walk the matchOrder string parsing out words and adding them to _matchOrder...Could use StringTokenizer
  //but this seem much simpler.
  while ( (nextSpace = matchOrder.indexOf(" ",index)) > 0)
  {
   _matchOrder.add(matchOrder.substring(index, nextSpace).toLowerCase());
   index=nextSpace;
   while ( (index < matchOrder.length()) && (matchOrder.charAt(index) == ' ') )
    index++;
  }
  _matchOrder.add(matchOrder.substring(index, matchOrder.length()).toLowerCase());
  
 }

 public ParsedOutput matchLine(String theLine, PatternMatcher matcher)
 {
  MatchResult result = null;
  try
  {
  if (!matcher.matches(theLine, _pattern))
   return null;
  result = matcher.getMatch(); 
  }
  catch (StringIndexOutOfBoundsException e)
  {
   //Getting an exception here, when theLine is an empty line for some patterns..should probably investigate, 
   //but for now we'll just handle it...
   return null;
  }
  
  String fileString = "";
  String lineString = ""; 
  //Groups start at 1 (group 0 is the entire match).
  for (int i = 1; i<_matchOrder.size(); i++)
  {
   if (((String)_matchOrder.get(i)).equals("file"))
    fileString = result.group(i);
   else if (((String)_matchOrder.get(i)).equals("line"))
    lineString = result.group(i);
  }
  int line = 1;
  try
  {
   line = Integer.parseInt(lineString);
  }
  catch (NumberFormatException e) {}
 
  return new ParsedOutput(_objType, theLine, fileString, line, 1);
 }


}






