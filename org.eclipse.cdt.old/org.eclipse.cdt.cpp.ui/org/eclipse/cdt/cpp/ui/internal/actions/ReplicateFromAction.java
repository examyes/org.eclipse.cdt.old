package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.dialogs.*;

import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.model.*;

import com.ibm.dstore.hosts.actions.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

public class ReplicateFromAction extends CustomAction
{ 
  public ReplicateFromAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
      }

    public void run()
    {
	DataElement target = _subject;
	DataElement sourceProject = null;
	ModelInterface api = ModelInterface.getInstance();
	
	ChooseProjectDialog dlg = new ChooseProjectDialog("Choose a Project To Replicate From", api.findWorkspaceElement());

	dlg.open();
	
	if (dlg.getReturnCode() == dlg.OK)
	    {
		sourceProject = dlg.getSelected();
	    }

	if (sourceProject != null && sourceProject != target)
	    {
		// do transfer files
		for (int i = 0; i < sourceProject.getNestedSize(); i++)
		    {
			DataElement source = sourceProject.get(i);
			if (!source.isReference())
			    {
				TransferFiles transferAction = new TransferFiles("transfer", source, target, null);
				transferAction.start();
			    }
		    }
	    }
    }
}


