package com.ibm.debug.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/util/QuoteUtil.java, java-util, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:33:03)
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

/** Class to handle quoting and unquoting strings
  * @see ArgumentTokenizer
  */

public class QuoteUtil
{
  /** Put a string in quotes.  A string processed by this method
    * will be successfully recovered by the unquote() method.
    * <br>i.e.:
    * <code>
    * String x = ....;
    * x.equals(unquote(enquote(x)) <!code> returns true
    * @param work The string to be quoted
    * @returns The quoted string
    * @see unquote
    */
  static public String
  enquote(String work)
  {
    // Enquote the provided string.
    // The character " is replaced with \"
    // The character \ is replaced by \\
    String enquoted = new String("\"");
    for (int i = 0; i < work.length(); i++)
    {
      switch (work.charAt(i))
      {
        case '\"':
        case '\\':
          enquoted += "\\";
          break;
      }
      enquoted += work.charAt(i);
    }

    return enquoted + "\"";
  }

  /** Put the string in quotes if required.
    * @param work The string to be quoted
    * @returns The string, in quotes if necessar
    * @see unquote
    * @see enquote
    */
  static public String
  enquoteIfNecessary(String work)
  {
    // Enquote the provided string if it contains special characters.
    for (int i = 0; i < work.length(); i++)
    {
      switch (work.charAt(i))
      {
        case ' ':               // whitespace
        case '\t':
        case '\r':
        case '\n':
        case '*':               // File name wildcard
        case '?':
        case '>':               // Shell special characters
        case '<':
        case '|':
        case '&':
        case '\'':              // Quotes
        case '\"':
        case '\\':              // escape
          return enquote(work);
      }
    }

    // no quoting required
    return work;
  }

  /** Remove quotes and escape sequences from the provided string.  The
    * character '\' is an escape character.  It is deleted, and the following
    * character taken literally when outside quotes.  Inside quotes, it escapes
    * only the " and \ characters.
    * <sp>
    * The string is assumed to be correctly formed, with balanced quotes, and
    * characters after escape sequences.
    * @param work The string to be unquoted
    * @returns The unquoted string
    * @see enquote
    */
  static public String
  unquote(String work)
  {
    String unquoted = new String();
    boolean quoted = false;
    for (int i = 0; i < work.length(); i++)
    {
      switch (work.charAt(i))
      {
        case '\"':
          quoted = !quoted;
          break;

        case '\\':
          i++;
          if (i >= work.length())
            break;
          if (quoted)
          {
            switch (work.charAt(i))
            {
            default:
              // Not an escape... Add to string
              unquoted += '\\';
              break;
            case '\"':
            case '\\':
              // ignore...
              break;
            }
          }

        default:
          unquoted += work.charAt(i);
      }
    }

    return unquoted;
  }
}
