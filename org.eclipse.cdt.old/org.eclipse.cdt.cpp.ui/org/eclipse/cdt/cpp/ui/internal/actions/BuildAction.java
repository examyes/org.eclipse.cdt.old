package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
		CppBuilder.getInstance().doBuild(project);
	    }
    }

}


