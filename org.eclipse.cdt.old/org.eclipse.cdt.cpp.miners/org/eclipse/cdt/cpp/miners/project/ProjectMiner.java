package com.ibm.cpp.miners.project;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;

import java.util.*;
import java.io.*;

public class ProjectMiner extends Miner
{
    private DataElement _workspace;

 public void load() 
    {
	_workspace = _dataStore.createObject(_minerData, getLocalizedString("project.Workspace"), getLocalizedString("project.Workspace"));
    }

    public void updateMinerInfo()
    {
	_dataStore.refresh(_minerData);
	_dataStore.refresh(_workspace);
    }

 
 public void extendSchema(DataElement schemaRoot)
 {
  DataElement markersD      = createObjectDescriptor(schemaRoot, getLocalizedString("project.markers"));
  createReference(markersD,   createObjectDescriptor(schemaRoot, getLocalizedString("project.error")));
  createReference(markersD,   createObjectDescriptor(schemaRoot, getLocalizedString("project.warning")));
  createReference(markersD,   createObjectDescriptor(schemaRoot, getLocalizedString("project.informational")));

  DataElement fsObjectD      = findDescriptor(getLocalizedString("project.FileSystemObjects"));
  DataElement workspaceD     = createObjectDescriptor(schemaRoot, getLocalizedString("project.Workspace"));
  DataElement projectD       = createObjectDescriptor(schemaRoot, getLocalizedString("project.Project"));
  DataElement closedProject  = createObjectDescriptor(schemaRoot, "Closed Project");
  DataElement remoteProjectD = createObjectDescriptor(schemaRoot, "Remote Project");
  DataElement closedRemoteProjectD = createObjectDescriptor(schemaRoot, "Closed Remote Project");
  DataElement projectFileD   = createObjectDescriptor(schemaRoot, getLocalizedString("project.ProjectFile"));
  DataElement projectsD      = createAbstractObjectDescriptor(schemaRoot, getLocalizedString("project.Projects"));

 
  createCommandDescriptor(projectD, "Close Project", "C_CLOSE_PROJECT");
  createCommandDescriptor(projectD, "Create Project", "C_CREATE_PROJECT").setDepth(0);

  createCommandDescriptor(closedProject, "Delete Project", "C_DELETE_PROJECT").setDepth(0);
  createCommandDescriptor(closedProject, "Open Project", "C_OPEN");
  //eateCommandDescriptor(closedRemoteProjectD, "Open Remote Project", "C_OPEN_REMOTE");
   
  
  createAbstractRelationship(fsObjectD, projectD);
  createAbstractRelationship(fsObjectD, workspaceD);
  createReference(projectsD,  projectD); 
  createReference(projectsD,  projectFileD); 
  createReference(projectsD,  fsObjectD); 
  createReference(workspaceD, projectsD);
 }
 
 
 public DataElement handleCommand(DataElement theCommand)
 {
  String          name = getCommandName(theCommand);
  DataElement   status = getCommandStatus(theCommand);
  DataElement  subject = getCommandArgument(theCommand, 0);
  String          type = subject.getType();
  
  if (!type.equals(getLocalizedString("project.Workspace")))
   if (type.indexOf("Project") < 0)
    if (!type.equals("directory"))
     return status;
  
  if (name.equals("C_OPEN"))
   handleOpenProject(subject, status);
  else if (name.equals("C_CLOSE_PROJECT"))
   handleCloseProject(subject, status);
  else if (name.equals("C_DELETE_PROJECT"))
      handleDeleteProject(subject, status);
  else if (name.equals("C_REFRESH"))
   handleRefreshProject(subject, status);
  else if (name.equals("C_CREATE_REMOTE"))
   handleCreateRemote(subject, status);
  else if (name.equals("C_OPEN_REMOTE"))
   handleOpenRemote(subject, status);
  else 
   return status;
  
  status.setAttribute(DE.A_NAME, "done");
  return status;
 }
 
 
 private DataElement findProject(String name)
 {
  return _dataStore.find(_workspace, DE.A_NAME, name, 1);
 }

 
 private void handleOpenRemote(DataElement project, DataElement status)
 {
  DataElement theProject = findProject(project.getName());
  if (theProject != null)
  {
   _dataStore.deleteObject(_workspace, theProject);
   _dataStore.refresh(_workspace);
  }
  
  createReference(_workspace, project);
  _dataStore.refresh(_workspace);
  
 }
 
 private void handleCreateRemote(DataElement project, DataElement status)
 {
  if (findProject(project.getName())!=null)
   return;
  _workspace.addNestedData(project,true);
  project.setParent(_workspace);
  _dataStore.refresh(project);
  _dataStore.refresh(_workspace);
 }
 

 private void handleOpenProject(DataElement project, DataElement status)
 {
  if (project.getType().equals("Closed Project"))
  {
   project.setAttribute(DE.A_TYPE, "Project");
   project.setDescriptor(null);
  }
  
  else
  {
   project.setParent(_workspace);
   _workspace.addNestedData(project, true);
  }
  
   project.expandChildren();
   DataElement refreshD = _dataStore.localDescriptorQuery(project.getDescriptor(), "C_REFRESH");
   _dataStore.command(refreshD, project);_dataStore.refresh(project);
   _dataStore.refresh(_workspace); 
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
  //DataElement refreshD = _dataStore.localDescriptorQuery(project.getDescriptor(), "C_REFRESH");
  // _dataStore.command(refreshD, project);
 } 


 public ResourceBundle getResourceBundle()  
 {
  try   { return ResourceBundle.getBundle(getName()); }
  catch (MissingResourceException mre) {}	  	
  return null;
 }
}





