package com.ibm.cpp.miners.parser.expressionevaluator;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;

public class ExpressionEvaluator
{
 private static void msg(Object s) {System.out.println(s);};
 
 public Double evaluate(String theExpression)
 {
  /* The simplest way to evaluate an algebraic expression is to first convert it to postfix order and then
     evaluate it, since both steps can be done just through the use of stacks (thus avoiding having to build up 
     an expression tree).  I'm a little wary of using Stacks though, since they are derived from Vectors which
     are not the best-performing structures due to the fact that they are synchronized.  In the future I may 
     want to implement my own Stack, maybe using ArrayLists.  I more or less follow the algorithms outlined on 
     http://www.cs.rit.edu/~cs4/CompilerProject/inToPost.html
  */
  return evaluatePostFix(convertToPostFix(theExpression));
 }

 private Double evaluatePostFix(ArrayList postfix)
 {
  /*This method accepts an ArrayList of terms\operators that it assumes is in proper postfix order
  The algorithm used is as follows:
   while there are more tokens:
     if the current token is a number, 
       Push its value onto the stack  
     else if the current token is an operator, 
       Pop the two top elements of the stack into variables x and y.
       Calculate y operator x. 
       Push the result of the calculation onto the stack. 
   Pop the top value of the stack. This is the result of the postfix expression. 
  */

  if (postfix == null)
   return null;
  Stack stack = new Stack();
  ExprToken t;
  Double x,y;
  
  for (int i=0; i<postfix.size(); i++)
  {
   t = (ExprToken)postfix.get(i);
   if (t.isNumber())
    stack.push(t.value);
   else if (t.isOperator())
   {
    x = (Double)stack.pop();
    y = (Double)stack.pop();
    stack.push(t.evaluate(y.doubleValue(),x.doubleValue()));
   }
   else
    return null;
  }
  return (Double)stack.pop();
  
 }

 private ArrayList convertToPostFix(String infix)
 {
  /* This method is used to take an expression in infix order and convert it to postfix order.  There are several
     algorithms to do this...I use the algorithm described on http://www.cs.rit.edu/~cs4/CompilerProject/inToPost.html
     I reproduce the essence of it here for reference:
    
    - Push '(' onto the stack. 
    - While the stack is not empty
      {
       - Read next token
       - If the current token is a ')' or we have reached the end of the expression
         { 
          - Pop operators from the top of the stack and insert them into the postfix expression until a '(' 
            is at the top of the stack. 
          - Pop (and discard) the '(' from the stack. 
         } 
       - If the current token is a digit, add it to the postfix expression. 
       - If the current token is a '(', push it onto the stack. 
       - If the current token is an operator, 
         { 
          - Pop operators (if there are any) at the top of the stack while they have equal or higher precedence than 
            the current token, and insert the popped operators into the postfix expression. 
          - Push the current token onto the stack. 
         }
      
      }  

 */
  ArrayList postfix = new ArrayList();
  Stack stack = new Stack();  
  ExprTokenizer tokenizer = new ExprTokenizer(infix);
  ExprToken t;
  ExprToken openParen = new ExprToken (ExprToken.OPENPAREN, 0);
  stack.push(openParen);
  
  while (!stack.isEmpty())
  { 
   t = tokenizer.getNextToken();
 
   if ((t == null) || (t.kind == ExprToken.CLOSEPAREN))
   { 
    try
    {
     ExprToken top = (ExprToken)stack.pop(); 
     while (top.kind != ExprToken.OPENPAREN)
     {
      postfix.add(top);
      top = (ExprToken)stack.pop();
     }
    }
    catch (EmptyStackException e) {}
   }
   else if (t.isNumber())
    postfix.add(t);
   else if (t.kind == ExprToken.OPENPAREN)
    stack.push(t);
   else if (t.isOperator())
   {
    try
    {
     while (((ExprToken)stack.peek()).kind >= t.kind)
      postfix.add(stack.pop());
    }
    catch (EmptyStackException e) {}
    stack.push(t);
   }
   else 
   {
    return null;
   }
  }
  return postfix;
 }

 private static void main (String args[])
 {
  ExpressionEvaluator eval = new ExpressionEvaluator();
  System.out.println(eval.evaluate(args[0]));
 }
 


}
 


 











