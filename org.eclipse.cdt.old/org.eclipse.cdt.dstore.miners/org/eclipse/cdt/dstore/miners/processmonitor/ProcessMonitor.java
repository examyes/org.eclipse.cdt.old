package org.eclipse.cdt.dstore.miners.processmonitor;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;

import java.util.*;
import java.lang.*;
import java.io.*;

public class ProcessMonitor extends Handler
{
    private DataElement _processRoot;
    private String      _psCommand;
    private BufferedReader _reader;
    private BufferedWriter _writer;
    private OutputHandler  _outputHandler;
    private ArrayList      _currentProcesses;

    public ProcessMonitor(String psCommand, BufferedReader reader, BufferedWriter writer, 
			  DataElement processRoot)
    {
	super();
	_processRoot = processRoot;
	_currentProcesses = new ArrayList();
	_psCommand = psCommand;
	_reader = reader;
	_writer = writer;
    }

    public synchronized void handle()
    {	
	try
	    {  
		_writer.write(_psCommand);
		_writer.write('\n');
		_writer.flush();
		
		_outputHandler.handle();
	    }
	catch (IOException e)
	    {
		System.out.println(e);
	    }		
    }

    public void setDataStore(DataStore dataStore)
    {
	super.setDataStore(dataStore);
	startOutputHandler();
    }

    private void startOutputHandler()
    {
	_outputHandler = new OutputHandler(_reader, _processRoot, _currentProcesses);
	_outputHandler.setDataStore(_dataStore);
    }

}
