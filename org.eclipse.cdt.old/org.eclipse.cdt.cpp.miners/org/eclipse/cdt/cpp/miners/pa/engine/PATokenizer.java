package org.eclipse.cdt.cpp.miners.pa.engine;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;

/**
 * PATokenizer is a utility class used to separate an input line in a PATraceFile
 * into individual tokens. It is similar with the StringTokenizer, but has some extra
 * capabilities. If you designate the max token number through the constructor, 
 * the number of tokens generated will be less than or equal to the given number. 
 * It is useful in parsing the flat profile because the last entry (the function name)
 * may contain whitespaces. 
 */
public class PATokenizer {

  private String _line;
  private String _delimiter;
  private int _tokenNumber;
  private int _maxTokenNumber;
  private ArrayList _tokens;

  /**
   * Create a PATokenizer from a given String.
   */
  public PATokenizer(String line) {
   _line = line;
   _tokenNumber = 0;
   _maxTokenNumber = 0;
   _tokens = new ArrayList();
   parse();
  }
  
  /**
   * Create a PATokenizer from a given String and delimiter.
   */
  public PATokenizer(String line, String delimiter) {
   _line = line;
   _tokenNumber = 0;
   _maxTokenNumber = 0;
   _tokens = new ArrayList();   
   _delimiter = delimiter;
   parse();
  }
  
  /**
   * Create a PATokenizer from a given String and a number which designates
   * the maximal number of tokens.
   */
  public PATokenizer(String line, int maxTokenNumber) {
   _line = line;
   _tokenNumber = 0;
   _maxTokenNumber = maxTokenNumber;
   _tokens = new ArrayList();
   parse();
  }
  
  /**
   * Create a PATokenizer from a given String, delimiter and the max token number.
   */
  public PATokenizer(String line, String delimiter, int maxTokenNumber) {
   _line = line;
   _delimiter = delimiter;
   _tokenNumber = 0;
   _maxTokenNumber = maxTokenNumber;
   _tokens = new ArrayList();
   parse();
  }

  /**
   * Parse the entry line
   */
  private void parse() {
  
    StringTokenizer tokenizer = null;
    
    if (_delimiter != null)
     tokenizer = new StringTokenizer(_line, _delimiter);
    else
     tokenizer = new StringTokenizer(_line);
     
    while (tokenizer.hasMoreTokens()) {
     String token = tokenizer.nextToken();
     _tokenNumber++;
     
     if ((_tokenNumber < _maxTokenNumber || _maxTokenNumber == 0) && 
          isValidLeadingCharacter(token.charAt(0), _tokenNumber-1)) 
     {
       _tokens.add(token);
     }
     else {
      int tokenIndex = _line.indexOf(token);
      _tokens.add(_line.substring(tokenIndex));
      break;
     }
      
    }
  }
  
  /**
   * Is it a valid leading character in a token?
   */
  public boolean isValidLeadingCharacter(char c, int index) {
  
   if (index == 0)
    return Character.isDigit(c) || c == '[';
   else
    return Character.isDigit(c);
    
  }
  
  /**
   * Return the number of tokens
   */
  public int getTokenNumber() {
   return _tokenNumber;
  }
  
  /**
   * Return the token at the given index
   */
  public String getToken(int index) {
  
    if (index >= 0 && index < _tokenNumber)
     return (String)_tokens.get(index);
    else
     return null;
  }
  
  /**
   * Return the token as an integer at the given index
   */
  public int getTokenAsInt(int index) throws Exception {
   
   String token = getToken(index);
   if (token != null) 
    return Integer.parseInt(token);
   else
    throw new PAException("Invalid index (" + index + ") for: " + _line);
  }
  
  /**
   * Return the token as a double at the given index
   */
  public double getTokenAsDouble(int index) throws Exception {
   
   String token = getToken(index);
   if (token != null) 
    return Double.parseDouble(token);
   else
    throw new PAException("Invalid index (" + index + ") for: " + _line);
  }

  /**
   * Return the last token
   */
  public String getLastToken() {
   return getToken(_tokenNumber-1);
  }
  
}