package org.eclipse.cdt.cpp.ui.internal.editor.contentoutliner;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.views.*;

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.views.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.ILinkable;

import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*; 

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;
import org.eclipse.core.runtime.*; 

import java.util.*;

import org.eclipse.ui.*;

public class SelectedObjectViewPart extends GenericViewPart
    implements ICppProjectListener

{
    private CppPlugin _plugin;
    public SelectedObjectViewPart()
    {
	super();
	_plugin = CppPlugin.getDefault();
    }
    
 
    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	DataStore dataStore = _plugin.getCurrentDataStore();
	ObjectWindow viewer =  new ObjectWindow(parent, ObjectWindow.TREE, dataStore, _plugin.getImageRegistry(), loader);
    
	return viewer;
    }


    public IActionLoader getActionLoader()
    {
	return CppActionLoader.getInstance();
    }


    public void createPartControl(Composite parent)
    {	
	super.createPartControl(parent);
	CppProjectNotifier notifier = _plugin.getModelInterface().getProjectNotifier();
	notifier.addProjectListener(this);

	updateViewBackground();
	updateViewForeground();
	updateViewFont();
    }

   
  public void initInput(DataStore dataStore)
  {
  	   	IAdaptable input = getSite().getPage().getInput();
    	
    	if (input != null && input instanceof DataElement)
    	{
    		_viewer.setInput(input);	
    	}
 
  
  }

    public void setInput(DataElement element)
    {    
	_viewer.setInput(element);     

	// open editor
	IActionLoader loader = getActionLoader();
	IOpenAction openAction = loader.getOpenAction();
	openAction.setSelected(element); 
	openAction.run();
    }


  public void setFocus()
  {  
      _viewer.setFocus();
  }

    public void updateViewForeground()
    {
      ArrayList colours = _plugin.readProperty("ViewForeground");
      if (colours.size() == 3)
	  {
	      int r = new Integer((String)colours.get(0)).intValue();
	      int g = new Integer((String)colours.get(1)).intValue();
	      int b = new Integer((String)colours.get(2)).intValue();
	      
	      _viewer.getViewer().setForeground(r, g, b);	      
	  }    
    }
    
  public void updateViewBackground()
  {
      ArrayList colours = _plugin.readProperty("ViewBackground");
      if (colours.size() == 3)
	  {
	      int r = new Integer((String)colours.get(0)).intValue();
	      int g = new Integer((String)colours.get(1)).intValue();
	      int b = new Integer((String)colours.get(2)).intValue();
	      
	      _viewer.getViewer().setBackground(r, g, b);	      
	  }    
  }

    public void updateViewFont()
    {
      ArrayList fontArray = _plugin.readProperty("ViewFont");
      if (fontArray.size() > 0)
	  {
	      String fontStr = (String)fontArray.get(0);
	      fontStr = fontStr.replace(',', '|');
	      FontData fontData = new FontData(fontStr);
	      _viewer.getViewer().setFont(fontData);
	  }
    }

    public void projectChanged(CppProjectEvent event)
    {
	int type = event.getType();
	switch (type)
	    {
	    case CppProjectEvent.VIEW_CHANGE:
		{
		    updateViewBackground();		
		    updateViewForeground();		
		    updateViewFont();
		}
		break;
		
		
	    default:
		break;
	    }
    }


}










