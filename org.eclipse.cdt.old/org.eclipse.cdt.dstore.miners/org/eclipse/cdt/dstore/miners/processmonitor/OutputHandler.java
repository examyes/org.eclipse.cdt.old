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


    public void handle()
    {
	synchronized(_currentProcesses)
	    {
		// read top whitespace
		String line = readLine();
		try
		    {
			while (_reader.ready() && (line = readLine()) != null)
			    {
				if (line != null)
				    {
					parseProcessLine(line);				
				    }
				else
				    {
					break;
				    }
			    }
		    }
		catch (IOException e)
		    {
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
    
    private String readLine ()
    {	    
	StringBuffer theLine = new StringBuffer();
	int ch;
	boolean done = false;
	while(!done && !isFinished())
	    {
		try
		    {
			ch = _reader.read();
			switch (ch)
			    {
			    case -1    : if (theLine.length() == 0)       //End of Reader 
				return null; 
				done = true; 
				break;                  
			    case 65535 : if (theLine.length() == 0)       //Check why I keep getting this!!! 
				return null; 
				done = true; 
				break;                     
			    case 10    : done = true;                     //Newline
				break;           
			    case 9     : theLine.append("     ");         //Tab
				break; 
			    case 13    : break;                          //Carriage Return
			    default    : theLine.append((char)ch);             //Any other character
			    }
			
			//Check to see if the BufferedReader is still ready which means there are more characters 
			//in the Buffer...If not, then we assume it is waiting for input.
			if (!_reader.ready())
			    done = true;
                    }  
		catch (IOException e)
		    {
			return null;
		    }
	    }
	return theLine.toString();
    }


    private void parseProcessLine(String processLine)
    {
	DataElement processD = _dataStore.find(_dataStore.getDescriptorRoot(), DE.A_NAME, "Process", 1);
	StringTokenizer tokenizer = new StringTokenizer(processLine);
	if (tokenizer.hasMoreTokens())
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
			String nextToken = tokenizer.nextToken();
			DataElement attributeType = (DataElement)attributes.get(i);
			DataElement attribute = _dataStore.find(process, DE.A_TYPE, attributeType.getName(), 1);
			if (attribute == null)
			    {
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
}
