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

public class SynchronizeWithAction extends CustomAction
{ 
    public SynchronizeWithAction(DataElement subject, String label, DataElement command, DataStore dataStore)
    {	
        super(subject, label, command, dataStore);
    }
    
    public void run()
    {
	DataElement project1 = _subject;
	DataElement project2 = null;
	ModelInterface api = ModelInterface.getInstance();
	
	ChooseProjectDialog dlg = new ChooseProjectDialog("Choose a Project To Synchronize With", 
							  api.findWorkspaceElement());
	dlg.open();
	
	if (dlg.getReturnCode() == dlg.OK)
	    {
		project2 = dlg.getSelected();
	    }
	
	if (project2 != null && project1 != project2)
	    {
		// transfer from project1 to project2
		for (int i = 0; i < project1.getNestedSize(); i++)
		    {
			DataElement source = project1.get(i);
			if (!source.isReference())
			    {
				TransferFiles transferAction = new TransferFiles("transfer", source, project2, null);
				transferAction.start();
			    }
		    }

		// transfer from project2 to project1
		for (int i = 0; i < project2.getNestedSize(); i++)
		    {
			DataElement source = project2.get(i);
			if (!source.isReference())
			    {
				TransferFiles transferAction = new TransferFiles("transfer", source, project1, null);
				transferAction.start();
			    }
		    }

	    }
    }
}


