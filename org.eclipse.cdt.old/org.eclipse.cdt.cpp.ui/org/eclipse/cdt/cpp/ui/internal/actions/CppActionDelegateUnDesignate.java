package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.dialogs.*;
import com.ibm.cpp.ui.internal.api.*;

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
public class CppActionDelegateUnDesignate implements IActionDelegate, ISelectionChangedListener {
	private static String TITLE = "Cpp ActionDelegate";
	private static String EXTENSION = "mak";

	Shell localShell;
  ISelection _selection;
  

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
    IResource currentResource= null;
      Iterator e= null;
      int i;
      String fileType="";


      ISelection selection= _selection;
      if ((selection != null) && (selection instanceof IStructuredSelection))
      {
         IStructuredSelection structuredSelection= (IStructuredSelection)selection;
	 currentResource = (IResource)structuredSelection.getFirstElement();
	 if (currentResource != null)
	     {
	 IPath newPath= currentResource.getFullPath();
	 QualifiedName qNameFileTypeProperty = new QualifiedName("com.ibm.cpp", newPath.toString());  // for now null qualifier
	 
	 try
	     {
		 fileType = currentResource.getPersistentProperty(qNameFileTypeProperty);
	     }
	 catch (CoreException ce)
	     {
              System.out.println("CppActionDelegateUnDesignate CoreException3 " +fileType +ce);
	     }
	 
	 if (fileType.equals("makefile"))
	     {
		 try
		     {
			 currentResource.setPersistentProperty(qNameFileTypeProperty, null); // remove server property
		     }
		 catch (CoreException ce)
		     {
			 System.out.println("CppActionDelegateUnDesignate CoreException2 " +ce);
		     }
	     }
	     }
      }
}
    
/**
 * Does nothing - we will let simple rules in the XML
 * config file react to selections.
 *
 * @param action  the action performed
 * @param selection  the new selection
 */
public void selectionChanged(IAction action, ISelection selection) 
{
    IResource currentResource= null;
    Iterator e= null;
    int i;
    String fileType="";
    
    _selection = selection;
    
    if (selection instanceof IStructuredSelection)
	{
	    IStructuredSelection structuredSelection= (IStructuredSelection)selection;
	    currentResource = (IResource)structuredSelection.getFirstElement();
	    if (currentResource != null)
		{
	    IPath newPath= currentResource.getFullPath();
	    QualifiedName qNameFileTypeProperty = new QualifiedName("com.ibm.cpp", newPath.toString());  // for now null qualifier
	    
	    String extension = currentResource.getFileExtension();
	    try
		{
		    fileType = currentResource.getPersistentProperty(qNameFileTypeProperty);
		}
	    catch (CoreException ce)
		{
		    System.out.println("CppActionDelegateUnDesignate CoreException3 " +fileType +ce);
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

public void selectionChanged(SelectionChangedEvent selection) {
}
}
