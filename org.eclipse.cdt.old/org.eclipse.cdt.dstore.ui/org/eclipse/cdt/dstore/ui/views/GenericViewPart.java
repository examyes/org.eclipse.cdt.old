package org.eclipse.cdt.dstore.ui.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.actions.*;

import org.eclipse.cdt.dstore.core.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*; 
import org.eclipse.cdt.dstore.ui.resource.*;

import org.eclipse.ui.*;
import org.eclipse.ui.part.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.jface.action.*; 
import org.eclipse.jface.resource.ImageRegistry;


import org.eclipse.core.runtime.*; 
import org.eclipse.core.resources.*;

import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;

import java.util.*;
import java.lang.reflect.*;
 

public class GenericViewPart extends ViewPart 
    implements ILinkable, ISelectionListener
{
 
    protected ObjectWindow       _viewer;
    protected   boolean          _isLinked;
    protected   IOpenAction      _openAction;

    protected   IActionLoader    _loader;

    public GenericViewPart()
    {
	super();

    }
    
    public ISelection getSelection() 
    {
	return _viewer.getViewer().getSelection();
    }
    
    protected boolean isImportant(IWorkbenchPart part) 
    {
	return true;  
    }
 
    public void setSelection(ISelection selection) 
    {
	_viewer.getViewer().setSelection(selection);
    }
        
    public void createPartControl(Composite parent)
    {
	_viewer = createViewer(parent, getActionLoader());       
	
	getSite().setSelectionProvider((ISelectionProvider)_viewer.getViewer());
	_viewer.getViewer().addSelectionChangedListener(new ISelectionChangedListener()
	    {
		public void selectionChanged(SelectionChangedEvent event)
		{
		    handleSelectionChanged(event);
		}
	    });
	  
	ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
	selectionService.addSelectionListener(this);

	if (selectionService.getSelection() instanceof IStructuredSelection)
	    {
		handleSelection((IStructuredSelection)selectionService.getSelection());	
	    }

	initInput(null);
	fillLocalToolBar();
    }
    
    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	return new ObjectWindow(parent, ObjectWindow.TREE, null, new ImageRegistry(), loader);
    }
     
    public IActionLoader getActionLoader()
    {
    	if (_loader == null)
    	{
    		_loader = new GenericActionLoader();
    	}

		return _loader;
    }

    public void setActionLoader(IActionLoader loader)
    {
    	_loader = loader;
		if (_viewer != null)
	    {
			_viewer.setActionLoader(loader);
	    }
    }

    public void initInput(DataStore dataStore)
    {
	_viewer.setInput((getSite().getPage().getInput()));      

    } 
    
    public Shell getShell()
    {
	return _viewer.getShell();
    }
    
    private void handleSelection(IStructuredSelection es)
    {
    }

    public void selectionChanged(IWorkbenchPart part, ISelection sel) 
    {
    }
    
    public void dispose()
    {
        setLinked(false);
	if (_viewer != null)
	    _viewer.dispose();
        super.dispose();
    }

    public void setViewDescription(String description)
    {
	setTitle(description);
    }
    
    public void setInput(DataElement element)
    {    
	_viewer.setInput(element);     
    }
    
    public void resetView()
      {
	  _viewer.resetView();
      }
    
    public void setLinked(boolean flag)
    {
        _isLinked = flag;
	if (_viewer != null)
	    {
		_viewer.setLinked(flag);
	    }
    }
    
    public boolean isLinked()
    {
        return _isLinked;
    }
    
    public boolean isLinkedTo(ILinkable to)
    {
	return _viewer.isLinkedTo(to);
    }

    public void linkTo(ILinkable viewer)
    {
	if (!viewer.isLinkedTo(this))
	    {
		_viewer.linkTo(viewer);
	    }
    }

    public void fixateOnRelationType(String relationType)
    {
	_viewer.fixateOnRelationType(relationType);
    }

    public void fixateOnObjectType(String objectType)
    {
	_viewer.fixateOnObjectType(objectType);
    }
    
    public void unlinkTo(ILinkable viewer)
    {
	_viewer.unlinkTo(viewer);
    }
    
    public void setFocus()
    {  
	_viewer.setFocus();
    }  
    
        
    protected void handleSelectionChanged(SelectionChangedEvent event)
    {
	IStructuredSelection sel = (IStructuredSelection)event.getSelection();
	DataElement element = (DataElement)sel.getFirstElement();
	if (element != null)
	    {
		updateStatusSelected(element);	    
	    }
    } 
    
    protected void updateStatusSelected(DataElement element)
    {
	IStatusLineManager mgr = getViewSite().getActionBars().getStatusLineManager();
	mgr.setMessage(element.getValue());
    }


    public void fillLocalToolBar() 
    {
   }
    
  
}











