package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.views.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;
import org.eclipse.cdt.cpp.ui.internal.editor.*;

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.resource.*;
import org.eclipse.cdt.dstore.core.model.*;

import com.ibm.lpex.alef.*;
import com.ibm.lpex.core.*;

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
    private static DataElement _element;

    private static IFile         _previousFile = null;
    private static IFile         _file = null;

    private static int         _previousLine = 0;
    private static int         _line = 0;

    private IEditorPart _previousEditor;
    private IEditorPart _editor;

    private CppPlugin   _plugin = CppPlugin.getDefault();
    private ModelInterface   _api;

  public OpenEditorAction(DataElement element)
      {
        super("Open Action");
        _element = element;
	_api = ModelInterface.getInstance();
      }

    public void resetSelection()
    {
	if ((_previousFile != null) && (_previousEditor != null))
	    {
		IFile prevFile  = _file; 
		_file = _previousFile;	
		_previousFile = prevFile;
		
		int prevLine  = _line;  
		_line = _previousLine;
		_previousLine = prevLine;
		
		IEditorPart prevEditor = _editor;
		_editor = _previousEditor;
		_previousEditor = prevEditor;
		
		showEditor(_editor);
		gotoLocation(_editor, _file, _line);
	    }
    }

    public void setLocation(String str, int line)
    {
    }

    public void setSelected(DataElement selected)
    {	
	if (_editor != null)
	    {
		_previousEditor = _editor;
		_previousLine = _line;
		_previousFile = _file;
	    }

	_element = selected;
    }

  public void run()
  {
    performGoto(true);
  }

  public IFile findFile(String fileName)
  {
      if (_file != null)
	  {
	      IPath location = _file.getLocation();
	      if (location != null)
		  {
		      if (_api.compareFileNames(location.toString(), fileName))
			  {
			      return _file;
			  }
		  }
	  }

      IResource resource =  _api.findFile(fileName);
      if (resource instanceof IFile)
	  {
	      return (IFile)resource;
	  }
      else
	  {
	      return null;
	  }
  }

    public void addNewFile(IFile file)
    {
	_api.addNewFile(file);
    }

    public DataElement getResourceFor(DataElement element)
    {
		DataElement des = element.getDescriptor();
		if (des != null && des.isOfType("file"))
		{		
			if (!element.getType().equals("file"))
	    	{
				return null;
	    	}  
			else
			{
				return element;
			}
		}

	String fileName   = (String)(element.getElementProperty(DE.P_SOURCE_NAME));
	DataElement resource = _api.findResourceElement(element.getDataStore(), fileName);

	if (resource == null)
	    {
		return element;
	    }
	else
	    {
		return resource;
	    }
    }
    
    public void performGoto(boolean openEditor)
      {
        if (_element != null)
        {
	    DataStore dataStore = _element.getDataStore();

	    if (_element.getParent() != dataStore.getDescriptorRoot())
		{
		    String src = _element.getSource();
		    if (src == null || src.length() == 0)
			{
			    return;
			}

		    DataElement resourceElement = getResourceFor(_element);

		    
		    if (resourceElement == null)
			{
			    return;
			}

		    DataElement projectElement = _api.getProjectFor(resourceElement);
		    
		    IProject project = null;
		    if (projectElement != null)
			{
			    project = _api.findProjectResource(projectElement);
			}
			if (project == null)
			{
			    project = _plugin.getCurrentProject();		
			} 
			


		    String fileName   = (String)(resourceElement.getElementProperty(DE.P_SOURCE_NAME));
		    fileName = fileName.replace('\\', '/');

		    
		    String elementType = resourceElement.getType();
		    
		    if ((fileName != null) && (fileName.length() > 0))
			{
			    java.io.File realFile = new java.io.File(fileName);
			    if (!realFile.exists() || !realFile.isDirectory())
				{		
				    IFile file = findFile(fileName);
				    
				    if (file == null && openEditor)
					{
					    DataElement fileElement = null;	
					    if (resourceElement.isOfType("file"))
						{
						    fileElement = resourceElement;			
						}
					    else
						{
						    String name = fileName;
						    
						    int indexOfSlash = fileName.lastIndexOf("/");
						    if (indexOfSlash > 0)
							{
							    name = fileName.substring(indexOfSlash + 1, fileName.length());		      		
							    fileElement = dataStore.createObject(null, "file", name, fileName);
							    // create the object on server
							    //DataElement temp = dataStore.getTempRoot();
							    //dataStore.setObject(temp);
							}	
						    else
							{
							    return;
							}
						}
					    
					    java.io.File theFile = fileElement.getFileObject(true);
					    if (!theFile.exists())
					    {
					    	return;	
					    }
					    
					    file = new FileResourceElement(fileElement, project);
					   
					    addNewFile(file);
					}
				    

				    if (file != null && file.exists())
					{	
					    _file = file;

					    if (_plugin != null)
						{
						    
						    IWorkbench desktop = _plugin.getWorkbench();
						    IWorkbenchPage persp= desktop.getActiveWorkbenchWindow().getActivePage();
						    IEditorPart editor = null;
						    
						    IEditorPart [] editors = persp.getEditors();
						    for (int i = 0; i < editors.length; i++)
							{
							    IEditorInput eInput = editors[i].getEditorInput();
							    if (eInput instanceof IFileEditorInput)
								{
								    IFileEditorInput input = (IFileEditorInput)eInput;
								    IFile openFile = input.getFile();
								    if ((input != null) && 
									openFile.getLocation().toString().equals(file.getLocation().toString()))
									{
									    editor = editors[i];
									    _editor = editor;
		
									    break;
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
							    _editor = editor;
							}
						    
						    showEditor(editor);
						    
						    Integer lineLocation = (Integer)(_element.getElementProperty(DE.P_SOURCE_LOCATION));
						    int line = lineLocation.intValue();	
						    if ((line > 0) && (editor != null))
							{	
							    gotoLocation(editor, file, line);
							    _line = line;
							}
						}
					}
				}	
			}
		}	
	}
      }


    protected void showEditor(IEditorPart editor)
    { 
	IWorkbench desktop = _plugin.getWorkbench();
	IWorkbenchPage persp= desktop.getActiveWorkbenchWindow().getActivePage();
	persp.bringToTop(editor);		
    }

    protected void gotoLocation(IEditorPart editor, IFile file, int line)
    {
	if (editor instanceof org.eclipse.cdt.cpp.ui.internal.editor.CppEditor)
	    {
		((org.eclipse.cdt.cpp.ui.internal.editor.CppEditor)editor).gotoLine(line);
	    }
	else
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
