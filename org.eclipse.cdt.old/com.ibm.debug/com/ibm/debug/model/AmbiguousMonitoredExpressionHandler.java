package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/AmbiguousMonitoredExpressionHandler.java, java-model, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:13:49)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;

class AmbiguousMonitoredExpressionHandler extends DebugEngineEventAdapter
{
   /**
    * This method will be invoked if an attempt was made to evaluate an
    * expression and the engine replied that the context for the
    * expression is ambiguous. This can happen when, for example, an
    * expression in a C++ function template is to be evaluated and the template
    * has more than one instantiation within the program being debugged.
    * The Model will try to resolve the ambiguity by evaluating the expression
    * in each of the instantiations.
    * @see Model.handleAmbiguousMonitoredExpressions
    */

   public void errorOccurred(ErrorOccurredEvent event)
   {
     Object eventSource = event.getSource();

     // Just a sanity check...

     if (!(eventSource instanceof DebugEngine))
         return;

     DebugEngine engine = (DebugEngine)eventSource;

     EPDC_Request originalRequest = engine.getMostRecentRequest();

     // Another sanity check...

     if (!(originalRequest instanceof EReqExpression))
         return;

     EReqExpression originalMonitoredExprRequest = (EReqExpression)originalRequest;

     EStdExpression2 expr = null;

     try
     {
       expr = originalMonitoredExprRequest.getExpression();
     }
     catch(java.io.IOException excp)
     {
     }

     if (expr == null)
         return;

     /*
      * Now try to get the function ids corresponding to this expression
      * so that it can be evaluated in both of the instantiations.
      */
     try
     {
       if (!engine.prepareForEPDCRequest(EPDC.Remote_ContextQualGet,
                                       DebugEngine.sendReceiveSynchronously) ||
           !engine.processEPDCRequest(new EReqContextQualGet(expr.getContext()),
                                      DebugEngine.sendReceiveSynchronously)
                                     )
           return;

       ERepContextQualGet reply = (ERepContextQualGet)engine.getMostRecentReply();

       if (reply == null || reply.getReturnCode() != EPDC.ExecRc_OK)
          return;

       int[] entryIDs = reply.getEntryIDs();

       if (entryIDs == null)
          return;

       /**
        * Evaluate the expression for each instantiation
        */
       for (int i = 0; i < entryIDs.length; i++)
       {
            expr.setEntryID(entryIDs[i]);

            if (engine.prepareForEPDCRequest(EPDC.Remote_Expression,
                                         DebugEngine.sendReceiveSynchronously))
                engine.processEPDCRequest(originalMonitoredExprRequest,
                                          DebugEngine.sendReceiveSynchronously);
       }
     }
     catch(java.io.IOException excp)
     {
       // Not much we can do.
     }
   }
}
