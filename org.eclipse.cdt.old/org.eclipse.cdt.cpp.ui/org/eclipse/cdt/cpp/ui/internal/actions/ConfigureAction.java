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


public class ConfigureAction extends CustomAction implements SelectionListener
{ 
	private MessageDialog dialog = new MessageDialog(null,null,null,null,0,null,0);
	CustomMessageDialog xbox;
	String[] extraLabels;
	boolean enableRunDialog = true;
	boolean enableRunUpdate = true;
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
		
		if(_command.getValue().equals("CREATE_CONFIGURE")||_command.getValue().equals("RUN_CONFIGURE")||
		_command.getValue().equals("UPDATE_AUTOCONF_FILES")||_command.getValue().equals("UPDATE_CONFIGURE_IN"))
			if (!subject.getType().equals("Project"))	
				setEnabled(false);
				
		//enable disable based on object files
		if(_command.getValue().equals("UPDATE_AUTOCONF_FILES")&& !projectHasSubdir())
				setEnabled(false);		
		
		if((_command.getValue().equals("UPDATE_MAKEFILE_AM")&&!doesFileExist("Makefile.am")))
				setEnabled(false);
				
		if (subject.getType().equals("Project"))	
			if((_command.getValue().equals("UPDATE_CONFIGURE_IN")&&!doesFileExist("configure.in")))
				setEnabled(false);
			
		if(_command.getValue().equals("CREATE_CONFIGURE")&& doesFileExist("configure")&& configureIsUptodate(_subject))
				setEnabled(false);
				
						
		if(_command.getValue().equals("RUN_CONFIGURE")&&!doesFileExist("configure") )
				setEnabled(false);		
		
	}
    public void run()
	{
		boolean execute = true;
		int runUpdate = 0;// 0 == ok, 1 == no, 2 == cancel
		int createUpdate = 0; // 0 == ok, 1 == no, 2 == cancel
		boolean noConfigfilesExist = true;
		
		Shell shell = _dataStore.getDomainNotifier().findShell();
		
		if(_command.getValue().equals("UPDATE_AUTOCONF_FILES"))
		{
			if(doesAutoconfSupportExist())
			{
				String message = new String
				("Attempting to update existing and/or generating configure.in and makefile.am's "
					+"\nOld files will be renamed *.old");
				execute = dialog.openConfirm(shell,"Updating configure.in and Makefile.am's ",message);
			}
		}	
		if(_command.getValue().equals("UPDATE_MAKEFILE_AM"))
		{
			if(doesFileExist("Makefile.am"))
			{
				String message = new String
				("Attempting to update existing makefile.am's"+
					"\nIf updated then old Makefile.am's will be renamed *.old");
				execute = dialog.openConfirm(shell,"Updating Makefile.am's ",message);
			}
		}	
		if(_command.getValue().equals("UPDATE_CONFIGURE_IN"))
		{
			if(doesFileExist("configure.in"))
			{
				String message = new String
				("Attempting to update existing configure.in "+
					"\nIf updated then old configure.in shall be renamed *.old");
				execute = dialog.openConfirm(shell,"Updating configure.in",message);
			}
		}	
		
		if(_command.getValue().equals("CREATE_CONFIGURE"))
		{
			String str1;
			String message;
			
			if(doesAutoconfSupportExist())
			{
				noConfigfilesExist = false;
				str1 = new String("\nWould you like the system to update and generate missing configuration files?");
				message = new String("\nGenerating project configuration files"+str1);
				CustomMessageDialog box = new CustomMessageDialog(shell,
										"Creating configure.in and Makefile.am's ",
										null,
										message,3,new String[]{
									  	IDialogConstants.YES_LABEL,
									  	IDialogConstants.NO_LABEL, 
										IDialogConstants.CANCEL_LABEL},
										0,
										new String[]{"Do not show this Dialog again"},
										this);
				createUpdate = box.open();
			}
			else
			{
				noConfigfilesExist = true;
				str1 = "";
				message = new String("\nGenerating project configuration files"+str1);
				PreventableMessageBox box = new PreventableMessageBox(shell,
										"Creating configure.in and Makefile.am's ",
										null,
										message,2,new String[]{
									  	IDialogConstants.OK_LABEL, 
										IDialogConstants.CANCEL_LABEL},0);
				createUpdate = box.open();
			}
		}
		if(_command.getValue().equals("RUN_CONFIGURE"))
		{
			ArrayList showDialogRun = org.eclipse.cdt.cpp.ui.internal.CppPlugin.readProperty("Show Dialog Run");
			if (!showDialogRun.isEmpty())
			{
				String preference = (String)showDialogRun.get(0);
				if (preference.equals("Yes"))
					enableRunDialog = true;
				else
					enableRunDialog=false;
			}
			
			// checking if automatic updating is enabled from the autoconf preferences page
			ArrayList autoUpdateRun = org.eclipse.cdt.cpp.ui.internal.CppPlugin.readProperty("Auto Update Run");
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
				if(enableRunDialog)
				{
					if(enableRunUpdate)
					{
						String message = new String
						("\nThe system detects that configure script is not up to date"+
						"\nWould you like to update and generate missing configuration files before running configure?");
						extraLabels = new String[]{"Do not show this dialog again"};
						xbox = new CustomMessageDialog(shell,
												"Running configure script ",
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
						runUpdate = xbox.open();
					}
					else
					{
						String message = new String
						("\nRunning existing configure script" 
						+"\nAutomatic update is turned off - You can use autoconf preferences page to turn it on");
						extraLabels = new String[]{"Do not show this dialog again"};
						xbox = new CustomMessageDialog(shell,
												"Running configure script ",
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
						runUpdate = xbox.open(); 
						// 0 is equiv to 1 ie run with no update , 
						//and 1 is equiv to 2 which is to cancel the action so we need to increment
						runUpdate++; // this just to reflect what the user request
						System.out.println("\n run Updatetetet = "+runUpdate);
						
					}
				}
				else
				{ 
					if(enableRunUpdate)
					{
						runUpdate = 0;
					}
					else
						runUpdate=1;
				}
			}
			else
			{
				runUpdate = 1;
			}
		

		}
		if(execute)
		{	
			if(createUpdate==1 && !noConfigfilesExist)
			{
				System.out.println("\n 1");
				DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_CREATE_CONFIGURE_NO_UPDATE");			
				DataElement status = _dataStore.command(configureCmd, _subject);
				ModelInterface api = ModelInterface.getInstance();
				api.monitorStatus(status);			
				api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
				RunThread thread = new RunThread(_subject, status);
				thread.start();
			}
			else if(runUpdate==1)
			{
				System.out.println("\n 2");
				DataElement configureCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_RUN_CONFIGURE_NO_UPDATE");			
				DataElement status = _dataStore.command(configureCmd, _subject);
				ModelInterface api = ModelInterface.getInstance();
				api.monitorStatus(status);			
				api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
				RunThread thread = new RunThread(_subject, status);
				thread.start();
			}
			else if(createUpdate==0&&runUpdate==0&&enableRunUpdate)
			{
				System.out.println("\n 3");		
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
					if (name.equals("Makefile")||name.equals("Makefile.am")
						||name.equals("Makefile.in")||name.equals("configure.in"))
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
		boolean selection = xbox.extraButtons[buttonId].getSelection();
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
			org.eclipse.cdt.cpp.ui.internal.CppPlugin.writeProperty("Show Dialog Run",list);
		}
    }
}


