package org.eclipse.cdt.cpp.ui.internal.wizards;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.widgets.*;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.jface.wizard.*;

public class ParseWizardPage extends WizardPage 
{
    public ParseBehaviourWorkbookPage    _workbookPageParseBehaviour;
    public ParseQualityWorkbookPage       _workbookPageParseQuality;

    protected CppPlugin    _plugin = CppPlugin.getPlugin();
    private CppNewProjectResourceWizard _wizard;

    public ParseWizardPage(CppNewProjectResourceWizard wizard)  
    {
	super("ParseWizardPage");
	setTitle("Parser Properties");
	setDescription("Configure the C/C++ Parser");
	setPageComplete(true);
	_wizard = wizard;
    }

    private void checkCompleteState() 
    {
    }

    public void createControl(Composite parent)
    {	
	Composite composite = new Composite(parent, SWT.NONE);

	TabFolder folder = new TabFolder(composite, SWT.NONE);
	folder.setLayout(new TabFolderLayout());	
	folder.setLayoutData(new GridData(GridData.FILL_BOTH));

	//page 1
	TabItem item1;
	_workbookPageParseBehaviour = new ParseBehaviourWorkbookPage(folder);
	item1 = new TabItem(folder, SWT.NONE);
	item1.setText("Parser Behaviour");
	item1.setData(_workbookPageParseBehaviour);
	item1.setControl(_workbookPageParseBehaviour.getControl());

	//page 3
	TabItem item3;
	_workbookPageParseQuality = new ParseQualityWorkbookPage(folder);
	item3 = new TabItem(folder, SWT.NONE);
	item3.setText("Parse Quality");
	item3.setData(_workbookPageParseQuality);
	item3.setControl(_workbookPageParseQuality.getControl());
		
	composite.setLayout(new FillLayout());
	setPageComplete(true);
	setControl(composite);
    }

    public void setVisible(boolean flag)
    {
	if (flag)
	    {
		enter(1);
	    }
	super.setVisible(flag);
    }

    public void enter(int direction) 
    {
	setPageComplete(true);
    }

}
