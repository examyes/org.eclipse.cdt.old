package org.eclipse.cdt.dstore.hosts.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.actions.*;

import org.eclipse.cdt.dstore.hosts.*;
import org.eclipse.cdt.dstore.hosts.actions.*;
import org.eclipse.cdt.dstore.hosts.dialogs.*;

import org.eclipse.cdt.dstore.ui.dialogs.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.core.runtime.*;

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
    private ITransferListener _listener = null;
    private IProgressMonitor  _pm = null;
    private boolean _checkTimestamps = true;

    public TransferFiles(String name, DataElement source, DataElement target, ITransferListener listener)
    {
	_source = source;
	_target = target;
	_listener = listener;
	_plugin = HostsPlugin.getInstance();
    }
    
    public void checkTimestamps(boolean flag)
    {
    	_checkTimestamps = flag;
    }

    public void run(IProgressMonitor pm)
    {
	_pm = pm;
	run();
    }

    public void run()
    {
		DataStore sourceDataStore = _source.getDataStore();
		DataStore targetDataStore = _target.getDataStore();

		boolean validSource = _source.getType().equals("file") || 
	    _source.getType().equals("directory") ||
	    _source.getType().equals("Project"); // hack 
	    
		if (validSource)
	    {
			recursiveQuery(_source);
			recursiveQuery(_target);
			
			transfer(_source, _target);

			recursiveQuery(_target);

			if (_listener != null)
		    {
				_listener.getShell().getDisplay().asyncExec(new Notify("Ready")); 
		    }
		    
	    }
	

    }

	private void recursiveQuery(DataElement source)
	{
		DataStore dataStore = source.getDataStore();
		DataElement oDescriptor = dataStore.localDescriptorQuery(source.getDescriptor(), "C_OPEN", 4);
		if (oDescriptor != null)
		{	
			// open opened project - recursive query
			dataStore.command(oDescriptor, source);
		}				
	}


    private void transfer(DataElement source, DataElement target)
    {
    	if (_pm != null)
    	{
    	   if (_pm.isCanceled())
    	   {
    	   	return;
    	   }	
    	}
    	
	target.setExpanded(true);

	DataStore targetDataStore = target.getDataStore();
	DataStore sourceDataStore = source.getDataStore();

	String targetStr = target.getSource();
	if (targetStr.charAt(targetStr.length() - 1) != '/')
	{
		targetStr = targetStr + "/";
	} 
	String newSourceStr = targetStr + source.getName();
	
	boolean needsUpdate = false; 
	boolean scratchUpdate = false;

	String type = source.getType();

	DataElement copiedSource = targetDataStore.find(target, DE.A_NAME, source.getName(), 1);
	if (copiedSource == null)
	    {	 		
	    	if (type.equals("file"))
	    	{
	    		copiedSource = targetDataStore.createObject(null, type, source.getName(), newSourceStr);

	    		needsUpdate = true;
	    	}	    
	    	else 
	    	{
	    		copiedSource = targetDataStore.createObject(null, type, source.getName(), newSourceStr);
	    		DataElement mkdir = targetDataStore.localDescriptorQuery(target.getDescriptor(), "C_CREATE_DIR", 3);
	    		if (mkdir != null)
	    		{

	    			ArrayList args = new ArrayList();
	    			args.add(copiedSource); 
	    			targetDataStore.synchronizedCommand(mkdir, args, target);
	    		}	    		
	    	}
	    	scratchUpdate = true;
	    }
	else if (type.equals("file"))
		{
			// compare dates
			if (_checkTimestamps)
			{
				needsUpdate = compareDates(source, copiedSource);
			}
			else
			{
				needsUpdate = true;
		    }
	    }
	

		// both projects on same machine
		if (needsUpdate)
		{
			String utask = null;	
			if (scratchUpdate)
			{
				utask = "Creating " + newSourceStr + "...";
			}
			else
			{
			    utask = "Updating " + newSourceStr + "...";
			}
		
			if (_listener != null)
		    {
				_listener.getShell().getDisplay().asyncExec(new Notify(utask)); 
		    }
			else if (_pm != null)
		    {
				_pm.subTask(utask);
		    }	
		
		if ((targetDataStore == sourceDataStore) || (!targetDataStore.isVirtual() && !sourceDataStore.isVirtual()))
	    {
		// files on the same system
			// simply copy them
			DataElement cmd = targetDataStore.localDescriptorQuery(target.getDescriptor(), "C_COMMAND");
			if (cmd != null)
			{
				java.io.File sFile = source.getFileObject();
				targetDataStore.replaceFile(newSourceStr, sFile);
				
				if (_checkTimestamps)
				{
					setDate(copiedSource, getDate(source));
				}

			}
	    }
		else if (targetDataStore == _plugin.getDataStore())
	    {
	
			source.getFileObject();
			String sourceMapping = sourceDataStore.mapToLocalPath(source.getSource());
			java.io.File newSource = new java.io.File(sourceMapping);

			if (newSource != null && newSource.exists())
			    {
					File newFile = new java.io.File(newSourceStr);
					if (newFile.exists())
				    {
						newFile.delete();
				    }
				
					newSource.renameTo(newFile);
			    }		
	    }
		else
	    {	

			// make sure we have a local copy of the file
			File theFile = source.getFileObject();
			
			try
			{ 
				FileInputStream input = new FileInputStream(theFile);
				long totalSize = theFile.length();
				boolean firstAppend = true;
				int bufferSize = 50000;
				byte[] buffer = new byte[bufferSize];
				int totalRead = 0;
				while (totalRead < totalSize)
				{ 
					int available = input.available();
					available = (bufferSize > available) ? available : bufferSize;
				
					int bytesRead = input.read(buffer, 0, available);
					if (bytesRead == -1)
						break;
						
					if (firstAppend) 
					{ 
						firstAppend = false;
						targetDataStore.replaceFile(newSourceStr, buffer, bytesRead);				
					}
					else
					{
						targetDataStore.replaceAppendFile(newSourceStr, buffer, bytesRead);
					}
					
					totalRead += bytesRead;
					
				if (_pm != null)
		  		  {
		  		  	String msg = "Writing " + newSourceStr;
		  		  	if (totalRead != totalSize)
		  		  	{
		  		  	  msg = "Writing " + newSourceStr + " (" + totalRead / 1000 + "K of " + totalSize / 1000 + "K bytes)";
		  		  	}
					_pm.subTask(msg);
		  		  }
				}
			}
			catch (IOException e)
			{
			}
			
			if (_checkTimestamps)
			{
				setDate(copiedSource, getDate(source));
			}
			
	    }
	}
	
	if ((type.equals("directory") || type.equals("Project")))
	    {	
		if (_checkTimestamps)
		{
			queryDates(source);	
			
			if (!scratchUpdate)
			{		
			  queryDates(copiedSource);
			}
		}
	
	    ArrayList children = source.getAssociated("contents");
		for (int i = 0; i < children.size(); i++)
		    {
			DataElement child = (DataElement)children.get(i);
			String ctype = child.getType();
			if (ctype.equals("directory") || ctype.equals("file"))
			    {	
				transfer(child, copiedSource);				
			    }

			if (_pm != null)
			    {
				_pm.worked(1);
			    }
		    }
	    }	
    }

    private boolean compareDates(DataElement newSource, DataElement oldSource)
    {
    	long date1 = getDate(newSource);
	long date2 = getDate(oldSource);

	return (date1 > date2);
    }

    public void queryDates(DataElement directory)
    {
	directory.doCommandOn("C_DATES", true);		
    }

    private long getDate(DataElement fileElement)
    {
	DataElement dateObj = null;
	ArrayList timeArray = fileElement.getAssociated("modified at");
	if (timeArray.size() > 0)
	    {
		dateObj = (DataElement)timeArray.get(0); 
	    }
	else
	    {
		fileElement.doCommandOn("C_DATE", true);
		return getDate(fileElement);
	    }

	if (dateObj != null && dateObj.getType().equals("date"))
	    {
		Long date = new Long(dateObj.getName());
		return date.longValue(); 
	    }

	return -1;
    } 

    private void setDate(DataElement fileElement, long newDate)
    {
	if (fileElement != null)
	    {
	    	DataElement dateObj = null;
		ArrayList timeArray = fileElement.getAssociated("modified at");
		if (timeArray.size() > 0)
		    {
			dateObj = (DataElement)timeArray.get(0); 
			dateObj.setAttribute(DE.A_NAME, "" + newDate);
		    }
		
		DataStore dataStore = fileElement.getDataStore();
		DataElement dateDescriptor = dataStore.localDescriptorQuery(fileElement.getDescriptor(), 
									    "C_SET_DATE");
		if (dateDescriptor != null)
		    {
			ArrayList args = new ArrayList();
			if (dateObj == null)
			{
				dateObj = dataStore.createObject(null, "date", "" + newDate);
			}
			args.add(dateObj);
			dataStore.command(dateDescriptor, args, fileElement);
		    }
	    }
    }
}
