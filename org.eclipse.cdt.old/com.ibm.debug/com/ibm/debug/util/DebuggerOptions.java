package com.ibm.debug.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/util/DebuggerOptions.java, java-util, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:33:02)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.lang.*;
import java.io.*;
import java.util.*;

/** The DebuggerOption class provides the basic functionality for parsing the
  * options of a java debugger.  It parses out the debugger options, the debuggee
  * name, and the debuggee parameters.  It can be given a personality by a
  * derived class that allows it to discard options that are not on a provided list.
  *
  * @see DebuggerOptionDescriptor
  */
public class DebuggerOptions
{
  //----------------------------------------------------------------------------
  // Constructors
  //----------------------------------------------------------------------------
  /**
    * Construct a DebuggerOptions object with an empty set of options
    */
  public DebuggerOptions() {}

  /**
    * Construct a DebuggerOptions object from a set of parameters, such as
    * those that are passed to a main() function.
    * @param args An array of strings.  The constructor uses all strings up
    *             to the end of the array, or the first null element,
    *             whichever comes first.
    */
  public DebuggerOptions(String[] args)
  {
    rebuild(args);
  }

  /**
    * Construct a DebuggerOptions object from another DebuggerOptions object.
    * Only the options table is copied.  The invalid options and personality
    * are not copied.
    * @param source The object to copy
    */
  public DebuggerOptions(DebuggerOptions source)
  {
    _debuggeeName = new String(source._debuggeeName);
    _debuggeeArgs = new String(source._debuggeeArgs);
    Enumeration keys = source._options.keys();
    while (keys.hasMoreElements())
    {
      String key = (String)keys.nextElement();
      String value = (String)source._options.get(key);
      _options.put(key, value);
    }
  }

  //----------------------------------------------------------------------------
  // Public Functions
  //----------------------------------------------------------------------------
  /**
    * Get the debuggee args
    * @returns The debuggee args.  The string may be empty.
    */
  public String
  debuggeeArgs()
  {
    // The debuggee args
    return _debuggeeArgs;
  }

  /**
    * Get the debuggee name
    * @returns The debuggee name.  The string may be empty.
    */
  public String
  debuggeeName()
  {
    // The debuggee name
    return _debuggeeName;
  }

  /**
    * Create a String object which represents this DebuggerOptions
    * object.  The string can be given to the recover() or rebuild()
    * methods to reconstitute the object.  This is used rather than
    * the more normal serialization methods, since the string may
    * need to be sent (or received from) a program written in C or C++.
    * The invalid parameter strings and the personality are not copied
    * into the flattened string.
    * @returns A string suitable for transmission on a serial line.
    * @see recover
    * @see rebuild
    */
  public String flatten()
  {
    String flattened = new String();

    Enumeration e = _options.keys();
    while (e.hasMoreElements())
    {
      String key = (String)e.nextElement();
      if (!key.equals(_nameSep))
      {
        String value = (String)_options.get(key);
        if (0 != value.length())
          flattened += " " + QuoteUtil.enquote(key + "=" + value);
        else
          flattened += " " + QuoteUtil.enquote(key);
      }
    }
    if (0 != _debuggeeName.length())
    {
      flattened += " " + _nameSep + " " + QuoteUtil.enquote(_debuggeeName);
      if (0 != _debuggeeArgs.length())
        flattened += " " + QuoteUtil.enquote(_debuggeeArgs);
    }
    return flattened;
  }

  /**
    * Get the options which were rejected as a result of personalization.
    * @returns An array of invalid options
    */
  public String[]
  invalidOptions()
  {
    String[] invalid = new String[_invalidOptions.size()];
    _invalidOptions.copyInto(invalid);
    return invalid;
  }

  /**
    * Method to rebuild from a response file.
    * <br>This mehod is used if a response file is provided.
    * @param responseFile Name of the response file
    * @param add Set to true if the response file is in addition
    *            to parameters already set.  Set to false if
    *            the response file overrides the current settings.
    */
  public void
  rebuildFromResponseFile(String responseFile, boolean add)
  {
    String optionStrings = new String();
    try
    {
      FileReader input = new FileReader(responseFile);
      BufferedReader breader = new BufferedReader(input);
      while (breader.ready())
      {
        String line = breader.readLine();
        if (line != null && line.length() != 0)
          optionStrings += " " + QuoteUtil.enquoteIfNecessary(line);
      }
    }
    catch (FileNotFoundException fnfEx)
    {
      rebuild(new String[0], add);
      return;
    }
    catch (IOException ioEx)
    {
      rebuild(new String[0], add);
      return;
    }

    String[] array = makeOptionArray(optionStrings);
    rebuild(array, add);
  }

  /**
    * Method to create a DebuggerParameter object from a string.
    * The string is of the form that would be acceptable as a
    * command line on a UNIX system.  Such a string is created by the
    * flatten() method.
    * @param optionStrings The string that the object is created from.
    * @return Anewly constructed DebuggerOptions object
    * @see flatten
    * @see rebuild
    */
  public static DebuggerOptions
  recover(String optionStrings)
  {
    DebuggerOptions opt = new DebuggerOptions();
    opt.rebuild(optionStrings);
    return opt;
  }

  /** Get the value of the named option.
    * <br>
    * If the option is of the form -name=y, and is located,
    * the value y is returned.  If the parameter is of the
    * form -name, and is found, an empty is returned.
    * Otherwise, null is returned.
    * @param name The parameter name
    * @returns The parameter value
    */
  public String
  valueByName(String name)
  {
    Object o = _options.get(name);
    return (String)o;
  }

  //----------------------------------------------------------------------------
  // Protected Functions
  //----------------------------------------------------------------------------
  /**
    * Add a DebuggerOptionDescriptor to personalize this object.  Options
    * are compared against descriptors in theorder that they are added.
    * This method must be called prior to calling the rebuild() methods.
    * @param descriptor The descriptor for a debugger option
    * @see rebuild
    */
  protected void
  addOptionDescriptor(DebuggerOptionDescriptor descriptor)
  {
    if (_optionDescriptors == null)
      _optionDescriptors = new Vector();
    _optionDescriptors.addElement(descriptor);
  }

  /**
    * Clear the option descriptors.  This reverts the DebuggerOptions
    * object into the unpersonalized state.
    */
  protected void
  clearOptionDescriptors()
  {
    _optionDescriptors = null;
  }

  /**
    * Method to allow a DebuggerParameter object to be reconsitututed
    * from a string.  The string is of the form that would be acceptable as a
    * command line on a UNIX system.  Such a string is created by the
    * flatten() method.
    * @param optionStrings The string that the object is reconstituted from.
    * @see flatten
    */
  protected void
  rebuild(String optionStrings)
  {
    String[] array = makeOptionArray(optionStrings);
    rebuild(array, false);
  }

  /**
    * Method to create an array of option strings from a command-line
    * @param optionStrings The string that the object is reconstituted from.
    * @return An array of parsed option strings
    * @see flatten
    */
  protected String[]
  makeOptionArray(String optionStrings)
  {
    ArgumentTokenizer tokenizer = new ArgumentTokenizer(optionStrings);
    Vector options = new Vector();

    // Place the composite string into an array of strings so
    // buildparser can do it's job properly.
    boolean nextOptionDebuggeeName = false;
    for (;;)
    {
      String option = tokenizer.nextToken();
      if (0 == option.length())
        break;

      if (!nextOptionDebuggeeName)
      {
        // Is this the debuggee name?
        nextOptionDebuggeeName = ('-' != rationalizeOption(option).charAt(0));
      }
      options.addElement(option);

      // If we have the debuggee name, the remainder of the string
      // is debuggee args.  DO NOT parse them!
      if (nextOptionDebuggeeName)
      {
        // the remainder of the string is the debuggee arguments
        options.addElement(QuoteUtil.unquote(tokenizer.remainderOfString()));
        break;
      }
      else
      {
        // This option may be the "next parm is debuggee name" flag
        // Remember it, since the parsing changes for debuggee args
        nextOptionDebuggeeName = option.equals(_nameSep);
      }
    }

    String[] optionArray = new String[options.size()];
    options.copyInto(optionArray);
    return optionArray;
  }

  /**
    * Method to allow a DebuggerParameter object to be reconstituted
    * from an array of strings, such as those passed to a main() method.
    * @param args The array of option string.  The method uses all strings up
    *             to the end of the array, or the first null element,
    *             whichever comes first.
    */
  protected void
  rebuild(String[] args)
  {
    rebuild(args, false);
  }

  /**
    * Method to allow a DebuggerParameter object to be reconstituted
    * from an array of strings, such as those passed to a main() method.
    * @param args The array of option string.  The method uses all strings up
    *             to the end of the array, or the first null element,
    *             whichever comes first.
    * @param add Set to true to add the options to the ones already present
    */
  protected void
  rebuild(String[] args, boolean add)
  {
    if (!add)
    {
      _invalidOptions.removeAllElements();    // clear the list of unrecognized parms
      _options.clear();             // clear the list of parmeters
      _debuggeeName = new String();
      _debuggeeArgs = new String();
    }

    boolean nextIsDebuggee = false;
    boolean twoOrMoreDebuggeeArgs = false;
    for (int argIndex = 0; argIndex < args.length && args[argIndex] != null; argIndex++)
    {
      if (0 == args[argIndex].length())
        continue;

      // if the debuggee name has been set, anything following it is debuggee arguments
      if (0 != _debuggeeName.length())
      {
        if (0 == _debuggeeArgs.length())
          _debuggeeArgs = args[argIndex];

        // The following situations do not occur when running under a debugger
        // wrapper, as the wrapper puts all debuggee arguments into a single
        // string.  When running the Java code standalone, we make a "best
        // effort" to reconstruct the debuggee command line.
        else if (twoOrMoreDebuggeeArgs)
        {
          _debuggeeArgs += " " + QuoteUtil.enquoteIfNecessary(args[argIndex]);
        }
        else
        {
          _debuggeeArgs = QuoteUtil.enquoteIfNecessary(_debuggeeArgs) + " " + QuoteUtil.enquoteIfNecessary(args[argIndex]);
          twoOrMoreDebuggeeArgs = true;
        }
        continue;
      }

      // Check if the last option was the debuggee name option
      if (nextIsDebuggee)
      {
        _debuggeeName = args[argIndex];
        continue;
      }

      // This is probably a debugger option
      // Handle the parameter naming differences between UNIX and Windows
      String arg = rationalizeOption(args[argIndex]);

      if (_nameSep.equals(arg))
      {
        // We have hit the argument that denotes the end of the debugger parms.
        // The next argument is the debuggee name
        nextIsDebuggee = true;
        continue;
      }

      if (_optionDescriptors == null)
      {
        // The DebuggerOptions object has not been given a personality.
        acceptUnknownOption(arg);
      }
      else
      {
        // There are descriptors for the options.  Walk the table to validate
        int optionIndex = -1;
        int argct = 0;
        DebuggerOptionDescriptor d = null;
        for ( ; argct == 0 &&
                ++optionIndex < _optionDescriptors.size() &&
                null != _optionDescriptors.elementAt(optionIndex);)
        {
          d = (DebuggerOptionDescriptor)_optionDescriptors.elementAt(optionIndex);
          argct = d.match(arg);
          if (argct == 2)
          {
            // potentially valid; check the next option for the parameter value
            if (++argIndex < args.length && args[argIndex] != null && '-' != rationalizeOption(args[argIndex]).charAt(0))
              d.setValue(args[argIndex]);
            else
            {
              // not valid!
              argIndex--;
              argct = 0;
            }
          }
          if (argct != 0)
            _options.put(d.name(), d.value());   // valid option
        }
        // was the option accepted?
        if (argct == 0)
        {
          if (_acceptUnknownOptions)
            acceptUnknownOption(arg);    // we will accept unknown options
          else if ('-' != arg.charAt(0))
            _debuggeeName = arg;           // not an option; must be the debuggee name
          else                             // option is invalid & is ignored
            _invalidOptions.addElement(args[argIndex]);
        }
      }
    }
  }

  /** Set the value of the named option.
    * @param name The parameter name
    * @param value The parameter value.  This may be null
    */
  protected void
  setValueByName(String name, String value)
  {
    // replace the specified value in the table
    _options.put(name, value);
  }

  /** Clear the debuggee name & options
    */
  protected void
  clearDebuggee()
  {
    _debuggeeName = "";
    _debuggeeArgs = "";
  }

  /**
   * Get the option hash table.
   */
  protected Hashtable getOptions()
  {
    return _options;
  }

  //----------------------------------------------------------------------------
  // Private Functions
  //----------------------------------------------------------------------------
  /** Accept the option using the "anonymous" rules, which are:
    * <ol>
    * <li>Must begin with '-'
    * <li>Parameters values must be separated from the option with '='
    * <li>The first parameter that dows not meeth the above rules is the debuggee name
    * <!ol>
    * @param arg The argument to be accepted.
    */
  private void
  acceptUnknownOption(String arg)
  {
    if ('-' != arg.charAt(0))
    {
      // must be the debuggee name
      _debuggeeName = arg;
    }
    else
    {
      int eqIndex = arg.indexOf('=');
      if (eqIndex == -1)
      {
        // This parameter has no '=' in it.
        _options.put(arg, "");
      }
      else if (eqIndex > (arg.length() - 1))
      {
        String key = arg.substring(0, eqIndex - 1);
        _options.put(key, "");
      }
      else
      {
        String key = arg.substring(0, eqIndex - 1);
        String value = arg.substring(eqIndex + 1);
        _options.put(key, value);
      }
    }
  }


  /** Convert an option into the canonical (UNIX) form, since
    * Windows platforms may use '/' as an option separator.
    * @param arg The argument to be converted
    * @returns The argument, in canonical form
    */
  private String
  rationalizeOption(String arg)
  {
    if (File.separatorChar == '\\')
    {
      // Windows system - translate parameter starting characters
       if (arg.charAt(0) == '/')
         return new String("-" + arg.substring(1));
    }
    return arg;
  }

  //----------------------------------------------------------------------------
  // Fields
  //----------------------------------------------------------------------------
  /** The debuggee name */
  private String _debuggeeName = new String();
  /** The debuggee arguments */
  private String _debuggeeArgs = new String();
  /** Flag to control if unknown options are flagged as invalid.
    * May be set by a derived class prior to using the rebuild() method.
    */
  protected boolean _acceptUnknownOptions = false;
  /** The table of valid options */
  private Hashtable _options = new Hashtable();
  /** The option that seprates the debuggee name from the options */
  private static String _nameSep = new String("--");
  /** The descriptors for the options */
  private Vector _optionDescriptors = null;
  /** Options that have been determined to be invalid */
  private Vector _invalidOptions = new Vector();
}

