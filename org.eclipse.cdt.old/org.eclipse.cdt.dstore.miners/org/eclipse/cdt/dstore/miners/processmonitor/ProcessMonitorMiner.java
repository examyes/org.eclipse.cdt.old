package org.eclipse.cdt.dstore.miners.processmonitor;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.miners.miner.*;
import java.util.*;
import java.lang.*;
import java.io.*;


public class ProcessMonitorMiner extends Miner
{
    private ProcessMonitor _monitor;
    private String         _psCommand;
    private Process        _theProcess;
    private BufferedReader _reader;
    private BufferedWriter _writer;
    private boolean        _validPS = false;
    private DataElement    _processes = null;

    public ResourceBundle getResourceBundle()
    {
	ResourceBundle resourceBundle = null;
	// setup resource bundle
	try
	    {
		resourceBundle = ResourceBundle.getBundle(getName());
	    }
	catch (MissingResourceException mre)
	    {
	    }	  	

	return resourceBundle;
    }

    public void load(DataElement status)
    {
	DataElement schemaRoot = _dataStore.getDescriptorRoot();
	DataElement processD = _dataStore.find(schemaRoot, DE.A_NAME, "Process", 1);	
	

	// run ps to get column formats
	try
	    {
		_theProcess = Runtime.getRuntime().exec("sh");	
		_reader = new BufferedReader(new InputStreamReader(_theProcess.getInputStream()));
		_writer = new BufferedWriter(new OutputStreamWriter(_theProcess.getOutputStream()));
		
		try
		    {  
			_writer.write(_psCommand);
			_writer.write('\n');
			_writer.flush();   			
		    }
		catch (IOException e)
		    {
			System.out.println(e);
		    }	
		
		
		try
		    {
			Thread.currentThread().sleep(1000);
		    }
		catch (InterruptedException e)
		    {
		    }

		if (_reader.ready())
		{
			String headers = _reader.readLine();
			if (headers != null)
			    {
				// validate cmd
				if (headers.charAt(0) == '\'')
				    {
					_validPS = false;
				    }
				else
				    {
					defineProcessSchema(schemaRoot, processD, headers);
					_validPS = true;
				    }
			    }		
		    }
		else
		    {
			_validPS = false;
		    }

		// get host object
		DataElement hostObject = _dataStore.getHostRoot();			
		_processes = _dataStore.createObject(hostObject, "Processes", "Processes");	
	    }
	catch (IOException e)
	    {
	//		e.printStackTrace();
	_validPS = false;
	    }

    }

    public void extendSchema(DataElement schemaRoot)
    {
	DataElement hostD = _dataStore.find(schemaRoot, DE.A_NAME, "host", 1);
	DataElement containerD = _dataStore.find(schemaRoot, DE.A_NAME, "Container Object", 1);

	DataElement processesD = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Processes");	
	_dataStore.createReference(processesD, containerD, 
				   _dataStore.getAbstractedByRelation(), _dataStore.getAbstractsRelation()); 
	

	DataElement processD = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Process");	
	_dataStore.createReference(hostD, processesD);
	_dataStore.createReference(processesD, processD);

	DataElement cprocessD = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Child Process");	
	_dataStore.createReference(cprocessD, processD, 
				   _dataStore.getAbstractedByRelation(), _dataStore.getAbstractsRelation()); 	

	DataElement killD = createCommandDescriptor(processD, "Kill", "C_KILL");
	createCommandDescriptor(processesD, "Query", "C_QUERY");

	String theOS = System.getProperty("os.name").replace(' ', '_');
	_psCommand = getLocalizedString(theOS + "_ps");
	if (_psCommand == null)
	    {
		// get default
		_psCommand = getLocalizedString("default_ps");
	    }
	
    }

    private void defineProcessSchema(DataElement schemaRoot, DataElement processD, String headers)
    {
	StringTokenizer tokenizer = new StringTokenizer(headers);
	String processType = tokenizer.nextToken();

	DataElement stringD = _dataStore.find(schemaRoot, DE.A_NAME, "String", 1);
	DataElement intD    = _dataStore.find(schemaRoot, DE.A_NAME, "Integer", 1);
	DataElement floatD  = _dataStore.find(schemaRoot, DE.A_NAME, "Float", 1);

	while(tokenizer.hasMoreTokens())
	    {
		String nextToken = tokenizer.nextToken();
		DataElement attributeD = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, nextToken);
		_dataStore.createReference(processD, attributeD, _dataStore.getAttributesRelation());
		
		// determine format
		if (nextToken.indexOf('%') == 0)
		    {
			_dataStore.createReference(attributeD, floatD, _dataStore.getAttributesRelation());
		    }
		else
		    {
			_dataStore.createReference(attributeD, stringD, _dataStore.getAttributesRelation());
		    }
	    }
    }
    
    public void finish()
    {
	try
	    {  
		if (_writer != null && _validPS)
		    {
			_writer.write("exit");
			_writer.write('\n');
			_writer.flush();   	
		    }		
	    }
	catch (IOException e)
	    {
		//	System.out.println(e);
	    }	
	
	if (_monitor != null)
	    {
		_monitor.finish();
	    }

	super.finish();
    }
    
    public DataElement handleCommand(DataElement theCommand)
    {
	String name          = getCommandName(theCommand);
	DataElement  status  = getCommandStatus(theCommand);
	DataElement  subject  = getCommandArgument(theCommand, 0);

	if (name.equals("C_KILL"))
	    {
		try
		    {
			Runtime.getRuntime().exec("kill " + subject.getName());	
		    }
		catch (IOException e)
		    {
		    }
	    }
	else if (name.equals("C_QUERY"))
	    {
		if (_validPS)
		    {
			_monitor = new ProcessMonitor(_psCommand, _reader, _writer, _processes);
			_monitor.setWaitTime(3000);
			_monitor.setDataStore(_dataStore);
			_monitor.start();
		    }
	    }

	status.setAttribute(DE.A_NAME, "done");
	return status;
    }


}

