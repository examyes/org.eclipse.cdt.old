package org.eclipse.cdt.cpp.miners.parser.expressionevaluator;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

public class ExprTokenizer
{
 private String _expression;
 private int    _index;
 private static void msg(Object s) {System.out.println(s);};
 
 public ExprTokenizer(String theExpression)
 {
  _expression = theExpression;
  _index = 0;
 }
 
 public ExprToken getNextToken()
 {
  skipSpaces();
 
  if (_index == _expression.length())
   return null;
    
  //Check to see if the current character is an operator..if so we're done.
  char c = _expression.charAt(_index);
   
  int opType = ExprToken.operatorType(c);
  if (opType != ExprToken.NOTANOPERATOR)
  {
   _index++;
   return ( new ExprToken(opType, 0)); 
  }
  
  //Now while the current character is a digit or a '.', append it to the current Token
  if ( (!Character.isDigit(c)) && (c != '.'))
   return null;
  
  StringBuffer token = new StringBuffer();
  while ((Character.isDigit(c)) || (c == '.'))
  { 
   token.append(c);
   _index++;
   if (_index == _expression.length())
    break;
   c = _expression.charAt(_index);
  }
  double theValue;
  try 
  {
   theValue = Double.parseDouble(token.toString());
  }
  catch (NumberFormatException e)
  {
   return null;
  }
  return ( new ExprToken(ExprToken.LITERAL, theValue));
 }

 private void skipSpaces()
 {
  while ( (_index < _expression.length()) && (_expression.charAt(_index) == ' '))
   _index++;
 }

}
