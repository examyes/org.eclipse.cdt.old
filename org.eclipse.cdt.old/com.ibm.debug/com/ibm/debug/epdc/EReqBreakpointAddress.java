package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqBreakpointAddress.java, java-epdc, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:25:46)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * The class to construct an address breakpoint (either adding a new
 * address breakpoint or replacing one).
 */

public class EReqBreakpointAddress extends EReqBreakpointLocation
{
  /**
   * Constuctor to set an address breakpoint
   */
  public EReqBreakpointAddress(short attr,  // enable, defer, etc.
                               EEveryClause clause,
                               String addrOrExpr,
                               String moduleName,
                               String partName,
                               String fileName,
                               EStdExpression2 condition,
                               int threadID,  // 0 for all threads
                               EStdView context)
  {
    super(EPDC.SetBkp,
          (short)EPDC.AddressBkpType,
          attr,
          clause,
          addrOrExpr, // addrOrExpr - addr bkps only
          moduleName,
          partName,
          fileName,
          condition,
          threadID,
          0,          // bkp ID
          0,          // entry ID
          null,       // stmt number
          context);
  }

  /**
   * Constructor to replace an address breakpoint
   */

  public EReqBreakpointAddress(short attr,  // enable, defer, etc.
                               EEveryClause clause,
                               String addrOrExpr,
                               String moduleName,
                               String partName,
                               String fileName,
                               EStdExpression2 condition,
                               int threadID,  // 0 for all threads
                               int bkpID,     // ID of bkp being replaced
                               EStdView context)
  {
    super(EPDC.ReplaceBkp,
          (short)EPDC.AddressBkpType,
          attr,
          clause,
          addrOrExpr,  // Addr bkps only
          moduleName,
          partName,
          fileName,
          condition,
          threadID,
          bkpID,
          0,          // entry ID
          null,       // stmt number
          context);
  }
}
