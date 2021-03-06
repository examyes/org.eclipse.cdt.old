package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;

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

import org.eclipse.swt.widgets.*;
import org.eclipse.jface.dialogs.*;

public class InvocationAction extends CustomAction 
{
    private String _invocation;
    public InvocationAction(DataElement subject, String invocation)
    {	
	super(subject, invocation, null, subject.getDataStore());
	_invocation = invocation;
    }

    public InvocationAction(DataElement subject, String label, String invocation)
    {	
	super(subject, label, null, subject.getDataStore());
	_invocation = invocation;
    }
    
    public void run()
    {	    
	DataElement cmdD = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_COMMAND");
	ArrayList args = new ArrayList();
	DataElement arg = _dataStore.createObject(null, "invocation", _invocation);
	args.add(arg);
	DataElement status = _dataStore.command(cmdD, args, _subject);
	ModelInterface api = ModelInterface.getInstance();
	api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
	api.monitorStatus(status);
    }
}

