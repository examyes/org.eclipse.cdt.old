package org.eclipse.cdt.cpp.ui.internal.api;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.util.*;


public class CppSchemaExtender implements ISchemaExtender
{
    private ExternalLoader _loader;

    public CppSchemaExtender()
    {
	_loader = new ExternalLoader(CppPlugin.getDefault().getDescriptor().getPluginClassLoader(), 
				     "org.eclipse.cdt.cpp.ui.*");
    }

    public ExternalLoader getExternalLoader()
    {
	return _loader;
    }

    public void extendSchema(DataElement schemaRoot)
    {
	DataStore   dataStore = schemaRoot.getDataStore();
	DataElement fsD   = dataStore.find(schemaRoot, DE.A_NAME, "Filesystem Objects", 1);
	DataElement dirD = dataStore.find(schemaRoot, DE.A_NAME, "directory", 1);
	DataElement rootD = dataStore.find(schemaRoot, DE.A_NAME, "root", 1);
	DataElement fileD    = dataStore.find(schemaRoot, DE.A_NAME, "file",1);	
	DataElement exeD    = dataStore.find(schemaRoot, DE.A_NAME, "binary executable",1);	
	DataElement projectD = dataStore.find(schemaRoot, DE.A_NAME, "Project", 1);
	DataElement closedProjectD = dataStore.find(schemaRoot, DE.A_NAME, "Closed Project", 1);

	DataElement targetD = dataStore.find(schemaRoot,DE.A_NAME, "Project Target",1);

	DataElement statement = dataStore.find(schemaRoot, DE.A_NAME, "Statements", 1);
	DataElement function  = dataStore.find(schemaRoot, DE.A_NAME, "Functions", 1);
	DataElement classD    = dataStore.find(schemaRoot, DE.A_NAME, "class", 1);
	DataElement processD    = dataStore.find(schemaRoot, DE.A_NAME, "Process", 1);

	
	// project actions

	DataElement openProject = dataStore.createObject(closedProjectD, DE.T_UI_COMMAND_DESCRIPTOR,
							 "Open Project",
							 "org.eclipse.cdt.cpp.ui.internal.actions.OpenProjectAction");

	DataElement build = dataStore.createObject(projectD, DE.T_UI_COMMAND_DESCRIPTOR,
						   "Build Project",
						   "org.eclipse.cdt.cpp.ui.internal.actions.BuildAction");
	build.setAttribute(DE.A_VALUE, "BUILD");

	DataElement clean = dataStore.createObject(projectD, DE.T_UI_COMMAND_DESCRIPTOR,
						   "Clean Project",
						   "org.eclipse.cdt.cpp.ui.internal.actions.BuildAction");
	clean.setAttribute(DE.A_VALUE, "CLEAN");


	DataElement closeProject = dataStore.createObject(projectD, DE.T_UI_COMMAND_DESCRIPTOR,
							 "Close Project",
							 "org.eclipse.cdt.cpp.ui.internal.actions.CloseProjectAction");

	DataElement deleteProject = dataStore.createObject(closedProjectD, DE.T_UI_COMMAND_DESCRIPTOR,
							 "Delete Project",
							 "org.eclipse.cdt.cpp.ui.internal.actions.DeleteProjectAction");
	dataStore.createReference(projectD, deleteProject);



	
	DataElement openFile = dataStore.createObject(fileD, DE.T_UI_COMMAND_DESCRIPTOR,
							 "Open File",
							 "org.eclipse.cdt.cpp.ui.internal.actions.OpenFileAction");





	// connection actions
	DataElement connect = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR,
						     dataStore.getLocalizedString("model.Connect_to"),
						     "org.eclipse.cdt.dstore.ui.connections.ConnectAction");
        connect.setAttribute(DE.A_VALUE, "C_CONNECT");

	
	DataElement disconnect = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR,
							dataStore.getLocalizedString("model.Disconnect_from"),
							"org.eclipse.cdt.dstore.ui.connections.DisconnectAction");	
        disconnect.setAttribute(DE.A_VALUE, "C_DISCONNECT");
	
	DataElement editConnection = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR,
						  "Edit Connection",
						  "org.eclipse.cdt.dstore.ui.connections.EditConnectionAction");	
        editConnection.setAttribute(DE.A_VALUE, "C_EDIT");

	DataElement removeConnection = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR,
						    dataStore.getLocalizedString("model.Delete_Connection"),
						    "org.eclipse.cdt.dstore.ui.connections.DeleteAction");	
        removeConnection.setAttribute(DE.A_VALUE, "C_DELETE");



        DataElement parseMenuD = dataStore.createObject(fileD, DE.T_ABSTRACT_COMMAND_DESCRIPTOR, "Parse", "");
	
        DataElement parseD = dataStore.createObject(parseMenuD, DE.T_UI_COMMAND_DESCRIPTOR,
        			"Begin Parse",
			       "org.eclipse.cdt.cpp.ui.internal.actions.ProjectParseAction");
				
				
	    DataElement saveParseD = dataStore.createObject(parseMenuD, DE.T_UI_COMMAND_DESCRIPTOR,
	    			"Save Parse Information",
			       "org.eclipse.cdt.cpp.ui.internal.actions.ProjectSaveParseAction");

	    DataElement removeParseD = dataStore.createObject(parseMenuD, DE.T_UI_COMMAND_DESCRIPTOR,
        			"Remove Parse Information",
			       "org.eclipse.cdt.cpp.ui.internal.actions.ProjectRemoveParseAction");



	// copy project actions
	DataElement copy = dataStore.createObject(fileD, DE.T_UI_COMMAND_DESCRIPTOR, "Copy to...",
						  "org.eclipse.cdt.cpp.ui.internal.actions.CopyAction");
	

	// replicate project actions
	DataElement replicate = dataStore.createObject(fsD, DE.T_ABSTRACT_COMMAND_DESCRIPTOR,
							   "Replicate");

	DataElement replicateFrom = dataStore.createObject(replicate, DE.T_UI_COMMAND_DESCRIPTOR,
							   "from...",
							   "org.eclipse.cdt.cpp.ui.internal.actions.ReplicateFromAction");
	replicateFrom.setAttribute(DE.A_VALUE, "C_REPLICATE_FROM");

	DataElement replicateTo = dataStore.createObject(replicate, DE.T_UI_COMMAND_DESCRIPTOR,
							   "to...",
							   "org.eclipse.cdt.cpp.ui.internal.actions.ReplicateToAction");
	replicateFrom.setAttribute(DE.A_VALUE, "C_REPLICATE_TO");

	DataElement synchronizeWith = dataStore.createObject(replicate, DE.T_UI_COMMAND_DESCRIPTOR,
							     "with...",
							     "org.eclipse.cdt.cpp.ui.internal.actions.SynchronizeWithAction");
	synchronizeWith.setAttribute(DE.A_VALUE, "C_SYNCHRONIZE_WITH");



	//*********************************************
	
	// target Actions
	DataElement buildCmd = dataStore.createObject(targetD,DE.T_UI_COMMAND_DESCRIPTOR,
							  "Build",
							  "org.eclipse.cdt.cpp.ui.internal.actions.TargetAction");
	buildCmd.setAttribute(DE.A_VALUE, "BUILD_TARGET");
	DataElement executeCmd = dataStore.createObject(targetD,DE.T_UI_COMMAND_DESCRIPTOR,
							  "Execute",
							  "org.eclipse.cdt.cpp.ui.internal.actions.ExecuteAction");
	executeCmd.setAttribute(DE.A_VALUE, "EXECUTE_TARGET");
	// autoconf
	DataElement autoconfCmds = dataStore.createObject(fsD, DE.T_ABSTRACT_COMMAND_DESCRIPTOR, "Autoconf");

	//****************************
	DataElement configurationCmds = dataStore.createObject(autoconfCmds, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, "Configure Cmds");
	DataElement configureCmd = dataStore.createObject(configurationCmds, DE.T_UI_COMMAND_DESCRIPTOR,
								 "Configure..",
								 "org.eclipse.cdt.cpp.ui.internal.actions.ConfigureAction");
	configureCmd.setAttribute(DE.A_VALUE,"CONFIGURE");

	dataStore.createReference(configurationCmds, autoconfCmds, "abstracts", "abstracted by");
	
	//****************************
	DataElement dummy =  dataStore.createObject(autoconfCmds, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, "Dummy");
	DataElement advancedCmds = dataStore.createObject(dummy, DE.T_ABSTRACT_COMMAND_DESCRIPTOR, "Advanced...");
	
	///////////////
	
	DataElement updatesCmds = dataStore.createObject(advancedCmds, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, "Update Cmds");
		
	DataElement updateAutoconfFilesCmd = dataStore.createObject(updatesCmds, DE.T_UI_COMMAND_DESCRIPTOR,
							  "Create/Update all automake files",
							  "org.eclipse.cdt.cpp.ui.internal.actions.AdvancedConfigureAction");
	updateAutoconfFilesCmd.setAttribute(DE.A_VALUE, "UPDATE_AUTOCONF_FILES");
	
	
	DataElement updateMakefileAmCmd = dataStore.createObject(updatesCmds, DE.T_UI_COMMAND_DESCRIPTOR,
							  "Update Makefile.am",
							  "org.eclipse.cdt.cpp.ui.internal.actions.AdvancedConfigureAction");
	updateMakefileAmCmd.setAttribute(DE.A_VALUE, "UPDATE_MAKEFILE_AM");
	
	DataElement updateConfigureInCmd = dataStore.createObject(updatesCmds, DE.T_UI_COMMAND_DESCRIPTOR,
							  "Update configure.in",
							  "org.eclipse.cdt.cpp.ui.internal.actions.AdvancedConfigureAction");
	updateConfigureInCmd.setAttribute(DE.A_VALUE, "UPDATE_CONFIGURE_IN");
	
	dataStore.createReference(updatesCmds, advancedCmds, "abstracts", "abstracted by");
	
	DataElement makefileCmds = dataStore.createObject(advancedCmds, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, "Create Cmds");
	DataElement libCmds = dataStore.createObject(makefileCmds, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, "Libs Cmds");
	
	DataElement toStatLibCmd = dataStore.createObject(libCmds, DE.T_UI_COMMAND_DESCRIPTOR,
							  "Create StaticLib Makfile.am",
							  "org.eclipse.cdt.cpp.ui.internal.actions.MakefileAmAction");
	toStatLibCmd.setAttribute(DE.A_VALUE,"STATICLIB_MAKEFILE_AM");
	
	DataElement toSharedLibCmd = dataStore.createObject(libCmds, DE.T_UI_COMMAND_DESCRIPTOR,
							  "Create SharedLib Makefile.am",
							  "org.eclipse.cdt.cpp.ui.internal.actions.MakefileAmAction");
	toSharedLibCmd.setAttribute(DE.A_VALUE,"SHAREDLIB_MAKEFILE_AM");

	dataStore.createReference(libCmds, makefileCmds, "abstracts", "abstracted by");
	
	DataElement toTopLevelCmd = dataStore.createObject(makefileCmds, DE.T_UI_COMMAND_DESCRIPTOR,
							  "Create TopLevel Makefile.am",
							  "org.eclipse.cdt.cpp.ui.internal.actions.MakefileAmAction");
	toTopLevelCmd.setAttribute(DE.A_VALUE,"TOPLEVEL_MAKEFILE_AM");
	
	DataElement toProgCmd = dataStore.createObject(makefileCmds, DE.T_UI_COMMAND_DESCRIPTOR,
							  "Create Programs Makefile.am",
							  "org.eclipse.cdt.cpp.ui.internal.actions.MakefileAmAction");
	toProgCmd.setAttribute(DE.A_VALUE,"PROGRAMS_MAKEFILE_AM");
	
	//
	DataElement confInCmds = dataStore.createObject(makefileCmds, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, "ConfigureIn Cmds");

	DataElement confInCmd = dataStore.createObject(confInCmds, DE.T_UI_COMMAND_DESCRIPTOR,
							  "Create configure.in",
							  "org.eclipse.cdt.cpp.ui.internal.actions.MakefileAmAction");	
	confInCmd.setAttribute(DE.A_VALUE,"INSERT_CONFIGURE_IN");						
	dataStore.createReference(confInCmds, makefileCmds, "abstracts", "abstracted by");
	//	
	
	dataStore.createReference(makefileCmds, advancedCmds, "abstracts", "abstracted by");
	////////////////////////////////////
	DataElement advancedConfCmds = dataStore.createObject(advancedCmds, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, "Advanced Configure Cmds");
	
	DataElement createConfigureCmd = dataStore.createObject(advancedConfCmds, DE.T_UI_COMMAND_DESCRIPTOR,
								 "Generate configure",
								 "org.eclipse.cdt.cpp.ui.internal.actions.CreateConfigureAction");
	createConfigureCmd.setAttribute(DE.A_VALUE,"CREATE_CONFIGURE");

	DataElement runConfigureCmd = dataStore.createObject(advancedConfCmds, DE.T_UI_COMMAND_DESCRIPTOR,
							  "Run configure",
							  "org.eclipse.cdt.cpp.ui.internal.actions.RunConfigureAction");
	runConfigureCmd.setAttribute(DE.A_VALUE,"RUN_CONFIGURE");
	
	
	dataStore.createReference(advancedConfCmds, advancedCmds, "abstracts", "abstracted by");
	
	///////////////

	
	dataStore.createReference(dummy, autoconfCmds, "abstracts", "abstracted by");
	
	//***********************************
	
	DataElement defCmds = dataStore.createObject(autoconfCmds, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, "Default Cmds");
						
	DataElement distCleanCmd = dataStore.createObject(defCmds, DE.T_UI_COMMAND_DESCRIPTOR,
							  "distclean",
							  "org.eclipse.cdt.cpp.ui.internal.actions.TargetAction");
	distCleanCmd.setAttribute(DE.A_VALUE,"DIST_CLEAN");
			
	DataElement maintainerCmd = dataStore.createObject(defCmds, DE.T_UI_COMMAND_DESCRIPTOR,
							  "maintainer-clean",
							  "org.eclipse.cdt.cpp.ui.internal.actions.TargetAction");
	maintainerCmd.setAttribute(DE.A_VALUE,"MAINTAINER_CLEAN");	
		
	DataElement installCmd = dataStore.createObject(defCmds, DE.T_UI_COMMAND_DESCRIPTOR,
							  "make-install",
							  "org.eclipse.cdt.cpp.ui.internal.actions.TargetAction");
	installCmd.setAttribute(DE.A_VALUE,"INSTALL");		

	dataStore.createReference(defCmds, autoconfCmds, "abstracts", "abstracted by");	
	
	
	//removed temporary
	//***********************************
	/*DataElement managedProjectD = dataStore.find(schemaRoot,DE.A_NAME,"Managed Project",1);
	DataElement makefileTargetCmds = dataStore.createObject(managedProjectD, DE.T_ABSTRACT_COMMAND_DESCRIPTOR, "Change Target to..");

	DataElement libTargetCmds = dataStore.createObject(makefileTargetCmds, DE.T_ABSTRACT_OBJECT_DESCRIPTOR, "Libs Cmds");
	
	DataElement toStatLibTargetCmd = dataStore.createObject(libTargetCmds, DE.T_UI_COMMAND_DESCRIPTOR,
							  "StaticLib",
							  "org.eclipse.cdt.cpp.ui.internal.actions.MakefileAmAction");
	toStatLibTargetCmd.setAttribute(DE.A_VALUE,"STATICLIB_MAKEFILE_AM_VIEW");
	
	DataElement toSharedLibTargetCmd = dataStore.createObject(libTargetCmds, DE.T_UI_COMMAND_DESCRIPTOR,
							  "SharedLib",
							  "org.eclipse.cdt.cpp.ui.internal.actions.MakefileAmAction");
	toSharedLibTargetCmd.setAttribute(DE.A_VALUE,"SHAREDLIB_MAKEFILE_AM_VIEW");

	dataStore.createReference(libTargetCmds, makefileTargetCmds, "abstracts", "abstracted by");

	DataElement toProgTargetCmd = dataStore.createObject(makefileTargetCmds, DE.T_UI_COMMAND_DESCRIPTOR,
							  "bin Programs",
							  "org.eclipse.cdt.cpp.ui.internal.actions.MakefileAmAction");
	toProgTargetCmd.setAttribute(DE.A_VALUE,"PROGRAMS_MAKEFILE_AM_VIEW");*/
	//***********************************



	//-------------------------------
	// Find help for functions
	DataElement functionHelp = dataStore.createObject(function, DE.T_UI_COMMAND_DESCRIPTOR,
							  "Find Help",
							  "org.eclipse.cdt.cpp.ui.internal.actions.HelpAction");
	// Find help for classes
	DataElement classHelp = dataStore.createObject(classD, DE.T_UI_COMMAND_DESCRIPTOR,
							  "Find Help",
							  "org.eclipse.cdt.cpp.ui.internal.actions.HelpAction");
	//-------------------------------

	// breakpoint actions
	/*
	dataStore.createObject(classD, DE.T_UI_COMMAND_DESCRIPTOR,
			       "Set Class Breakpoint",
	 		       "org.eclipse.cdt.cpp.ui.internal.actions.AddClassBreakpoint");
	*/
	dataStore.createObject(function, DE.T_UI_COMMAND_DESCRIPTOR,
			       "Set Breakpoint",
			       "org.eclipse.cdt.cpp.ui.internal.actions.AddFunctionBreakpoint");
	dataStore.createObject(statement, DE.T_UI_COMMAND_DESCRIPTOR,
			       "Set Breakpoint",
			       "org.eclipse.cdt.cpp.ui.internal.actions.AddStatementBreakpoint");


	// debug actions
	dataStore.createObject(exeD, DE.T_UI_COMMAND_DESCRIPTOR,
			       "Run...",
			       "org.eclipse.cdt.cpp.ui.internal.actions.RunAction");
	dataStore.createObject(exeD, DE.T_UI_COMMAND_DESCRIPTOR,
			       "Debug...",
			       "org.eclipse.cdt.cpp.ui.internal.actions.DebugAction");
	dataStore.createObject(processD, DE.T_UI_COMMAND_DESCRIPTOR,
			       "Attach to...",
			       "org.eclipse.cdt.cpp.ui.internal.actions.AttachAction");


    } 
}
