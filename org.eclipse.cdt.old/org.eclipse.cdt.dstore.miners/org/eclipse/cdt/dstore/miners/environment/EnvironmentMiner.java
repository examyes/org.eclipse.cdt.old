package org.eclipse.cdt.dstore.miners.environment;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.miners.miner.*;
import org.eclipse.cdt.dstore.core.model.*;
import java.io.*;
import java.util.*;

public class EnvironmentMiner extends Miner
{
    private DataElement _system;
    
    
    public void load() 
    {
	_system = _dataStore.createObject(_minerData, "Environment Variable", "System Environment");
	getSystemEnvironment();
    }
    
    protected ArrayList getDependencies()
    {
	ArrayList dependencies = new ArrayList();
	dependencies.add("org.eclipse.cdt.dstore.miners.filesystem.FileSystemMiner");
	return dependencies;
    } 

	
    public void extendSchema(DataElement schemaRoot) 
    { 
	DataElement envVar = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Environment Variable");
	_dataStore.createReference(envVar, _dataStore.createObject(schemaRoot, DE.T_RELATION_DESCRIPTOR, "Parent Environment"));
 	DataElement containerObjectD = _dataStore.find(schemaRoot, DE.A_NAME, "Container Object", 1);
	_dataStore.createReference(containerObjectD, envVar, "abstracts", "abstracted by");
	
	createCommandDescriptor(containerObjectD, "Set Environment Variables", "C_SET_ENVIRONMENT_VARIABLES", false);
	
	DataElement fsObj = _dataStore.find(schemaRoot, DE.A_NAME, "Filesystem Objects", 1);
	DataElement inhabits = _dataStore.createRelationDescriptor(schemaRoot, "inhabits");
	DataElement sustains = _dataStore.createRelationDescriptor(schemaRoot, "sustains");
	inhabits.setDepth(0);
	sustains.setDepth(0);
	
	_dataStore.createReference(envVar, sustains);
	_dataStore.createReference(fsObj, inhabits);
    }
    
    public DataElement handleCommand (DataElement theElement)
    {
	String         name = getCommandName(theElement);
	DataElement  status = getCommandStatus(theElement);
	DataElement subject = getCommandArgument(theElement, 0);
	DataElement     env = getCommandArgument(theElement, 1);
	
	
	if (name.equals("C_SET_ENVIRONMENT_VARIABLES"))
	    handleSetEnvironment(subject, env);
	
	status.setAttribute(DE.A_NAME, "done");
	return status;
    }
    
    public void handleSetEnvironment(DataElement theElement, DataElement environment)
    {
	String envName = theElement.getValue() + ".env";
	
	//First check to see if we already have an Environment for theElement..and get rid of it if we do.
	DataElement envRoot = _dataStore.find(_minerData, DE.A_NAME, envName, 1);
	if (envRoot != null)
	    {
		_dataStore.deleteObject(_minerData, envRoot);
		_dataStore.refresh(_minerData);
		ArrayList theReferences = theElement.getAssociated("inhabits");
		if (theReferences.size() > 0)
		    {
			_dataStore.deleteObject(theElement, (DataElement)theReferences.get(0));
			_dataStore.refresh(theElement);
		    }
	    }
	
	environment.setAttribute(DE.A_NAME, envName);
	environment.setAttribute(DE.A_VALUE, envName);
	
	_minerData.addNestedData(environment, false);
	environment.setParent(_minerData);
	_dataStore.refresh(_minerData);
	_dataStore.createReference(theElement, environment, "inhabits", "sustains");
	_dataStore.refresh(environment);
	_dataStore.refresh(theElement);
    }
    
    //This sucks, but the best way to get the current list of environment variables is to run the "env" (or "set" on
    //windows), and grab the output.  Can't use System.properties since this list only includes environment variables
    //that you passed in as parameters when you started the VM.
    private void getSystemEnvironment()
    {
	String envCommand  = "sh -c env";
	
	//If we're on windows, change the envCommand. 
	if (System.getProperty("os.name").toLowerCase().startsWith("win"))
	    envCommand = "cmd /c set"; 
	
	try
	    {
		Process        _process = Runtime.getRuntime().exec(envCommand);
		BufferedReader _output  = new BufferedReader(new InputStreamReader(_process.getInputStream()));
		
		String curLine;
		while ( (curLine = _output.readLine()) != null)
		    {
			if (curLine.indexOf("=") > 0)
			    _dataStore.createObject(_system, "Environment Variable", curLine, curLine);
   }
		_dataStore.refresh(_system);
		
	    }
	catch (IOException e) 
	    {
		System.err.println("Error getting System Environment Variables\n" + e.getMessage());
	    }
    }
}











