package com.ibm.dstore.hosts.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.actions.*;

import com.ibm.dstore.hosts.*;
import com.ibm.dstore.hosts.actions.*;
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

public class TransferFiles extends Thread
{
    public class Notify implements Runnable
    {
	private String _message;
	public Notify(String message)
	{
	    _message = message;
	}

	public void run()
	{
	    _listener.update(_message);
	}
    }

    private DataElement _source;
    private DataElement _target;
    private HostsPlugin _plugin;
    private ITransferListener _listener;

    public TransferFiles(String name, DataElement source, DataElement target, ITransferListener listener)
    {
	_source = source;
	_target = target;
	_listener = listener;
	_plugin = HostsPlugin.getInstance();
    }

    public void run()
    {
	// this should work for receiving remotely...
	DataStore sourceDataStore = _source.getDataStore();
	DataStore targetDataStore = _target.getDataStore();

	String sourceMapping = sourceDataStore.mapToLocalPath(_source.getSource());
	
	String targetMapping = targetDataStore.getAttribute(DataStoreAttributes.A_LOCAL_PATH) +
	    java.io.File.separator + _target.getName() + java.io.File.separator + _source.getName();
	
	transfer(_source, _target);

	if (targetDataStore == _plugin.getDataStore())
	    {
		java.io.File newSource = new java.io.File(sourceMapping);
		if (newSource.exists())
		    {
			newSource.renameTo(new java.io.File(targetMapping));
		    }
	    }

	targetDataStore.refresh(_target);
	_listener.getShell().getDisplay().asyncExec(new Notify("Ready")); 
    }

    private void transfer(DataElement source, DataElement target)
    {
	target.setExpanded(true);
	source.expandChildren();

	DataStore targetDataStore = target.getDataStore();
	DataStore sourceDataStore = source.getDataStore();

	String targetStr = target.getSource();
	String newSourceStr = targetStr + java.io.File.separator + source.getName();

	_listener.getShell().getDisplay().asyncExec(new Notify("Creating " + newSourceStr + "...")); 
	

	DataElement copiedSource = targetDataStore.createObject(target, 
								source.getType(), 
								source.getName(),
								newSourceStr);
	
	if (targetDataStore == _plugin.getDataStore())
	    {
		source.getFileObject();
	    }
	else
	    {
		targetDataStore.setObject(target);
	
		if (source.getType().equals("file"))
		    {
			File theFile = new File(source.getSource());
			targetDataStore.replaceFile(newSourceStr, theFile);
		    }
	    }

	if (source.getType().equals("directory"))
	    {
		for (int i = 0; i < source.getNestedSize(); i++)
		    {
			DataElement child = source.get(i);
			transfer(child, copiedSource);
			targetDataStore.refresh(target);
		    }

	    }	

    }
}
