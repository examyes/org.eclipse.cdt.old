package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Line.java, java-model, eclipse-dev, 20011128
// Version 1.17.1.2 (last modified 11/28/01 16:11:04)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;

/**
 * This class represents a line of text from a file within a view.
 * @see ViewFile
 */

public class Line
{
  Line(ViewFile owningFile, int lineNumberWithinFile, EStdSourceLine epdcLine)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
       try
       {
         Model.TRACE.evt(4, "Creating Line : ViewFile=" + owningFile.baseFileName() + " Line#=" + lineNumberWithinFile);
       }
       catch (java.io.IOException excp)
       {
       }

    _owningFile = owningFile;
    _lineNumberWithinFile = lineNumberWithinFile;
    _lineText = epdcLine.lineText();
    _isExecutable = epdcLine.isExecutable();
  }

  /**
   * Returns the ViewFile object representing the file that contains this line.
   *
   */

  public ViewFile fileContainingThisLine()
  {
    return _owningFile;
  }

  /**
   * Returns the line number of this line within the file.
   *
   */

  public int lineNumberWithinFile()
  {
    return _lineNumberWithinFile;
  }

  /**
   * Returns a String containing the actual text of the line. If this line
   * is contained within a view that has a prefix area, the text will
   * include the prefix area.
   *
   */

  public String lineText()
  {
    return _lineText;
  }

  /**
   * Get only the prefix text for this line. Returns null if there is no
   * prefix.
   */

  public String getPrefix()
  {
    View view = _owningFile.view();

    byte prefixLength = view.prefixLength();

    if (!view.viewInformation().hasPrefixArea() || prefixLength == 0)
       return null;
    else
       return _lineText.substring(0, prefixLength);
  }

  /**
   * Get the line text without the prefix.
   */

  public String getLineTextWithoutPrefix()
  {
    View view = _owningFile.view();

    byte prefixLength = view.prefixLength();

    if (!view.viewInformation().hasPrefixArea() || prefixLength == 0)
       return _lineText;
    else
       return _lineText.substring(prefixLength);
  }

  /**
   * Returns an indication of whether or not this line is executable. A UI
   * may want to use this attribute to show the line to the user in a
   * different colour, detect when the user has tried to set a breakpoint on
   * a line that is not executable, etc.
   *
   */

  public boolean isExecutable()
  {
    return _isExecutable;
  }

  /**
   * Set a breakpoint on this line.
   * @param sendReceiveControlFlags A set of flags which specify the mode
   * in which this request is to be performed. There is a set of constants
   * in DebugEngine which define the possible values for this argument.
   * For example, a value of DebugEngine.sendReceiveDefault means that the
   * request is to be done asynchronously, while a value of
   * DebugEngine.sendRequestDefault
   * means that the request is to be performed synchronously.
   * <p>When done asynchronously,
   * this method will return immediately after sending the request to the
   * debug engine without waiting for a response from
   * the debug engine. The response to the request will be
   * received on a separate thread and client code will be notified of the
   * the response via the event listener mechanism.
   *
   *  @return true if the request was sent to the debug engine successfully,
   *  false otherwise. Note that a value of true does not imply that the
   *  breakpoint was successfully created by the debug engine - it simply
   *  means that the request was <i>sent</i> to the debug engine successfully.
   *  Whether or not the debug engine was actually able to set the breakpoint
   *  will be indicated via the event listener mechanism.
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   *
   */

  public boolean setBreakpoint(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "Line[" + _lineNumberWithinFile + "].setBreakpoint()");

    DebugEngine debugEngine = _owningFile.view().part().module().process().debugEngine();

    if (debugEngine.getCapabilities().getBreakpointCapabilities().statementBreakpointSupported())
    {
        String prefix = getPrefix();
        String stmtNumber = null;

        // If the prefix area is null the statement number is null.
        if (prefix != null)
            stmtNumber = prefix.trim();

        return _owningFile.setBreakpoint(true, _lineNumberWithinFile,
                                         0, 0, 0, null, 0,
                                         stmtNumber,
                                         sendReceiveControlFlags);
    }

    return _owningFile.setBreakpoint(_lineNumberWithinFile, sendReceiveControlFlags);
  }

  private ViewFile _owningFile;
  private int _lineNumberWithinFile;
  private String _lineText;
  private boolean _isExecutable;
}
