package org.eclipse.cdt.cpp.ui.internal.builder;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.preferences.*;
import org.eclipse.cdt.cpp.ui.internal.widgets.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;

import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

import org.eclipse.core.resources.*;
import java.io.*;
import java.util.*;

import org.eclipse.ui.dialogs.*;

public class PathsPropertyPage extends PropertyPage
{	
    private PathWorkbook             _control;

    public PathsPropertyPage()
    {
        super();
    }
    
    protected Control createContents(Composite parent)
    {
        IProject project= getProject();
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
	
        if (CppPlugin.isCppProject(project))
	    {	
		_control = new PathWorkbook(parent, SWT.NONE);	    
		_control.setLayout(new FillLayout());

		CppPlugin plugin = CppPlugin.getDefault();
		
		ArrayList includePath        = plugin.readProperty(project, "Include Path");
		ArrayList externalSourcePath = plugin.readProperty(project, "External Source");
		ArrayList libraries          = plugin.readProperty(project, "Libraries");
		
		_control.setIncludePath(includePath);
		_control.setExternalSourcePath(externalSourcePath);
		_control.setLibraries(libraries);
	
		return _control;
	    }
        else
	    {
		Composite cnr = new Composite(parent, SWT.NONE);
		Label label = new Label(cnr, SWT.NULL);
		label.setText("Not a C/C++ Project");
		cnr.setLayout(layout);
		cnr.setLayoutData(new GridData(GridData.FILL_BOTH));
		return cnr;
	    }
    }
    
    
    public void performDefaults() 
    {
	CppPlugin plugin = CppPlugin.getDefault();

	ArrayList includePath        = plugin.readProperty("DefaultIncludePath");
	ArrayList externalSourcePath = plugin.readProperty("DefaultExternalSourcePath");
	ArrayList libraries          = plugin.readProperty("DefaultLibraries");

	_control.setIncludePath(includePath);
	_control.setExternalSourcePath(externalSourcePath);
	_control.setLibraries(libraries);
    }

    public boolean performOk()
    {
	CppPlugin plugin = CppPlugin.getDefault();

	ArrayList includePath        = _control.getIncludePath();
	ArrayList externalSourcePath = _control.getExternalSourcePath();
	ArrayList libraries          = _control.getLibraries();

	IProject project = getProject();
	
        plugin.writeProperty(project, "Include Path", includePath);
        plugin.writeProperty(project, "External Source", externalSourcePath);
        plugin.writeProperty(project, "Libraries", libraries);
	return true;
    }

  private IProject getProject()
      {
        return (IProject)getElement();
      }

}
