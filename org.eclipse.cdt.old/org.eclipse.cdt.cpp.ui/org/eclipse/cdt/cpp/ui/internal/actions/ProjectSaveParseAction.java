package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import java.util.*;

public class ProjectSaveParseAction extends ProjectAction
{ 
 DataElement _saveCommand;
 
 public ProjectSaveParseAction(DataElement subject, String label, DataElement command, DataStore dataStore)
 {	
  super(subject, label, command, dataStore);
  _saveCommand = _dataStore.localDescriptorQuery(subject.getDescriptor(), "C_SAVE_PARSE");
 }

 public void run()
 {
     DataElement status = _dataStore.command(_saveCommand, _subject);
     _api.monitorStatus(status);
 }
}
