package com.ibm.dstore.ui.views;
 
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.*;

import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*; 
import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.action.*;

import org.eclipse.ui.*;

public class DetailsViewPart extends GenericViewPart
{
    public class ZoomAction extends Action
    {
	public ZoomAction(String label, ImageDescriptor image)
	{
	    super(label, image);
	}

	public void run()
	{
	    DataElement input = _viewer.getInput();
	    if (input != null)
		{
		    DataElement parent = input.getParent();
		    if (parent != null)
			{
			    _viewer.setInput(parent);
			}
		}
	}
    }
    
  public DetailsViewPart()
  {
    super();
  }

    public void createPartControl(Composite parent)
    {
	super.createPartControl(parent);  
    }

    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	return new ObjectWindow(parent, 0, null, new ImageRegistry(), loader, true);
    }

    
    public void initInput(DataStore dataStore)
    {    
	_viewer.setInput((getSite().getPage().getInput()));      
  
    }
    
    public void selectionChanged(IWorkbenchPart part, ISelection sel) 
    {
	if (part != this)
	    {
		if (part instanceof ILinkable)
		    {
			((ILinkable)part).linkTo(this);	
			setLinked(true);
		    }
	    }
    }    

    public void fillLocalToolBar() 
    {
	super.fillLocalToolBar();
       	IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	toolBarManager.add(new ZoomAction("Zoom Out", DataStoreCorePlugin.getInstance().getImageDescriptor("up.gif")));
    }
    
}










