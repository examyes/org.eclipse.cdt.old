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

public class ReplicateToAction extends CustomAction
{ 
  public ReplicateToAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
      }

    public void run()
    {
	DataElement sourceProject = _subject;
	ModelInterface api = ModelInterface.getInstance();
	
	ChooseProjectDialog dlg = new ChooseProjectDialog("Choose a Project To Replicate To", 
							  api.findWorkspaceElement());
	
	dlg.open();
	
	if (dlg.getReturnCode() == dlg.OK)
	    {
		List selection = dlg.getSelected();
		for (int i = 0; i < selection.size(); i++)
		    {
			DataElement targetProject = ((DataElement)selection.get(i)).dereference();

			if (targetProject != null && sourceProject != targetProject)
			    {
				// do transfer files
				for (int j = 0; j < sourceProject.getNestedSize(); j++)
				    {
					DataElement source = sourceProject.get(j);
					if (!source.isReference())
					    {
						TransferFiles transferAction = new TransferFiles("transfer", source, 
												 targetProject, null);
						transferAction.start();
					    }
				    }
			    }
		    }
	    }
    }
}


