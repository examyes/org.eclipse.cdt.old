package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/AutoSaveRestorableObjects.java, java-model, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:13:39)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

class AutoSaveRestorableObjects extends AutoSave
{
  AutoSaveRestorableObjects(RestorableObjects restorableObjects)
  {
    super(restorableObjects.getSerialization(),
          restorableObjects.getProcess(),
          restorableObjects.getProcess().debugEngine());

    _restorableObjects = restorableObjects;
  }

  // TODO: This event is fired EVERY time the Model updates are complete which
  // is a bit too often for our purposes. An optimization would be to create
  // a new event called 'restorableObjectsChanged' and listen for that
  // event instead. It would only be fired when restorable objects really
  // have been changed.


  public void modelStateChanged(ModelStateChangedEvent event)
  {
    DebuggeeProcess process = _restorableObjects.getProcess();
    DebugEngine engine = process.debugEngine();
    int saveFlags = _restorableObjects.getSaveFlags();

    // Compare what we are saving with what has changed and only save if what
    // we are saving has changed:

    if (SaveRestoreFlags.breakpointsFlagIsSet(saveFlags) &&
        process.breakpointsHaveChanged() ||
        SaveRestoreFlags.programMonitorsFlagIsSet(saveFlags) &&
        process.monitoredExpressionsHaveChanged() ||
        SaveRestoreFlags.storageFlagIsSet(saveFlags) &&
        process.storageMonitorsHaveChanged() ||
        SaveRestoreFlags.defaultDataRepresentationsFlagIsSet(saveFlags) &&
        engine.defaultDataRepresentationsHaveChanged() ||
        SaveRestoreFlags.exceptionFiltersFlagIsSet(saveFlags) &&
        engine.exceptionFiltersHaveChanged())
       super.modelStateChanged(event); // super.model - get it?
  }

  private RestorableObjects _restorableObjects;
}
