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

public class OutputHandler extends Handler
{
    private BufferedReader _reader;
    private DataElement _processRoot;
    private ArrayList   _currentProcesses;

    public OutputHandler(BufferedReader reader, DataElement processRoot, ArrayList currentProcesses)
    {
	super();
	_reader = reader;
	_processRoot = processRoot;
	_currentProcesses = currentProcesses;
    }


    public synchronized void handle()
    {
	// read top whitespace
	try
	    {
		if (_reader.ready())
		    {
			String line = _reader.readLine();
			_currentProcesses.clear();
			while (_reader.ready() && (line = _reader.readLine()) != null)
			    {
				if (line != null && line.length() > 0)
				    {
					parseProcessLine(line);				
				    }
				else
				    {
					break;
				    }
			    }
			
			// find all non-updated elements
			for (int i = _processRoot.getNestedSize() - 1; i >= 0; i--)
			    {
				DataElement process = _processRoot.get(i);
				if (!_currentProcesses.contains(process))
				    {
					_dataStore.deleteObject(_processRoot, process);
				    }
			    }		
		    }
	    }
	catch (IOException e)
	    {
	    }		
    }
    

    private void parseProcessLine(String processLine)
    {
	try
	    {
		DataElement processD = _dataStore.find(_dataStore.getDescriptorRoot(), DE.A_NAME, "Process", 1);
		StringTokenizer tokenizer = new StringTokenizer(processLine);
		if (processD != null && tokenizer.hasMoreTokens())
		    {
			String processName = tokenizer.nextToken();
			DataElement process = _dataStore.find(_processRoot, DE.A_NAME, processName, 1);
			if (process == null)
			    {
				process = _dataStore.createObject(_processRoot, processD, processName);
			    }
			
			_currentProcesses.add(process);
		
			int i = 0;
			ArrayList attributes = processD.getAssociated("attributes");
			while(tokenizer.hasMoreTokens() && i < attributes.size())
			    {
				DataElement attributeType = (DataElement)attributes.get(i);
				
				String nextToken = tokenizer.nextToken();
				if (attributeType.getName().equals("STIME"))
				    {
					//*** hack for windows ***//
					if (Character.isLetter(nextToken.charAt(0)))
					    {
						nextToken += " " + tokenizer.nextToken();
					    }
				    }
				
				DataElement attribute = _dataStore.find(process, DE.A_TYPE, attributeType.getName(), 1);
				if (attribute == null)
				    {
					if (attributeType.getName().equals("PPID") && 
					    (!nextToken.equals("1") && !nextToken.equals("0")))
					    {
						if (process.getAttribute(DE.A_TYPE).equals("Process"))
						    {
							process.setAttribute(DE.A_TYPE, "Child Process");
						    }
					    }			       
					
					attribute = _dataStore.createObject(process, attributeType, nextToken);
					_dataStore.createReference(process, attribute, "attributes");
					
				    }
				else
				    {
					String curName = attribute.getAttribute(DE.A_NAME);
					if (!curName.equals(nextToken))
					    {
						attribute.setAttribute(DE.A_NAME, nextToken);
						_dataStore.refresh(attribute);
					    }
				    }
				i++;
			    }
		    }
	    }    
	catch (Exception e)
	    {
	    }
    }
}
