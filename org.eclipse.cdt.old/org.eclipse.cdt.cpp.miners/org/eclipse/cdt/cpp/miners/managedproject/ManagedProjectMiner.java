package com.ibm.cpp.miners.managedproject;
/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;
import com.ibm.cpp.miners.managedproject.amparser.*;

import java.util.*;
import java.io.*;

public class ManagedProjectMiner extends Miner
{
	public void load() 
	{
	}
 
	public void extendSchema(DataElement schemaRoot)
	{
		DataElement projectD = _dataStore.find(schemaRoot, DE.A_NAME, "Project");
		DataElement fsObjectD = _dataStore.find(schemaRoot, DE.A_NAME, "Filesystem Objects");
	
		
		createCommandDescriptor(projectD, "Manage Project", "C_MANAGE_PROJECT", false);
		createCommandDescriptor(projectD, "configure", "C_CONFIGURE",false);
		createCommandDescriptor(projectD,"Switch to static_lib Makefile.am","C_SWITCH_TO_STATIC_LIB", false);
		createCommandDescriptor(fsObjectD,"Switch to shared_lib Makefile.am","C_SWITCH_TO_SHARED_LIB", false);
		createCommandDescriptor(projectD,"Switch to default Makefile.am","C_DEFAULT_MAKEFILE_AM",false);
		createCommandDescriptor(projectD,"Switch to top level Makefile.am","C_TOPLEVEL_MAKEFILE_AM",false);
    
		DataElement managedProjectD  = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, Am.MANAGED_PROJECT);
		createCommandDescriptor(managedProjectD, "Unmanage Project", "C_UNMANAGE_PROJECT");
		DataElement targetD          = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, Am.PROJECT_TARGET);
  
		DataElement targetAttributeTypeD = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, Am.TARGET_ATTRIBUTE_TYPE);
		DataElement targetAttributeD = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, Am.TARGET_ATTRIBUTE);
		DataElement targetOptionD    = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, Am.TARGET_OPTION);
  
		DataElement workspaceD       = _dataStore.find(schemaRoot, DE.A_NAME, "Workspace", 1);
		DataElement projectFileD     = _dataStore.find(schemaRoot, DE.A_NAME, "Project File", 1);
  
		DataElement managedProjectsD = _dataStore.createObject(schemaRoot, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, "Managed Projects");
		_dataStore.createReference(managedProjectsD, managedProjectD);
		_dataStore.createReference(managedProjectsD, targetD);
		_dataStore.createReference(managedProjectsD, projectFileD);
		_dataStore.createReference(managedProjectsD, targetAttributeTypeD);
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
  		String          name = getCommandName(theCommand);
  		DataElement   status = getCommandStatus(theCommand);
  		DataElement  project = getCommandArgument(theCommand, 0);
  		AutoconfManager manager = new AutoconfManager(project);
  		  
  		//if (!project.getType().equals("project"))  // refer to jeff regarding  this line
   			//return status;
		if (name.equals("C_UNMANAGE_PROJECT"))
 	 		_dataStore.deleteObject(project.getParent(), project);
		else if (name.equals("C_MANAGE_PROJECT"))
		{
			manager.manageProject(status);
			parseAmFile(project); 
		}
		else if (name.equals("C_CONFIGURE"))
		{
			manager.runConfigureScript(status);
		}
		else if (name.equals("C_DEFAULT_MAKEFILE_AM"))
		{
			manager.getMakeFileAmManager().setMakefileAmToDefault(project.getFileObject(),status);
		}
		else if (name.equals("C_SWITCH_TO_STATIC_LIB"))
		{
			manager.getMakeFileAmManager().setMakefileAmToStaticLib(project.getFileObject(),status);
		}
		else if (name.equals("C_TOPLEVEL_MAKEFILE_AM"))
		{
			manager.getMakeFileAmManager().setMakefileAmToTopLevel(project.getFileObject(),status);
		}
		else if (name.equals("C_SWITCH_TO_SHARED_LIB"))
		{
			manager.getMakeFileAmManager().setMakefileAmToSharedLib(project.getFileObject(),status);
		}
		else if (name.equals("C_REFRESH"))
  			refreshProject(project);
  
  		status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
  		return status;
	}
	private DataElement parseAmFile(DataElement theUnmanagedProject)
	{
		AmParser theParser = new AmParser(theUnmanagedProject);
		DataElement theManagedProject = theParser.parse();
		_dataStore.refresh(theManagedProject);
		_dataStore.refresh(theManagedProject.getParent());
		return theManagedProject;
	}
	private DataElement refreshProject(DataElement theUnmanagedProject)
	{

		return null;
	}
}

