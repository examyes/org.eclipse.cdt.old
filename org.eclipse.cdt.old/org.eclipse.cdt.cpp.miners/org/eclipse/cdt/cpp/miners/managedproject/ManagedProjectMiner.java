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
	private TargetManager targetManager;
	private DataElement _workspace = null;
	
	public void load() 
	{
		autoconfManager = new AutoconfManager();
		targetManager = new TargetManager();
	}
	public void extendSchema(DataElement schemaRoot)
	{
		DataElement projectD = _dataStore.find(schemaRoot, DE.A_NAME, "Project");
		DataElement fsObjectD = _dataStore.find(schemaRoot, DE.A_NAME, "Filesystem Objects");

		DataElement managedProjectD  = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, Am.MANAGED_PROJECT);
		//_dataStore.createReference(projectD, managedProjectD, "abstracts", "abstracted by");
		DataElement cmdD = _dataStore.localDescriptorQuery(projectD, "C_COMMAND");
		_dataStore.createReference(managedProjectD, cmdD);
		
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

		//createCommandDescriptor(managedProjectD, "New Target", "C_ADD_TARGET");
		createCommandDescriptor(targetD, "Build", "C_BUILD_TARGET",false);
		createCommandDescriptor(targetD, "Execute", "C_EXECUTE_TARGET",false);
		
		// autoconf	
		createCommandDescriptor(projectD, "Initialize Autoconf", "C_GENERATE_AUTOCONF_FILES", false);
		createCommandDescriptor(projectD, "All - configure.in and Makefile.am's", "C_UPDATE_AUTOCONF_FILES", false);
		//
		createCommandDescriptor(fsObjectD,"Makefile.am","C_UPDATE_MAKEFILE_AM",false);
		createCommandDescriptor(projectD,"configure.in","C_UPDATE_CONFIGURE_IN",false);		
		//
		
		createCommandDescriptor(projectD, "Create configure ", "C_CREATE_CONFIGURE",false);
		createCommandDescriptor(projectD, "Run configure", "C_RUN_CONFIGURE",false);
		createCommandDescriptor(projectD, "Initialize configure.in & Makefile.am's and createte & run configure", "C_MANAGE_PROJECT", false);
		createCommandDescriptor(projectD, "DistClean", "C_DIST_CLEAN", false);
		
		//
		createCommandDescriptor(fsObjectD,"Add/Change to TopLevel Makefile.am","C_TOPLEVEL_MAKEFILE_AM",false);
		createCommandDescriptor(fsObjectD,"Add/Change to Programs Makefile.am ","C_PROGRAMS_MAKEFILE_AM",false);		
		createCommandDescriptor(fsObjectD,"Add/Change to StaticLib Makefile.am","C_SWITCH_TO_STATIC_LIB",false);
		createCommandDescriptor(fsObjectD,"Add/Change to SharedLib Makefile.am","C_SWITCH_TO_SHARED_LIB",false);		
		createCommandDescriptor(fsObjectD,"Add configure.in file","C_INSERT_CONFIGURE_IN",false);
		//_dataStore.createReference(fsObjectD, makefileCmds);
		//
		createCommandDescriptor(managedProjectD,"TopLevel","C_TOPLEVEL_MAKEFILE_AM",false);
		createCommandDescriptor(managedProjectD,"Programs","C_PROGRAMS_MAKEFILE_AM",false);		
		createCommandDescriptor(managedProjectD,"StaticLib","C_SWITCH_TO_STATIC_LIB",false);
		createCommandDescriptor(managedProjectD,"SharedLib","C_SWITCH_TO_SHARED_LIB",false);
		//_dataStore.createReference(fsObjectD, makefileCmds);		
		
					
	}
	

	
	public DataElement getWorkspace()
	{
		return _workspace;
	}
	
	public DataElement handleCommand(DataElement theCommand)
 	{
  		String          name = getCommandName(theCommand);
  		DataElement   status = getCommandStatus(theCommand);
  		DataElement  subject = getCommandArgument(theCommand, 0);

	
		if (subject.getType().equals("Project") || subject.getType().equals("Closed Project"))
		{
			DataElement project = subject;
			
			if (_workspace == null)
			{
				_workspace = project.getParent();
				autoconfManager.setWorkspaceLocation(_workspace.getSource());
			}
	 
			if (name.equals("C_UNMANAGE_PROJECT"))
			{
 	 			_dataStore.deleteObject(project.getParent(), project);
			}
			else if (name.equals("C_MANAGE_PROJECT"))
			{
				autoconfManager.manageProject(project, status);
				parseAmFile(project); 
			}
			else if (name.equals("C_DIST_CLEAN"))
			{
				autoconfManager.distClean(project,status);
			}
			else if (name.equals("C_GENERATE_AUTOCONF_FILES"))
			{
				autoconfManager.generateAutoconfFiles(project, status,false);
				parseAmFile(project); 
			}
			else if (name.equals("C_UPDATE_AUTOCONF_FILES"))
			{
				autoconfManager.updateAutoconfFiles(project, status,false);
				parseAmFile(project); 
			}
			else if (name.equals("C_UPDATE_CONFIGURE_IN"))
			{
				autoconfManager.configureInManager.updateConfigureIn(project,false);
				parseAmFile(project); 
			}		
			else if (name.equals("C_CREATE_CONFIGURE"))
			{
				autoconfManager.runSupportScript(project, status);
			}
			else if (name.equals("C_RUN_CONFIGURE"))
			{
				autoconfManager.runConfigureScript(project, status);
			}
		}
		
		
		if (subject.getType().equals("directory") || subject.getType().equals("Project"))
		{
		 	if (name.equals("C_UPDATE_MAKEFILE_AM"))
			{
				autoconfManager.makefileAmManager.updateMakefileAm(subject,false);
				parseAmFile(subject); 
			}
			else if (name.equals("C_OPEN") || name.equals("C_REFRESH"))
			    {
		
				//parseAmFile(subject); 
			    }
		}
			
		if (subject.getType().equals("directory") || 
			subject.getType().equals("Project") ||
			subject.getType().equals("Managed Project"))
		{	
			if (name.equals("C_PROGRAMS_MAKEFILE_AM"))
			{
				autoconfManager.getMakeFileAmManager().setMakefileAmToPrograms(subject.getFileObject(),status);
			}
			else if (name.equals("C_SWITCH_TO_STATIC_LIB"))
			{
				autoconfManager.getMakeFileAmManager().setMakefileAmToStaticLib(subject.getFileObject(),status);
			}
			else if (name.equals("C_TOPLEVEL_MAKEFILE_AM"))
			{
				autoconfManager.getMakeFileAmManager().setMakefileAmToTopLevel(subject,status);
			}
			else if (name.equals("C_SWITCH_TO_SHARED_LIB"))
			{
				autoconfManager.getMakeFileAmManager().setMakefileAmToSharedLib(subject.getFileObject(),status);
			}
			else if (name.equals("C_INSERT_CONFIGURE_IN"))
			{
				autoconfManager.configureInManager.generateConfigureIn(subject);
			}
		}
		
		if (subject.getType().equals("Project Target"))
		{		
			if (name.equals("C_BUILD_TARGET"))
			{
				targetManager.buildTarget(subject,status,autoconfManager);
			}
			else if (name.equals("C_EXECUTE_TARGET"))
			{
				targetManager.executeTarget(subject,status,_workspace.getSource());
			}
		}
		

		
  		status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
  		return status;
	}
	private DataElement parseAmFile(DataElement theUnmanagedProject)
	{
		DataElement theManagedProject = null;
		AmParser theParser = null;
		if (theUnmanagedProject.getType().equals("Project"))
		{
			theParser = new AmParser(theUnmanagedProject);
			theManagedProject = theParser.parse();
			}
		else if (theUnmanagedProject.getType().equals("directory"))
		{
			theParser = new AmParser(theUnmanagedProject.getParent(), theUnmanagedProject.getName());
			theManagedProject = theParser.parse();
			_dataStore.refresh(theManagedProject);
		}
	    return null;
	}

}

