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
	
	_gdbPiclPath = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + "com.ibm.debug.gdbPicl";
	String debugPath = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + "com.ibm.debug";
	String ps = System.getProperty("path.separator");
	String fs = "/";
        _debugJarPath =  debugPath;
	_debugJarPath += ps + debugPath + fs + "ibm_debug.jar";
	_debugJarPath += ps + _gdbPiclPath + fs + "debug_gdbPicl.jar";
	_debugInvocation = "java -cp " + _debugJarPath + " " + _debugOptions + " com.ibm.debug.gdbPicl.Gdb ";
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
	String invocationStr = _debugInvocation + "-qhost=" + hostName.getName() +
                                              " -quiport=" + port.getName() +
                                              " -startupKey=" + key.getName();

	if (System.getProperty("os.name").equals("Linux"))
	{
	    invocationStr += " -gdbPath=" + _gdbPiclPath;
	}
	DataElement invocation = _dataStore.createObject(null, "invocation", invocationStr);

	DataElement cmdDescriptor = _dataStore.localDescriptorQuery(directory.getDescriptor(), "C_COMMAND");
	if (cmdDescriptor != null)
	    {
		ArrayList args = new ArrayList();
		args.add(invocation);
		args.add(status);
		_dataStore.command(cmdDescriptor, args, directory);
	    }
    }
}

