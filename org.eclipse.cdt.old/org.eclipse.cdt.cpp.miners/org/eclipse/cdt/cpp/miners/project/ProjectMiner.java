package com.ibm.cpp.miners.project;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;

import java.util.*;
import java.io.*;

public class ProjectMiner extends Miner
{
    public void load() 
    {
	DataElement workspace = _dataStore.createObject(_minerData, "Workspace", "Workspace");
	/*  
	//Create some dummy content:
	String fileLocation = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + "/com.ibm.cpp.miners/sample";
	
	DataElement project1  = _dataStore.createObject(workspace, "Project", "MyProject", fileLocation + File.separator + "Makefile.am");
	DataElement project2  = _dataStore.createObject(workspace, "Project", "Another Project");
	_dataStore.createObject(project2, "Project File", "Some File");
	
	
	_dataStore.createObject(project1, "Project File", "aux.cpp");
	_dataStore.createObject(project1, "Project File", "aux.h");
	_dataStore.createObject(project1, "Project File", "bye.c");
	_dataStore.createObject(project1, "Project File", "bye.h");
	_dataStore.createObject(project1, "Project File", "hello.cpp");
	_dataStore.createObject(project1, "Project File", "Makefile.am", fileLocation + File.separator + "Makefile.am");
	
	DataElement utilDirectory = _dataStore.createObject(project1, "directory", "util");
	_dataStore.createObject(utilDirectory, "Project File", "Makefile.am", fileLocation + File.separator + "util" + File.separator + "Makefile.am");  
	_dataStore.createObject(utilDirectory, "Project File", "util.c");
	_dataStore.createObject(utilDirectory, "Project File", "util.h");
	_dataStore.createObject(utilDirectory, "Project File", "fooX.o");
	*/		
    }
 
    public void extendSchema(DataElement schemaRoot)
    {
	DataElement workspaceD    = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Workspace");
	DataElement projectD      = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "CProject");
	DataElement projectFileD  = _dataStore.createObject(schemaRoot, DE.T_OBJECT_DESCRIPTOR, "Project File");
	DataElement projectsD     = _dataStore.createObject(schemaRoot, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, "CProjects");

	DataElement fsD           = _dataStore.find(schemaRoot, DE.A_NAME, "Filesystem Objects", 1);

	_dataStore.createReference(fsD, projectD,  
				   getLocalizedString("model.abstracts"), getLocalizedString("model.abstracted_by"));
	
	
	_dataStore.createReference(projectsD, projectD);
	_dataStore.createReference(projectsD, projectFileD);
	_dataStore.createReference(projectsD, fsD);
	_dataStore.createReference(workspaceD, projectsD);
	
	
	DataElement openProjectD  = createCommandDescriptor(workspaceD, "Open Project", "C_OPEN_PROJECT", false);
	DataElement closeProjectD = createCommandDescriptor(projectD, "Close Project", "C_CLOSE_PROJECT", false);
	DataElement refreshProjectD = createCommandDescriptor(projectD, "Refresh Project", "C_REFRESH_PROJECT", false);
    }
 
    public DataElement handleCommand(DataElement theCommand)
    {
	String name          = getCommandName(theCommand);
	DataElement  status  = getCommandStatus(theCommand);
	DataElement  subject = getCommandArgument(theCommand, 0);
	
	if (name.equals("C_OPEN_PROJECT"))
	    {
		handleCreateProject(subject, getCommandArgument(theCommand, 1), status);
	    }
	else if (name.equals("C_CLOSE_PROJECT"))
	    {
		handleCloseProject(subject, status);
	    }
	else if (name.equals("C_REFRESH_PROJECT"))
	    {
		handleRefreshProject(subject, status);
	    }
	
	status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
	return status;
    }
    
    
    private void handleCreateProject(DataElement workspace, DataElement project, DataElement status)
    {
	project.setParent(workspace);
	workspace.addNestedData(project, true);

	project.expandChildren();
	_dataStore.refresh(workspace);
    }
    
    private void handleCloseProject(DataElement project, DataElement status)
    {
	DataElement workspace = project.getParent();
	//	_dataStore.deleteObject(workspace, project);
	workspace.removeNestedData(project);
       	_dataStore.refresh(workspace);
    }

    private void handleRefreshProject(DataElement project, DataElement status)
    {
	DataElement refreshD = _dataStore.localDescriptorQuery(project.getDescriptor(), "C_REFRESH");
	_dataStore.command(refreshD, project);
    } 
    
    
}
