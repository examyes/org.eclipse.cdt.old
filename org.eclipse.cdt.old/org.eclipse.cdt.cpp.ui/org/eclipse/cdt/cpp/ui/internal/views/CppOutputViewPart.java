package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

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
{
    public void createPartControl(Composite container)
    {
	_plugin = HostsPlugin.getDefault();
	
	Table table = new Table(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.MULTI | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
	
	_viewer = new CppOutputViewer(table);
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
}
