package com.ibm.cpp.miners.debug;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;

import java.io.*;
import java.util.*;

public class DebugMiner extends Miner
{    
    private String _debugJarPath = null;
    private String _debugInvocation = null;
    
    public void load()
    {  
	_debugJarPath = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + "com.ibm.debug.gdb.Gdb/derdebug.jar";
	_debugInvocation = "java -cp " + _debugJarPath + " ";
    }
        
    public void extendSchema(DataElement schemaRoot)
    {
	DataElement fileD      = _dataStore.find(schemaRoot, DE.A_NAME, "file", 1);
	DataElement cmdD      = createCommandDescriptor(fileD, "Debug", "C_DEBUG");
    }
    
    public DataElement handleCommand(DataElement theCommand)
    {
	String name          = getCommandName(theCommand);
	DataElement  status  = getCommandStatus(theCommand);
	DataElement  subject = getCommandArgument(theCommand, 0);

	if (name.equals("C_DEBUG"))
	    {
		handleDebug(subject);
	    }

	status.setAttribute(DE.A_NAME, "done");
	return status;
    }

    public void handleDebug(DataElement fileToDebug)
    {
	String fileName = fileToDebug.getSource();
	File debugFile = new File(fileName);
	File debugDirectory = debugFile.getParentFile();

	DataElement directoryObject = _dataStore.createObject(null, "directory", 
							      debugDirectory.getName(), debugDirectory.getAbsolutePath());
	
	String invocationStr = _debugInvocation + fileToDebug.getName(); 
	DataElement invocation = _dataStore.createObject(null, "invocation", invocationStr);

	DataElement cmdDescriptor = _dataStore.localDescriptorQuery(directoryObject, "C_COMMAND");
	if (cmdDescriptor != null)
	    {
		ArrayList args = new ArrayList();
		args.add(invocation);

		System.out.println("doing " + invocation);
		_dataStore.command(cmdDescriptor, invocation, directoryObject);
	    }
    }
}

