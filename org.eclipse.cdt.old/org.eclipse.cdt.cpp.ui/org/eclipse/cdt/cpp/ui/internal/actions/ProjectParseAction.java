package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import java.util.*;

public class ProjectParseAction extends ProjectAction
{ 
 DataElement _parseCommand;
 
 public ProjectParseAction(DataElement subject, String label, DataElement command, DataStore dataStore)
 {	
  super(subject, label, command, dataStore);
  _parseCommand = _dataStore.createCommandDescriptor(null, "C_PARSE", "com.ibm.cpp.miners.parser.ParseMiner", "C_PARSE");

 }

 public void run()
 {
  ArrayList args = new ArrayList();
  args.add(_project);
  DataElement status = _dataStore.command(_parseCommand, args, _subject, false);
  _api.monitorStatus(status);
  //_api.showView("com.ibm.cpp.ui.internal.views.ParsedSourceViewPart", null);
  //_api.showView("com.ibm.cpp.ui.internal.views.DetailsViewPart", null);
 }
}


