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

public class CppActionDelegateBuild implements IActionDelegate, ISelectionChangedListener 
{
  private static String TITLE = "Cpp ActionDelegate";
  private static String EXTENSION = "mak";
  
  private QualifiedName  fqnHistory= null;
  private IResource      fCurrentResource= null;

  private String _path;

  public void run(IAction action) 
      {
        BuildDialog d= new BuildDialog(fCurrentResource, "Build History");
	d.open();

        if (d.getReturnCode() == d.OK)
        {	  
	  String invocation = d.getInvocation();
	  ModelInterface api = CppPlugin.getModelInterface();	
	  api.command(_path, invocation);
	}
      }


  public void selectionChanged(IAction action, ISelection selection) 
      {
        boolean state= !selection.isEmpty();
        Iterator e= null;
        int i;
        String fileType="";
        
        if (selection instanceof IStructuredSelection)
        {
          IStructuredSelection structuredSelection= (IStructuredSelection)selection;
	  fCurrentResource = (IResource)structuredSelection.getFirstElement();
	  if (fCurrentResource != null)
	      {
	  IPath newPath= fCurrentResource.getFullPath();
            
	  if (fCurrentResource instanceof IFolder)
	      {
		  _path = new String(fCurrentResource.getLocation().toOSString());    
	      }
	  else if (fCurrentResource instanceof IFile)
	      {		      
		  IPath location = fCurrentResource.getParent().getLocation();
		  if (location != null)
		      {		  
			  _path = new String(location.toOSString());
		      }	      
	      }		 
	  
	  QualifiedName qNameFileTypeProperty = new QualifiedName("com.ibm.cpp", newPath.toString());
	  
	  fqnHistory = qNameFileTypeProperty;
	  String extension = fCurrentResource.getFileExtension();
	  try
	      {
		  fileType = fCurrentResource.getPersistentProperty(qNameFileTypeProperty);
	      }
	  catch (CoreException ce)
	      {
		  System.out.println("CppActionDelegateBuild CoreException3 " +fileType +ce);
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
