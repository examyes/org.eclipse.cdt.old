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
	_debugInvocation = "java -cp " + _debugJarPath + " com.ibm.debug.gdb.Gdb -qhost=localhost ";
    }
        
    public void extendSchema(DataElement schemaRoot)
    {
	DataElement dirD      = _dataStore.find(schemaRoot, DE.A_NAME, "directory", 1);
	DataElement cmdD      = createCommandDescriptor(dirD, "Debug", "C_DEBUG");
    }
    
    public DataElement handleCommand(DataElement theCommand)
    {
	String name          = getCommandName(theCommand);
	DataElement  status  = getCommandStatus(theCommand);
	DataElement  subject = getCommandArgument(theCommand, 0);

	if (name.equals("C_DEBUG"))
	    {
		handleDebug(subject, getCommandArgument(theCommand, 1), getCommandArgument(theCommand, 2));
	    }

	status.setAttribute(DE.A_NAME, "done");
	return status;
    }

    public void handleDebug(DataElement directory, DataElement port, DataElement key)
    {
	String invocationStr = _debugInvocation + "-quiport=" + port.getName() + " -startupKey=" + key.getName(); 
	DataElement invocation = _dataStore.createObject(null, "invocation", invocationStr);

	DataElement cmdDescriptor = _dataStore.localDescriptorQuery(directory, "C_COMMAND");
	if (cmdDescriptor != null)
	    {
		ArrayList args = new ArrayList();
		args.add(invocation);

		System.out.println("doing " + invocation);
		_dataStore.command(cmdDescriptor, invocation, directory);
	    }
    }
}

