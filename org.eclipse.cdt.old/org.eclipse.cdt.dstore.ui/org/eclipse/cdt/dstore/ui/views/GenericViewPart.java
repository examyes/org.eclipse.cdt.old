package com.ibm.dstore.ui.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.actions.*;

import com.ibm.dstore.core.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*; 
import com.ibm.dstore.ui.resource.*;

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
    class LockViewAction extends Action
    {
	public LockViewAction(String label, ImageDescriptor image)
	{
	    super(label, image );
	}
     
	public void run()
	{
	    _viewer.toggleLock();
	}
    }


    protected ObjectWindow       _viewer;
    protected   boolean          _isLinked;
    protected   IOpenAction      _openAction;
    protected   LockViewAction   _lockAction;
    private     boolean          _isLocked;

    public GenericViewPart()
    {
	super();
	_isLinked = true;
	_isLocked = false;
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
	return new ObjectWindow(parent, 0, null, new ImageRegistry(), loader);
    }
     
    public IActionLoader getActionLoader()
    {
	return new GenericActionLoader();
    }

    public void setActionLoader(IActionLoader loader)
    {
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
	if (!_isLocked)
	    {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		
		DataStoreCorePlugin plugin = DataStoreCorePlugin.getInstance();
		String path = plugin.getInstallLocation() + java.io.File.separator + "com.ibm.dstore.ui" + java.io.File.separator;
		String imageStr = path + "icons" + java.io.File.separator + "lock.gif";
		ImageDescriptor image = plugin.getImageDescriptor(imageStr, false);
		
		_lockAction = new LockViewAction("Lock View", image);
		_lockAction.setChecked(_viewer.isLocked());
		toolBarManager.add(_lockAction);
	    }
    }
    
    public void lock(boolean flag)
    {
	if (_viewer.isLocked() != flag)
	    {
		_viewer.toggleLock();

		if (flag == true)
		    {
			if (_lockAction != null)
			    {
				IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
				toolBarManager.removeAll();
			    }
			_isLocked = flag;
		    }
	    }
    }
    
}











