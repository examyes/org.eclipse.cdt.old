package com.ibm.debug.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/util/ArgumentTokenizer.java, java-util, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:33:04)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.lang.*;
import java.io.File;
import java.util.*;

/**
  * The ArgumentTokenizer class is used to strip tokens from
  * an argument string.  The rules used are dependent on the
  * platform on which the methods run.  This class differs
  * from the standard StringTokenizer class in that it
  * recognizes escape characters and quoted strings, and uses
  * whitespace as the only delimiter.
  * @see QuoteUtil
  */
public class ArgumentTokenizer
{
  /** Create a tokenizer, with a String
    * @param string The string to be tokenized
    */
  public
  ArgumentTokenizer(String string)
  {
    resetString(string);
  }

  /** Get the next token from the string.  The token is parsed
    * from the string as though the string had been passed to the
    * command interpreter on the platform.  Quotes and escape
    * characters are removed.
    * @return The next token from the string
    */
  public String
  nextToken()
  {
    int stringLen = _string.length();
    String token = new String();
    boolean inQuotedString = false;
    int pos = 0;
    char nextChar = getCharacter(0);
    for (; pos < stringLen; )
    {
      char c = nextChar;
      nextChar = getCharacter(++pos);
      if (c == '\"')
      {
        // This either starts or ends a quoted string
        inQuotedString = !inQuotedString;
      }
      else if (c == '\\')
      {
        // This character MAY function as an escape character
        if (inQuotedString)
        {
          // The / is an escape only with / and "
          switch (nextChar)
          {
            case '\\':
            case '\"':
              // escape...
              token += nextChar;
              break;
            case ' ':
              if (pos >= stringLen)
              {
                // malformed string (bad escape sequence, with no closing quote)
                break;
              }
              // fall through..
            default:
              token += '\\';
              token += nextChar;
          }
          nextChar = getCharacter(++pos);
        }
        else if ('\\' == File.separatorChar)
        {
          // Windows platform - accept character
          token += c;
        }
        else
        {
          // UNIX platform - use UNIX escape rules
          // Next character is always taken literally
          if (nextChar == ' ' && pos < stringLen)
            token += nextChar;
          nextChar = getCharacter(++pos);
        }
      }
      else if (inQuotedString || !Character.isWhitespace(c))
      {
        // If in a quoted string, or if the next character is not whitespace,
        // the character is added to the token.
        token += c;
      }
      else if (0 < token.length())
        break;   // end of string reached
    }

    if (pos < _string.length())
      _string = _string.substring(pos);
    else
      _string = "";
    return token;
  }

  /**
    * Get the reaminder of the string (The part yet to be tokenized)
    * @return The untokenized part of the string
    */
  public String
  remainderOfString()
  {
    int stringLen = _string.length();
    int pos = 0;
    for (; pos < stringLen; pos++)
    {
      if (!Character.isWhitespace(_string.charAt(pos)))
        return _string.substring(pos);
    }
    return "";
  }

  /**
    * Reset the string being parsed
    * @param The new string to parse
    */
  public void
  resetString(String s)
  {
    _string = new String(s);
  }

  /** Get the character from a position on the string.
    * the character '\0' is returned if the position is not in the string.
    * @param The position in the string
    * @return the character.
    */
  private char
  getCharacter(int pos)
  {
    if (pos >= _string.length() || pos < 0)
      return '\0';
    else
      return _string.charAt(pos);
  }

  /** Convert a debuggee arguments string to an array of Strings in the
    * same way that the command parser of the platform would do so.
    * @param arguments The argument string
    * @return An array of argument tokens
    */
  public static String[]
  fullParse(String arguments)
  {
    Vector args = new Vector();
    ArgumentTokenizer tokenizer = new ArgumentTokenizer(arguments);

    // Place the composite string into an array of strings
    for (;;)
    {
      String option = tokenizer.nextToken();
      if (0 == option.length())
        break;
      args.addElement(option);
    }

    String[] result = new String[args.size()];
    args.copyInto(result);
    return result;
  }


  private String _string;
}