package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.builder.*;

import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

public class BuildAction extends CustomAction
{ 
  public BuildAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
      }

 public BuildAction(java.util.List subjects, String label, DataElement command, DataStore dataStore)
      {	
        super(subjects, label, command, dataStore);
      }


    public void run()
    {
		ModelInterface api = ModelInterface.getInstance();
	
		for (int i = 0; i < _subjects.size(); i++)
		{
			DataElement subject = (DataElement)_subjects.get(i);
			IProject project = api.findProjectResource(subject);
			if (project != null)
	    	{
			if (_command.getValue().equals("BUILD"))
		    {
				CppBuilder.getInstance().doBuild(project);
		    }
			else
		    {
				CppBuilder.getInstance().doBuild(project, false);
		    }
	    	}
	    }
	    
    }

}


