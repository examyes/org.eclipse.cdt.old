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


public class ConfigureAction extends CustomAction
{ 
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
	
	public ConfigureAction(DataElement subject, String label, DataElement command, DataStore dataStore)
	{	
		super(subject, label, command, dataStore);
		if(_command.getValue().equals("GENERATE_AUTOCONF_FILES")||_command.getValue().equals("CREATE_CONFIGURE")
			||_command.getValue().equals("RUN_CONFIGURE")||_command.getValue().equals("UPDATE_AUTOCONF_FILES")
			||_command.getValue().equals("UPDATE_CONFIGURE_IN"))
			if (!subject.getType().equals("Project"))	
				setEnabled(false);
	}
    public void run()
	{
	
		boolean execute = true;
	
		Shell shell = _dataStore.getDomainNotifier().findShell();
		if(_command.getValue().equals("GENERATE_AUTOCONF_FILES"))
		{
			MessageDialog dialog = new MessageDialog(shell,null,null,null,3,null,0);
			String message = new String("This action will generate all the files needed for Autoconf Support"+
				"\nExisting configure.in and Makefile.am's will be overwritten"+
				"\nDo you wish to continue?");
			if(!dialog.openConfirm(shell,"Generating configure.in and Makefile.am's ",message))
			{			
				execute = false;
			}
		}
		
		if(_command.getValue().equals("UPDATE_AUTOCONF_FILES"))
		{
			MessageDialog dialog = new MessageDialog(shell,null,null,null,3,null,0);
			String message = new String
			("Trying to update existing configure.in and makefile.am's"+
				"\nIf updated then old configure.in and Makefile.am's will be renamed *.old");
			dialog.openInformation(shell,"Updating configure.in and Makefile.am's ",message);
		}	
		
		if(execute)
		{			
			DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_" + _command.getValue());			
			DataElement status = _dataStore.command(configureCmd, _subject);
			ModelInterface api = ModelInterface.getInstance();
			api.monitorStatus(status);			

			api.showView("com.ibm.cpp.ui.CppOutputViewPart", status);

			
			RunThread thread = new RunThread(_subject, status);
			thread.start();
		}
	}
}


