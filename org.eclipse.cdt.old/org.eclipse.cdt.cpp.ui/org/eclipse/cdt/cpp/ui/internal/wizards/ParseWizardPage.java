package com.ibm.cpp.ui.internal.wizards;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.jface.wizard.*;

public class ParseWizardPage extends WizardPage 
{
    public ParsePathWorkbookPage    _workbookPageParsePath;
    public ParseQualityWorkbookPage _workbookPageParseQuality;

    protected CppPlugin    _plugin = CppPlugin.getPlugin();

    public ParseWizardPage()  
    {
	super("ParseWizardPage");
	setTitle("Parser Properties");
	setDescription("Configure the C/C++ Parser");
	setPageComplete(true);
    }

    private void checkCompleteState() 
    {
    }

    public void createControl(Composite parent)
    {
	
	Composite composite = new Composite(parent, SWT.NONE);

	TabFolder folder = new TabFolder(composite, SWT.NONE);
	folder.setLayout(new GridLayout());
	GridData gdFolder= new GridData(GridData.FILL_HORIZONTAL);
	folder.setLayoutData(gdFolder);

	//page 1
	TabItem item1;
	_workbookPageParsePath = new ParsePathWorkbookPage(folder);
	item1 = new TabItem(folder, SWT.NONE);
	item1.setText(_plugin.getLocalizedString("createProjectWizard.Info.ParseTab"));
	item1.setData(_workbookPageParsePath);
	item1.setControl(_workbookPageParsePath.getControl());

	//page 2
	TabItem item2;
	_workbookPageParseQuality = new ParseQualityWorkbookPage(folder);
	item2 = new TabItem(folder, SWT.NONE);
	item2.setText("Parse Quality");
	item2.setData(_workbookPageParseQuality);
	item2.setControl(_workbookPageParseQuality.getControl());
		
	composite.setLayout(new FillLayout());
	setPageComplete(true);
	setControl(composite);
    }

    protected void enter(int direction) 
    {
	setPageComplete(false);
    }

}
