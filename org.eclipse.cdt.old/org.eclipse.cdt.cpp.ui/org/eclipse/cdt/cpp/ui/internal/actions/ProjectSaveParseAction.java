package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
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
