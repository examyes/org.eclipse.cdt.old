package org.eclipse.cdt.dstore.hosts.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.hosts.*;

import org.eclipse.cdt.dstore.ui.ILinkable;
import org.eclipse.cdt.dstore.ui.IActionLoader;
import org.eclipse.cdt.dstore.ui.ConvertUtility;
import org.eclipse.cdt.dstore.ui.actions.*;


import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;

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
    protected OutputViewer        _viewer;
    
    public OutputViewPart()
    {
		super();
    }
    
    public void createPartControl(Composite container)
    {
	Table table = new Table(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
	
	_viewer = new OutputViewer(table, null);
	TableLayout layout = new TableLayout();
	table.setLayout(layout);
	table.setHeaderVisible(true);
	
	layout.addColumnData(new ColumnWeightData(256));
	TableColumn tc = new TableColumn(table, SWT.NONE, 0);
	tc.setText("Output");
	
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


    public Shell getShell()
    {
	return _viewer.getShell();
    }
}
