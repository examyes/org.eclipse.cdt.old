package com.ibm.dstore.core.miners.miner;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
 
import com.ibm.dstore.core.model.*;

import java.lang.*;
import java.io.*;
import java.util.*;

public abstract class Miner
{
    public DataStore   _dataStore;
    public DataElement _minerElement; 
    public DataElement _minerData;
    public DataElement _minerTransient;
    
    private boolean    _initialized;
    protected String   _name = null;
    protected String   _value = null;
    
    protected ResourceBundle _resourceBundle = null;  
    
    public Miner()
    {
	_initialized = false;
    }
    
    public void finish()
    {
	DataElement root = _dataStore.getMinerRoot();
	
	_minerData.removeNestedData();
	_minerElement.removeNestedData();
	_dataStore.update(_minerElement);
	
	root.getNestedData().remove(_minerElement);
	root.setExpanded(false);
	root.setUpdated(false);
	
	_dataStore.update(root);
    }
    
    public ResourceBundle getResourceBundle()  
    {
	return null;
    }
    
    public void load()
    {
    }

    public void load(DataElement status)
    {
	load();
    }
    
    public void updateMinerInfo()
    {
    }

    public String getName()
    {
        if (_name == null)
	    _name = getClass().getName();
        return _name;
    }
    
    public String getValue()
    {
        if (_value == null)
	    {
		String name = getName();
		int indexOfValue = name.lastIndexOf(".");
		_value = name.substring(indexOfValue + 1, name.length());
	    }
        return _value;
    }
    
    public DataElement command(DataElement command)
    {
	String      name   = getCommandName(command);
	DataElement status = getCommandStatus(command);
	long startTime = System.currentTimeMillis();
	
	status.setAttribute(DE.A_NAME, getLocalizedString("model.working"));

	if (name.equals("C_INIT_MINERS"))
	    {
		if (!_initialized)
		    {
			load(status);
			_initialized = true;
		    }
		updateMinerInfo();

		DataElement minerRoot = _dataStore.getMinerRoot();
		_dataStore.refresh(minerRoot, true);

		if (status.getAttribute(DE.A_NAME).equals(getLocalizedString("model.incomplete")))
		    {
			_dataStore.refresh(status);
		    }
		else
		    {
			status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
			_dataStore.refresh(status);
		    }
	    }    
	else
	    {	
		try
		    {
			status = handleCommand(command);
		    }
		catch(Exception e)
		    {
			
			System.out.println(e);
			e.printStackTrace();
			
			status.setAttribute(DE.A_NAME, "Failed with Exception");
			status.setAttribute(DE.A_VALUE, getLocalizedString("model.done"));
			_dataStore.refresh(status);
			
			String exc = null;
			if (e.getMessage() != null)
			    exc = e.getMessage();
			else
			    exc = "Exception";
			DataElement exception = _dataStore.createObject(status, getLocalizedString("model.error"), exc);
			
			e.printStackTrace();
			
		    }
	    }
	
	if (_dataStore.logTimes())
	    {
		long endTime = System.currentTimeMillis(); 
		
		DataElement time    = getCommandTime(command);
		DataElement endMinerTime = _dataStore.createObject(time, getLocalizedString("model.property"), "Time in " + _value);      
		endMinerTime.setAttribute(DE.A_VALUE, new String(endTime - startTime + "ms"));
	    }
	return status;
    }
    
    
    public void setDataStore(DataStore dataStore)
    {
	_dataStore = dataStore;
	
	DataElement root = _dataStore.getMinerRoot();
	String name = getName();
	String value = getValue();
	
	_resourceBundle = getResourceBundle();

	_minerElement   = _dataStore.createObject(root, getLocalizedString("model.miner"), name, name);
	_minerElement.setAttribute(DE.A_VALUE, value);
	
	_minerData      = _dataStore.createObject(_minerElement, getLocalizedString("model.data"), getLocalizedString("model.Data"), name);
	_minerTransient = _dataStore.createObject(_minerElement, getLocalizedString("model.transient"), getLocalizedString("model.Transient_Objects"), name);

	
	// extend schema
	DataElement schemaRoot = _dataStore.getDescriptorRoot();
	extendSchema(schemaRoot);

	_dataStore.refresh(root, true);	
	_dataStore.refresh(_minerElement);	
    }
    
    
    // creation helpers
    public DataElement createAbstractCommandDescriptor(DataElement descriptor, String name, String value) 
    {
	return _dataStore.createAbstractCommandDescriptor(descriptor, name, getName(), value);
    }
    
    public DataElement createCommandDescriptor(DataElement descriptor, String name, String value)
    {
	return createCommandDescriptor(descriptor, name, value, true);
    }
    
    public DataElement createCommandDescriptor(DataElement descriptor, String name, String value, boolean visible)
    {
        DataElement cmdD = _dataStore.createCommandDescriptor(descriptor, name, getName(), value);
	if (!visible)
	    {
		cmdD.setDepth(0);
	    }
	
        return cmdD;
    }
    
    public DataElement createAbstractObjectDescriptor(DataElement descriptor, String name)
    {
	return _dataStore.createAbstractObjectDescriptor(descriptor, name);
    }

    public DataElement createAbstractObjectDescriptor(DataElement descriptor, String name, String source)
    {
	return _dataStore.createAbstractObjectDescriptor(descriptor, name, source);
    }
    
    public DataElement createObjectDescriptor(DataElement descriptor, String name)
    {
	return _dataStore.createObjectDescriptor(descriptor, name);
    }

    public DataElement createObjectDescriptor(DataElement descriptor, String name, String source)
    {
	return _dataStore.createObjectDescriptor(descriptor, name, source);
    }

    public DataElement createAbstractRelationship(DataElement from, DataElement to)
    {
     return _dataStore.createReference(from, to, "abstracts", "abstracted by");
    }
     
    public DataElement createReference(DataElement from, DataElement to)
    {
     return _dataStore.createReference(from, to);
    }
    
    public DataElement createRelationDescriptor(DataElement descriptor, String name)
    {
     return _dataStore.createRelationDescriptor(descriptor, name);
    }
 
 
     

 //Find an object under the schema Root.
 public DataElement findDescriptor(String descName)
 {
  return _dataStore.find(_dataStore.getDescriptorRoot(), DE.A_NAME, descName, 1);
 }
 

    

    
    // gets
    public DataElement getMinerElement()
    {
        return _minerElement;
    }
    
    public DataElement getMinerData()
      {
	  return _minerData;
      }
    
    public DataElement getMinerTransient()
    {
        return _minerTransient;
    }
    
    public void makeTransient(DataElement objectDescriptor)
    {
        _dataStore.createReference(_minerTransient, objectDescriptor);
    }
    
    // functions for extracting command information
    public String getCommandName(DataElement command)
    {
	return (String)command.getAttribute(DE.A_NAME);
    }
    
    public DataElement getCommandStatus(DataElement command)
    {
	return _dataStore.find(command, DE.A_TYPE, getLocalizedString("model.status"), 1);
    }
    
    public DataElement getCommandTime(DataElement command)
    {
	return _dataStore.find(getCommandStatus(command), DE.A_TYPE, getLocalizedString("model.time"), 1);
    }
    
    public int getNumberOfCommandArguments(DataElement command)
    {
	return command.getNestedSize();
    }
    
    public DataElement getCommandArgument(DataElement command, int arg)
    {
	if (command.getNestedSize() > 0)
	    {
		DataElement argument = command.get(arg);
		if (argument != null)
		    {
			return argument.dereference();
		    }
	    }

	return null;
    }
    
    
    public String getLocalizedString(String key)
    {
	try
	    {
		if (_resourceBundle != null && key != null)
		    {
			return _resourceBundle.getString(key);
		    }
	    }
	catch (MissingResourceException mre)
	    {
	    }

	// attempt getting resource from datastore
	return _dataStore.getLocalizedString(key);
    }

 public DataElement getSchemaRoot()
 {
  return _dataStore.getDescriptorRoot();
  
  }
 

    // functions that miners need to implement
    public abstract void extendSchema(DataElement schemaRoot);
    public abstract DataElement handleCommand (DataElement theElement);
}

