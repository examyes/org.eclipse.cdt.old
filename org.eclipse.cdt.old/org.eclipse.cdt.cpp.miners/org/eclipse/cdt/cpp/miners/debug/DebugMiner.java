package com.ibm.cpp.miners.debug;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;

import org.eclipse.core.runtime.*;
import java.io.*;
import java.util.*;

public class DebugMiner extends Miner
{
    private String _debugJarPath = null;
    private String _debugInvocation = null;

    public void load()
    {
       String pluginPath = null;
       pluginPath = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH);

       String osString = org.eclipse.core.boot.BootLoader.getOS();
		 if (osString.equals("win32"))
		 {			
         _debugJarPath = pluginPath + "com.ibm.debug.gdb/epdc.jar;" + pluginPath + "com.ibm.debug.gdb/gdbPicl.jar"; // ";" on Windows
		 }
		 else
		 {
         _debugJarPath = pluginPath + "com.ibm.debug.gdb/epdc.jar:" + pluginPath + "com.ibm.debug.gdb/gdbPicl.jar"; // ":" on Linux
		 }
       _debugInvocation = "java -cp " + _debugJarPath + " com.ibm.debug.gdb.Gdb ";
    }

    public void extendSchema(DataElement schemaRoot)
    {
	DataElement dirD      = _dataStore.find(schemaRoot, DE.A_NAME, "directory", 1);
	DataElement cmdD      = createCommandDescriptor(dirD, "Debug", "C_DEBUG", false);
    }

    public DataElement handleCommand(DataElement theCommand)
    {
	String name          = getCommandName(theCommand);
	DataElement  status  = getCommandStatus(theCommand);
	DataElement  subject = getCommandArgument(theCommand, 0);

	if (name.equals("C_DEBUG"))
	    {
		handleDebug(subject,
			    getCommandArgument(theCommand, 1),
			    getCommandArgument(theCommand, 2),
			    getCommandArgument(theCommand, 3),
			    status);
	    }

	status.setAttribute(DE.A_NAME, "done");
	return status;
    }

    public void handleDebug(DataElement directory, DataElement hostName, DataElement port, DataElement key, DataElement status)
    {
	String invocationStr = _debugInvocation + "-qhost=" +
	    hostName.getName() + " -quiport=" + port.getName() + " -startupKey=" + key.getName();
	DataElement invocation = _dataStore.createObject(null, "invocation", invocationStr);

	DataElement cmdDescriptor = _dataStore.localDescriptorQuery(directory.getDescriptor(), "C_COMMAND");
	System.out.println("cd for  " + directory + " is " + cmdDescriptor);
	if (cmdDescriptor != null)
	    {
		//		File dir = new File(directory.getName());
		//	directory.setAttribute(DE.A_SOURCE, dir.getAbsolutePath());
		System.out.println("launching engine in " + directory.getSource());
		ArrayList args = new ArrayList();
		args.add(invocation);
		args.add(status);
		_dataStore.command(cmdDescriptor, args, directory);
	    }
    }
}

