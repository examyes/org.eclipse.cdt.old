package org.eclipse.cdt.cpp.miners.parser.expressionevaluator;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

public class ExprToken 
{
 public static final int NOTANOPERATOR        = 0;
 public static final int LITERAL              = 1;
 public static final int OPENPAREN            = 2;
 public static final int CLOSEPAREN           = 3;
 public static final int STARTOFOPERATORS     = 4;
 public static final int ADD                  = 5;
 public static final int SUBTRACT             = 6;
 public static final int DIVIDE               = 7;
 public static final int MULTIPLY             = 8;
 public static final int EXPONENT             = 9;
 public static final int ENDOFOPERATORS       = 10;


 private static void msg(String s) {System.out.println(s);};
 public int kind;
 public Double value;
 public ExprToken (int theKind, double theValue)
 {
  kind  = theKind;
  value = new Double(theValue);
 }
 public boolean isOperator()
 {
  return ((kind > STARTOFOPERATORS) && (kind < ENDOFOPERATORS));
 }
 public boolean isNumber()
 {
  return (kind == LITERAL);
 }
 
 public Double evaluate(double var1, double var2)
 {
  if (!isOperator())
   return null;
  
  switch (kind)
  {
   case ADD:        return new Double (var1 + var2);
   case SUBTRACT:   return new Double (var1 - var2);
   case DIVIDE:     return new Double (var1 / var2);
   case MULTIPLY:   return new Double (var1 * var2);
    //case EXPONENT:   return var1 ^ var2;
  }
  return null;
 }
 
 public String toString()
 {
  if (isNumber())
   return (value.toString());
  switch (kind)
  {
   case ADD:        return "+";
   case SUBTRACT:   return "-";
   case DIVIDE:     return "/";
   case MULTIPLY:   return "*";
   case EXPONENT:   return "^";
   case OPENPAREN:  return "(";
   case CLOSEPAREN: return ")";
  };
  return "Unknown term...kind = " + kind + " and value = " + value;
 }

 public static int operatorType(char c)
 {
  switch (c)
  {
   case '+' : return ADD;
   case '-' : return SUBTRACT;
   case '/' : return DIVIDE;
   case '*' : return MULTIPLY;
   case '^' : return EXPONENT;
   case '(' : return OPENPAREN;
   case ')' : return CLOSEPAREN;
  };
  return NOTANOPERATOR;
 }
 
 
}
