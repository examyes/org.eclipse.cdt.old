package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.dialogs.*;
import org.eclipse.cdt.cpp.ui.internal.views.*;

import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.cdt.dstore.hosts.actions.*;

import java.io.*; 
import java.util.*;

import org.eclipse.cdt.cpp.ui.internal.vcm.*;
import org.eclipse.jface.action.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.dialogs.*;

import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import java.lang.reflect.InvocationTargetException;

public class OpenFileAction extends CustomAction
{ 
  public OpenFileAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);

		if (subject.getType().equals("file"))
	    {
			setEnabled(true);
	    }
		else
	    {
			setEnabled(false);
	    }
      }
      
    public OpenFileAction(java.util.List subjects, String label, DataElement command, DataStore dataStore)
    {
    	super(subjects, label, command, dataStore);
    	
    	for (int i = 0; i < subjects.size(); i++)
    	{
    		DataElement subject = (DataElement)subjects.get(i);
    		String type = subject.getType();
    		if (!type.equals("file"))
    		{
    			setEnabled(false);
    			return;
    		}
    	}
    }  

    public void run()
    { 
		IActionLoader loader = CppActionLoader.getInstance();
		IOpenAction openAction = loader.getOpenAction();
		
		for (int i = 0; i < _subjects.size(); i++)
		{
			DataElement subject = (DataElement)_subjects.get(i);
			openAction.setSelected(subject);
			openAction.run();
		}
    }
}
