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
	// classification constants
	private MakefileAmClassifier classifier;
	private final String TOPLEVEL = "1";
	private final String PROGRAMS = "2";
	private final String STATICLIB = "3";
	private final String SHAREDLIB = "4";
	private String _workspaceLocation = "";
	
	public void load() 
	{
		autoconfManager = new AutoconfManager();
		targetManager = new TargetManager();
	}
	
	public void extendSchema(DataElement schemaRoot)
	{
		
		DataElement contObjD = _dataStore.find(schemaRoot, DE.A_NAME, "Container Object");
		
		DataElement projectD = _dataStore.find(schemaRoot, DE.A_NAME, "Project");
		DataElement fsObjectD = _dataStore.find(schemaRoot, DE.A_NAME, "Filesystem Objects");

		DataElement managedProjectD  = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, Am.MANAGED_PROJECT);
		_dataStore.createReference(contObjD, managedProjectD, "abstracts", "abstracted by");
		DataElement cmdD = _dataStore.localDescriptorQuery(projectD, "C_COMMAND");
		_dataStore.createReference(managedProjectD, cmdD);
		
		createCommandDescriptor(fsObjectD, "Check Update State", "C_CHECK_UPDATE_STATE", false);
		createCommandDescriptor(fsObjectD, "Classify Makefile", "C_CLASSIFY_MAKEFILE_AM", false);
		
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
  
		//******_dataStore.createReference(workspaceD, managedProjectsD); // disable this for now

		//createCommandDescriptor(managedProjectD, "New Target", "C_ADD_TARGET");
		createCommandDescriptor(targetD, "Build", "C_BUILD_TARGET", false);
		createCommandDescriptor(targetD, "Execute", "C_EXECUTE_TARGET", false);
		
		// autoconf	
		createCommandDescriptor(projectD, "Updating all - configure.in and Makefile.am's - missing files will be generated", "C_UPDATE_AUTOCONF_FILES", false);
		//
		createCommandDescriptor(fsObjectD,"Updating Makefile.am","C_UPDATE_MAKEFILE_AM",false);
		createCommandDescriptor(projectD,"Updating configure.in","C_UPDATE_CONFIGURE_IN",false);		
		//
		
		createCommandDescriptor(projectD, "Creating configure script", "C_CREATE_CONFIGURE",false);
		createCommandDescriptor(projectD, "Creating configure without updating", "C_CREATE_CONFIGURE_NO_UPDATE",false);
		createCommandDescriptor(projectD, "Running configure script - creating configure if needed", "C_RUN_CONFIGURE",false);
		createCommandDescriptor(projectD, "Running configure script without updating", "C_RUN_CONFIGURE_NO_UPDATE",false);
		createCommandDescriptor(projectD, "Cleaning package for distribution", "C_DIST_CLEAN", false);
		createCommandDescriptor(projectD, "maintainer-clean - recommended for package developer", "C_MAINTAINER_CLEAN", false);
		createCommandDescriptor(projectD, "make-install", "C_INSTALL", false);
		
		//
		createCommandDescriptor(fsObjectD,"Creating TopLevel Makefile.am - existing file will be renamed *.old","C_TOPLEVEL_MAKEFILE_AM",false);
		createCommandDescriptor(fsObjectD,"Creating Programs Makefile.am - existing file will be renamed *.old","C_PROGRAMS_MAKEFILE_AM",false);		
		createCommandDescriptor(fsObjectD,"Creating StaticLib Makefile.am - existing file will be renamed *.old","C_STATICLIB_MAKEFILE_AM",false);
		createCommandDescriptor(fsObjectD,"Creating SharedLib Makefile.am - existing file will be renamed *.old","C_SHAREDLIB_MAKEFILE_AM",false);		
		createCommandDescriptor(fsObjectD,"Adding configure.in file","C_INSERT_CONFIGURE_IN",false);
		createCommandDescriptor(fsObjectD,"Defining Compiler Flags","C_COMPILER_FLAGS",false);
		//_dataStore.createReference(fsObjectD, makefileCmds);
		//
		createCommandDescriptor(managedProjectD,"TopLevel","C_TOPLEVEL_MAKEFILE_AM",false);
		createCommandDescriptor(managedProjectD,"Programs","C_PROGRAMS_MAKEFILE_AM",false);		
		createCommandDescriptor(managedProjectD,"StaticLib","C_STATICLIB_MAKEFILE_AM",false);
		createCommandDescriptor(managedProjectD,"SharedLib","C_SHAREDLIB_MAKEFILE_AM",false);
		//_dataStore.createReference(fsObjectD, makefileCmds);		
		
		 createRelationDescriptor(schemaRoot, "class type");			
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
				
				// classifying Makefile.am
				String location = _workspace.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH);
				classifier = new MakefileAmClassifier(location);
				
				_workspaceLocation = _workspace.getSource();
				autoconfManager.setWorkspaceLocation(_workspaceLocation);
			}
	 
			if (name.equals("C_UNMANAGE_PROJECT"))
			{
 	 			_dataStore.deleteObject(project.getParent(), project);
 	 			//project.getParent().removeNestedData();
			}
			else if (name.equals("C_UPDATE_AUTOCONF_FILES"))
			{
				autoconfManager.updateAutoconfFiles(project, status,false,classifier);
				project.refresh(false);
				//parseAmFile(project); 
			}
			else if (name.equals("C_CHECK_UPDATE_STATE"))
			{
				// do whatever it takes to figure out whether we're updated or not
				status.setAttribute(DE.A_NAME, "done");
				DataElement state = _dataStore.createObject(status, "state", "uptodate");
				_dataStore.refresh(status);	
			}
			else if (name.equals("C_UPDATE_CONFIGURE_IN"))
			{
				autoconfManager.configureInManager.updateConfigureIn(project,false);
				project.refresh(false);
				//parseAmFile(project); 
			}		
			else if (name.equals("C_CREATE_CONFIGURE"))
			{
				autoconfManager.createConfigure(project, status,true,classifier);
				project.refresh(false);
			}
			else if (name.equals("C_CREATE_CONFIGURE_NO_UPDATE"))
			{
				autoconfManager.createConfigure(project, status,false,classifier);
				project.refresh(false);
			}
			else if (name.equals("C_RUN_CONFIGURE"))
			{
				autoconfManager.runConfigure(project, status,true,classifier);
				project.refresh(false);
			}
			else if (name.equals("C_RUN_CONFIGURE_NO_UPDATE"))
			{
				autoconfManager.runConfigure(project, status,false,classifier);
				project.refresh(false);
			}
			else if (name.equals("C_DIST_CLEAN"))
			{
				autoconfManager.distClean(project,status);
				project.refresh(false);
			}
			else if (name.equals("C_INSTALL"))
			{
				autoconfManager.install(project,status);
				project.refresh(false);
			}
			else if (name.equals("C_MAINTAINER_CLEAN"))
			{
				autoconfManager.maintainerClean(project,status);
				project.refresh(false);
			}

		}
		
		
		if (subject.getType().equals("directory") || subject.getType().equals("Project"))
		{	
		 	if (name.equals("C_UPDATE_MAKEFILE_AM"))
			{
				autoconfManager.makefileAmManager.updateMakefileAm(subject,false,classifier);
				subject.refresh(false);
				//parseAmFile(subject); 
			}
			else if (name.equals("C_REFRESH"))
			{
				//parseAmFile(subject); 
			}
			else if (name.equals("C_OPEN"))
			{
				//parseAmFile(subject);
			}
			else if (name.equals("C_CLASSIFY_MAKEFILE_AM"))
			{
				File makefileAm = getMakefileAm(subject.getFileObject());
				if(makefileAm!=null)
				{	
					String classification = getMakefileClassification(makefileAm);		
					//DataElement state = _dataStore.createObject(status, "classification", classification);
					//_dataStore.refresh(status);
					
					
					DataElement state = null;
					ArrayList updated = subject.getAssociated("class type");
					if (updated.size() > 0)
					{
						state = (DataElement)updated.get(0);	
						state.setAttribute(DE.A_NAME, classification);
					}
					else
					{							
						state = _dataStore.createObject(null, "classification", classification);
						_dataStore.createReference(subject, state, "class type");	
					}


					_dataStore.refresh(status);
					_dataStore.refresh(subject);
					status.setAttribute(DE.A_NAME, "done");
					
				}	
			}
			
		}
			
		if (subject.getType().equals("directory") || 
			subject.getType().equals("Project") ||
			subject.getType().equals("Managed Project"))
		{	
			if (name.equals("C_PROGRAMS_MAKEFILE_AM"))
			{
				autoconfManager.getMakeFileAmManager().setMakefileAmToPrograms(subject.getFileObject(),status,classifier);
				
				ArrayList updated = subject.getAssociated("class type");
				if (updated.size() > 0)
				{
					DataElement state = (DataElement)updated.get(0);	
					state.setAttribute(DE.A_NAME, "2");
					_dataStore.refresh(state);
				}
				subject.refresh(false);
			}
			else if (name.equals("C_STATICLIB_MAKEFILE_AM"))
			{
				autoconfManager.getMakeFileAmManager().setMakefileAmToStaticLib(subject.getFileObject(),status,classifier);
			
					ArrayList updated = subject.getAssociated("class type");
				if (updated.size() > 0)
				{
					DataElement state = (DataElement)updated.get(0);	
					state.setAttribute(DE.A_NAME, "4");
					_dataStore.refresh(state);
				}
			
				subject.refresh(false);
			}
			else if (name.equals("C_TOPLEVEL_MAKEFILE_AM"))
			{
				autoconfManager.getMakeFileAmManager().setMakefileAmToTopLevel(subject,status,classifier);
				ArrayList updated = subject.getAssociated("class type");
				if (updated.size() > 0)
				{
					DataElement state = (DataElement)updated.get(0);	
					state.setAttribute(DE.A_NAME, "1");
					_dataStore.refresh(state);
				}
				
				subject.refresh(false);
			}
			else if (name.equals("C_SHAREDLIB_MAKEFILE_AM"))
			{
				autoconfManager.getMakeFileAmManager().setMakefileAmToSharedLib(subject.getFileObject(),status,classifier);
				
				ArrayList updated = subject.getAssociated("class type");
				if (updated.size() > 0)
				{
					DataElement state = (DataElement)updated.get(0);	
					state.setAttribute(DE.A_NAME, "3");
					_dataStore.refresh(state);
				}
				subject.refresh(false);
			}
			else if (name.equals("C_INSERT_CONFIGURE_IN"))
			{
				autoconfManager.configureInManager.generateConfigureIn(subject);
				subject.refresh(false);
			}
		/*	else if (name.equals("C_COMPILER_FLAGS"))
			{
				autoconfManager.makefileAmManager.defineCompilerFlags(subject);
			}*/
		}
		
		if (subject.getType().equals("Project Target"))
		{		
			if (name.equals("C_BUILD_TARGET"))
			{
				targetManager.buildTarget(subject,status,autoconfManager);
				subject.refresh(false);
			}
			else if (name.equals("C_EXECUTE_TARGET"))
			{
				targetManager.executeTarget(subject,status,_workspace.getSource());
				subject.refresh(false);
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
			//_dataStore.refresh(theManagedProject);
		}
		else if (theUnmanagedProject.getType().equals("directory"))
		{
			theParser = new AmParser(theUnmanagedProject.getParent(), theUnmanagedProject.getName());
			theManagedProject = theParser.parse();
			_dataStore.refresh(theManagedProject);
		}
	    return null;
	}
	private File getMakefileAm(File dir)
	{
		File[] list = dir.listFiles();
		for(int i = 0; i < list.length; i++)
			if(list[i].getName().equals("Makefile.am"))
				return list[i];
		return null;
	}
	private String getMakefileClassification(File makefileAm)
	{
		int classification = classifier.classify(makefileAm);
		Integer classifier = new Integer(classification);
		return new String(classifier.toString());
	}
}

	

