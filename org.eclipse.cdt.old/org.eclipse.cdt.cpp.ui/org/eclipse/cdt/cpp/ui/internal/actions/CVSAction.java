package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.model.*;

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
		api.showView("com.ibm.cpp.ui.CppOutputViewPart", status);
	    }
    }

}


