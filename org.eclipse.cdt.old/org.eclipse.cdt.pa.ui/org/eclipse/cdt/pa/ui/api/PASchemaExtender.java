package org.eclipse.cdt.pa.ui.api;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.pa.ui.*;

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.util.*;

public class PASchemaExtender implements ISchemaExtender
{
    private ExternalLoader _loader;

    public PASchemaExtender()
    {
	_loader = new ExternalLoader(PAPlugin.getDefault().getDescriptor().getPluginClassLoader(), 
				     "org.eclipse.cdt.pa.ui.*");
    }

    public ExternalLoader getExternalLoader()
    {
	return _loader;
    }

    /**
     * Extend the schema
     */
    public void extendSchema(DataElement schemaRoot) 
    {	
	// System.out.println("extend schema");
	DataStore   dataStore 	= schemaRoot.getDataStore();
	
	// DataElement fileD         = dataStore.find(schemaRoot, DE.A_NAME, "file",1);
	// dataStore.createObject(fileD,         DE.T_UI_COMMAND_DESCRIPTOR, "Add trace file", "org.eclipse.cdt.pa.ui.actions.AddTraceFileAction");    
	
	DataElement executableD	= dataStore.find(schemaRoot, DE.A_NAME, "binary executable",1);
	DataElement traceFileD    = dataStore.find(schemaRoot, DE.A_NAME, "trace file", 1);
	DataElement traceProgramD = dataStore.find(schemaRoot, DE.A_NAME, "trace program", 1);
	
	dataStore.createObject(executableD,   DE.T_UI_COMMAND_DESCRIPTOR, "Analyze...", "org.eclipse.cdt.pa.ui.actions.AddTraceProgramAction");      
	dataStore.createObject(traceFileD,    DE.T_UI_COMMAND_DESCRIPTOR, "Remove", "org.eclipse.cdt.pa.ui.actions.RemoveTraceTargetAction");
	dataStore.createObject(traceProgramD, DE.T_UI_COMMAND_DESCRIPTOR, "Remove", "org.eclipse.cdt.pa.ui.actions.RemoveTraceTargetAction");
	dataStore.createObject(traceProgramD, DE.T_UI_COMMAND_DESCRIPTOR, "Run", "org.eclipse.cdt.pa.ui.actions.RunTraceProgramAction");
	dataStore.createObject(traceProgramD, DE.T_UI_COMMAND_DESCRIPTOR, "Analyze", "org.eclipse.cdt.pa.ui.actions.AnalyzeTraceProgramAction");
	dataStore.createObject(traceProgramD, DE.T_UI_COMMAND_DESCRIPTOR, "Run and Analyze", "org.eclipse.cdt.pa.ui.actions.RunAndAnalyzeTraceProgramAction");
	
    }
}
