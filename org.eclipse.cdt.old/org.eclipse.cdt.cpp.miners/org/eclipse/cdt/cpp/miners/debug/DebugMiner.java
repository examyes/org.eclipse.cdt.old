package com.ibm.cpp.miners.debug;
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;

import java.io.*;
import java.util.*;

public class DebugMiner extends Miner
{
    private String _gdbPluginPath = null;
    private String _debugJarPath = null;
    private String _debugInvocation = null;

    public void load()
    {
      String gdbPiclPath = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + "com.ibm.debug.gdbPicl";
      String ps = System.getProperty("path.separator");
      String fs = "/";
      _debugJarPath =       gdbPiclPath + fs + "epdc.jar";
      _debugJarPath += ps + gdbPiclPath + fs + "debug_gdbPicl.jar";
      _debugJarPath += ps + gdbPiclPath;
       _debugInvocation = "java -cp " + _debugJarPath + " com.ibm.debug.gdbPicl.Gdb ";
    }

    public void extendSchema(DataElement schemaRoot)
    {
	DataElement fsD      = _dataStore.find(schemaRoot, DE.A_NAME, "Filesystem Objects", 1);
	DataElement cmdD      = createCommandDescriptor(fsD, "Debug", "C_DEBUG", false);
    }

    public DataElement handleCommand(DataElement theCommand)
    {
	String name          = getCommandName(theCommand);
	DataElement  status  = getCommandStatus(theCommand);
	DataElement  subject = getCommandArgument(theCommand, 0);

	if (name.equals("C_DEBUG"))
	    {
		DataElement jre = getCommandArgument(theCommand, 4);
		if (jre != status)
		    {
			handleDebug(subject,
				    getCommandArgument(theCommand, 1),
				    getCommandArgument(theCommand, 2),
				    getCommandArgument(theCommand, 3),			
				    jre,
				    status);
		    }
		else
		    {
			handleDebug(subject,
 				    getCommandArgument(theCommand, 1),
				    getCommandArgument(theCommand, 2),
				    getCommandArgument(theCommand, 3),			
				    status);
		    }
	    }

	status.setAttribute(DE.A_NAME, "done");
	return status;
    }

    public void handleDebug(DataElement directory, DataElement hostName, DataElement port,
			    DataElement key, DataElement status)
    {
	handleDebug(directory, hostName, port, key, null, status);
    }

    public void handleDebug(DataElement directory, DataElement hostName, DataElement port,
			    DataElement key, DataElement jre, DataElement status)
    {
	String invocationStr = _debugInvocation + "-qhost=" + hostName.getName() +
                                              " -quiport=" + port.getName() +
                                              " -startupKey=" + key.getName();

	if (System.getProperty("os.name").equals("Linux"))
	{
	    invocationStr += " -gdbPath=" + _gdbPluginPath;
	}

	/**
	if (jre != null)
	    {
		invocationStr = jre.getName() + invocationStr;
	    }
	**/

	System.out.println("invocation = " + invocationStr);
	DataElement invocation = _dataStore.createObject(null, "invocation", invocationStr);

	DataElement cmdDescriptor = _dataStore.localDescriptorQuery(directory.getDescriptor(), "C_COMMAND");
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

