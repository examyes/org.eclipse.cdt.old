package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

public class CVSAction extends CustomAction
{ 
  public CVSAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
      }

    public void run()
    {
	String cmdValue = _command.getAttribute(DE.A_VALUE);
	DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_" + cmdValue);
	if (configureCmd != null)
	    {
		DataElement status = _dataStore.command(configureCmd, _subject);		    
		ModelInterface api = ModelInterface.getInstance();
		api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
	    }
    }

}


