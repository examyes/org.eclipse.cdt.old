package org.eclipse.cdt.linux.help.preferences;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import java.util.*;

import org.eclipse.cdt.cpp.ui.internal.widgets.TabFolderLayout;

import org.eclipse.cdt.linux.help.*;

public class HelpWorkbook extends Composite 
{
    public SearchWorkbookPage _searchPage;
    public IndexWorkbookPage _indexPage;
    public FilterWorkbookPage _filterPage;

    private HelpPlugin _plugin;

    public HelpWorkbook(Composite parent, int style)  
    {
	super(parent, style);
	_plugin= HelpPlugin.getDefault();
	initialize();
    }

    public void initialize()
    {	
	TabFolder folder = new TabFolder(this, SWT.NONE);
	folder.setLayout(new TabFolderLayout());	
	folder.setLayoutData(new GridData(GridData.FILL_BOTH));
	
	//page 1
	TabItem item1;
	_searchPage = new SearchWorkbookPage(folder);
	item1 = new TabItem(folder, SWT.NONE);
	item1.setText(_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_PREFERENCES_SEARCHWORKBOOKPAGE_TITLE));
	//item1.setImage(_plugin.getImage("full/"));
	item1.setData(_searchPage);
	item1.setControl(_searchPage.getControl());
	
	//page 2
	TabItem item2;
	_indexPage = new IndexWorkbookPage(folder);
	item2 = new TabItem(folder, SWT.NONE);
	item2.setText(_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_PREFERENCES_INDEXWORKBOOKPAGE_TITLE));
	//item2.setImage(_plugin.getImage("full/"))
	item2.setData(_indexPage);
	item2.setControl(_indexPage.getControl());
	
	//page 3
	TabItem item3;
	_filterPage = new FilterWorkbookPage(folder);
	item3 = new TabItem(folder, SWT.NONE);
	item3.setText(_plugin.getLocalizedString(IHelpNLConstants.SETTINGS_PREFERENCES_FILTERWORKBOOKPAGE_TITLE));
	//item3.setImage(_plugin.getImage("full/clcl16/filter_misc.gif"));
	item3.setData(_filterPage);
	item3.setControl(_filterPage.getControl());
    }

    public void performDefaults()
    {
	_searchPage.performDefaults();
	_indexPage.performDefaults();
	_filterPage.performDefaults();
    }

    public void performOk()
    {
	_searchPage.performOk();
	_indexPage.performOk();
	_filterPage.performOk();
    }
}
