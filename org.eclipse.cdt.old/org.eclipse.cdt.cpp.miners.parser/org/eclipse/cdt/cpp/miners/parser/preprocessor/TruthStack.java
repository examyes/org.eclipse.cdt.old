package org.eclipse.cdt.cpp.miners.parser.preprocessor;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.lang.*;
import java.util.*;

public class TruthStack
{
 public int T   = 0;
 public int F   = 1;
 public int I   = 2;
 private Stack stateStack;
   
 TruthStack()
 {
  stateStack = new Stack();
  stateStack.push (new Integer(T));
 }
 
 public void reset()
 {
  stateStack.clear();
  stateStack.push(new Integer(T));
 }
 
 //Construct the TruthStack and prime it with some state...we start at the end of theState and work
 //backwords while pushing onto the stack.
 TruthStack(String theState)
 {
  stateStack = new Stack();
  for (int i = theState.length()-1; i>=0; i--)
   stateStack.push(new Integer(Character.digit(theState.charAt(i),10)));
 }
 
 public void push(int state)
 {
  stateStack.push(new Integer(state));
 }
 
 public void pop()
 {
  try
   {
    stateStack.pop(); 
   }
  catch (EmptyStackException e) {}
 }
 
 public int peek()
 {
  try
  {
   return ((Integer)stateStack.peek()).intValue();
  }
  catch (EmptyStackException e) {return -1;}
  
 }
 
 public boolean empty()
 {
  //Stack is "considered" to be empty, if it's size is 1, since
  //the global scope state is always there by default.
  return (stateStack.size() == 1);
 }

 //This converts the stack into a string of digits that represents the state.
 //The first character in the string is the top of the stack.
 public String toString()
 {
  StringBuffer theState = new StringBuffer();
  while (!empty())
  {
   theState.append(peek());
   pop();
  }
  //add the initial True (which refers to the global scope)
  theState.append(T);
  return theState.toString();
 }
}
