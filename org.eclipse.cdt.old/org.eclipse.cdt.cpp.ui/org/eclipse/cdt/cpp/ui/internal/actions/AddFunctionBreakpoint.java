package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.ui.actions.*;

import com.ibm.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerRulerAction;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugConstants;

public class AddFunctionBreakpoint extends AddStatementBreakpoint
{ 
    public AddFunctionBreakpoint(DataElement subject, String label, DataElement command, DataStore dataStore)
    {	
        super(subject, label, command, dataStore);
    }

    public void createFunctionBreakpoint(DataElement function)
    {
	  // find the first statement
	  DataStore dataStore = function.getDataStore();
	  DataElement statement = dataStore.find(function, DE.A_TYPE, "statement", 1);
	  if (statement != null)
	      {
		  createBreakpointMarker(statement);
	      }	
    }

    public void run()
      {
	  createFunctionBreakpoint(_subject);
      }
}


