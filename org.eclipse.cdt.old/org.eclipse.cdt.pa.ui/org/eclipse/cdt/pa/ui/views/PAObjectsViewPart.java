package org.eclipse.cdt.pa.ui.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.views.*;

import org.eclipse.cdt.pa.ui.PAPlugin;
import org.eclipse.cdt.pa.ui.api.*; 
import org.eclipse.swt.widgets.*;


public class PAObjectsViewPart extends ObjectsViewPart implements IPATraceListener
{
    protected PAModelInterface 	_api;
    protected PAPlugin      	_plugin;
    
    // constructor
    public PAObjectsViewPart()
    {
	  super();
	  _plugin = PAPlugin.getDefault();	  _api = PAModelInterface.getInstance();
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
    
    public void initInput(DataStore dataStore)
    {
      DataElement dummy = _api.getDummyElement();
      if (dummy != null)
	   _viewer.setInput(dummy);
	  else	   super.initInput(dataStore);
    } 

    public void setFocus() {
    }

    public void dispose()
    {	
	  PATraceNotifier notifier = _api.getTraceNotifier();
	  notifier.removeTraceListener(this);	
      super.dispose();
    }
    
    
    public void projectChanged(CppProjectEvent event)
    {
	  int type = event.getType();
	  switch (type)
	  {
	    case CppProjectEvent.COMMAND:
		{
		  updateSelectionStatus(event);
		}
		break;		
	    case CppProjectEvent.VIEW_CHANGE:
		{		  updateViewBackground();		
		  updateViewForeground();		
		  updateViewFont();
		}
		break;			
	    default:
		 break;
	  }
    }
    
    
    public void traceChanged(PATraceEvent event)
    {      
    }    
}
