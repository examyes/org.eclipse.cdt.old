package org.eclipse.cdt.cpp.miners.debug;
/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.miners.miner.*;

import java.io.*;
import java.util.*;

public class DebugMiner extends Miner
{
    private String _gdbPiclPath = null;
    private String _debugJarPath = null;
    private String _debugInvocation = null;
    private String _debugOptions = null;

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

    public void load()
    {
	_debugOptions = getLocalizedString("debug_options");
	
	_gdbPiclPath = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + "org.eclipse.cdt.debug.gdbPicl";
	String debugPath = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + "com.ibm.debug";
	String ps = System.getProperty("path.separator");
	String fs = "/";
        _debugJarPath =  debugPath;
	_debugJarPath += ps + debugPath + fs + "ibmdebug.jar";
        _debugJarPath += ps + debugPath + fs + "epdc.jar";
        _debugJarPath += ps + _gdbPiclPath;
	_debugJarPath += ps + _gdbPiclPath + fs + "debug_gdbPicl.jar";
	_debugInvocation = "java -cp " + _debugJarPath + " " + _debugOptions + " org.eclipse.cdt.debug.gdbPicl.Gdb ";
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
	    invocationStr += " -gdbPath=" + _gdbPiclPath;
	}

	/**
	if (jre != null)
	    {
		invocationStr = jre.getName() + invocationStr;
	    }
	**/

	//System.out.println("invocation = " + invocationStr);
	DataElement invocation = _dataStore.createObject(null, "invocation", invocationStr);

	DataElement cmdDescriptor = _dataStore.localDescriptorQuery(directory.getDescriptor(), "C_COMMAND");
	if (cmdDescriptor != null)
	    {
		//		File dir = new File(directory.getName());
		//	directory.setAttribute(DE.A_SOURCE, dir.getAbsolutePath());
		//System.out.println("launching engine in " + directory.getSource());
		ArrayList args = new ArrayList();
		args.add(invocation);
		args.add(status);
		_dataStore.command(cmdDescriptor, args, directory);
	    }
    }
}

