package com.ibm.dstore.ui.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.resource.*;
import com.ibm.dstore.core.model.*;

/***/
import org.eclipse.ui.internal.*;
import org.eclipse.core.internal.resources.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.dialogs.*;

import org.eclipse.jface.action.*; 
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import java.util.*;

import org.eclipse.ui.texteditor.*;

public class OpenEditorAction extends Action implements IOpenAction
{
  private DataElement _element;
  public OpenEditorAction(DataElement element)
      {
        super("Open Action");
        _element = element;
      }
  
  public void setSelected(DataElement selected)
  {
    _element = selected;
  }  

  public void run()
  {
    performGoto(true); 
  }
   
  public IFile getNewFile(String fileName)
  {
    IFile file = null;
    
    if (fileName != null) 
      {	  
	  Workspace ws = (Workspace)WorkbenchPlugin.getPluginWorkspace();
	  IWorkspaceRoot root = ws.getRoot();

	  file = findFile(root, fileName);
      }
    
    return file;    
  }
    
    public IFile findFile(IWorkspaceRoot root, String fileName)
    {
	IProject projects[] = root.getProjects();
	for (int i = 0; i < projects.length; i++)
	    {
		IFile result = findFile(projects[i], fileName);
		if (result != null)
		    return result;
	    }
	
	return null;
    }

    public IFile findFile(IContainer root, String fileName)
    {
	try
	    {
		IResource resources[] = root.members();
		for (int i = 0; i < resources.length; i++)
		    {
			IResource resource = resources[i];
			String path = resource.getLocation().toOSString();
			
			if (path.equalsIgnoreCase(fileName))
			    {
				return (IFile)resource;
			    } 
			
			if (fileName.startsWith(path) && resource instanceof IContainer)
			    {
				IFile result = findFile((IContainer)resource, fileName);
				if (result != null)
				    return result;
			    }
		    }
	    }
	catch (CoreException e)
	    {
	    }
	
	return null;
    }

  public void performGoto(boolean openEditor)
      {
        if (_element != null) 
        {
	    DataStore dataStore = _element.getDataStore();

	    if (_element.getParent() != dataStore.getDescriptorRoot())
		{
		    String fileName   = (String)(_element.getElementProperty(DE.P_SOURCE_NAME));
		    String elementType = (String)(_element.getElementProperty(DE.P_TYPE));

		    if ((fileName != null) && (fileName.length() > 0))
			{
			    java.io.File realFile = new java.io.File(fileName);
			    if ( !realFile.isDirectory())
				{		
				    IFile file = getNewFile(fileName);
				    
				    if (file == null)
					{
					    DataElement fileElement = null;	
					    if (elementType.equals("file"))
						{
						    fileElement = _element;			
						}
					    else
						{
						    String name = fileName;
						    
						    int indexOfSlash1 = fileName.lastIndexOf('/');
						    int indexOfSlash2 = fileName.lastIndexOf('\\');
						    int indexOfSlash  = (indexOfSlash1 > indexOfSlash2) ? indexOfSlash1 : indexOfSlash2; 
						    if (indexOfSlash > 0)
							{
							    name = fileName.substring(indexOfSlash + 1, fileName.length());		      		
							    fileElement = dataStore.createObject(null, "file", name, fileName);
							    // create the object on server
							    dataStore.setObject(fileElement);
							}	
						    else
							{
							    return;
							}
						}
					    
					    file = new com.ibm.dstore.ui.resource.FileResourceElement(fileElement, null);
					}
				    		    
				    if (file != null)
					{	  
					    if (WorkbenchPlugin.getDefault() != null)
						{
						    IWorkbench desktop = WorkbenchPlugin.getDefault().getWorkbench();
						    IWorkbenchPage persp= desktop.getActiveWorkbenchWindow().getActivePage();
				    
						    IEditorPart editor = null;
						    
						    IEditorPart [] editors = persp.getEditors();
						    for (int i = 0; i < editors.length; i++)
							{
							    IEditorInput einput = editors[i].getEditorInput();
							    if (einput instanceof IFileEditorInput)
								{
								    IFileEditorInput input = (IFileEditorInput)einput;
								    if ((input != null) 
									&& input.getName().equals(file.getName()))
									{
									    editor = editors[i];		      
									    persp.bringToTop(editor);		      
									}
								}
							}
						    
						    if (editor == null)
							{
							    if (!openEditor)
								{
								    return;		      
								}
							    
							    try
								{
								    persp.openEditor(file);
								}
							    catch (org.eclipse.ui.PartInitException e)
								{
								}
							    
							    editor = persp.getActiveEditor();
							}
						    
						    Integer lineLocation = (Integer)(_element.getElementProperty(DE.P_SOURCE_LOCATION));
						    int line = lineLocation.intValue();	

						    if ((line > 0) && (editor != null))
							{	
							    try
								{
								    IMarker marker = file.createMarker(IMarker.TEXT);
								    marker.setAttribute(IMarker.LINE_NUMBER, line);
								    marker.setAttribute(IMarker.CHAR_START, -1);
								    marker.setAttribute(IMarker.CHAR_END, -1);
								    
								    editor.gotoMarker(marker);
								}
							    catch (CoreException e)
								{
								}
							}
						}
					}
				}	
			}
		}	
	}
      }
}
