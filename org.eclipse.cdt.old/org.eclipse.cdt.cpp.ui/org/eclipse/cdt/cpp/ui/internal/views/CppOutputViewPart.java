package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;

import com.ibm.dstore.hosts.*;
import com.ibm.dstore.hosts.views.*;

import com.ibm.dstore.ui.ILinkable;
import com.ibm.dstore.ui.ConvertUtility;
import com.ibm.dstore.ui.actions.*;

import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;

import org.eclipse.ui.*;
import org.eclipse.core.resources.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.part.*;

import java.util.*;

public class CppOutputViewPart extends OutputViewPart 
    implements ICppProjectListener

{
    protected CppPlugin         _plugin;  

	public CppOutputViewPart()
	{
		super();

	}

    public void createPartControl(Composite container)
    {
	_plugin = CppPlugin.getDefault();
	
	Table table = new Table(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.MULTI | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
	
	_viewer = new CppOutputViewer(table);
	TableLayout layout = new TableLayout();
	table.setLayout(layout);
	table.setHeaderVisible(true);
	
	layout.addColumnData(new ColumnWeightData(256));
	TableColumn tc = new TableColumn(table, SWT.NONE, 0);
	tc.setText("Output");
	
	CppProjectNotifier notifier = _plugin.getModelInterface().getProjectNotifier();
	notifier.addProjectListener(this);

	updateViewBackground();
	updateViewForeground();
	updateViewFont();
	
	getSite().setSelectionProvider(_viewer);
	fillLocalToolBar();
	
    }

    public void updateViewForeground()
    {
      ArrayList colours = _plugin.readProperty("OutputViewForeground");
      if (colours.size() == 3)
	  {
	      int r = new Integer((String)colours.get(0)).intValue();
	      int g = new Integer((String)colours.get(1)).intValue();
	      int b = new Integer((String)colours.get(2)).intValue();
	      
	      _viewer.setForeground(r, g, b);	      
	  }    
    }
    
  public void updateViewBackground()
  {
      ArrayList colours = _plugin.readProperty("OutputViewBackground");
      if (colours.size() == 3)
	  {
	      int r = new Integer((String)colours.get(0)).intValue();
	      int g = new Integer((String)colours.get(1)).intValue();
	      int b = new Integer((String)colours.get(2)).intValue();
	      
	      _viewer.setBackground(r, g, b);	      
	  }    
  }

    public void updateViewFont()
    {
      ArrayList fontArray = _plugin.readProperty("OutputViewFont");
      if (fontArray.size() > 0)
	  {
	      String fontStr = (String)fontArray.get(0);
	      fontStr = fontStr.replace(',', '|');
	      FontData fontData = new FontData(fontStr);
	      _viewer.setFont(fontData);
	  }
      else
	  {
	      Font font = JFaceResources.getTextFont();
	      _viewer.setFont(font);
	      
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
