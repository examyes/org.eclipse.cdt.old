package com.ibm.dstore.miners.environment;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.miners.miner.*;
import com.ibm.dstore.core.model.*;
import java.io.*;
import java.util.*;

public class EnvironmentMiner extends Miner
{
 private DataElement _system;
 
 public EnvironmentMiner () 
    { 
	super(); 
    };


 public void finish() { super.finish(); }
 public void updateMinerInfo() {}

 public void load() 
 {
  _system = _dataStore.createObject(_minerData, "Environment Variable", "System Environment");
  getSystemEnvironment();
 }
 
 public void extendSchema(DataElement schemaRoot) 
    { 
	DataElement containerObjectD = _dataStore.findDescriptor(DE.T_ABSTRACT_OBJECT_DESCRIPTOR, getLocalizedString("model.Container_Object"));
	
	DataElement envVar = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Environment Variable");
	_dataStore.createReference(containerObjectD, envVar, "abstracts", "abstracted by");


  _dataStore.createReference(envVar, envVar, "contents");
  DataElement parentD    = _dataStore.createObject(schemaRoot, DE.T_RELATION_DESCRIPTOR, "Parent Environment");
  _dataStore.createReference(envVar, parentD);
  _dataStore.createReference(envVar, parentD);
  
  DataElement projectD        = _dataStore.find(schemaRoot, DE.A_NAME, "Project", 1);
  createCommandDescriptor(projectD, "Set Environment Variables", "C_SET_ENVIRONMENT_VARIABLES");
 }
 
 public DataElement handleCommand (DataElement theElement)
 {
  String name         = getCommandName(theElement);
  DataElement status  = getCommandStatus(theElement);
  DataElement subject = getCommandArgument(theElement, 0);
   
  if (name.equals("C_SET_ENVIRONMENT_VARIABLES"))
   handleSetEnvironment(subject, getCommandArgument(theElement, 1));

  status.setAttribute(DE.A_NAME, "done");
  return status;
 }

 public void handleSetEnvironment(DataElement preferenceRoot, DataElement environment)
 {
  DataElement envRoot = _dataStore.find(_minerData, DE.A_NAME, environment.getName(), 1);
  if (envRoot != null)
  {
   _dataStore.deleteObject(_minerData, envRoot);
   _dataStore.refresh(_minerData);
  }
  envRoot = _dataStore.find(preferenceRoot, DE.A_NAME, environment.getName(), 1);
  if (envRoot != null)
  {
   _dataStore.deleteObject(preferenceRoot, envRoot);
   _dataStore.refresh(preferenceRoot);
  }   
 
  _minerData.addNestedData(environment, false);
  environment.setParent(_minerData);
  _dataStore.refresh(environment);
  _dataStore.refresh(_minerData);
  if (_dataStore.find(preferenceRoot, DE.A_NAME, environment.getName(), 1) == null)
   _dataStore.createReference(preferenceRoot, environment);
  _dataStore.refresh(preferenceRoot);
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
    _dataStore.createObject(_system, "Environment Variable", curLine, curLine);
   }
  }
  catch (IOException e) 
  {
   System.err.println("Error getting System Environment Variables\n" + e.getMessage());
  }
 }
}











