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
    private String         _psCommand = "ps --format pid,ucomm,%cpu,%mem,user -a";
    private Process        _theProcess;
    private BufferedReader _reader;
    private BufferedWriter _writer;

    public void extendSchema(DataElement schemaRoot)
    {
	DataElement hostD = _dataStore.find(schemaRoot, DE.A_NAME, "host", 1);
	DataElement containerD = _dataStore.find(schemaRoot, DE.A_NAME, "Container Object", 1);

	DataElement processesD = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Processes");	
	_dataStore.createReference(processesD, containerD, "abstracted by", "abstracts"); 
	
	DataElement processD = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Process");	
	_dataStore.createReference(hostD, processesD);
	_dataStore.createReference(processesD, processD);

	DataElement killD = createCommandDescriptor(processD, "Kill", "C_KILL");

	String theOS = System.getProperty("os.name");
	if (theOS.toLowerCase().equals("linux"))
	    {
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
			
			
			String headers = _reader.readLine();
			if (headers != null)
			    {
				defineProcessSchema(schemaRoot, processD, headers);
			    }

		    }
		catch (IOException e)
		    {
			e.printStackTrace();
		    }
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
	// get host object
	DataElement hostObject = _dataStore.getHostRoot();
	
	String theOS = System.getProperty("os.name");
	if (theOS.toLowerCase().equals("linux"))
	    {	
		DataElement processes = _dataStore.createObject(hostObject, "Processes", "Processes");	
		_monitor = new ProcessMonitor(_psCommand, _reader, _writer, processes);
		_monitor.setWaitTime(3000);
		_monitor.setDataStore(_dataStore);
		_monitor.start();
	    }
    }
    
    public void finish()
    {
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

