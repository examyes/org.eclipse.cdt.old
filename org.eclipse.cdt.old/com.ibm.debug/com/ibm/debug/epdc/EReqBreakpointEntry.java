package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqBreakpointEntry.java, java-epdc, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:25:19)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * This class exists solely to make it a bit easier to construct a function
 * breakpoint request.
 */

public class EReqBreakpointEntry extends EReqBreakpointLocation
{
  /**
   * Use this ctor to set a function breakpoint.
   */

  public EReqBreakpointEntry(short attr,  // enable, defer, etc.
                            EEveryClause clause,
                            String entryName,
                            String moduleName,
                            String partName,
                            String fileName,
                            EStdExpression2 condition,
                            int threadID,  // 0 for all threads
                            int entryID)
  {
    super(EPDC.SetBkp,
          EPDC.EntryBkpType,
          attr,
          clause,
          entryName, // function name
          moduleName, // optional for function bkp
          partName, // optional for function bkp
          fileName,
          condition,
          threadID,
          0,       // bkp ID
          entryID, // entry ID
          null,    // stmt number
          null     // context - line bkps only
          );
  }

  /**
   * Use this ctor to replace a function breakpoint.
   */

  public EReqBreakpointEntry(short attr, // enable, defer, etc.
                            EEveryClause clause,
                            String entryName,
                            String moduleName,
                            String partName,
                            String fileName,
                            EStdExpression2 condition,
                            int threadID, // 0 for all threads
                            int bkpID,  // ID of bkp being replaced
                            int entryID)
  {
    super(EPDC.ReplaceBkp,
          EPDC.EntryBkpType,
          attr,
          clause,
          entryName, // function name
          moduleName, //optional
          partName, //optional
          fileName,
          condition,
          threadID,
          bkpID,
          entryID, // entry ID
          null,    // stmt number
          null     // context - line bkps only
          );
  }
}
