package com.ibm.cpp.miners.parser.preprocessor;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.*;
import java.util.*;
class SourceReader 
{
 private Stack           _trimmerStack;
 private Trimmer         _currentTrimmer;
 public SourceReader (Preprocessor pp)
 {
  _trimmerStack = new Stack();
 }
 public void reset()
 {
  _trimmerStack.clear();
 }
 public void setFile (String fileName)
 { 
  _currentTrimmer = new Trimmer(fileName);
 }
 public void pushState()
 {
  _trimmerStack.push(_currentTrimmer);
 }
 public void popState()
 {
  try   { _currentTrimmer = (Trimmer)_trimmerStack.pop();}
  catch (EmptyStackException e) {_currentTrimmer = null;}
 }
 public String getNextLine() 
 {
  if (_currentTrimmer == null) return null;
  return _currentTrimmer.readLine();
 }
 public String currentFile() {return _currentTrimmer.fileName;}
 public int    currentLine() {return _currentTrimmer.lineNumber;}
}














