package com.ibm.cpp.miners.managedproject;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;

import java.util.*;
import java.io.*;


public class ManagedProjectMiner extends Miner
{
 public void load() 
 {
 }
 
 public void extendSchema(DataElement schemaRoot)
 {
  DataElement projectD = _dataStore.find(schemaRoot, DE.A_NAME, "CProject");
  createCommandDescriptor(projectD, "Manage Project", "C_MANAGE_PROJECT");
  
    
  
  DataElement managedProjectD  = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Managed Project");
  createCommandDescriptor(managedProjectD, "Unmanage Project", "C_UNMANAGE_PROJECT");
  DataElement targetD          = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Project Target");
  DataElement targetAttributeD = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Target Attribute");
  DataElement targetOptionD    = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Target Option");
  
  DataElement workspaceD       = _dataStore.find(schemaRoot, DE.A_NAME, "Workspace", 1);
  DataElement projectFileD     = _dataStore.find(schemaRoot, DE.A_NAME, "Project File", 1);
  
  DataElement managedProjectsD = _dataStore.createObject(schemaRoot, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, "Managed Projects");
  _dataStore.createReference(managedProjectsD, managedProjectD);
  _dataStore.createReference(managedProjectsD, targetD);
  _dataStore.createReference(managedProjectsD, projectFileD);
  _dataStore.createReference(managedProjectsD, targetAttributeD);
  _dataStore.createReference(managedProjectsD, targetOptionD);
  
  
  _dataStore.createReference(workspaceD, managedProjectsD);

  createCommandDescriptor(managedProjectD, "New Target", "C_ADD_TARGET");
  createCommandDescriptor(targetD, "Build Target", "C_BUILD_TARGET");
  createCommandDescriptor(targetD, "Modify Target", "C_MODIFY_TARGET");
  createCommandDescriptor(targetD, "Remove Target", "C_REMOVE_TARGET");
 }
 
 public DataElement handleCommand(DataElement theCommand)
 {
  String name          = getCommandName(theCommand);
  DataElement  status  = getCommandStatus(theCommand);
  DataElement  project = getCommandArgument(theCommand, 0);
 
  if (name.equals("C_UNMANAGE_PROJECT"))
  {
   _dataStore.deleteObject(project.getParent(), project);
  }
  
  else if (name.equals("C_MANAGE_PROJECT"))
  {
   String fileLocation = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + "/com.ibm.cpp.miners/sample";

   DataElement workspace = project.getParent();
   DataElement managedProject = _dataStore.createObject(workspace, "Managed Project", "MyProject", fileLocation + File.separator + "Makefile.am");
   DataElement hello          = _dataStore.createObject(managedProject, "Project Target", "hello");
   DataElement goodbye        = _dataStore.createObject(managedProject, "Project Target", "goodbye");
  
   DataElement helloSources        = _dataStore.createObject(hello, "Target Attribute", "SOURCES");
   DataElement helloLdadd          = _dataStore.createObject(hello, "Target Attribute", "LDADD");
   DataElement helloLdFlags        = _dataStore.createObject(hello, "Target Attribute", "LDFLAGS");
   DataElement helloDependencies   = _dataStore.createObject(hello, "Target Attribute", "DEPENDENCIES");
   DataElement goodbyeSources      = _dataStore.createObject(goodbye, "Target Attribute", "SOURCES");

   
  _dataStore.createObject(helloSources, "Project File", "aux.cpp");
  _dataStore.createObject(helloSources, "Project File", "aux.h");
  _dataStore.createObject(helloSources, "Project File", "hello.cpp");
  _dataStore.createObject(helloLdadd, "Project File", "util.a");
  _dataStore.createObject(helloLdFlags, "Target Option", "-all-static");
  _dataStore.createReference(helloDependencies, goodbye);
  
  _dataStore.createObject(goodbyeSources, "Project File", "bye.c");
  _dataStore.createObject(goodbyeSources, "Project File", "bye.h");



  DataElement utilManagedProject = _dataStore.createObject(managedProject, "Managed Project", "util", fileLocation + File.separator + "util" + File.separator + "Makefile.am");
  DataElement util = _dataStore.createObject(utilManagedProject, "Project Target", "util");
  DataElement utilSources        = _dataStore.createObject(util, "Target Attribute", "SOURCES");
  DataElement utilLibadd         = _dataStore.createObject(util, "Target Attribute", "LIBADD");
  _dataStore.createObject(utilSources, "Project File", "util.c");
  _dataStore.createObject(utilSources, "Project File", "util.h");
  _dataStore.createObject(utilLibadd, "Project File", "fooX.o");
 }
      
  status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
  return status;
 }
}

