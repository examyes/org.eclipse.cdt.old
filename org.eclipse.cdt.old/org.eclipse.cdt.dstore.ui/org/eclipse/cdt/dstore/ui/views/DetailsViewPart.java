package com.ibm.dstore.ui.views;
 
/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
	fillLocalToolBar();
    }

    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	return new ObjectWindow(parent, 0, null, new ImageRegistry(), this, true);
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
	IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	toolBarManager.add(new ZoomAction("Zoom Out", DataStoreCorePlugin.getInstance().getImageDescriptor("up.gif")));
	super.fillLocalToolBar();
    }
    
}










