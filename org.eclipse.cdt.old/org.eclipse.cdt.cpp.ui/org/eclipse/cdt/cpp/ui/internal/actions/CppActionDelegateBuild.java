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

public class CppActionDelegateBuild extends CppActionDelegate 
{
    private static String TITLE = "Cpp ActionDelegate";
    private static String EXTENSION = "mak";
  
    private QualifiedName  fqnHistory= null;
    
    public void run(IAction action) 
    {
        BuildDialog d= new BuildDialog(_currentResource, "Build History");
	d.open();
	
        if (d.getReturnCode() == d.OK)
	    {	  
		String invocation = d.getInvocation();
		ModelInterface api = CppPlugin.getModelInterface();	
		api.command(_currentResource, invocation, true);
	    }
    }
    

    protected void checkEnabledState(IAction action)
    {
	String fileType = null;
	if (_currentResource != null)
	    {
		IPath newPath= _currentResource.getFullPath();
		
		QualifiedName qNameFileTypeProperty = new QualifiedName("com.ibm.cpp", newPath.toString());
		
		fqnHistory = qNameFileTypeProperty;
		String extension = _currentResource.getFileExtension();
		try
		    {
			fileType = _currentResource.getPersistentProperty(qNameFileTypeProperty);
		    }
		catch (CoreException ce)
		    {
			System.out.println("CppActionDelegateBuild CoreException3 " +fileType +ce);
		    }
		
		if (fileType!=null && fileType.equals("makefile"))
		    {
			((Action)action).setEnabled(true);

			if (_currentResource instanceof IFile)
			    {
				_currentResource = _currentResource.getParent();
			    }
		    }
		else
		    {
			((Action)action).setEnabled(false);
		    }         
	    }
    }    
    
}
