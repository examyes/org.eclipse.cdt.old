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

public class MakefileAmAction extends CustomAction {
	public MakefileAmAction(DataElement subject, String label, DataElement command, DataStore dataStore)
	{	
		super(subject, label, command, dataStore);
		if(_command.getValue().equals("INSERT_CONFIGURE_IN")||_command.getValue().equals("TOPLEVEL_MAKEFILE_AM"))
		{
			if (!subject.getType().equals("Project"))	
				setEnabled(false);
		}
	}
	public void run()
	{
		DataElement makefileAmCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_" + _command.getValue());
		DataElement status = _dataStore.command(makefileAmCmd, _subject);
		ModelInterface api = ModelInterface.getInstance();
		api.showView("com.ibm.cpp.ui.CppOutputViewPart", status);
		api.monitorStatus(status);
    }
}

