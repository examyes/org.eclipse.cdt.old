package org.eclipse.cdt.pa.ui.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.views.*;

import org.eclipse.cdt.pa.ui.PAPlugin;
import org.eclipse.cdt.pa.ui.api.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;
import org.eclipse.ui.*;


public class PAObjectsViewPart extends GenericViewPart implements IPATraceListener
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


    protected PAModelInterface 	_api;
    protected PAPlugin      	_plugin;
    protected LockViewAction    _lockAction;
    private   boolean           _isLocked;
    
    // constructor
    public PAObjectsViewPart()
    {
	  super();
	  _plugin = PAPlugin.getDefault();
	  _api = PAModelInterface.getInstance();
	  _isLocked = false;
    }
    
    protected String getF1HelpId()
    {
     return "org.eclipse.cdt.pa.ui.objects_view_context";
    }

    public void createPartControl(Composite parent)
    {
      super.createPartControl(parent);    
      PATraceNotifier notifier = _api.getTraceNotifier();
      notifier.addTraceListener(this);
    }

    public IActionLoader getActionLoader()
    {
  	  IActionLoader loader = PAActionLoader.getInstance();
  	  return loader;
    }

    public Shell getShell()
    {
      return _api.getShell();
    }
    
    public void initInput(DataStore dataStore)
    {
	  _viewer.setInput(_api.getDummyElement());      
    } 


    public void dispose()
    {
      IWorkbench aWorkbench = _plugin.getWorkbench();
      IWorkbenchWindow win= aWorkbench.getActiveWorkbenchWindow();
      win.getSelectionService().removeSelectionListener(this);
	
	  PATraceNotifier notifier = _api.getTraceNotifier();
	  notifier.removeTraceListener(this);

	  if (_viewer != null)
	  {
	    _viewer.dispose();
	  }
	  
      super.dispose();
    }
    
    
    public void traceChanged(PATraceEvent event)
    {
      
    }


    public void fillLocalToolBar() 
  	{
  		super.fillLocalToolBar();
    	if (!_isLocked)
	    {
		  IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		
		  ImageDescriptor image = _plugin.getImageDescriptor("lock");
			
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

		if (flag)
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