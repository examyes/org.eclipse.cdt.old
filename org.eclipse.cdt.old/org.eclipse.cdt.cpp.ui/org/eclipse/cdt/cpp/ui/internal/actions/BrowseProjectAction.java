package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.views.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.views.*;
import org.eclipse.cdt.dstore.ui.actions.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.core.internal.resources.*;

import java.util.*;
import java.lang.reflect.*;

import org.eclipse.jface.action.*;
import org.eclipse.ui.internal.*;

public class BrowseProjectAction extends Action 
{
    private DataElement _subject;
    private String      _perspective;

    public BrowseProjectAction(String label, DataElement subject)
    {	
        super(label);
	_subject = subject;
	_perspective = "org.eclipse.cdt.cpp.ui.CppBrowsePerspective";
    }

    public BrowseProjectAction(String label, DataElement subject, String perspective)
    {	
        super(label);
	_subject = subject;
	_perspective = perspective;
    }
    
    public void run() 
    {
	if (_subject != null)
	    {
		openPerspective(_subject);
		
		ModelInterface api = ModelInterface.getInstance();
		api.parse(_subject, false, false);
	    }
    }
    
    private void openPerspective(DataElement input)
    {
        IWorkbench workbench = CppPlugin.getDefault().getWorkbench();
	IWorkbenchPage persp = null;
	IWorkbenchWindow dw = workbench.getActiveWorkbenchWindow();
	try 
	    {
		persp = workbench.showPerspective(_perspective, dw, input);
	    }
        catch (WorkbenchException e)
	    {
	    }		    
    }

}
