package org.eclipse.cdt.cpp.miners.pa.engine;

import java.util.*;


public class PATokenizer {

  private String _line;
  private String _delimiter;
  private int _tokenNumber;
  private int _maxTokenNumber;
  private ArrayList _tokens;

  // Constructor
  public PATokenizer(String line) {
   _line = line;
   _tokenNumber = 0;
   _maxTokenNumber = 0;
   _tokens = new ArrayList();
   parse();
  }
  
  public PATokenizer(String line, String delimiter) {
   _line = line;
   _tokenNumber = 0;
   _maxTokenNumber = 0;
   _tokens = new ArrayList();   
   _delimiter = delimiter;
   parse();
  }
  
  public PATokenizer(String line, int maxTokenNumber) {
   _line = line;
   _tokenNumber = 0;
   _maxTokenNumber = maxTokenNumber;
   _tokens = new ArrayList();
   parse();
  }
  
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