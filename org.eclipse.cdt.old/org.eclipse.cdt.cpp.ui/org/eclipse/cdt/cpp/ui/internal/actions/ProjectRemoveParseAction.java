package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
  _api.showView("com.ibm.cpp.ui.internal.views.ParsedSourceViewPart", null);
  _api.showView("com.ibm.cpp.ui.internal.views.DetailsViewPart", null);
 }
}
