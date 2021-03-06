package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.File;

import org.eclipse.cdt.cpp.ui.internal.api.ModelInterface;
import org.eclipse.cdt.dstore.core.model.DataElement;
import org.eclipse.cdt.dstore.core.model.DataStore;
import org.eclipse.cdt.dstore.core.model.Handler;
import org.eclipse.cdt.dstore.ui.actions.CustomAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

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
		
		if(_command.getValue().equals("DIST_CLEAN")||_command.getValue().equals("MAINTAINER_CLEAN")||
		_command.getValue().equals("INSTALL"))
			if (!subject.getType().equals("Project"))
				setEnabled(false);
		if((_command.getValue().equals("DIST_CLEAN")||_command.getValue().equals("MAINTAINER_CLEAN")||
		_command.getValue().equals("INSTALL"))&&!doesFileExist("Makefile"))
			setEnabled(false);		
	}
	public void run()
	{
		Shell shell = _dataStore.getDomainNotifier().findShell();
		String message = new String("No Makefile to build '"+_subject.getName()+"' has been found in this directory"+
		"\nYou may want to create and or run configure before performing this action");
		MessageDialog dialog = new MessageDialog(shell,null,null,null,3,null,0);
		
		File parent = _subject.getFileObject();
		File Makefile = new File(parent,"Makefile");
		if(!Makefile.exists())
			dialog.openWarning(shell,"Building "+_subject.getName(),message);
		//if(!doesFileExists("Makefile"))
			//dialog.openWarning(shell,"Building "+_subject.getName(),message);
		else
		{
			DataElement makefileAmCmd = _dataStore.localDescriptorQuery(_subject.getDescriptor(), "C_" + _command.getValue());
			DataElement status = _dataStore.command(makefileAmCmd, _subject);
			ModelInterface api = ModelInterface.getInstance();
			api.showView("org.eclipse.cdt.cpp.ui.CppOutputViewPart", status);
			api.monitorStatus(status);
		
			RunThread monitor = new RunThread(_subject, status);
			monitor.start();
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
}

