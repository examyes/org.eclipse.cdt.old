package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.builder.*;

import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.model.*;

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

    public void run()
    {
	ModelInterface api = ModelInterface.getInstance();
	IProject project = api.findProjectResource(_subject);
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


