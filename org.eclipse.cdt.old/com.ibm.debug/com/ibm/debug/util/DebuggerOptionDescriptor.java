package com.ibm.debug.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/util/DebuggerOptionDescriptor.java, java-util, eclipse-dev, 20011129
// Version 1.1.1.3 (last modified 11/29/01 14:15:42)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.lang.*;
import java.util.*;

/** The DebuggerOptionDescriptor controls how options are parsed.  If no descriptors are
  * provided, no error checking is perfromed, and all options are assumed to
  * start with the character '-' (or '/' on Windows).  The DebuggerOptionDescriptor
  * class describes how a particular option can be recognized.
  *
  * @see DebuggerOptions
  */

public class DebuggerOptionDescriptor
{
  /** Construct an DebuggerOptionDescriptor
    * @param name The name of the parameter.  This should be the complete
    *         name of the parameter, in the correct case, with the
    *         appropriate parameter flag ('-' or '+').  e.g. -fubar
    * @param minLength The minimum number of characters in the name that
    *         will allow the parameter to be recognized.  i.e. the shortest
    *         allowed abbreviation.  Characters beyond
    *         this length must match the parameter name, and no additional
    *         characters will be recognized
    * @param takesParms If true, this option takes parameters.  The parameter
    *         may be separated from the name of the option by whitespace or
    *         an equals sign.
    *         <br>
    *         e.g.:
    *         <br>
    *         for DebuggerOptionDescriptor("fubar", 3, true)
    *         < br>
   *         fub=3            is valid
    *         < br>
    *         fub 3           is valid
    *         < br>
    *         fubar=3         is valid
    *         < br>
    *         fubar 3         is valid
    *         < br>
    *         fu=3            is not valid (not minimum length)
    *         < br>
    *         fuber=3         is not valid (not a name match)
    *         < br>
    *         fubarx=3        is not valid (extra characters)
    *         < br>
    * @param mayConcatenate If the name of the option is not abbreviated,
    *         the parameter may be concatenated to the option.
    *         <br>
    *         e.g.:
    *         < br>
    *         fubar3    is valid and
    *         <br>
    *         fuba3     is not valid (concatenated to abbreviated name)
    */
  public DebuggerOptionDescriptor(String name,
                          int minLength,
                          boolean takesParms,
                          boolean mayConcatenate)
  {
    _name = name;
    _minName = name.substring(0, minLength);
    _takesParms = takesParms;
    _mayConcatenate = mayConcatenate;
  }

  /** The value obtained from the last successful match() operation
    * @return string containing the value of the last successful option match
    */
  String
  value()
  {
    return _value;
  }

  /** Set the value into the parm
    * @param value The value to be set into the parameter
    */
  void
  setValue(String value)
  {
    _value = value;
  }

  /** The full name of the option (not abbreviated)
    * @return name of the option
    */
  String
  name()
  {
    return _name;
  }

  /** Check for a parameter match
    * @param arg The argument from the command line
    * @return number of arguments consumed.  This will be one of the following:
    * <br>
    * 0 - The argument dows not match the parameters
    * 1 - The argument matches the parameter.  The value is in "_value"
    * 2 - The argument matches the parameter, but the value is in the
    *         next argument.
    */
  int
  match(String arg)
  {
    // quick check
    if (!arg.startsWith(_minName))
      return 0;                   // not minumum abbreviation

    int i = _minName.length();
    for (; i < arg.length() && i < _name.length(); i++)
    {
      if (arg.charAt(i) != _name.charAt(i))
      {
        // Must be an equals sign
        if (arg.charAt(i) != '=')
          return 0;

        if (arg.length() == ++i)
          _value = "";     // null parameter value
        else if (!_takesParms)
          return 0;       // non-null parameter not allowed
        else
          _value = arg.substring(i);
        return 1;     // parameter returned in "value"
      }
    }

    if (i == arg.length())
    {
      // Scanned to the end of the argument.  The value is null.
      // If the option takes parameters, the user must get it himself
      _value = "";
      return _takesParms ? 2 : 1;
    }

    if (arg.charAt(i) == '=')
    {
      // There is an equals sign as the next character.  That means we
      // have a parameter value. If the option takes no parms, it's
      // invalid if any parameter value other than a null string is
      // specified.
      if (++i == arg.length())
      {
        _value = "";   // null parameter value
        return 1;
      }
      else if (!_takesParms)
        return 0;
      else
      {
        _value = arg.substring(i);
        return 1;
      }
    }

    // The name is a mismatch if we take no parameters
    if (!_takesParms)
      return 0;

    // If concatenation of the value is not allowed,
    // there is a name mismatch
    if (!_mayConcatenate)
      return 0;

    _value = arg.substring(i);
    return 1;
  }

  private String _name;               // full name
  private String _minName;            // minimum abbreviation
  private String _value = null;       // value from last successful match() operation
  private boolean _takesParms;        // parameters required
  private boolean _mayConcatenate;    // concatenation of parms allowed

}