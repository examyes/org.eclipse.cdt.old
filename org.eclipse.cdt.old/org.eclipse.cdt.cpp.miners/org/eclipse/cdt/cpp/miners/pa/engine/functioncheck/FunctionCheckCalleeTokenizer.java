package org.eclipse.cdt.cpp.miners.pa.engine.functioncheck;


import java.util.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;

/**
 * The class FunctionCheckCallerTokenizer is used to break the callee line in the
 * FunctionCheck call graph into separate function tokens.
 * We use the delimiters <, >, (, ) and space to separate each fuction.
 */
public class FunctionCheckCalleeTokenizer {

  private String _line;
  private int _length;
  private int _startIndex;
  private int _endIndex;
  private boolean _hasParameters;
  
  // Constructor
  public FunctionCheckCalleeTokenizer(String line) {
   _line = line;
   _length = line.length();
   _startIndex = 0;
   _endIndex = 0;
   
   _hasParameters = (line.indexOf('(') >= 0);
   
  }
  
  /**
   * Are there any more tokens?
   */
  public boolean hasMoreTokens() {
   return (_endIndex < _length);
  }
  
  /**
   * Return the next token
   */
  public String nextToken() {
  
   while (hasMoreTokens() && Character.isWhitespace(_line.charAt(_endIndex)))
    _endIndex++;
    
   _startIndex = _endIndex;
       
   char ch = 0;
   if (!_hasParameters) {
    
    while (hasMoreTokens() && !Character.isWhitespace(_line.charAt(_endIndex)))
     _endIndex++;
    
    if (!hasMoreTokens()) {
     return getTailingToken();
    }
    else {
     int oldStartIndex = _startIndex;
     _startIndex = _endIndex;
     return _line.substring(oldStartIndex, _endIndex);
    }
     
   }
   
   while (hasMoreTokens() && (ch = _line.charAt(_endIndex)) != '(' && ch != '<') {
    _endIndex++;
   }

   if (!hasMoreTokens()) {
    return getTailingToken();   
   }
   else if (ch == '(') {
    findMatchingParen();
   }
   else if (ch == '<') {
   
    findMatchingBracket();
    
    while (hasMoreTokens() && _line.charAt(_endIndex) != '(')
     _endIndex++;
    
    if (!hasMoreTokens()) {
     return getTailingToken();   
    }
    else {    
     findMatchingParen();
    }
    
   }
   
   if (!hasMoreTokens()) {
    return getTailingToken();   
   }
   else {
    _endIndex++;
    int oldStartIndex = _startIndex;
    _startIndex = _endIndex;
    return _line.substring(oldStartIndex, _endIndex);
   }
    
 }
 
 /**
  * Find the matching parenthesis and move the end pointer there
  */
 private void findMatchingParen() {
 
  int numParens = 1;
  while (hasMoreTokens() && numParens > 0) {
   _endIndex++;
   char ch = _line.charAt(_endIndex);
   
   if (ch == '(')
    numParens++;
   else if (ch == ')')
    numParens--;
    
  }
  
 }
 
 /**
  * Find the matching angle bracket and move the end pointer there
  */
 private void findMatchingBracket() {
 
  int numBrackets = 1;
  while (hasMoreTokens() && numBrackets > 0) {
   _endIndex++;
   char ch = _line.charAt(_endIndex);
   
   if (ch == '<')
    numBrackets++;
   else if (ch == '>')
    numBrackets--;
    
  }
   
 }
 
 /**
  * Return the tail token
  */
 private String getTailingToken() {
     
  if (_endIndex > _startIndex && _endIndex <= _length)
   return _line.substring(_startIndex, _endIndex);
  else
   return null;
 
 }
 
 // Command line test driver
 public static void main(String[] args) {
 
  if (args.length < 1) {
   System.out.println("Usage: fctoken line");
   System.exit(-1);
  }
  
  FunctionCheckCalleeTokenizer tokenizer = new FunctionCheckCalleeTokenizer(args[0]);
  while (tokenizer.hasMoreTokens()) {
   System.out.println(tokenizer.nextToken());
  }
 }
  
}
