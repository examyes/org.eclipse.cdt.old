package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import java.util.*;

public class ProjectRemoveParseAction extends ProjectAction
{ 
 DataElement _removeCommand;
 
 public ProjectRemoveParseAction(DataElement subject, String label, DataElement command, DataStore dataStore)
 {	
  super(subject, label, command, dataStore);
  _removeCommand = _dataStore.localDescriptorQuery(subject.getDescriptor(), "C_REMOVE_PARSE");

 }

 public void run()
 {
  DataElement status = _dataStore.command(_removeCommand, _subject);
  _api.monitorStatus(status);
 }
}
