package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.vcm.*;

import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.ui.resource.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.ui.connections.*;

import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.action.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import java.io.*;
import java.util.*;
import java.lang.*;

public class SynchronizeFiles implements IActionDelegate
{
    protected ISelection _selection;
    protected ArrayList  _projects = new ArrayList();

    public void run(IAction action)
    {
	_projects.clear();
	if (_selection != null && (_selection instanceof IStructuredSelection))
	    {
		IStructuredSelection structuredSelection= (IStructuredSelection)_selection;
		java.util.List list = structuredSelection.toList();
		for (int i = 0; i < list.size(); i++)
		    {
			if (list.get(i) instanceof Repository)
			    {
				_projects.add(list.get(i));
			  }
		    }
	    }	
    }
    

    public void selectionChanged(IAction action, ISelection selection)
    {
	boolean state= !selection.isEmpty();
	_selection = selection;
    }
}
