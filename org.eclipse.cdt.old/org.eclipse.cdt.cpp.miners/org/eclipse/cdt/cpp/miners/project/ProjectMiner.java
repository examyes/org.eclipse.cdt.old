package org.eclipse.cdt.cpp.miners.project;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.miners.miner.*;

import java.util.*;
import java.io.*;

public class ProjectMiner extends Miner
{
    private DataElement _workspace;

    public void load() 
    {
	_workspace = _dataStore.createObject(_minerData, getLocalizedString("project.Workspace"), 
					     "Workspace");
    }
    
    public void updateMinerInfo()
    {
	_dataStore.refresh(_minerData);
	_dataStore.refresh(_workspace);
    }

    protected ArrayList getDependencies()
    {
	ArrayList dependencies = new ArrayList();
	dependencies.add("org.eclipse.cdt.dstore.miners.filesystem.FileSystemMiner");
	return dependencies;
    }
    
 
    public void extendSchema(DataElement schemaRoot)
    {
	DataElement containerObjectD = findDescriptor(getLocalizedString("model.Container_Object"));
	DataElement contentsD        = findDescriptor(getLocalizedString("model.contents"));
	DataElement allD             = findDescriptor(getLocalizedString("model.all"));
	DataElement fsObjectD      = findDescriptor(getLocalizedString("project.FileSystemObjects"));
	DataElement directoryD     = findDescriptor(getLocalizedString("model.directory"));
	DataElement fileD          = findDescriptor(getLocalizedString("model.file"));



	DataElement markersD      = createObjectDescriptor(schemaRoot, getLocalizedString("project.markers"));
	createReference(markersD,   createObjectDescriptor(schemaRoot, getLocalizedString("project.error")));
	createReference(markersD,   createObjectDescriptor(schemaRoot, getLocalizedString("project.warning")));
	createReference(markersD,   createObjectDescriptor(schemaRoot, getLocalizedString("project.informational")));	

	DataElement workspaceD     = createObjectDescriptor(schemaRoot, getLocalizedString("project.Workspace"));
	DataElement projectD       = createObjectDescriptor(schemaRoot, getLocalizedString("project.Project"));
	DataElement closedProject  = createObjectDescriptor(schemaRoot, "Closed Project");
	DataElement projectFileD   = createObjectDescriptor(schemaRoot, getLocalizedString("project.ProjectFile"));
	DataElement projectsD      = createAbstractObjectDescriptor(schemaRoot, getLocalizedString("project.Projects"));
	DataElement oprojectsD      = createAbstractObjectDescriptor(schemaRoot, "Open Projects");
	DataElement pContainersD   = createAbstractObjectDescriptor(schemaRoot, "Project Containers");

	DataElement propertiesRootD  = createObjectDescriptor(schemaRoot, getLocalizedString("project.Properties_Root"));
	DataElement propertiesD  = createRelationDescriptor(schemaRoot, getLocalizedString("project.properties"));
	DataElement pathD     = createObjectDescriptor(schemaRoot, getLocalizedString("project.path"));
	
		
	projectsD.setDepth(100);
	projectD.setDepth(100);
			
	DataElement closeD = createCommandDescriptor(projectD, "Close Project", "C_CLOSE_PROJECT");
	closeD.setDepth(0);
	closeD.setAttribute(DE.A_SOURCE, "*");
	
	DataElement closeAllD = createCommandDescriptor(workspaceD, "Close Projects", "C_CLOSE_PROJECTS");
	closeAllD.setDepth(0);
	closeAllD.setAttribute(DE.A_SOURCE, "*");

	
	createCommandDescriptor(projectD, "Create Project", "C_CREATE_PROJECT", false);	
	createCommandDescriptor(closedProject, "Delete Project", "C_DELETE_PROJECT", false);
	DataElement openD = createCommandDescriptor(closedProject, "Open Project", "C_OPEN", false);
	openD.setAttribute(DE.A_SOURCE, "*");

	createCommandDescriptor(projectD, "Set Paths", "C_SET_PATHS", false);
	_dataStore.createReference(projectD, propertiesD, contentsD);
	_dataStore.createReference(projectD, propertiesRootD, propertiesD);
	_dataStore.createReference(propertiesRootD, contentsD, contentsD);
	_dataStore.createReference(propertiesRootD, pathD, contentsD);
	_dataStore.createReference(pathD, contentsD, contentsD);
	_dataStore.createReference(pathD, pathD, contentsD);
	_dataStore.createReference(pathD, fileD, contentsD);
	_dataStore.createReference(pathD, allD, contentsD);
		
	createAbstractRelationship(fsObjectD, projectD);
	_dataStore.createReference(containerObjectD, workspaceD, "abstracts", "abstracted by");
	
	_dataStore.createReference(projectsD,  projectD, "abstracts", "abstracted by"); 
	_dataStore.createReference(projectsD,  closedProject, "abstracts", "abstracted by"); 
	_dataStore.createReference(projectsD,  projectFileD, "abstracts", "abstracted by"); 
	_dataStore.createReference(projectsD,  directoryD, "abstracts", "abstracted by"); 
	_dataStore.createReference(projectsD,  fileD, "abstracts", "abstracted by"); 
	
	_dataStore.createReference(oprojectsD,  projectD, "abstracts", "abstracted by"); 
	_dataStore.createReference(oprojectsD,  projectFileD, "abstracts", "abstracted by"); 
	_dataStore.createReference(oprojectsD,  directoryD, "abstracts", "abstracted by"); 
	_dataStore.createReference(oprojectsD,  fileD, "abstracts", "abstracted by"); 
	
	_dataStore.createReference(pContainersD, projectD, "abstracts", "abstracted by");
	_dataStore.createReference(pContainersD, closedProject, "abstracts", "abstracted by");
	_dataStore.createReference(pContainersD, directoryD, "abstracts", "abstracted by");
	
	
	_dataStore.createReference(workspaceD, contentsD, contentsD);
	createReference(workspaceD, projectsD);
	createReference(workspaceD, oprojectsD);
	
	projectsD.setDepth(100); 
    }
    
    
    public DataElement handleCommand(DataElement theCommand)
    {
	String          name = getCommandName(theCommand);
	DataElement   status = getCommandStatus(theCommand);
	DataElement  subject = getCommandArgument(theCommand, 0);
	String          type = subject.getType();
	
	if (name.equals("C_OPEN"))
	    handleOpenProject(subject, status);
	else if (name.equals("C_CLOSE_PROJECT"))
	    handleCloseProject(subject, status);
	else if (name.equals("C_DELETE_PROJECT"))
	    handleDeleteProject(subject, status);
	else if (name.equals("C_REFRESH"))
	    handleRefreshProject(subject, status);
	else if (name.equals("C_SET_PATHS"))
	    handleSetPaths(subject, 
			   getCommandArgument(theCommand, 1),
			   getCommandArgument(theCommand, 2),
			   getCommandArgument(theCommand, 3),
			   status);
	else 
	    return status;
	
	status.setAttribute(DE.A_NAME, "done");
	return status;
    }
    
    
    private DataElement findProject(String name)
    {
	return _dataStore.find(_workspace, DE.A_NAME, name, 1);
    }
    
    
 private void handleOpenProject(DataElement project, DataElement status)
    {
	if (project.getType().equals("Closed Project"))
	    {
		project.setAttribute(DE.A_TYPE, "Project");
		project.setDescriptor(null);
		_workspace.addNestedData(project, true);
		project.setParent(_workspace);
	    }  
	_dataStore.refresh(_workspace);
	
	status.setAttribute(DE.A_NAME, "done");
	_dataStore.refresh(status);
    }
    
    private void handleCloseProject(DataElement project, DataElement status)
    {
	DataElement workspace = project.getParent();
	_dataStore.deleteObjects(project);
	
	project.setAttribute(DE.A_TYPE, "Closed Project");
	project.setDescriptor(null);
	_dataStore.refresh(project);
	_dataStore.refresh(workspace);
    }
    
    private void handleDeleteProject(DataElement project, DataElement status)
    {
	DataElement workspace = project.getParent();
	_dataStore.deleteObject(workspace, project);
	_dataStore.refresh(workspace);
    }
    
    private void handleRefreshProject(DataElement project, DataElement status)
    {
    } 
    

    private void handleSetPaths(DataElement project, DataElement includePath, 
				DataElement externalSource, DataElement libraries, DataElement status)
    {
	DataElement propertiesRoot = null; 
	ArrayList properties = project.getAssociated(getLocalizedString("project.properties"));
	if (properties != null && properties.size() > 0)
	    {
		propertiesRoot = (DataElement)properties.get(0);
	    }
	else
	    {
		// create properties root
		propertiesRoot = _dataStore.createObject(null, getLocalizedString("project.Properties_Root"), "Project Properties");
		_dataStore.createReference(project, propertiesRoot, getLocalizedString("project.properties"));
	    }
	
	// find old path properties
	DataElement oldIncludePath = _dataStore.find(propertiesRoot, DE.A_NAME, "Include Path", 1);
	DataElement oldExternalSource = _dataStore.find(propertiesRoot, DE.A_NAME, "External Source", 1);
	DataElement oldLibraries = _dataStore.find(propertiesRoot, DE.A_NAME, "Libraries", 1);

       	_dataStore.deleteObjects(propertiesRoot);
	propertiesRoot.addNestedData(includePath, false);
	propertiesRoot.addNestedData(externalSource, false);
	propertiesRoot.addNestedData(libraries, false);
	
	_dataStore.refresh(propertiesRoot);
    }
    
    public ResourceBundle getResourceBundle()  
    {
	try   { return ResourceBundle.getBundle(getName()); }
	catch (MissingResourceException mre) {}	  	
	return null;
    }
}





