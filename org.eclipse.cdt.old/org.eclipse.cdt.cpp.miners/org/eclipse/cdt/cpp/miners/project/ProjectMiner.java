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

 
 public void extendSchema(DataElement schemaRoot)
 {
  DataElement markersD      = createObjectDescriptor(schemaRoot, getLocalizedString("project.markers"));
  createReference(markersD,   createObjectDescriptor(schemaRoot, getLocalizedString("project.error")));
  createReference(markersD,   createObjectDescriptor(schemaRoot, getLocalizedString("project.warning")));
  createReference(markersD,   createObjectDescriptor(schemaRoot, getLocalizedString("project.informational")));

  DataElement fsObjectD      = findDescriptor(getLocalizedString("project.FileSystemObjects"));
  DataElement directoryD     = findDescriptor(getLocalizedString("model.directory"));
  DataElement fileD          = findDescriptor(getLocalizedString("model.file"));
  DataElement workspaceD     = createObjectDescriptor(schemaRoot, getLocalizedString("project.Workspace"));
  DataElement projectD       = createObjectDescriptor(schemaRoot, getLocalizedString("project.Project"));
  DataElement closedProject  = createObjectDescriptor(schemaRoot, "Closed Project");
  DataElement projectFileD   = createObjectDescriptor(schemaRoot, getLocalizedString("project.ProjectFile"));
  DataElement projectsD      = createAbstractObjectDescriptor(schemaRoot, getLocalizedString("project.Projects"));
  DataElement oprojectsD      = createAbstractObjectDescriptor(schemaRoot, "Open Projects");
  DataElement pContainersD   = createAbstractObjectDescriptor(schemaRoot, "Project Containers");
  DataElement containerObjectD = findDescriptor(getLocalizedString("model.Container_Object"));
 
  projectsD.setDepth(100);
  projectD.setDepth(100);

 	
 
  DataElement closeD = createCommandDescriptor(projectD, "Close Project", "C_CLOSE_PROJECT");
  closeD.setDepth(0);
  closeD.setAttribute(DE.A_SOURCE, "*");


 createCommandDescriptor(projectD, "Create Project", "C_CREATE_PROJECT").setDepth(0);

  createCommandDescriptor(closedProject, "Delete Project", "C_DELETE_PROJECT").setDepth(0);
  DataElement openD = createCommandDescriptor(closedProject, "Open Project", "C_OPEN");
  openD.setDepth(0);
  openD.setAttribute(DE.A_SOURCE, "*");
  
  
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
    

    createReference(workspaceD, projectsD);
    createReference(workspaceD, oprojectsD);
    createReference(workspaceD, pContainersD);
    
  projectsD.setDepth(100); 
 }
 
 
 public DataElement handleCommand(DataElement theCommand)
 {
  String          name = getCommandName(theCommand);
  DataElement   status = getCommandStatus(theCommand);
  DataElement  subject = getCommandArgument(theCommand, 0);
  String          type = subject.getType();

  /*  
  if (!type.equals(getLocalizedString("project.Workspace")))
   if (type.indexOf("Project") < 0)
    if (!type.equals("directory"))
     return status;
  */

  if (name.equals("C_OPEN"))
      handleOpenProject(subject, status);
  else if (name.equals("C_CLOSE_PROJECT"))
   handleCloseProject(subject, status);
  else if (name.equals("C_DELETE_PROJECT"))
      handleDeleteProject(subject, status);
  else if (name.equals("C_REFRESH"))
   handleRefreshProject(subject, status);
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
  else if  (project.getType().equals("Project"))
  {
   project.setParent(_workspace);
   _workspace.addNestedData(project, true);
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





