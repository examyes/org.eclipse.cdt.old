package org.eclipse.cdt.cpp.miners.pa.engine.functioncheck;


import java.util.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;

/**
 * The class FunctionCheckCallerTokenizer is used to break the callee line in the
 * FunctionCheck call graph into separate function tokens.
 * We use the delimiters <, >, (, ) and space to separate each fuction.
 */
public class FunctionCheckCalleeTokenizer {

  private String 	_line;
  private int 		_length;
  private int 		_startIndex;
  private int 		_endIndex;
  private boolean 	_hasParameters;
  
  
  // Constructor
  public FunctionCheckCalleeTokenizer(String line) {
   _line = line;
   _length = line.length();
   _startIndex = 0;
   _endIndex = 0;
   
   _hasParameters = isLastCharAParen();
   
  }
  
  /**
   * Are there any more tokens?
   */
  public boolean hasMoreTokens() {
   return (_endIndex < _length);
  }
  
  /**
   * Is the character at the given index a white space?
   */
  private boolean isWhitespace(int index) {
   return Character.isWhitespace(_line.charAt(index));
  }
  
  
  /**
   * Return the next token
   */
  public FunctionCheckCalleeToken nextToken() {
  
   // create a new token
   FunctionCheckCalleeToken token = new FunctionCheckCalleeToken();
   
   // Move the pointer to the first non-space character
   while (hasMoreTokens() && isWhitespace(_endIndex))
    _endIndex++;
    
   _startIndex = _endIndex;
   
   // Find the call number
   if (_line.charAt(_endIndex) == '(') {
   
     int closeParenIndex = _line.indexOf(')', _endIndex);
     if (closeParenIndex > _endIndex) {
      String numberString = _line.substring(_endIndex+1, closeParenIndex);
      token.setNumber(numberString);
      _endIndex = closeParenIndex + 1;
      _startIndex = _endIndex;
     }
   
     // Skip white space characters
     while (hasMoreTokens() && isWhitespace(_endIndex))
      _endIndex++;

     _startIndex = _endIndex;
       
   }
   
   
   // If the function signature does not include parameters
   if (!_hasParameters) {
    
    while (hasMoreTokens() && !isWhitespace(_endIndex))
     _endIndex++;
    
    if (hasMoreTokens()) {
     int oldStartIndex = _startIndex;
     _startIndex = _endIndex;
     token.setName(_line.substring(oldStartIndex, _endIndex));
    }
    else {
     token.setName(getTailingToken());
    }
    
    return token;
   }
   
   // If the function signature includes parameters
   
   // Find the next "(" or "<" character
   char ch = 0;
   while (hasMoreTokens() && (ch = _line.charAt(_endIndex)) != '(' && ch != '<') {
    _endIndex++;
   }

   // Move the pointer to the end parenthesis
   if (ch == '(') {
     findMatchingParen();
   }
   else if (ch == '<') {
   
     findMatchingBracket();
    
     while (hasMoreTokens() && _line.charAt(_endIndex) != '(')
      _endIndex++;
     
     if (hasMoreTokens()) {
       findMatchingParen();
     }
    
   }
   
   // construct the returned token
   if (hasMoreTokens()) {
    _endIndex++;
    int oldStartIndex = _startIndex;
    _startIndex = _endIndex;
    token.setName(_line.substring(oldStartIndex, _endIndex));
   }
   else {
    token.setName(getTailingToken());   
   }
    
   return token;
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
 
 /**
  * Is the last character a close parenthesis (')')?
  */
 private boolean isLastCharAParen() {
 
   if (_length > 1) {
   
     int index = _length - 1;
     while (isWhitespace(index))
       index--;
     
     if (index > 0)
      return _line.charAt(index) == ')';
     else
      return false;
   }
   else
     return false;
   
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
