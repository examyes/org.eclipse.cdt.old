package com.ibm.cpp.miners.managedproject;
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;
import com.ibm.cpp.miners.managedproject.amparser.*;

import java.util.*;
import java.io.*;

public class ManagedProjectMiner extends Miner
{	private AutoconfManager autoconfManager;
	private DataElement _workspace = null;
	
	public void load() 
	{
		autoconfManager = new AutoconfManager();
	}
	public void extendSchema(DataElement schemaRoot)
	{
		DataElement projectD = _dataStore.find(schemaRoot, DE.A_NAME, "Project");
		DataElement fsObjectD = _dataStore.find(schemaRoot, DE.A_NAME, "Filesystem Objects");

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
		
		// autoconf	
		createCommandDescriptor(projectD, "Generate Autoconf/Automake files", "C_GENERATE_AUTOCONF_FILES", false);
		createCommandDescriptor(projectD, "Update Autoconf/Automake files", "C_UPDATE_AUTOCONF_FILES", false);
		createCommandDescriptor(projectD, "Create configure ", "C_CREATE_CONFIGURE",false);
		createCommandDescriptor(projectD, "Run configure", "C_RUN_CONFIGURE",false);
		createCommandDescriptor(projectD, "Manage Project", "C_MANAGE_PROJECT", false);
		
		DataElement makefileCmds = _dataStore.createObject(managedProjectD, DE.T_ABSTRACT_COMMAND_DESCRIPTOR, "Customconf");

		createCommandDescriptor(makefileCmds,"Add/Switch to Static lib Makefile.am","C_SWITCH_TO_STATIC_LIB");
		createCommandDescriptor(makefileCmds,"Add/Switch to Shared lib Makefile.am","C_SWITCH_TO_SHARED_LIB");
		createCommandDescriptor(makefileCmds,"Add/Change to Toplevel Makefile.am","C_TOPLEVEL_MAKEFILE_AM");
		createCommandDescriptor(makefileCmds,"Add/Change to Programs Makefile.am ","C_PROGRAMS_MAKEFILE_AM");		
		createCommandDescriptor(makefileCmds,"Add configure.in file","C_INSERT_CONFIGURE_IN");
		 _dataStore.createReference(fsObjectD, makefileCmds);
					
	}
	
	private void refresh(DataElement object)
	{
	//	object.
	}
	
	public DataElement getWorkspace()
	{
		return _workspace;
	}
	
	public DataElement handleCommand(DataElement theCommand)
 	{
  		String          name = getCommandName(theCommand);
  		DataElement   status = getCommandStatus(theCommand);
  		DataElement  project = getCommandArgument(theCommand, 0);

		if (_workspace == null)
		{
			_workspace = project.getParent();
			autoconfManager.setWorkspaceLocation(_workspace.getSource());
		}

  		//if (!project.getType().equals("project"))  // refer to jeff regarding  this line
   			//return status;
		if (name.equals("C_UNMANAGE_PROJECT"))
 	 		_dataStore.deleteObject(project.getParent(), project);
		else if (name.equals("C_MANAGE_PROJECT"))
		{
			autoconfManager.manageProject(project, status);
			refresh(project);
			parseAmFile(project); 
		}
		else if (name.equals("C_GENERATE_AUTOCONF_FILES"))
		{
			autoconfManager.generateAutoconfFiles(project, status,false);
			refresh(project);
			parseAmFile(project); 
		}
		else if (name.equals("C_UPDATE_AUTOCONF_FILES"))
		{
			autoconfManager.updateAutoconfFiles(project, status,false);
			refresh(project);
			parseAmFile(project); 
		}
		else if (name.equals("C_CREATE_CONFIGURE"))
		{
			autoconfManager.runSupportScript(project, status);
			refresh(project);
		}
		else if (name.equals("C_RUN_CONFIGURE"))
		{
			autoconfManager.runConfigureScript(project, status);
			refresh(project);
		}
		else if (name.equals("C_PROGRAMS_MAKEFILE_AM"))
		{
			autoconfManager.getMakeFileAmManager().setMakefileAmToPrograms(project.getFileObject(),status);
			refresh(project);
		}
		else if (name.equals("C_SWITCH_TO_STATIC_LIB"))
		{
			autoconfManager.getMakeFileAmManager().setMakefileAmToStaticLib(project.getFileObject(),status);
			refresh(project);
		}
		else if (name.equals("C_TOPLEVEL_MAKEFILE_AM"))
		{
			autoconfManager.getMakeFileAmManager().setMakefileAmToTopLevel(project,status);
			refresh(project);
		}
		else if (name.equals("C_SWITCH_TO_SHARED_LIB"))
		{
			autoconfManager.getMakeFileAmManager().setMakefileAmToSharedLib(project.getFileObject(),status);
			refresh(project);
		}
		else if (name.equals("C_INSERT_CONFIGURE_IN"))
		{
			autoconfManager.configureInManager.generateConfigureIn(project);
			refresh(project);
		}
		else if (name.equals("C_REFRESH"))
        {
		 	refreshProject(project);
  			parseAmFile(project); 
		}
		
  		status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
  		return status;
	}
	private DataElement parseAmFile(DataElement theUnmanagedProject)
	{
		AmParser theParser = new AmParser(theUnmanagedProject);
		DataElement theManagedProject = theParser.parse();
		return theManagedProject;
	}
	private DataElement refreshProject(DataElement theUnmanagedProject)
	{

		return null;
	}
}

