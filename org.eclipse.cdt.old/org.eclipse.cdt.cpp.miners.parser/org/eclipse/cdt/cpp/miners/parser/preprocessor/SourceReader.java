package com.ibm.cpp.miners.parser.preprocessor;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
  try { _currentTrimmer = new Trimmer(fileName); }
  catch (IOException e) {}
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














