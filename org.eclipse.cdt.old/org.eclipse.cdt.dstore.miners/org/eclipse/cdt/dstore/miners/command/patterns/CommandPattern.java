package org.eclipse.cdt.dstore.miners.command.patterns;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.util.regex.text.regex.*;
import java.util.*;

public class CommandPattern
{
 private Pattern   _pattern;
 private ArrayList _outputPatterns;

 public CommandPattern(Pattern theCommandPattern)
 {
  _pattern = theCommandPattern;
  _outputPatterns = new ArrayList();
 }

 public void addOutputPattern(OutputPattern op)
 {
  _outputPatterns.add(op);
 }
 
 public boolean matchCommand(String theLine, PatternMatcher matcher)
 {
  return matcher.matches(theLine, _pattern);
 }
 
 public ParsedOutput matchLine(String theLine, PatternMatcher matcher)
 {
  int patterns = _outputPatterns.size();
  ParsedOutput  matchedOutput;
  OutputPattern curPattern;
  for (int i=0; i<patterns; i++)
  {
   curPattern = (OutputPattern)_outputPatterns.get(i);
   matchedOutput = curPattern.matchLine(theLine, matcher);
   if (matchedOutput != null)
    return matchedOutput;
  }
  return null;
 }


}


