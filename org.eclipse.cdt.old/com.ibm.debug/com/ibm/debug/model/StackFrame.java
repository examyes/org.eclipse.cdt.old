package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/StackFrame.java, java-model, eclipse-dev, 20011128
// Version 1.20.1.2 (last modified 11/28/01 16:12:10)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;
import java.util.Vector;
import java.io.*;

/**
 * This class represents stack entries within a stack.
 */


public class StackFrame extends DebugModelObject
{
  StackFrame (Stack owningStack, int index, ERepGetNextStackEntry epdcStackEntry)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(3, "Creating StackFrame : index=" + index);

    _owningStack = owningStack;
    _index = index;
    change(epdcStackEntry, true);
  }

  void change(ERepGetNextStackEntry epdcStackEntry)
  {
    change(epdcStackEntry, false);
  }

  private void change(ERepGetNextStackEntry epdcStackEntry, boolean isNew)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "StackFrame[" + _index + "].change(" + epdcStackEntry + ")");

    _epdcStackEntry = epdcStackEntry;

    if (!isNew)
    {
      _currentLocation = null; // Assume saved locations no longer valid

      //Queue StackFrameChangedEvent

      DebugEngine debugEngine = _owningStack.owningThread().owningProcess().debugEngine();
      int requestCode = debugEngine.getMostRecentReply().getReplyCode();

      debugEngine.getEventManager().addEvent(new StackFrameChangedEvent(this,
                                                                   this,
                                                                   requestCode),
                                             _eventListeners);
    }
  }

  public void addEventListener(StackFrameEventListener eventListener)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "StackFrame[" + _index + "].addEventListener(" + eventListener + ")");

    _eventListeners.addElement(eventListener);
  }

  public void removeEventListener(StackFrameEventListener eventListener)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(3, "StackFrame[" + _index + "].removeEventListener(" + eventListener + ")");

    int index = _eventListeners.indexOf(eventListener);

    if (index != -1)
    {
        try
        {
          _eventListeners.setElementAt(null, index);
        }
        catch(ArrayIndexOutOfBoundsException excp)
        {
        }
    }
  }

  /**
   *  Get the stack which owns this stackframe.
   */

  public Stack owningStack()
  {
    return _owningStack;
  }

  int index()
  {
    return _index;
  }

  /**
   * Return an array of column contents.
   * The column headings and meanings of each column is determined by using StackColumnDetails.
   * @see StackColumnDetails
   */
  public String[] columns()
  {
    return _epdcStackEntry.columns();
  }

  /**
   * Return remaining stack size (OS/2 only).
   */
  public String stackEntryRemStkSize()
  {
    return _epdcStackEntry.stackEntryRemStkSize();
  }

  /**
   * Return number of parameters.
   */
  public int numOfParms()
  {
    return _epdcStackEntry.numOfParms();
  }

  /**
   * Print the stackframe object.
   */

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
      String[] columns = columns();
      if ((columns != null) && (columns.length > 0))
        for (int j=0; j<columns.length; j++)
          printWriter.print(columns[j] + "  ");
      printWriter.print(stackEntryRemStkSize() + "  ");
      printWriter.println(numOfParms());

      super.print(printWriter);
      printWriter.println();
    }
  }

  /**
   * Return the current location (file and line number) of this StackFrame object.
   * @param viewInformation The view information object for a particular kind
   * of view e.g. source, disassembly, etc. The location returned will be
   * a location within a view of this type. Will return null if the current location
   * has no view of this type.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */

  public Location getCurrentLocation(ViewInformation viewInformation)
  throws IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "StackFrame[" + _index + "].getCurrentLocation()");

    short viewNo = viewInformation.index();

    DebuggeeProcess process = _owningStack.owningThread().owningProcess();
    DebugEngine debugEngine = process.debugEngine();

    if (_currentLocation == null)
    {
        _currentLocation = new Location[debugEngine.numberOfSupportedViews()+1];
    }

    if (_currentLocation[viewNo] == null)
    {
       EStdView[] epdcLocations = _epdcStackEntry.stackEntryViewInfo();

       EStdView epdcLocation = null;

       // To begin with, assume that the array of EStdViews is in order of
       // view number. If not, loop through the array looking for the right
       // entry.

       if (viewNo == epdcLocations[viewNo - 1].getViewNo())
          epdcLocation = epdcLocations[viewNo - 1];
       else
          for (int i = 0; i < epdcLocations.length; i++)
              if (viewNo == epdcLocations[i].getViewNo())
                 epdcLocation = epdcLocations[i];

       if (epdcLocation == null || !epdcLocation.isComplete())
       {
          if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_StackBuildView,
                                         DebugEngine.sendReceiveSynchronously))
             return null;

          if (Model.TRACE.EVT && Model.traceInfo())
              Model.TRACE.evt(2, "Sending EPDC request: Remote_StackBuildView");

          if (!debugEngine.processEPDCRequest(new EReqStackBuildView(_owningStack.owningThread().debugEngineAssignedID(), _index),
                                            DebugEngine.sendReceiveSynchronously))
              return null;

          ERepStackBuildView reply = (ERepStackBuildView)debugEngine.getMostRecentReply();

          if (reply == null || reply.getReturnCode() != EPDC.ExecRc_OK)
              return null;

          epdcLocation = reply.stackView();

          // The engine still does not have the context most probably because
          // there is no context information available to the engine for this
          // stack frame.
          if (!epdcLocation.isComplete())
              return null;

          _currentLocation[viewNo] = debugEngine.switchView(epdcLocation,
                                                            viewInformation);
       }
       else
         try
         {
            _currentLocation[viewNo] = new Location(process, epdcLocation);
         }
         catch (LocationConstructionException excp)
         {
            return null;
         }
    }

    return _currentLocation[viewNo];
  }

  void prepareToDie()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "StackFrame[" + _index + "].prepareToDie()");

    DebugEngine debugEngine = _owningStack.owningThread().owningProcess().debugEngine();

    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new StackFrameEndedEvent(this,
                                                                    this,
                                                                    requestCode
                                                                    ),
                                           _eventListeners
                                          );
  }

  private Stack _owningStack;
  private int _index;
  private ERepGetNextStackEntry _epdcStackEntry;
  private Vector _eventListeners = new Vector();
  private Location[] _currentLocation;
}
