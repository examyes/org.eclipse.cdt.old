package com.ibm.cpp.ui.internal.wizards;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;

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
	ArrayList list = CppPlugin.readProperty("DefaultBuildInvocation");
	String defaultStr = "nmake";
	if (list.size() > 0)
	    {
		defaultStr = (String)list.get(0);
	    }

	_buildInvocationEntry = new BuildInvocationEntry(parent, "Build Invocation", defaultStr);
	_buildInvocationEntryControl = _buildInvocationEntry.getControl();
    }

/**
 * getInvocations method comment.
 */

 public ArrayList getInvocations()
 {
  return _buildInvocationEntry.getInvocations();
 }

public Control getControl() {

   return _buildInvocationEntryControl;
}

}

