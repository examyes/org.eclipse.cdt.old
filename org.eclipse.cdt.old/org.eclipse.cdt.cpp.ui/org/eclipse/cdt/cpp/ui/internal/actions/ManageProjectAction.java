package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.miners.managedproject.*;

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

//
import org.eclipse.jface.dialogs.*;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.*;

public class ManageProjectAction extends CustomAction
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
	public class CustomDialog extends MessageDialog
	{
		public CustomDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex)
		{
			super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType,dialogButtonLabels,defaultIndex);
		}
		public Control createCustomArea(Composite parent)
		{
			Label lb = new Label(parent, SWT.CENTER);
			lb.setText("Testing");
			return lb;
		}
	}
	
	public ManageProjectAction(DataElement subject, String label, DataElement command, DataStore dataStore)
	{	
		super(subject, label, command, dataStore);
		if (!subject.getType().equals("Project"))	
				setEnabled(false);
	}
    public void run()
	{
		String overwrite = "";
		if(doesAutoconfSupportExists())
			overwrite = "\nAll existing Autoconf related files will be overwritten";
		org.eclipse.swt.widgets.Shell shell = _dataStore.getDomainNotifier().findShell();
		String message = new String("This action will generate all the files needed for Autoconf Support"
				+overwrite+"\nDo you wish to continue?");
				
		CustomDialog dialog = new CustomDialog(shell,null,null,null,3,null,0);
		if(dialog.openConfirm(shell,"Generating Autoconf Support Files ",message))
		{
			DataElement manageProjectCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_MANAGE_PROJECT");
			DataElement status = _dataStore.command(manageProjectCmd, _subject);
			ModelInterface api = ModelInterface.getInstance();
			api.showView("com.ibm.cpp.ui.CppOutputViewPart", status);
			api.monitorStatus(status);
			
			RunThread thread = new RunThread(_subject, status);
			thread.start();
		}
	}
	private boolean doesAutoconfSupportExists()
	{
		File project = _subject.getFileObject();
		ProjectStructureManager structure = new ProjectStructureManager(project);
		File[]fileList = structure.getFiles();

		for (int i = 0; i < fileList.length; i++)
			if(fileList[i].getName().equals("Makefile")||fileList[i].getName().equals("Makefile.am")||fileList[i].getName().equals("Configure.in"))
				return true;
		return false;
	}
}
