package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/View.java, java-model, eclipse-dev, 20011128
// Version 1.23.1.2 (last modified 11/28/01 16:11:14)
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

/**
 * A View is a mechanism for retrieving the lines of code in a part
 * (compilation unit). Exactly what those lines will contain varies depending
 * on the kind of view being used. For example, in a source view, each line
 * contains a line of source from a source file, while in a disassembly view,
 * each line contains a disassembled machine instruction.
 * <p>Each View object has an associated ViewInformation object which describes
 * the characteristics of the view. This ViewInformation object can be
 * retrieved via the viewInformation() method. Among other things, the
 * ViewInformation object will indicate what kind of view this is - source,
 * disassembly, etc.
 * <p>View objects are contained within Part objects to reflect the fact that
 * this is a view of a particular part. Note that the number of views contained
 * within a part will be <= the number of view types supported by the debug engine.
 * This is because not every kind of view can be built for every part. For
 * example, a source view cannot be built for a part that has no debug
 * information, even if the debug engine generally supports the building of
 * source views.
 * <p>A View object will contain a list of one or more ViewFile objects
 * representing the files that make up the view. These can be retrieved via
 * the files() method. It is through the ViewFile objects that client code
 * can actually retrieve lines of text in the view. In a source view, there
 * is typically one ViewFile object for every source file used in the
 * compilation unit. For example, in a C++ part, there will usually be
 * one ViewFile object for the .cpp file and one for each .hpp file that
 * has been #included in that .cpp file (directly or indirectly).
 * (Note: Files that do not contain any lines of executable
 * code might be omitted e.g. a header file which has only type definitions.)
 * @see ViewInformation
 * @see DebugEngine#supportedViews()
 * @see Part#views()
 * @see ViewFile
 */

public class View extends DebugModelObject
{
  View(Part owningPart, short index, EViewData epdcView)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(3, "Creating View : OwningPart=" + owningPart.name() + " Index=" + index);

    _owningPart = owningPart;
    _index = index;
    _viewInformation = _owningPart.module().process().debugEngine().getViewInformation(_index);
    change(epdcView);
  }

  void change(EViewData epdcView)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "In View[" + _owningPart.name() + ", " + _index + "].change()");

    _epdcView = epdcView;

    Vector epdcFiles = epdcView.files();

    int numberOfFiles;
    int i;

    if ((numberOfFiles = epdcFiles.size()) > 0)
    {
       if (numberOfFiles+1 > _files.size())
          _files.setSize(numberOfFiles+1);

       for (i = 0; i < numberOfFiles; i++)
       {
         ViewFile file = (ViewFile)_files.elementAt(i + 1);

         if (file == null)
         {
             file = new ViewFile(this, i+1, (EViews)epdcFiles.elementAt(i));

             addViewFile(file);
         }
         else
            file.change((EViews)epdcFiles.elementAt(i));
       }

       _filesHaveBeenRetrieved = true;
    }
  }

  /**
   * Add a ViewFile
   */
  void addViewFile(ViewFile file)
  {
    if (_files == null)
        _files = new Vector();

    setVectorElementToObject(file, _files, file.index());
  }

  void breakpointAdded(LocationBreakpoint bkp)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
       Model.TRACE.dbg(2, "In View.breakpointAdded");

    // Get the EPDC location of the bkp within this view:

    EStdView epdcLocation = bkp.getEPDCLocation(viewInformation());

    short viewID = epdcLocation.getViewNo();
    int fileIndex = epdcLocation.getSrcFileIndex();

    // The following are indications that the bkp has no valid location in
    // this view, so just return:

    if (viewID != _index || fileIndex < 1)
       return;

    // Make sure the _files Vector is large enough to hold this file:

    if (fileIndex >= _files.size())
       _files.setSize(fileIndex+1);

    ViewFile file = (ViewFile)_files.elementAt(fileIndex);

    // See if this is a file for which we do not yet have a ViewFile object:

    if (file == null)
    {
       // Create a ViewFile object and add it to the list:

       file = new ViewFile(this, fileIndex);
       _files.setElementAt(file, fileIndex);
    }

    // Tell the ViewFile that a bkp was added and on what line number:

    file.breakpointAdded(bkp, epdcLocation.getLineNum());
  }

  void breakpointRemoved(LocationBreakpoint bkp)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
       Model.TRACE.dbg(2, "In View.breakpointRemoved");

    // Get the EPDC location of the bkp within this view:

    EStdView epdcLocation = bkp.getEPDCLocation(viewInformation());

    short viewID = epdcLocation.getViewNo();
    int fileIndex = epdcLocation.getSrcFileIndex();

    // The following are indications that the bkp has no valid location in
    // this view, so just return:

    if (viewID != _index || fileIndex < 1)
       return;

    if (_files == null)
       return;  // TODO: This shouldn't happen so use trace facility here
                //       to print "something is wrong" message

    ViewFile file = (ViewFile)_files.elementAt(fileIndex);

    if (file == null)
       return;  // TODO: This shouldn't happen so use trace facility here
                //       to print "something is wrong" message

    // Tell the ViewFile that a bkp was removed and from what line number:

    file.breakpointRemoved(bkp, epdcLocation.getLineNum());
  }

  short index()
  {
    return _index;
  }

  /**
   * Retrieve the list of files that make up this view.
   * The list is a Vector of ViewFile objects. Retrieving this list
   * of files from the debug engine is done synchronously - the method
   * will wait until a reply has been received from the debug engine.
   * Note: Some entries in the files Vector may be null. Note also that
   * client code should use discretion when it comes to retrieving lists
   * of files in a view since the debug engine does not automatically
   * provide this list - a separate request must be sent to the debug engine
   * for each list of files retrieved.
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   */

  public Vector getFiles()
  throws java.io.IOException
  {
    if (!_filesHaveBeenRetrieved) // Do a "views verify" to get the list of files
    {
        if (!_owningPart.verify()) // Request could not be sent
            return null;
    }

    if (_files == null || _files.size() == 0)
       return null;
    else
       return _files;
  }

  ViewFile file(int index)
  throws java.io.IOException
  {
    if (!_filesHaveBeenRetrieved) // Do a "views verify" to get the list of files
    {
        if (!_owningPart.verify()) // Request could not be sent
            return null;
    }

    return getFileNoVerify(index);
  }

  ViewFile getFileNoVerify(int index)
  {
    if (_files == null || _files.size() == 0 || index >= _files.size())
       return null;
    else
       return (ViewFile)_files.elementAt(index);
  }

  /**
   * Get the length of the prefix area for this view.
   * @see ViewInformation#hasPrefixArea()
   */

  public byte prefixLength()
  {
    return _epdcView.prefixLength();
  }

  /**
   * Get the part that is associated with this view.
   */

  public Part part()
  {
    return _owningPart;
  }

  /**
   * Get the ViewInformation object for this view. The ViewInformation object
   * contains the attributes for this view, including what kind of view it
   * is e.g. source, disassembly, etc.
   */

  public ViewInformation viewInformation()
  {
    return _viewInformation;
  }

  /** What kind of view is this? e.g. source, disassembly, etc.
   *  Values returned by this method correspond to the constants for view
   *  type in com.ibm.debug.epdc.EPDC e.g. EPDC.View_Class_Source == 2.
   *  @see com.ibm.debug.epdc.EPDC#View_Class_Unk
   */

  public short kind()
  {
    return _viewInformation.kind();
  }

  /**
   * Determine if this is a source view.
   */

  public boolean isSourceView()
  {
    return _viewInformation.isSourceView();
  }

  /**
   * Determine if this is a disassembly view.
   */

  public boolean isDisassemblyView()
  {
    return _viewInformation.isDisassemblyView();
  }

  /**
   * Determine if this is a mixed view.
   */

  public boolean isMixedView()
  {
    return _viewInformation.isMixedView();
  }

  /**
   * Determine if this is a listing view.
   */

  public boolean isListingView()
  {
    return _viewInformation.isListingView();
  }

  /**
   * Indicates whether or not this view supports monitoring expressions.
   * Expressions must be monitored within the context of a view and this
   * method indicates whether or not this view can be used to specify
   * the context.
   */

  public boolean isMonitorCapable()
  {
    return _viewInformation.isMonitorCapable();
  }

  /**
   * Indicates whether or not this view supports setting line
   * breakpoints.
   */

  public boolean isLineBreakpointCapable()
  {
    return _viewInformation.isLineBreakpointCapable();
  }

  /**
   * Remove references so they can be gc'ed.
   */
  void cleanup()
  {
    _owningPart = null;
    _epdcView = null;
    if (_files != null)
    {
       int cnt = _files.size();
       for (int i = 0; i < cnt; i++)
       {
          ViewFile f = (ViewFile)_files.elementAt(i);
          if (f != null)
             f.cleanup();
       }
       _files.removeAllElements();
       _files = null;
    }
    if (_viewInformation != null)
    {
       _viewInformation.cleanup();
       _viewInformation = null;
    }
  }

  private Vector _files = new Vector();
  private Part _owningPart;
  private EViewData _epdcView;
  private short _index;
  private boolean _filesHaveBeenRetrieved = false;
  private ViewInformation _viewInformation;
}
