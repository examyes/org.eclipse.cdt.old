package org.eclipse.cdt.cpp.miners.parser.invocation;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.lang.*;
import java.io.*;
import org.eclipse.cdt.cpp.miners.parser.preprocessor.*;
import org.eclipse.cdt.cpp.miners.parser.dstore.*;
import org.eclipse.cdt.cpp.miners.parser.invocation.*;
import org.eclipse.cdt.cpp.miners.parser.grammar.*;

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
  //Preprocessor            _thePreprocessor = null; // new Preprocessor(null);
  Parser                  _theParser         = null; // new Parser(null);

  _theParser.setSymbolTable(_theSymbolTable); 
  
  /* BufferedReader input = new BufferedReader(new StringReader(_thePreprocessor.preprocess(theFile)));
  ParserTokenManager token_mgr = new ParserTokenManager(new SimpleCharStream(input, 1, 1));
  token_mgr.setSymbolTable(_theSymbolTable);
  _theParser.ReInit(token_mgr);
  */
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




