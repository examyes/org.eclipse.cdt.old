package com.ibm.dstore.hosts.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.actions.*;

import com.ibm.dstore.hosts.*;
import com.ibm.dstore.hosts.dialogs.*;

import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*; 

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;


public class RenameResource extends CustomAction
{
    public RenameResource(DataElement subject, String label, DataElement command, DataStore dataStore)
    {	
	 super(subject, label, command, dataStore);
	
	 String type = subject.getType();
	 if (!type.equals("directory") && !type.equals("file"))
	    {
		setEnabled(false);
	    }
    }

    public void run()
    {

	 String oldNameStr = _subject.getName();
     RenameDialog dialog = new RenameDialog("Rename Resource", oldNameStr);
     dialog.open();
     if (dialog.getReturnCode() == dialog.OK)
     {     
	 	String newNameStr = dialog.getName();

	 	if (!newNameStr.equals(oldNameStr))
	    {
			DataElement cmdDescriptor = _dataStore.localDescriptorQuery(_subject.getDescriptor(), 
									    "C_RENAME", 4);
			if (cmdDescriptor != null)
		    {
		   	    	
		    	ArrayList args = new ArrayList();
		    	DataElement newName = _dataStore.createObject(null, "name", newNameStr);
		    	
		    	args.add(newName);
		    	
		    	DataElement notifyD = _dataStore.localDescriptorQuery(_dataStore.getRoot().getDescriptor(), "C_NOTIFICATION", 1);
		    	if (notifyD != null)
		    	{
		    			DataElement delDescriptor = _dataStore.createObject(null, "dummy", "C_DELETE");
		    			ArrayList nargs = new ArrayList();
		    			nargs.add(_subject);
		    			_dataStore.command(notifyD, nargs, delDescriptor);
		    	}
	
				_dataStore.command(cmdDescriptor, args, _subject);
				
				if (notifyD != null)
				{
    					DataElement addDescriptor = _dataStore.createObject(null, "dummy", "C_ADD");
		    			
		    			ArrayList nargs = new ArrayList();
		    			nargs.add(_subject);
		    			_dataStore.command(notifyD, nargs, addDescriptor);
					
				}
		    }
	    }
    }
    }    
}

