package com.ibm.dstore.hosts.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.hosts.*;

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

public class OutputViewPart extends ViewPart implements ILinkable
{
    private OutputViewer        _viewer;
    protected HostsPlugin         _plugin;  

    public OutputViewPart()
    {
	super();
    }
    
    public void createPartControl(Composite container)
    {
	_plugin = HostsPlugin.getDefault();
	
	Table table = new Table(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
	
	_viewer = new OutputViewer(table);
	TableLayout layout = new TableLayout();
	table.setLayout(layout);
	table.setHeaderVisible(true);
	
	layout.addColumnData(new ColumnWeightData(256));
	TableColumn tc = new TableColumn(table, SWT.NONE, 0);
	tc.setText("Output");
	
	updateViewBackground();
	updateViewForeground();
	updateViewFont();
	
	getSite().setSelectionProvider(_viewer);
	fillLocalToolBar();
	
    }

    public void selectReveal(ISelection selection)
    {
	_viewer.setSelection(selection, true);
    }
    
    public void linkTo(ILinkable l)
    {
    }
    
    public void unlinkTo(ILinkable l)
    {
    }
    
    public void setLinked(boolean flag)
    {
    }
    
    public boolean isLinked()
    {
        return false;
    }
    
    public boolean isLinkedTo(ILinkable to)
      {
        return false;
      }
    
    public void setFocus()
    {
	_viewer.getTable().setFocus();
    }
    
    public void fillLocalToolBar()
    {
	IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	_viewer.fillLocalToolBar(toolBarManager);
    }
    
    public void resetView()
    {
    }


    public void setInput(DataElement element)
    {
	element.getDataStore().getDomainNotifier().addDomainListener(_viewer);	
	_viewer.setInput(element);
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

    public Shell getShell()
    {
	return _viewer.getShell();
    }
}
