package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/LocalJavaDebugEngine.java, java-model, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:13:20)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;
import com.ibm.debug.epdc.*;
import com.ibm.debug.connection.*;
//import com.ibm.debug.engine.*;

/**
 * A Java debug engine which is loaded in the same JVM as the UI.
 */

public class LocalJavaDebugEngine extends DebugEngine
{
   public LocalJavaDebugEngine(Host host)
   {
     super(host);

     if (Model.TRACE.EVT && Model.traceInfo())
       Model.TRACE.evt(1, "Creating LocalJavaDebugEngine");

     /* Temporarily remove reference to the engine

     try
     {
       Temporarily remove reference to the engine

       PipedConnection uiConnection = new PipedConnection();

       uiConnection.startDumping();

       super.setConnection(uiConnection);

       new com.ibm.debug.engine.DebugEngine(uiConnection.getConnectionForOtherEndOfPipe(),
                                            true,
                                            "",
                                            null,
                                            null
                                           ).start();

       super.setIsLoaded(true);
     }
     catch(java.io.IOException excp)
     {
     }

     */

   }
}
