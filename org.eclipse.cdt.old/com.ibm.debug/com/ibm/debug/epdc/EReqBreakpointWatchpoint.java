package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqBreakpointWatchpoint.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:25:48)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */


/**
 * The class to construct a watchpoint breakpoint (either adding a new
 * address change breakpoint or replacing one).
 */
public class EReqBreakpointWatchpoint extends EReqBreakpointEvent
{
  /**
   * Constuctor to set an address change breakpoint
   */
  public EReqBreakpointWatchpoint(short attr,
                                  EEveryClause clause,
                                  String addrOrExpr,
                                  String moduleName,
                                  String partName,
                                  String fileName,
                                  EStdExpression2 condition,
                                  int byteCount,
                                  EStdView context,
                                  int threadID,  // 0 for all threads
                                  String computedAddress)
  {
    super(EPDC.SetBkp,
          (short)EPDC.ChangeAddrBkpType,
          attr,
          clause,
          addrOrExpr,
          moduleName,
          partName,
          fileName,
          condition,
          byteCount,
          context,
          threadID,
          0,          // bkp ID
          computedAddress);
  }

  /**
   * Constructor to replace an address change breakpoint
   */

  public EReqBreakpointWatchpoint(short attr,
                                  EEveryClause clause,
                                  String addrOrExpr,
                                  String moduleName,
                                  String partName,
                                  String fileName,
                                  EStdExpression2 condition,
                                  int byteCount,
                                  EStdView context,
                                  int threadID,  // 0 for all threads
                                  int bkpID,     // ID of bkp being replaced
                                  String computedAddress)
  {
    super(EPDC.ReplaceBkp,
          (short)EPDC.ChangeAddrBkpType,
          attr,
          clause,
          addrOrExpr,  // Addr bkps only
          moduleName,
          partName,
          fileName,
          condition,
          byteCount,
          context,
          threadID,
          bkpID,
          computedAddress);
  }
}
