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


import org.eclipse.swt.widgets.*;
import org.eclipse.jface.dialogs.*;

public class TargetAction extends CustomAction 
{
	DataElement _subject;
	public class RunThread extends Handler
	{
		private DataElement _subject;
		private DataElement _status;
		
		public RunThread(DataElement subject, DataElement status)
		{
			_subject = subject;
			_status = status;
		}
		
		public void handle()
		{
			if (_status.getName().equals("done"))
			{
				_subject.refresh(false);
		
				finish();
			}		
		}
	}
	public TargetAction(DataElement subject, String label, DataElement command, DataStore dataStore)
	{	
		super(subject, label, command, dataStore);
		_subject = subject;
	}
	public void run()
	{
		Shell shell = _dataStore.getDomainNotifier().findShell();
		String message = new String("No Makefile has been found in this directory"+
		"\nYou may want to create and or run configure before performing this action");
		MessageDialog dialog = new MessageDialog(shell,null,null,null,3,null,0);
		
		
		File parent = _subject.getFileObject().getParentFile();
		File Makefile = new File(parent,"Makefile");
		System.out.println("\nTest\n"+_subject);
		System.out.println("\nParent \n"+parent.getAbsolutePath());
		if(!Makefile.exists())
			dialog.openWarning(shell,"Generating Autoconf Support Files ",message);
		else
		{
			DataElement makefileAmCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_" + _command.getValue());
			DataElement status = _dataStore.command(makefileAmCmd, _subject);
			ModelInterface api = ModelInterface.getInstance();
			api.showView("com.ibm.cpp.ui.CppOutputViewPart", status);
			api.monitorStatus(status);
		
			RunThread monitor = new RunThread(_subject, status);
			monitor.start();
		}
    }
}

