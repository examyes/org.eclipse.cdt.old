package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.dialogs.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;

import org.eclipse.ui.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;


import java.io.*;
import java.util.*;

public abstract class CppActionDelegate implements IActionDelegate, ISelectionChangedListener 
{
    protected IResource      _currentResource= null;
    
    public abstract void run(IAction action);

    public void selectionChanged(IAction action, ISelection selection) 
    {
        boolean state= !selection.isEmpty();
        Iterator e= null;
        int i;
        String fileType="";
        
        if (selection instanceof IStructuredSelection)
	    {
		IStructuredSelection structuredSelection= (IStructuredSelection)selection;
		Object selected = structuredSelection.getFirstElement();

		_currentResource = getResourceFor(selected);
		checkEnabledState(action);
	    }
    }
    
    public void selectionChanged(SelectionChangedEvent selection) 
    {
    }

    protected IResource getResourceFor(Object object)
    {
	if (object instanceof IResource)
	    {
		_currentResource = (IResource)object;
	    }
	else if (object instanceof IAdaptable)
	    {
		_currentResource = (IResource)((IAdaptable)object).getAdapter(IResource.class);
	    }
	else
	    {
		_currentResource = null;
	    }

	return _currentResource;
    }

    protected abstract void checkEnabledState(IAction action);
}
