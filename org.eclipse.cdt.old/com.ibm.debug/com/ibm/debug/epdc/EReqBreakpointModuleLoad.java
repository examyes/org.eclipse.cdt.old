package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqBreakpointModuleLoad.java, java-epdc, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:25:49)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */


/**
 * The class to construct a breakpoint when a module is to be loaded.
 */
public class EReqBreakpointModuleLoad extends EReqBreakpointEvent
{
  /**
   * Constuctor to set a breakpoint when a particular dll file is
   * to be loaded.
   */
  public EReqBreakpointModuleLoad(short attr,
                                  EEveryClause clause,
                                  String moduleName,
                                  int threadID  // 0 for all threads
                                 )
  {
    super(EPDC.SetBkp,
          (short)EPDC.LoadBkpType,
          attr,
          clause,
          moduleName,
          null, // module name
          null, // part name
          null, // file name
          null, // condition
          0,  // byte count
          null, // context
          threadID,
          0,          // bkp ID
          null); // computed addr
  }

  /**
   * Constructor to replace the name of the module of the breakpoint
   */

  public EReqBreakpointModuleLoad(short attr,
                                  EEveryClause clause,
                                  String addrOrExpr,
                                  String moduleName,
                                  String partName,
                                  String fileName,
                                  EStdExpression2 condition,
                                  EStdView context,
                                  int threadID,  // 0 for all threads
                                  int bkpID,     // ID of bkp being replaced
                                  String computedAddress)
  {
    super(EPDC.ReplaceBkp,
          (short)EPDC.LoadBkpType,
          attr,
          clause,
          addrOrExpr,  // Addr bkps only
          moduleName,
          partName,
          fileName,
          condition,
          0,  // byte count
          context,
          threadID,
          bkpID,
          computedAddress);
  }
}
