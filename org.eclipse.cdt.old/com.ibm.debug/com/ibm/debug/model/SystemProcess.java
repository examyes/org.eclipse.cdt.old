package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/SystemProcess.java, java-model, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:13:40)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.PrintWriter;

/**
 * Objects of this class represent processes which can be attached to for
 * debugging. A list of these objects can be retrieved by calling
 * DebugEngine.getSystemProcessList.
 * <p>In order to attach to a process using a SystemProcess object, first
 * construct a DebuggeeAttachOptions object using the SystemProcess object
 * and then call DebugEngine.attach, passing to it the DebuggeeAttachOptions
 * object that was constructed.
 * @see DebugEngine#getSystemProcessList
 * @see DebuggeeAttachOptions
 * @see DebugEngine#attach
 */

public class SystemProcess extends DebugModelObject
{
  SystemProcess(String[] processDetails, int index)
  {
    _processDetails = processDetails;
    _index = index;
  }

  /**
   * Get the information sent to us by the debug engine for this process.
   * @return An array of strings containing the information for this process.
   * This array represents a row in a table of processes that can be
   * presented to the user, and each string in the array represents a column
   * in the table. The column headings can be retrieved by calling
   * DebugEngine.getProcessListColumnDetails which returns an array of
   * ProcessListColumnDetails objects. Note that each ProcessListColumnDetails
   * object not only contains the text for the column heading, but also
   * specifies what alignment the UI should use (left, right, or centered)
   * when displaying the contents of the column.
   * @see DebugEngine#getProcessListColumnDetails
   */

  public String[] getProcessDetails()
  {
    return _processDetails;
  }

  int getIndex()
  {
    return _index;
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
       for (int i = 0; i < _processDetails.length; i++)
           printWriter.print(_processDetails[i] + " ");

       printWriter.println();
    }
  }

  private String[] _processDetails;
  private int _index;
}
