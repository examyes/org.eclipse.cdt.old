
package org.eclipse.cdt.dstore.core.model;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.core.model.*;

import java.util.*;
import java.lang.*;

public class DataStoreSchema
{
    private DataStore _dataStore;
    private DataElement _abstractedBy;
    private DataElement _abstracts;
    private DataElement _contents;
    private DataElement _container;
    private DataElement _attributes;

    public DataStoreSchema(DataStore dataStore)
    {
	_dataStore = dataStore;
    }

    public DataElement getAbstractedByRelation()
    {
	return _abstractedBy;
    }

    public DataElement getAbstractsRelation()
    {
	return _abstracts;
    }

    public DataElement getContentsRelation()
    {
	return _contents;
    }

    public DataElement getAttributesRelation()
    {
	return _attributes;
    }

    public DataElement getContainerType()
    {
	return _container;
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
	_contents     = _dataStore.createRelationDescriptor(schemaRoot, getLocalizedString("model.contents"));
	_contents.setDepth(100);
	
	DataElement descriptorForD  = _dataStore.createRelationDescriptor(schemaRoot, getLocalizedString("model.descriptor_for"));
	descriptorForD.setDepth(0);
	
	DataElement parentD       = _dataStore.createRelationDescriptor(schemaRoot, getLocalizedString("model.parent"));	
	parentD.setDepth(0);

	_attributes   = _dataStore.createRelationDescriptor(schemaRoot, "attributes");
	_attributes.setDepth(0);
	
	DataElement argsD         = _dataStore.createRelationDescriptor(schemaRoot, getLocalizedString("model.arguments"));	
	_abstracts     = _dataStore.createRelationDescriptor(schemaRoot, getLocalizedString("model.abstracts"));
	
	_abstractedBy  = _dataStore.createRelationDescriptor(schemaRoot, getLocalizedString("model.abstracted_by"));		

	DataElement caRelations = _dataStore.createAbstractRelationDescriptor(schemaRoot, getLocalizedString("model.contents&arguments"));
	_dataStore.createReference(caRelations, _contents, _contents);
	_dataStore.createReference(caRelations, argsD, _contents); 
	
        _dataStore.createReference(objectDescriptor, _contents, _contents);
        _dataStore.createReference(objectDescriptor, parentD, _contents);
        _dataStore.createReference(objectDescriptor, _abstracts, _contents);
        _dataStore.createReference(objectDescriptor, _abstractedBy, _contents);
	
        _dataStore.createReference(abstractObjectDescriptor, _contents, _contents);
        _dataStore.createReference(abstractObjectDescriptor, parentD, _contents); 
        _dataStore.createReference(abstractObjectDescriptor, _abstracts, _contents);
        _dataStore.createReference(abstractObjectDescriptor, _abstractedBy, _contents);
	
	_dataStore.createReference(statusD, _contents, _contents);	
	
	_dataStore.createReference(commandDescriptor, allD, _contents);	
	_dataStore.createReference(commandDescriptor, caRelations, _contents);
	_dataStore.createReference(commandDescriptor, argsD, _contents);	
	_dataStore.createReference(commandDescriptor, _contents, _contents);	
	
	
        DataElement logDetails = _dataStore.createAbstractObjectDescriptor(logD, getLocalizedString("model.Commands"));
        _dataStore.createReference(logDetails, commandDescriptor, _contents);
        _dataStore.createReference(logDetails, allD, _contents);        
	_dataStore.createReference(logD, caRelations, _contents);
	_dataStore.createReference(logD, _contents, _contents);
	
	//Base Container Object
        _container = _dataStore.createAbstractObjectDescriptor(schemaRoot, getLocalizedString("model.Container_Object"));
        _dataStore.createCommandDescriptor(_container, getLocalizedString("model.Query"),   "*", "C_QUERY", false);
	_dataStore.createReference(_container, _contents, _contents);
	
	// file objects
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
	
	_dataStore.createReference(_container, fsObject, _abstracts, _abstractedBy);
	
	_dataStore.createReference(fileD,    fsObject, _abstracts, _abstractedBy);
	_dataStore.createReference(fsObject, dirD, _abstracts, _abstractedBy);
	_dataStore.createReference(fsObject, deviceD, _abstracts, _abstractedBy);

        _dataStore.createReference(fsObject, fileD,    _contents);
        _dataStore.createReference(fsObject, dirD,     _contents);
	_dataStore.createReference(fsObject, fsObject, _contents);

	_dataStore.createReference(hostD,    fsObject, _contents); 

	_dataStore.createReference(deviceD,  fileD, _contents);
	_dataStore.createReference(deviceD,  dirD, _contents);

	_dataStore.createReference(dirD,     fileD, _contents);
	_dataStore.createReference(dirD,     dirD, _contents);
	
	
	// miner descriptors
	DataElement minersD      = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.miners"));
	DataElement minerD       = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.miner"));
	DataElement dataD        = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.data"));
	DataElement transientD   = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.transient"));
	DataElement stateD       = _dataStore.createObjectDescriptor(schemaRoot, getLocalizedString("model.state"));


        DataElement hostsDetails = _dataStore.createAbstractObjectDescriptor(rootD, getLocalizedString("model.Hosts"));	
	_dataStore.createReference(hostsDetails, rootD, _abstracts, _abstractedBy);	
	_dataStore.createReference(hostsDetails, hostD, _abstracts, _abstractedBy);	
	_dataStore.createReference(hostsDetails, deviceD, _abstracts, _abstractedBy);	
	_dataStore.createReference(hostsDetails, dirD, _abstracts, _abstractedBy);	
	
        DataElement minerObjects = _dataStore.createAbstractObjectDescriptor(rootD, getLocalizedString("model.Tools"));
        _dataStore.createReference(minerObjects, minersD, _abstracts, _abstractedBy);
        _dataStore.createReference(minerObjects, minerD, _abstracts, _abstractedBy);
        _dataStore.createReference(minerObjects, dataD, _abstracts, _abstractedBy);
		
	// containers
	_dataStore.createReference(_container, rootD, _abstracts, _abstractedBy);	
	_dataStore.createReference(_container, hostD, _abstracts, _abstractedBy);	
	_dataStore.createReference(_container, logD, _abstracts, _abstractedBy);	
	_dataStore.createReference(_container, minersD, _abstracts, _abstractedBy);	
	_dataStore.createReference(_container, minerD, _abstracts, _abstractedBy);	
	_dataStore.createReference(_container, dataD, _abstracts, _abstractedBy);	

	// type descriptors
        _dataStore.createObjectDescriptor(schemaRoot, "String");
        _dataStore.createObjectDescriptor(schemaRoot, "Date");

        DataElement numberD  = _dataStore.createAbstractObjectDescriptor(schemaRoot, "Number");
        DataElement integerD = _dataStore.createObjectDescriptor(schemaRoot, "Integer");
        DataElement floatD   = _dataStore.createObjectDescriptor(schemaRoot, "Float");
	_dataStore.createReference(numberD, integerD, _abstracts, _abstractedBy);
	_dataStore.createReference(numberD, floatD, _abstracts, _abstractedBy);
	
	
        // basic commands
	_dataStore.createCommandDescriptor(cancellable, getLocalizedString("model.Cancel"), "*", "C_CANCEL");	
	_dataStore.createCommandDescriptor(rootD, getLocalizedString("model.Set"), "-", "C_SET", false); 
	_dataStore.createCommandDescriptor(rootD, getLocalizedString("model.Set_Host"), "-", "C_SET_HOST", false);
	_dataStore.createCommandDescriptor(rootD, getLocalizedString("model.Init_Miners"), "*", "C_INIT_MINERS", false);
	_dataStore.createCommandDescriptor(rootD, "Add Miners", "-", "C_ADD_MINERS", false);
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
