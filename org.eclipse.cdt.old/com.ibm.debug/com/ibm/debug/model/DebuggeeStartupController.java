package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/DebuggeeStartupController.java, java-model, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:13:38)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

class DebuggeeStartupController extends DebugEngineEventAdapter
{
  public void processAdded(ProcessAddedEvent event)
  {
    DebuggeeProcess process = event.getProcess();

    DebugEngine engine = process.debugEngine();

    DebuggeeStartupOptions debuggeeStartupOptions =
                           engine.getDebuggeeStartupOptions();

    boolean executeAfterStartup = debuggeeStartupOptions.executeAfterStartup();

    // Remove this event listener unless we need to let the debuggee run:
    // This must be removed BEFORE we start the debuggee up!
    if (!executeAfterStartup)
       engine.removeEventListener(this);

    // See if this process was added due to a "prepare"
    // (as opposed to an "attach"):

    if (debuggeeStartupOptions instanceof DebuggeePrepareOptions)
    {
       // See if we need to run the debuggee to "main":

       if ((((DebuggeePrepareOptions)debuggeeStartupOptions).runToMainEntryPoint() &&
            !executeAfterStartup) ||
           process.isPostMortem()
          )
          try
          {
            process.runToMainEntryPoint(DebugEngine.sendReceiveSynchronously);
          }
          catch (java.io.IOException excp)
          {
            // Not much we can do!
          }
    }

    // See if we need to restore saved objects:

    if (debuggeeStartupOptions.restoreSavedObjects())
       try
       {
         if (Model.TRACE.DBG && Model.traceInfo())
            Model.TRACE.dbg(1, "About to restore saved objects");

         int saveRestoreFlags = debuggeeStartupOptions.getSaveRestoreFlags();

         int temporaryRestoreFlags = saveRestoreFlags;

         // First pass, restore everything except monitored expressions:

         if ((saveRestoreFlags & SaveRestoreFlags.PROGRAM_MONITORS) != 0)
            temporaryRestoreFlags ^= SaveRestoreFlags.PROGRAM_MONITORS;

         if ((saveRestoreFlags & SaveRestoreFlags.LOCAL_MONITORS) != 0)
            temporaryRestoreFlags ^= SaveRestoreFlags.LOCAL_MONITORS;

         process.restoreSavedObjects(process.getSaveRestoreFileName(),
                                     temporaryRestoreFlags,
                                     false);

         // If there are engine-specific restorable objects, restore them:

         RestorableObjects engineSpecificRestorableObjects =
                           DebugEngine.findOrCreateEngineSpecificRestorableObjects(process);

         if (engineSpecificRestorableObjects != null)
         {
            engineSpecificRestorableObjects.setProcess(process);
            engineSpecificRestorableObjects.restore();
         }

         RestorableObjects debuggeeSpecificRestorableObjects = process.getRestorableObjects();

         if (debuggeeSpecificRestorableObjects != null)
         {
            // Restore monitored exprs which didn't get restored in first pass:

            temporaryRestoreFlags =
                      (saveRestoreFlags & SaveRestoreFlags.PROGRAM_MONITORS) |
                      (saveRestoreFlags & SaveRestoreFlags.LOCAL_MONITORS);

            if (temporaryRestoreFlags != 0)
            {
               debuggeeSpecificRestorableObjects.setRestoreFlags(temporaryRestoreFlags);
               debuggeeSpecificRestorableObjects.restore();
            }

            // Finally, set the save/restore flags to what they should be:

            debuggeeSpecificRestorableObjects.setSaveFlags(saveRestoreFlags);
            debuggeeSpecificRestorableObjects.setRestoreFlags(saveRestoreFlags);
         }
       }
       catch (java.io.IOException excp)
       {
         // Not much we can do!
       }
  }

  public void modelStateChanged(ModelStateChangedEvent event)
  {
    DebugEngine engine = event.getDebugEngine();

    if (engine.isAcceptingAsynchronousRequests())
    {
       DebuggeeProcess process = engine.process();

       if (process != null)
          try
          {
            process.run(DebugEngine.sendReceiveDefault);
          }
          catch (java.io.IOException excp)
          {
          }

       engine.removeEventListener(this);
    }
  }
}
