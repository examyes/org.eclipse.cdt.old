package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ViewInformation.java, java-model, eclipse-dev, 20011128
// Version 1.11.1.2 (last modified 11/28/01 16:11:16)
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
 * Objects of this class represent the various kinds of views that are
 * supported by a debug engine e.g. source view, disassembly view, etc.
 * ViewInformation objects are contained within their associated DebugEngine
 * object and can be retrieved from that object by calling
 * DebugEngine.supportedViews().
 * <p>Note that there is also a class called "View", and its relationship to the
 * ViewInformation class is as follows: ViewInformation objects are associated
 * with debug engines and describe
 * the <i>kinds</i> of views generally supported by the debug engine, whereas
 * View objects are associated with parts (i.e. compilation units) and
 * represent the views that can actually be built for that part. Each View
 * in a part will have an associated ViewInformation object which describes
 * what kind of view it is, but the opposite is not necessarily true - each
 * ViewInformation object in a debug engine will not always have a corresponding
 * View object in every part. This is because it may not be possible for the
 * debug engine to build a view of every type for every part. As an example,
 * if the debug engine generally supports building source views, then there
 * will be a ViewInformation object for source views in the DebugEngine object.
 * As well, every part that contains debug information (or at least a
 * line number table) will have a View object for a source view.
 * However,
 * parts which do not contain debug information will not have a source view
 * View object since the debug engine cannot build a source view those parts.
 * @see DebugEngine#supportedViews()
 * @see Part#views()
 * @see View
 */

public class ViewInformation extends DebugModelObject
{
  ViewInformation(ERepGetViews epdcViewInformation, short viewIndex)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(4, "Creating ViewInformation() : " + epdcViewInformation.name());

    _epdcViewInformation = epdcViewInformation;
    _index = viewIndex;
  }

  /**
   * Get the name of this view type e.g. "Source", "Disassembly", etc.
   */

  public String name()
  {
    return _epdcViewInformation.name();
  }

  /**
   * Indicates whether a prefix area has been prepended to each line of text
   * in a view of this type. What will be in the prefix area varies depending
   * on the type of the view. Typically, in a source view, the prefix area
   * contains line numbers, and in a disassembly view, it contains addresses.
   * <p>In order to determine the length of the prefix area, use the
   * View.prefixLength() method.
   */

  public boolean hasPrefixArea()
  {
    return _epdcViewInformation.hasPrefixArea();
  }

  /** What kind of view does this ViewInformation object describe? e.g.
   *  source, disassembly, etc.
   *  Values returned by this method correspond to the constants for view
   *  type in com.ibm.debug.epdc.EPDC e.g. EPDC.View_Class_Source == 2.
   *  @see com.ibm.debug.epdc.EPDC#View_Class_Unk
   */

  public short kind()
  {
    return _epdcViewInformation.kind();
  }

  /**
   * Determine if this ViewInformation object describes a source view.
   */

  public boolean isSourceView()
  {
    return _epdcViewInformation.kind() == EPDC.View_Class_Source;
  }

  /**
   * Determine if this ViewInformation object describes a disassembly view.
   */

  public boolean isDisassemblyView()
  {
    return _epdcViewInformation.kind() == EPDC.View_Class_Disasm;
  }

  /**
   * Determine if this ViewInformation object describes a mixed view.
   */

  public boolean isMixedView()
  {
    return _epdcViewInformation.kind() == EPDC.View_Class_Mixed;
  }

  /**
   * Determine if this ViewInformation object describes a listing view.
   */

  public boolean isListingView()
  {
    return _epdcViewInformation.kind() == EPDC.View_Class_Listing;
  }

  /**
   * Indicates whether or not views of this type support monitoring expressions.
   * Expressions must be monitored within the context of a view and this
   * method indicates whether or not this type of view can be used to specify
   * the context.
   */

  public boolean isMonitorCapable()
  {
    return _epdcViewInformation.isMonitorCapable();
  }

  /**
   * Indicates whether or not views of this type support setting line
   * breakpoints.
   */

  public boolean isLineBreakpointCapable()
  {
    return _epdcViewInformation.isLineBreakpointCapable();
  }

  short index()
  {
    return _index;
  }

  /**
   * Remove references so they can be gc'ed.
   */
  void cleanup()
  {
    _epdcViewInformation = null;
  }

  // NOTE: There is currently no writeObject nor readObject in this class
  // because we always want the default Java serialization done!

  private ERepGetViews _epdcViewInformation;
  private short _index;
}
