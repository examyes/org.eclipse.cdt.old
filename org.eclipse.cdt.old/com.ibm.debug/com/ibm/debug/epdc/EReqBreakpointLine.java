package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqBreakpointLine.java, java-epdc, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:24:01)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * This class exists solely to make it a bit easier to construct a line
 * breakpoint request.
 */

public class EReqBreakpointLine extends EReqBreakpointLocation
{
  /**
   * Use this ctor to set a line breakpoint.
   */

  public EReqBreakpointLine(short attr,  // enable, defer, etc.
                            EEveryClause clause,
                            String varPtr,
                            String moduleName,
                            String partName,
                            String fileName,
                            EStdExpression2 condition,
                            int threadID,  // 0 for all threads
                            String stmtNum,
                            EStdView context)
  {
    super(EPDC.SetBkp,
          EPDC.LineBkpType,
          attr,
          clause,
          varPtr,
          moduleName,
          partName,
          fileName,
          condition,
          threadID,
          0, // bkp ID
          0, // entry ID
          stmtNum,
          context);
  }

  /**
   * Use this ctor to replace a line breakpoint.
   */

  public EReqBreakpointLine(short attr,  // enable, defer, etc.
                            EEveryClause clause,
                            String moduleName,
                            String partName,
                            String fileName,
                            EStdExpression2 condition,
                            int threadID,  // 0 for all threads
                            int bkpID,     // ID of bkp being replaced
                            String stmtNum,
                            EStdView context)
  {
    super(EPDC.ReplaceBkp,
          EPDC.LineBkpType,
          attr,
          clause,
          null,  // Addr bkps only
          moduleName,
          partName,
          fileName,
          condition,
          threadID,
          bkpID,
          0, // entry ID
          stmtNum,
          context);
  }
}
