package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Location.java, java-model, eclipse-dev, 20011128
// Version 1.18.1.2 (last modified 11/28/01 16:11:06)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;
import java.util.Vector;
import com.ibm.debug.epdc.*;

/**
 * Objects of this class are used to identify specific locations within the program
 * being debugged. These locations consist of a file (a ViewFile object) and
 * a line number within that file.
 */

public class Location
{
  /** Construct a Location object by specifying a file and line number.
   */

  public Location(ViewFile file, int lineNumber)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
       try
       {
         Model.TRACE.evt(4, "Creating Location : File=" + file.baseFileName() + " Line#=" + lineNumber);
       }
       catch (java.io.IOException excp)
       {
       }

    _file = file;
    _lineNumber = lineNumber;
  }

  /** Construct a Location object by specifying a file, line number and column number.
   */

  Location(ViewFile file, int lineNumber, int column)
  {
    this(file, lineNumber);

    _column = column;
  }

  /**
   * Get the column number of this Location. Note that not all Locations have
   * valid column numbers, in which case this method will return 0. The
   * Location object returned by ViewFile.findString <i>does</i> have a
   * valid column number but most other Location objects do not.
   */

  public int getColumnNumber()
  {
    return _column;
  }

  Location(DebuggeeProcess process, EStdView epdcLocation)
  throws LocationConstructionException, java.io.IOException
  {
    LocationConstructionException exception = new LocationConstructionException();

    if (process == null || epdcLocation == null)
       throw exception;

    Part part = process.getPart(epdcLocation.getPPID());

    if (part == null)
       throw exception;

    View view = part.getView(epdcLocation.getViewNo());

    if (view == null)
       throw exception;

    _file = view.file(epdcLocation.getSrcFileIndex());

    if (_file == null)
       throw exception;

    _lineNumber = epdcLocation.getLineNum();

    _epdcLocation = epdcLocation;
  }

  /**
   * Get the ViewFile object associated with this Location object.
   */

  public ViewFile file()
  {
    return _file;
  }

  /**
   * Get the line number of this Location.
   */

  public int lineNumber()
  {
    return _lineNumber;
  }

  /**
   * This method returns the function(s) which contain this location. Will
   * return null if the location can't be mapped to a particular function.
   * The returned Vector may also contain some null entries.
   */

  public Vector getFunctionsAtThisLocation()
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "Location[" + _file.baseFileName() + ", " + _lineNumber + "].getFunctionsAtThisLocation()");

    if (_functions == null)
    {
        DebuggeeProcess process = _file.view().part().module().process();
        DebugEngine engine = process.debugEngine();

        if (Model.TRACE.EVT && Model.traceInfo())
            Model.TRACE.evt(2, "Sending EPDC request: Remote_ContextQualGet");

        if (!engine.prepareForEPDCRequest(EPDC.Remote_ContextQualGet,
                                       DebugEngine.sendReceiveSynchronously) ||
            !engine.processEPDCRequest(new EReqContextQualGet(getEStdView()),
                                       DebugEngine.sendReceiveSynchronously)
                                      )
            return null;

        ERepContextQualGet reply = (ERepContextQualGet)engine.getMostRecentReply();

        if (reply == null || reply.getReturnCode() != EPDC.ExecRc_OK)
            return null;

        int[] functionIDs = reply.getEntryIDs();

        if (functionIDs == null || functionIDs.length == 0)
            return null;

        _functions = new Vector(functionIDs.length);
        Function function;

        for (int i = 0; i < functionIDs.length; i++)
        {
            if ((function = process.getFunction(functionIDs[i], true)) != null)
               _functions.addElement(function);
        }
    }

    return _functions;
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
       try
       {
         printWriter.print("File: " + _file.name());
       }
       catch (java.io.IOException excp)
       {
       }
       printWriter.print("  line#: " + _lineNumber);
    }
  }

  EStdView getEStdView()
  {
    if (_epdcLocation == null)
    {
       View view = _file.view();

       return new EStdView(view.part().id(),
                           view.index(),
                           _file.index(),
                           _lineNumber);
    }
    else
      return _epdcLocation;
  }

  private ViewFile _file;
  private int _lineNumber;
  private int _column;
  private EStdView _epdcLocation;
  private Vector _functions = null;
}
