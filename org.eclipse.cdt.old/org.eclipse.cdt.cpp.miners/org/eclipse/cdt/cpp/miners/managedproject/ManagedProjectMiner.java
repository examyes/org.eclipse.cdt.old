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
		createCommandDescriptor(projectD, "Manage Project", "C_MANAGE_PROJECT");
		createCommandDescriptor(projectD, "configure", "C_CONFIGURE", false);
    
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
  		// Yasser
  		AutoconfManager manager = new AutoconfManager(project);
  		  
  		//if (!project.getType().equals("project"))  // refer to jeff regarding  this line
   			//return status;
		if (name.equals("C_UNMANAGE_PROJECT"))
 	 		_dataStore.deleteObject(project.getParent(), project);
		else if (name.equals("C_MANAGE_PROJECT"))
		{
			manager.manageProject();
			parseAmFile(project); // to be put back again
		}
		else if (name.equals("C_CONFIGURE"))
		{
		    manager.runConfigureScript(status);
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
		System.out.println("Got it");
		return null;
	}
}

