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

  DataElement fsObjectD     = findDescriptor(getLocalizedString("project.FileSystemObjects"));
  DataElement workspaceD    = createObjectDescriptor(schemaRoot, getLocalizedString("project.Workspace"));
  DataElement projectD      = createObjectDescriptor(schemaRoot, getLocalizedString("project.Project"));
  DataElement projectFileD  = createObjectDescriptor(schemaRoot, getLocalizedString("project.ProjectFile"));
  DataElement projectsD     = createAbstractObjectDescriptor(schemaRoot, getLocalizedString("project.Projects"));

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
 
  System.out.println(theCommand + " " + subject);
  
  if (!subject.getType().equals(getLocalizedString("project.Project")))
   if (!subject.getType().equals(getLocalizedString("project.Workspace")))
    return status;
  
  if (name.equals("C_OPEN"))
   handleOpenProject(subject, getCommandArgument(theCommand, 1), status);
  else if (name.equals("C_CLOSE"))
   handleCloseProject(subject, status);
  else if (name.equals("C_REFRESH"))
   handleRefreshProject(subject, status);
  else 
   return status;
  
  status.setAttribute(DE.A_NAME, "done");
  return status;
 }
    
    
 private void handleOpenProject(DataElement project, DataElement hmm, DataElement status)
 {
     project.setParent(_workspace);
     _workspace.addNestedData(project, true);
     
     project.expandChildren();
     _dataStore.refresh(_workspace);     
 }
    
 private void handleCloseProject(DataElement project, DataElement status)
 {
  DataElement workspace = project.getParent();
  workspace.removeNestedData(project);
  _dataStore.refresh(workspace);
 }

 private void handleRefreshProject(DataElement project, DataElement status)
 {
  DataElement refreshD = _dataStore.localDescriptorQuery(project.getDescriptor(), "C_REFRESH");
  _dataStore.command(refreshD, project);
 } 


 public ResourceBundle getResourceBundle()  
 {
  try   { return ResourceBundle.getBundle(getName()); }
  catch (MissingResourceException mre) {}	  	
  return null;
 }
}





