package org.eclipse.cdt.dstore.hosts.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.ui.actions.*;

import org.eclipse.cdt.dstore.hosts.*;
import org.eclipse.cdt.dstore.hosts.dialogs.*;

import org.eclipse.cdt.dstore.ui.dialogs.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*; 

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;


public class NewFile extends CustomAction implements IDomainListener
{
    private HostsPlugin _plugin;
    private String _newNameStr;
    private DataElement _status;

    public NewFile(DataElement subject, String label, DataElement command, DataStore dataStore)
    {	
	 super(subject, label, command, dataStore);
	
	 isValid(subject);	
	 _plugin = HostsPlugin.getInstance();
    }
    
    private boolean isValid(DataElement subject)
	{
	    return true;
	}

    public void run()
    {

	 String oldNameStr = new String(_subject.getName());
	 String oldSourceStr = new String(_subject.getSource());
	 
	 NewFileDialog dialog = new NewFileDialog(_plugin.getLocalizedString("actions.New_File"));
	 dialog.open();
	 if (dialog.getReturnCode() == dialog.OK)
	     {     
		 _newNameStr = dialog.getName();
		 
		 DataElement cmdDescriptor = _dataStore.localDescriptorQuery(_subject.getDescriptor(), 
									     "C_CREATE_FILE", 4);
		 if (cmdDescriptor != null)
		     {
			 ArrayList args = new ArrayList();
			 DataElement newName = _dataStore.createObject(null, "name", _newNameStr);		    	
			 args.add(newName);
			 _status = _dataStore.command(cmdDescriptor, args, _subject);
			 _dataStore.getDomainNotifier().addDomainListener(this);
		     }
	     }
    }

    public Shell getShell()
    {
	return null;
    }
    
    public boolean listeningTo(DomainEvent e)
    {
	if (e.getParent() == _status)
	    {
		return true;
	    }
	return false;
    }

    public void domainChanged(DomainEvent e)
    {
	if (_status.getName().equals("done"))
	    {
		_dataStore.getDomainNotifier().removeDomainListener(this);

		// find the new file
		DataElement newFile = _dataStore.find(_subject, DE.A_NAME, _newNameStr, 1); 
		if (newFile != null)
		    {
			IOpenAction openA = _plugin.getActionLoader().getOpenAction();
			openA.setSelected(newFile);
			openA.run();
		    }

	    }
    }
}

