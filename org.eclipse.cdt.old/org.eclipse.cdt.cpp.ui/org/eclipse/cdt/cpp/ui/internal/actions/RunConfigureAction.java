package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.dialogs.CustomMessageDialog;
import org.eclipse.cdt.cpp.ui.internal.dialogs.PreventableMessageBox;
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

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.dialogs.*;


public class RunConfigureAction extends CustomAction implements SelectionListener
{ 
	CustomMessageDialog box;
	String[] extraLabels = new String[]{"Do not show this dialog again"};
	String title = "Running configure script";
	boolean enableRunDialog = true;
	boolean enableRunUpdate = true;
	
	String runDialogKey = "Show_Run_Dialg";
	String runUpdateKey = "Update_When_Run";
	
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
	
	public RunConfigureAction(DataElement subject, String label, DataElement command, DataStore dataStore)
	{	
		super(subject, label, command, dataStore);
		
		if(_command.getValue().equals("RUN_CONFIGURE"))
			if (!subject.getType().equals("Project"))	
				setEnabled(false);

		if(_command.getValue().equals("RUN_CONFIGURE")&&!doesFileExist("configure") )
				setEnabled(false);		
		
	}
    public void run()
	{
		boolean execute = true;
		int runUpdate = 0;// 0 == ok, 1 == no, 2 == cancel
		boolean noConfigfilesExist = true;
		
		Shell shell = _dataStore.getDomainNotifier().findShell();
		

		if(_command.getValue().equals("RUN_CONFIGURE"))
		{
			// checking if automatic updating is enabled from the autoconf preferences page
			ArrayList autoUpdateRun = org.eclipse.cdt.cpp.ui.internal.CppPlugin.readProperty(runUpdateKey);
			if(!autoUpdateRun.isEmpty())
			{
				String preference = (String)autoUpdateRun.get(0);
				if (preference.equals("Yes"))
				{
					enableRunUpdate = true;
				}
				else
				{
					enableRunUpdate = false;;		
				}
			}
			if(!configureIsUptodate(_subject))
			{
				if(enableRunUpdate)
				{
					String message = new String
					("\nThe system detects that configure script is not up to date"+
					"\nWould you like to update and generate missing configuration files before running configure?");
					box = new CustomMessageDialog(shell,
											title,
											null,
											message,3,
											new String[]{
										  	IDialogConstants.YES_LABEL,
										  	IDialogConstants.NO_LABEL, 
												IDialogConstants.CANCEL_LABEL},
											0,
											extraLabels,
											this
											);
					// open	the xbox		
					int result = box.open(runDialogKey);
					if(result != -1)
						runUpdate = result; 
					else 
						runUpdate = 0;
				}
				else
				{
					String message = new String
					("\nRunning existing configure script" 
					+"\nAutomatic update is turned off - You can use the preferences page to turn it on");
					box = new CustomMessageDialog(shell,
											title,
											null,
											message,2,
											new String[]{
										  	IDialogConstants.OK_LABEL,
										  	IDialogConstants.CANCEL_LABEL},
											0,
											extraLabels,
											this
											);
					// open	the xbox		
					int result = box.open(runDialogKey); 
					if(result!=-1)
						runUpdate = ++result;
					else
						runUpdate = 1; 
					// 0 is equiv to 1 ie run with no update , 
					//and 1 is equiv to 2 which is to cancel the action so we need to increment
				}
			}
			else
			{
			runUpdate = 1;
			}
		}
		if(execute)
		{	
			if(runUpdate==1)
			{
				DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_RUN_CONFIGURE_NO_UPDATE");			
				DataElement status = _dataStore.command(configureCmd, _subject);
				ModelInterface api = ModelInterface.getInstance();
				api.monitorStatus(status);			
				api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
				RunThread thread = new RunThread(_subject, status);
				thread.start();
			}
			else if(runUpdate==0)//&&enableRunUpdate)
			{	
				DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_" + _command.getValue());			
				DataElement status = _dataStore.command(configureCmd, _subject);
				ModelInterface api = ModelInterface.getInstance();
				api.monitorStatus(status);			
				api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
				RunThread thread = new RunThread(_subject, status);
				thread.start();
			}	
		}
	}
	private boolean doesFileExist(String fileName)
	{
		for (int i = 0; i < _subject.getNestedSize(); i++)
		    {
			DataElement child = _subject.get(i).dereference();
			if (!child.isDeleted() && child.getName().equals(fileName))
			    {
					return true;
			    }
		    }
		return false;
	}
	private boolean configureIsUptodate(DataElement root)
	{
		DataElement cmdD = _dataStore.localDescriptorQuery(root.getDescriptor(), "C_CHECK_UPDATE_STATE", 4);
	
		if (cmdD != null)
		{
			DataElement status = _dataStore.synchronizedCommand(cmdD, root);		
			DataElement updateState = (DataElement)status.get(0);
		    String state = updateState.getName();
		    if(state.equals("uptodate"))
		    	return true;
		}
		return false;
	}
	public void widgetDefaultSelected(SelectionEvent e)
    {
		widgetSelected(e);
    }

    public void widgetSelected(SelectionEvent e)
    {
		Widget source = e.widget;
		int buttonId = ((Integer)e.widget.getData()).intValue();
		boolean selection = box.extraButtons[buttonId].getSelection();
		ArrayList list = new ArrayList();
		if(buttonId == 0)
		{
			// persist this value for thos project
			if(selection)
			{
				//enableRunDialog = false;
				list.add("No");
			}
			else
			{
				//enableRunDialog = true;
				list.add("Yes");
			}
			org.eclipse.cdt.cpp.ui.internal.CppPlugin.writeProperty(runDialogKey,list);
		}
    }
}


