package org.eclipse.cdt.cpp.ui.internal.widgets;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import java.util.*;

public class PathWorkbook extends Composite 
{
    public IncludePathWorkbookPage    _includePathPage;
    public LibraryWorkbookPage        _libraryPage;
    public ExternalSourceWorkbookPage _externalSourcePage;

    protected CppPlugin    _plugin = CppPlugin.getPlugin();

    public PathWorkbook(Composite parent, int style)  
    {
	super(parent, style);
	initialize();
    }

    public void initialize()
    {	
	TabFolder folder = new TabFolder(this, SWT.NONE);
	folder.setLayout(new TabFolderLayout());	
	folder.setLayoutData(new GridData(GridData.FILL_BOTH));

	//page 1
	TabItem item1;
	_includePathPage = new IncludePathWorkbookPage(folder);
	item1 = new TabItem(folder, SWT.NONE);
	item1.setText("Include Path");
	item1.setData(_includePathPage);
	item1.setControl(_includePathPage.getControl());

	//page 2
	TabItem item2;
	_libraryPage = new LibraryWorkbookPage(folder);
	item2 = new TabItem(folder, SWT.NONE);
	item2.setText("Libraries");
	item2.setData(_libraryPage);
	item2.setControl(_libraryPage.getControl());

	//page 3
	TabItem item3;
	_externalSourcePage = new ExternalSourceWorkbookPage(folder);
	item3 = new TabItem(folder, SWT.NONE);
	item3.setText("External Source Path");
	item3.setData(_externalSourcePage);
	item3.setControl(_externalSourcePage.getControl());	
    }

    public void setRemote(boolean isRemote)
    {
	_includePathPage.setRemote(isRemote);
	_externalSourcePage.setRemote(isRemote);
	_libraryPage.setRemote(isRemote);
    }

    public ArrayList getIncludePath()
    {
	return _includePathPage.getPaths();
    }

    public ArrayList getExternalSourcePath()
    {
	return _externalSourcePage.getPaths();
    }
    
    public ArrayList getLibraries()
    {
	return _libraryPage.getPaths();
    }

    public void setIncludePath(ArrayList paths)
    {
	_includePathPage.setPaths(paths);
    }

    public void setExternalSourcePath(ArrayList paths)
    {
	_externalSourcePage.setPaths(paths);
    }
    
    public void setLibraries(ArrayList paths)
    {
	_libraryPage.setPaths(paths);
    }

}
