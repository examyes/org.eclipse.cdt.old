
package org.eclipse.cdt.dstore.core.model;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.util.*; 
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.core.server.ILoader;

import java.util.*;
import java.lang.*;
import java.io.*;

public class DataStoreSchema
{
    private DataStore _dataStore;

    public DataStoreSchema(DataStore dataStore)
    {
	_dataStore = dataStore;
    }

    public void extendSchema(DataElement schemaRoot)
    {	 
		// miner-specific descriptors are defined in the miners when they extend the schema
	
		// these first elements are the most fundamental	  
        DataElement uiCmdD    = _dataStore.createObject(schemaRoot, DE.T_UI_COMMAND_DESCRIPTOR, DE.T_UI_COMMAND_DESCRIPTOR);
	
        DataElement commandDescriptor      = _dataStore.createCommandDescriptor (schemaRoot, DE.T_COMMAND_DESCRIPTOR);
        DataElement objectDescriptor     = _dataStore.createObjectDescriptor  (schemaRoot, DE.T_OBJECT_DESCRIPTOR);
		DataElement relationDescriptor      = _dataStore.createRelationDescriptor(schemaRoot, DE.T_RELATION_DESCRIPTOR);
	
		DataElement abstractObjectDescriptor      = _dataStore.createAbstractObjectDescriptor   (schemaRoot, DE.T_ABSTRACT_OBJECT_DESCRIPTOR);
        DataElement abstractCommandDescriptor      = _dataStore.createAbstractCommandDescriptor  (schemaRoot, DE.T_ABSTRACT_COMMAND_DESCRIPTOR);
        DataElement abstractRelationDescriptor      = _dataStore.createAbstractRelationDescriptor (schemaRoot, DE.T_ABSTRACT_RELATION_DESCRIPTOR);
	
	
		// cancellable command base descriptor
		DataElement cancellable = _dataStore.createAbstractObjectDescriptor(schemaRoot, 
									    getLocalizedString("model.Cancellable"));

        DataElement rootD    = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.root"));
        DataElement hostD    = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.host"));
       
		DataElement logD      = _dataStore.createObjectDescriptor(schemaRoot,  getLocalizedString("model.log"));
		DataElement statusD   = _dataStore.createObjectDescriptor(schemaRoot,  getLocalizedString("model.status"));

		DataElement deletedD   = _dataStore.createObjectDescriptor(schemaRoot,  getLocalizedString("model.deleted"));


		// misc
        DataElement allD      = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.all"));
 
		DataElement invokeD   = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.invocation"));	
		DataElement patternD  = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.pattern"));	

		DataElement inputD    = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.input"));
		DataElement outputD   = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.output"));

		// types of relationships
		DataElement containsD     = _dataStore.createRelationDescriptor(schemaRoot, getLocalizedString("model.contents"));
		containsD.setDepth(100);

		DataElement descriptorForD  = _dataStore.createRelationDescriptor(schemaRoot, getLocalizedString("model.descriptor_for"));	
		DataElement parentD       = _dataStore.createRelationDescriptor(schemaRoot, getLocalizedString("model.parent"));	
		DataElement argsD         = _dataStore.createRelationDescriptor(schemaRoot, getLocalizedString("model.arguments"));	
		DataElement abstracts     = _dataStore.createRelationDescriptor(schemaRoot, getLocalizedString("model.abstracts"));
	
		DataElement abstractedBy  = _dataStore.createRelationDescriptor(schemaRoot, getLocalizedString("model.abstracted_by"));		
		DataElement caRelations = _dataStore.createAbstractRelationDescriptor(schemaRoot, getLocalizedString("model.contents&arguments"));
		_dataStore.createReference(caRelations, containsD, containsD);
		_dataStore.createReference(caRelations, argsD, containsD); 
	
        _dataStore.createReference(objectDescriptor, containsD, containsD);
        _dataStore.createReference(objectDescriptor, parentD, containsD);
        _dataStore.createReference(objectDescriptor, abstracts, containsD);
        _dataStore.createReference(objectDescriptor, abstractedBy, containsD);

        _dataStore.createReference(abstractObjectDescriptor, containsD, containsD);
        _dataStore.createReference(abstractObjectDescriptor, parentD, containsD); 
        _dataStore.createReference(abstractObjectDescriptor, abstracts, containsD);
        _dataStore.createReference(abstractObjectDescriptor, abstractedBy, containsD);

		_dataStore.createReference(statusD, containsD, containsD);	

		_dataStore.createReference(commandDescriptor, allD, containsD);	
		_dataStore.createReference(commandDescriptor, caRelations, containsD);
		_dataStore.createReference(commandDescriptor, argsD, containsD);	
		_dataStore.createReference(commandDescriptor, containsD, containsD);	


        DataElement logDetails = _dataStore.createAbstractObjectDescriptor(logD, getLocalizedString("model.Commands"));
        _dataStore.createReference(logDetails, commandDescriptor, containsD);
        _dataStore.createReference(logDetails, allD, containsD);        
		_dataStore.createReference(logD, caRelations, containsD);
		_dataStore.createReference(logD, containsD, containsD);

		 //Base Container Object
        DataElement containerObjectD = _dataStore.createAbstractObjectDescriptor(schemaRoot, getLocalizedString("model.Container_Object"));
        _dataStore.createCommandDescriptor(containerObjectD, getLocalizedString("model.Query"),   "*", "C_QUERY", false);

		// file objects
		_dataStore.createReference(hostD, containsD, containsD);	


        DataElement deviceD  = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.device"), 
						      "org.eclipse.cdt.dstore.miners");
        DataElement fileD    = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.file"), 
						      "org.eclipse.cdt.dstore.miners");
        DataElement dirD     = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.directory"), 
						      "org.eclipse.cdt.dstore.miners");

        DataElement fsObject = _dataStore.createAbstractObjectDescriptor(schemaRoot, getLocalizedString("model.Filesystem_Objects"), "org.eclipse.cdt.dstore.miners");

        _dataStore.createCommandDescriptor(fsObject, getLocalizedString("model.Refresh"), "*", "C_REFRESH");
        _dataStore.createCommandDescriptor(fsObject, getLocalizedString("model.Open"),    "*", "C_OPEN", false);
        _dataStore.createCommandDescriptor(fsObject, getLocalizedString("model.Close"),   "*", "C_CLOSE", false);

		_dataStore.createReference(containerObjectD, fsObject, abstracts, abstractedBy);
		_dataStore.createReference(fsObject, deviceD,  abstracts, abstractedBy);

		_dataStore.createReference(fileD,    fsObject, abstracts, abstractedBy);
		_dataStore.createReference(fsObject, dirD, abstracts, abstractedBy);
        _dataStore.createReference(fsObject, fileD,    containsD);
        _dataStore.createReference(fsObject, dirD,     containsD);
		_dataStore.createReference(fsObject, fsObject, containsD);
		_dataStore.createReference(hostD,    fsObject, containsD);
		_dataStore.createReference(deviceD,  dirD, containsD);
		_dataStore.createReference(deviceD,  fileD, containsD);
		_dataStore.createReference(dirD,     dirD, containsD);
		_dataStore.createReference(dirD,     fileD, containsD);

		DataElement hostDirectories  = _dataStore.createAbstractObjectDescriptor(hostD, getLocalizedString("model.Directories"),
								      "org.eclipse.cdt.dstore.miners");	
		_dataStore.createReference(hostDirectories, dirD, containsD);
		_dataStore.createReference(hostDirectories, deviceD, containsD);

		DataElement hostDetails  = _dataStore.createAbstractObjectDescriptor(hostD, getLocalizedString("model.Details"));	        
		_dataStore.createReference(hostDetails, hostDirectories, containsD);
        _dataStore.createReference(hostDetails, fileD, containsD);

		// miner descriptors
		DataElement minersD      = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.miners"));
		DataElement minerD       = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.miner"));
		DataElement dataD        = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.data"));
		DataElement transientD   = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.transient"));
		DataElement stateD       = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.state"));
        
		
        DataElement hostsDetails = _dataStore.createAbstractObjectDescriptor(rootD, getLocalizedString("model.Hosts"));	
		_dataStore.createReference(hostsDetails, rootD, abstracts, abstractedBy);	
		_dataStore.createReference(hostsDetails, hostD, abstracts, abstractedBy);	
		_dataStore.createReference(hostsDetails, deviceD, abstracts, abstractedBy);	
		_dataStore.createReference(hostsDetails, dirD, abstracts, abstractedBy);	

        DataElement minerObjects = _dataStore.createAbstractObjectDescriptor(rootD, getLocalizedString("model.Tools"));
        _dataStore.createReference(minerObjects, minersD, abstracts, abstractedBy);
        _dataStore.createReference(minerObjects, minerD, abstracts, abstractedBy);
	

		// containers
		_dataStore.createReference(containerObjectD, rootD, abstracts, abstractedBy);	
		_dataStore.createReference(containerObjectD, hostD, abstracts, abstractedBy);	
		_dataStore.createReference(containerObjectD, logD, abstracts, abstractedBy);	
		_dataStore.createReference(containerObjectD, minersD, abstracts, abstractedBy);	
		_dataStore.createReference(containerObjectD, minerD, abstracts, abstractedBy);	
		_dataStore.createReference(containerObjectD, dataD, abstracts, abstractedBy);	
	

        // basic commands
		_dataStore.createCommandDescriptor(cancellable, getLocalizedString("model.Cancel"), "*", "C_CANCEL");	
		_dataStore.createCommandDescriptor(rootD, getLocalizedString("model.Set"), "-", "C_SET", false); 
		_dataStore.createCommandDescriptor(rootD, getLocalizedString("model.Set_Host"), "-", "C_SET_HOST", false);
		_dataStore.createCommandDescriptor(rootD, getLocalizedString("model.Init_Miners"), "*", "C_INIT_MINERS", false);
		_dataStore.createCommandDescriptor(rootD, "Set Miners", "-", "C_SET_MINERS", false);
		_dataStore.createCommandDescriptor(rootD, getLocalizedString("model.Show_Ticket"), "-", "C_VALIDATE_TICKET", false);	
		_dataStore.createCommandDescriptor(rootD, getLocalizedString("model.Get_Schema"), "*", "C_SCHEMA", false);	
		_dataStore.createCommandDescriptor(rootD, getLocalizedString("model.Exit"), "*", "C_EXIT", false);
	     
    	_dataStore.createCommandDescriptor(rootD, "Notification", "*", "C_NOTIFICATION", false);
    	_dataStore.createCommandDescriptor(rootD, "Send Input", "*", "C_SEND_INPUT", false);
      }


    public String getLocalizedString(String key)
    {
	return _dataStore.getLocalizedString(key);
    }
}
