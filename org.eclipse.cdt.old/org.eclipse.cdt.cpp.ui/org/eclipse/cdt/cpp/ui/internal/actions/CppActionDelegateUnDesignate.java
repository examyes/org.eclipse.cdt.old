package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.dialogs.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import java.io.*;
import java.util.*;

import org.eclipse.jface.viewers.*;

import org.eclipse.jface.action.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.ui.*;

/**
 * This class shows how IActionDelegate implementations
 * should be used for global action registration for menu
 * and tool bars. Action proxy object is created in the
 * desktop based on presentation information in the plugin.xml
 * file. Delegate is not loaded until the first time the user
 * presses the button or selects the menu. Based on the action
 * availability, it is possible that the button will disable
 * instead of executing.
 */
public class CppActionDelegateUnDesignate extends CppActionDelegate 
{
    private static String TITLE = "Cpp ActionDelegate";
    private static String EXTENSION = "mak";
    
    
    /**
     * Checks the current selection and runs the separate browser
     * to show the content of the Cpp file. This code shows how
     * to launch separate browsers that are not VA/Base desktop parts.
     *
     * @param action  the action that was performed
     * @param shell  the shell in which the action was performed
     */
    
    public void run(IAction action) 
    {
	String fileType="";
	
	if (_currentResource != null)
	    {
		IPath newPath= _currentResource.getFullPath();

		// for now null qualifier
		QualifiedName qNameFileTypeProperty = new QualifiedName("com.ibm.cpp", newPath.toString()); 
		try
		    {
			fileType = _currentResource.getPersistentProperty(qNameFileTypeProperty);
		    }
		catch (CoreException ce)
		    {
			System.out.println("CppActionDelegateUnDesignate CoreException3 " +fileType +ce);
		    }
		
		if (fileType.equals("makefile"))
		    {
			try
			    {
				// remove server property
				_currentResource.setPersistentProperty(qNameFileTypeProperty, null); 
			    }
			catch (CoreException ce)
			    {
				System.out.println("CppActionDelegateUnDesignate CoreException2 " +ce);
			    }
		    }
	    }
    }

    protected void checkEnabledState(IAction action)
    {
	String fileType = null;
	if (_currentResource != null)
	    {
		IPath newPath= _currentResource.getFullPath();
		
		// for now null qualifier
		QualifiedName qNameFileTypeProperty = new QualifiedName("com.ibm.cpp", newPath.toString());
		
		String extension = _currentResource.getFileExtension();
		try
		    {
			fileType = _currentResource.getPersistentProperty(qNameFileTypeProperty);
		    }
		catch (CoreException ce)
		    {
			System.out.println("CppActionDelegateDesignate CoreException3 " +fileType +ce);
		    }
		
		
		if (fileType!=null && fileType.equals("makefile"))
		    {
			((Action)action).setEnabled(true);
		    }
		else
		    {
			((Action)action).setEnabled(false);
		    }
	    }
    }
    
}
