package com.ibm.cpp.miners.parser.invocation;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import java.lang.*;
import java.io.*;
import com.ibm.cpp.miners.parser.preprocessor.*;
import com.ibm.cpp.miners.parser.dstore.*;
import com.ibm.cpp.miners.parser.invocation.*;
import com.ibm.cpp.miners.parser.grammar.*;

class CommandLine
{
 public static void main(String args[])
 {
  String theFile = null;
  try  { theFile = args[0]; }
  catch (ArrayIndexOutOfBoundsException e)
  {
   emit("Usage:   parse <FileName> ");
   return;
  }
  
  CommandLineSymbolTable  _theSymbolTable  = new CommandLineSymbolTable();
  Preprocessor            _thePreprocessor = new Preprocessor(null);
  Parser                  _theParser       = new Parser();

  _theParser.setSymbolTable(_theSymbolTable); 
  
  BufferedReader input = new BufferedReader(new StringReader(_thePreprocessor.preprocess(theFile)));
  ParserTokenManager token_mgr = new ParserTokenManager(new ASCII_CharStream(input, 1, 1));
  token_mgr.setSymbolTable(_theSymbolTable);
  _theParser.ReInit(token_mgr);
  
  try
  {
   _theParser.translation_unit();
  }
  catch (Throwable e) 
  {
   e.printStackTrace();
  }
 }
 
 private static void emit (String s) 
 {
  System.out.println(s);
 }
}




