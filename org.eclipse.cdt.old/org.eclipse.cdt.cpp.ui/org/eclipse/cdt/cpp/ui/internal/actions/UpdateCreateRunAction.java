package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.miners.managedproject.*;

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
//
import org.eclipse.jface.dialogs.*;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.*;

public class UpdateCreateRunAction extends CustomAction
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
	
	public UpdateCreateRunAction(DataElement subject, String label, DataElement command, DataStore dataStore)
	{	
		super(subject, label, command, dataStore);
		if (!subject.getType().equals("Project"))	
				setEnabled(false);
		if(_command.getValue().equals("UPDATE_CREATE_RUN")&& !projectHasSubdir())
				setEnabled(false);		
	}
    public void run()
	{
		org.eclipse.swt.widgets.Shell shell = _dataStore.getDomainNotifier().findShell();
		String message = new String("Updating and or generating all the files needed by Automake and Autoconf"
				+"\nDo you wish to continue?");
				
		CustomDialog dialog = new CustomDialog(shell,null,null,null,3,null,0);
		if(dialog.openConfirm(shell,"Updating Autoconf Support Files ",message))
		{
			DataElement manageProjectCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_UPDATE_CREATE_RUN");
			DataElement status = _dataStore.command(manageProjectCmd, _subject);
			ModelInterface api = ModelInterface.getInstance();
			api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
			api.monitorStatus(status);
			
			RunThread thread = new RunThread(_subject, status);
			thread.start();
		}
	}
    private boolean projectHasSubdir()
    {
    	return projectHasSubdirHelper(_subject);
    }    
    private boolean projectHasSubdirHelper(DataElement root)
	{
		for (int i = 0; i < root.getNestedSize(); i++)
		{
			DataElement child = root.get(i).dereference();
			String type = child.getType();
			if (type.equals("Project") || type.equals("directory"))
				return true;
		}
		return false;
	}
	
	private boolean doesAutoconfSupportExist()
    {
	return doesAutoconfSupportExistHelper(_subject);
    }
    
    private boolean doesAutoconfSupportExistHelper(DataElement root)
	{
		for (int i = 0; i < root.getNestedSize(); i++)
		    {
			DataElement child = root.get(i).dereference();
			String type = child.getType();
			if (type.equals("file"))
			    {
				if (!child.isDeleted())
				    {
					String name = child.getName();
					if (name.equals("Makefile") ||
					    name.equals("Makefile.am") ||
					    name.equals("configure.in"))
					    {
							return true;
					    }
				    }
			    }
			else if (type.equals("Project") || type.equals("directory"))
			    {
				return doesAutoconfSupportExistHelper(child);
			    }
		    }
		return false;
	}
}
