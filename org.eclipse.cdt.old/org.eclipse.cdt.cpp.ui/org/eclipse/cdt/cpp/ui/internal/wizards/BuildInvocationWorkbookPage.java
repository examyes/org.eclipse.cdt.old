package org.eclipse.cdt.cpp.ui.internal.wizards;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.swt.layout.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import java.util.ArrayList;
import java.io.File;

public class BuildInvocationWorkbookPage {

  Control _buildInvocationEntryControl;
  private BuildInvocationEntry _buildInvocationEntry;

/**
 * BuildInvocationWorkbookPage constructor comment.
 * @param parent com.ibm.itp.ui.parts.Workbook
 */

public BuildInvocationWorkbookPage(Composite parent) 
    {
	ArrayList blist = CppPlugin.readProperty("DefaultBuildInvocation");
	String defaultBuildStr = "gmake -k";
	if (blist.size() > 0)
	    {
		defaultBuildStr = (String)blist.get(0);
	    }

	ArrayList clist = CppPlugin.readProperty("DefaultCleanInvocation");
	String defaultCleanStr = "gmake clean";
	if (clist.size() > 0)
	    {
		defaultCleanStr = (String)clist.get(0);
	    }

	_buildInvocationEntry = new BuildInvocationEntry(parent, 
							 "Build Invocation", defaultBuildStr, 
							 "Clean Invocation", defaultCleanStr );
	_buildInvocationEntryControl = _buildInvocationEntry.getControl();
    }

    public ArrayList getBuildInvocations()
    {
	return _buildInvocationEntry.getBuildInvocations();
    }
    
    public ArrayList getCleanInvocations()
    {
	return _buildInvocationEntry.getCleanInvocations();
    }
    
    public Control getControl() 
    {	
	return _buildInvocationEntryControl;
    }

}

