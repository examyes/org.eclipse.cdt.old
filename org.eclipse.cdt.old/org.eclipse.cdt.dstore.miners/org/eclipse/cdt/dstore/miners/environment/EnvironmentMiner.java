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
 
 public EnvironmentMiner () {super();};

 public void finish() { super.finish(); }
 public void updateMinerInfo() {}
 public void load() 
 {
  System.out.println("Loading Environment Miner");
  _system = _dataStore.createObject(_minerData, "Environment Variable", "System Environment");
  getSystemEnvironment();
 }
 
 public void extendSchema(DataElement schemaRoot) 
 { 
  DataElement envVar = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Environment Variable");
  _dataStore.createReference(envVar, _dataStore.createObject(schemaRoot, DE.T_RELATION_DESCRIPTOR, "Parent Environment"));
  DataElement containerObjectD = _dataStore.find(schemaRoot, DE.A_NAME, "Container Object", 1);
  _dataStore.createReference(containerObjectD, envVar, "abstracts", "abstracted by");
  
  createCommandDescriptor(containerObjectD, "C_SET_ENVIRONMENT_VARIABLES", "C_SET_ENVIRONMENT_VARIABLES");
 }
 
 public DataElement handleCommand (DataElement theElement)
 {
  String         name = getCommandName(theElement);
  DataElement  status = getCommandStatus(theElement);
  DataElement subject = getCommandArgument(theElement, 0);
  DataElement     env = getCommandArgument(theElement, 1);
  
  System.out.println("Handle Command " + theElement);
  System.out.println("subject " + subject);
  System.out.println("environment " + env);
  if (name.equals("C_SET_ENVIRONMENT_VARIABLES"))
   handleSetEnvironment(subject, env);

  status.setAttribute(DE.A_NAME, "done");
  return status;
 }

 public void handleSetEnvironment(DataElement theElement, DataElement environment)
 {
  System.out.println("theElement = " + theElement);
  System.out.println("env = " + environment);
  
  //First check to see if we already have an Environment for theElement..and get rid of it if we do.
  DataElement envRoot = _dataStore.find(_minerData, DE.A_NAME, environment.getId(), 1);
  if (envRoot != null)
  {
   _dataStore.deleteObject(_minerData, envRoot);
   _dataStore.refresh(_minerData);
   ArrayList theReferences = theElement.getAssociated("Environment");
   if (theReferences.size() > 0)
   {
    _dataStore.deleteObject(theElement, (DataElement)theReferences.get(0));
    _dataStore.refresh(theElement);
   }
   
  }
  _minerData.addNestedData(environment, false);
  environment.setParent(_minerData);
  _dataStore.refresh(environment);
  _dataStore.refresh(_minerData);
  _dataStore.createReference(theElement, environment);
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











