package org.eclipse.cdt.cpp.ui.internal.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.jface.action.*;
import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.*;


public class CppProjectsViewPart extends ObjectsViewPart implements ISelectionListener, ICppProjectListener
{ 
	
	private IPartListener _partListener = new IPartListener() 
	{
		public void partActivated(IWorkbenchPart part) 
		{
			if (part instanceof IEditorPart)
				editorActivated((IEditorPart) part);
		}
		public void partBroughtToTop(IWorkbenchPart part) {
		}
		public void partClosed(IWorkbenchPart part) {
		}
		public void partDeactivated(IWorkbenchPart part) {
		}
		public void partOpened(IWorkbenchPart part) {
		}
	};
	
    private boolean _inputed = false;

    public CppProjectsViewPart()
    {
	super();
    }
    
    public void createPartControl(Composite parent)
    {
	super.createPartControl(parent);
    }
    
    protected String getF1HelpId()
    {
	return "org.eclipse.cdt.cpp.ui.cpp_projects_view_context";
    }
    
    protected void editorActivated(IEditorPart editor)
    {
    	IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput) 
		{
			IFileEditorInput fileInput = (IFileEditorInput) input;
			IFile file = fileInput.getFile();
			DataElement fileElement = _api.findResourceElement(file);
			if (fileElement != null)
			{
				ISelection newSelection = new StructuredSelection(fileElement);
				if (_viewer != null)
				{
					TreeViewer treeViewer = (TreeViewer)_viewer.getViewer();
					if (!treeViewer.getSelection().equals(newSelection)) 
					{
						treeViewer.setSelection(newSelection);
					}
				}
			}
		}	
    }
    
    public void initInput(DataStore dataStore)
    {
	if (!_inputed)
	    {
		
		setTitle("C/C++ Projects");
		dataStore = _plugin.getDataStore();
		DataElement projectMinerData = dataStore.findMinerInformation("org.eclipse.cdt.cpp.miners.project.ProjectMiner");
		if (projectMinerData != null)
		    {
			DataElement rootElement = null;
			IAdaptable input = getSite().getPage().getInput();

			if (input instanceof IWorkspaceRoot)
			    {
				rootElement = _api.findWorkspaceElement(dataStore);
			    }
			else if (input instanceof IResource)
			    {
				IResource resource = (IResource)input;
				rootElement = _api.findResourceElement(resource);
			    }
			else if (input instanceof DataElement)
			    {
				rootElement = (DataElement)input;
			    }
			
			if (rootElement != null)
			    {
				_viewer.setInput(rootElement);
				_viewer.setSorter(DE.P_NAME);
				lock(true);
				_inputed = true;
				getSite().getPage().addPartListener(_partListener);
			    }
			
			return;
		    } 
		_viewer.setInput(null);
	    }
    }
    



    protected void internalSelectionChanged(IWorkbenchPart part, ISelection sel) 
    {
	if (part == this && (sel instanceof IStructuredSelection))
	    {

		IStructuredSelection ssel = (IStructuredSelection)sel;
		Object object = ssel.getFirstElement();
		if (object instanceof DataElement)
		    {
			DataElement theObject = (DataElement)object;
			
			
			DataElement theParent = _api.getProjectFor(theObject);
			
			if (theParent != null)
			    {
				IProject project = _api.findProjectResource(theParent);
				if (project != null)
				    {
					DataStore dataStore = theParent.getDataStore();
					_plugin.setCurrentProject(project);
					_plugin.setCurrentDataStore(dataStore);
				    }
			    }			
		    }
		
	    }
    }


    public void projectChanged(CppProjectEvent event)
    {
	int type = event.getType();
	IProject project = event.getProject();
	switch (type)
	    {
	    case CppProjectEvent.OPEN:
		{
		    _viewer.resetView(false);
		}
		break;
	    case CppProjectEvent.CLOSE:
	    case CppProjectEvent.DELETE:
		{
		    _viewer.resetView(false);
		}
		break;
		
	    case CppProjectEvent.COMMAND:
		break;
	    default:
		super.projectChanged(event);
		break;
	    }
    }

}










