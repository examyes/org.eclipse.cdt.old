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

    public void extendSchema(DataElement schemaRoot)
    {
	DataElement hostD = _dataStore.find(schemaRoot, DE.A_NAME, "host", 1);
	DataElement containerD = _dataStore.find(schemaRoot, DE.A_NAME, "Container Object", 1);

	DataElement processesD = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Processes");	
	_dataStore.createReference(processesD, containerD, "abstracted by", "abstracts"); 
	

	DataElement processD = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Process");	
	_dataStore.createReference(hostD, processesD);
	_dataStore.createReference(processesD, processD);

	DataElement cprocessD = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Child Process");	
	_dataStore.createReference(processD, cprocessD, "abstracts", "abstracted by");

	DataElement killD = createCommandDescriptor(processD, "Kill", "C_KILL");

	String theOS = System.getProperty("os.name").replace(' ', '_');
	_psCommand = getLocalizedString(theOS + "_ps");
	if (_psCommand == null)
	    {
		// get default
		_psCommand = getLocalizedString("default_ps");
	    }
	
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
	    }
	catch (IOException e)
	    {
		e.printStackTrace();
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
		_dataStore.createReference(processD, attributeD, "attributes");
		
		// determine format
		if (nextToken.indexOf('%') == 0)
		    {
			_dataStore.createReference(attributeD, floatD, "attributes");
		    }
		else
		    {
			_dataStore.createReference(attributeD, stringD, "attributes");
		    }
	    }
    }
    
    public void load()
    {
	if (_validPS)
	    {
		// get host object
		DataElement hostObject = _dataStore.getHostRoot();
		
		DataElement processes = _dataStore.createObject(hostObject, "Processes", "Processes");	
		_monitor = new ProcessMonitor(_psCommand, _reader, _writer, processes);
		_monitor.setWaitTime(3000);
		_monitor.setDataStore(_dataStore);
		_monitor.start();
	    }
    }
    
    public void finish()
    {
	try
	    {  
		_writer.write("exit");
		_writer.write('\n');
		_writer.flush();   			
	    }
	catch (IOException e)
	    {
		//	System.out.println(e);
	    }	
	
	_monitor.finish();
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

	status.setAttribute(DE.A_NAME, "done");
	return status;
    }


}

