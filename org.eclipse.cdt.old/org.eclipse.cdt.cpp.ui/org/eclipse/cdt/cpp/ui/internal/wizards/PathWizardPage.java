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

import java.util.*;
import org.eclipse.jface.wizard.*;

public class PathWizardPage extends WizardPage 
{
    public PathWorkbook    _pathWorkbook;

    protected CppPlugin    _plugin = CppPlugin.getPlugin();
    private CppNewProjectResourceWizard _wizard;

    public PathWizardPage(CppNewProjectResourceWizard wizard)  
    {
	super("PathWizardPage");
	setTitle("Paths");
	setDescription("Set Project Paths");
	setPageComplete(true);
	_wizard = wizard;
    }

    private void checkCompleteState() 
    {
    }

    public void createControl(Composite parent)
    {	
	_pathWorkbook = new PathWorkbook(parent, SWT.NONE);
	
	setPageComplete(true);
	_pathWorkbook.setLayout(new FillLayout());

	setControl(_pathWorkbook);
	setDefaults();
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
	_pathWorkbook.setRemote(_wizard.isRemote());
    }

    private void setDefaults()
    {
	CppPlugin plugin = CppPlugin.getDefault();
	ArrayList includePath        = plugin.readProperty("DefaultIncludePath");
	ArrayList externalSourcePath = plugin.readProperty("DefaultExternalSourcePath");
	ArrayList libraries          = plugin.readProperty("DefaultLibraries");

	_pathWorkbook.setIncludePath(includePath);
	_pathWorkbook.setExternalSourcePath(externalSourcePath);
	_pathWorkbook.setLibraries(libraries);
    }

    public ArrayList getIncludePath()
    {
	return _pathWorkbook.getIncludePath();
    }

    public ArrayList getExternalSourcePath()
    {
	return _pathWorkbook.getExternalSourcePath();
    }
    
    public ArrayList getLibraries()
    {
	return _pathWorkbook.getLibraries();
    }
}
