package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.views.*;
import com.ibm.cpp.ui.internal.vcm.*;

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.resource.*;

import com.ibm.dstore.core.model.*;

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
    private CppPlugin   _plugin = CppPlugin.getDefault();

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
      com.ibm.cpp.ui.internal.api.ModelInterface api = _plugin.getModelInterface();
      return api.getNewFile(fileName);
  }

    public void addNewFile(IFile file)
    {
	com.ibm.cpp.ui.internal.api.ModelInterface api = _plugin.getModelInterface();
	api.addNewFile(file);
    }

  public void performGoto(boolean openEditor)
      {
        if (_element != null)
        {
	    DataStore dataStore = _element.getDataStore();
	    if (_element.getParent() != dataStore.getDescriptorRoot())
		{
		    String fileName   = (String)(_element.getElementProperty(DE.P_SOURCE_NAME));
		    fileName = fileName.replace('\\', '/');
		    
		    String elementType = (String)(_element.getElementProperty(DE.P_TYPE));
		    
		    if ((fileName != null) && (fileName.length() > 0))
			{
			    java.io.File realFile = new java.io.File(fileName);
			    if ( !realFile.isDirectory())
				{		
				    IFile file = getNewFile(fileName);
				    if (file == null && openEditor)
					{
					    DataElement fileElement = null;	
					    if (elementType.equals("file"))
						{
						    fileElement = _element;			
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
							    dataStore.setObject(fileElement);
							}	
						    else
							{
							    return;
							}
						}
					    
					    file = new com.ibm.dstore.ui.resource.FileResourceElement(fileElement, 
												      _plugin.getCurrentProject());
					    addNewFile(file);
					}
				    
				    if (file != null)
					{	
					    if (_plugin != null)
						{
						    IWorkbench desktop = _plugin.getWorkbench();
						    IWorkbenchPage persp= desktop.getActiveWorkbenchWindow().getActivePage();
						    
						    IEditorPart editor = null;
						    
						    IEditorPart [] editors = persp.getEditors();
						    for (int i = 0; i < editors.length; i++)
							{
							    IFileEditorInput input = (IFileEditorInput)editors[i].getEditorInput();
							    IFile openFile = input.getFile();
							    if ((input != null) && 
								openFile.getLocation().toString().equals(file.getLocation().toString()))
								{
								    editor = editors[i];		
								    persp.bringToTop(editor);		
								    break;
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
							    if (editor instanceof com.ibm.cpp.ui.internal.editor.CppEditor)
								{
								    ((com.ibm.cpp.ui.internal.editor.CppEditor)editor).gotoLine(line);
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
					}
				}	
			}
		}	
	}
      }
}
