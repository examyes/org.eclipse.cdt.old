package com.ibm.dstore.core.util;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.util.*;
import com.ibm.dstore.core.model.*;
//import com.ibm.dstore.core.extra.*;

import java.util.*;

public class CommandGenerator
{
  private DataStore _dataStore = null;
  private DataElement _log = null;
  private int index = 0;
  private int resIndex = 0;
  static private int _id = 0;

  public CommandGenerator()
    {
    }

  public void setDataStore(DataStore dataStore) 
  {
    _dataStore = dataStore;
    _log = _dataStore.getLogRoot();
  }

  public DataElement logCommand(DataElement commandObject)
  {
    // prevent duplicate queries
    String name = commandObject.getAttribute(DE.A_NAME);
    if (name.equals("C_QUERY"))
      {
        // don't query log objects or schema objects
        DataElement subject = (DataElement)commandObject.get(0);
        DataElement desObj = _dataStore.find(_dataStore.getDescriptorRoot(), DE.A_ID, subject.getId(), 1);
        if (desObj != null)
        {	  
          return null;
        } 
	String subjectType = subject.dereference().getType();
	if (subjectType.equals(DE.T_OBJECT_DESCRIPTOR) ||
	    subjectType.equals(DE.T_COMMAND_DESCRIPTOR) ||
	    subjectType.equals(DE.T_ABSTRACT_OBJECT_DESCRIPTOR) ||
	    subjectType.equals(DE.T_ABSTRACT_COMMAND_DESCRIPTOR))
	  {
	    return null;
	  }
	 
        // don't prevent requery of transient objects
        boolean isTransient = _dataStore.isTransient(commandObject);

        if (!isTransient)
        {
          for (int i = 0; i < _log.getNestedSize(); i++)
	  {
	    DataElement loggedCommand = (DataElement)_log.get(i);
            if (loggedCommand.getAttribute(DE.A_NAME).equals("C_QUERY"))
            {
              DataElement loggedSubject = loggedCommand.get(0);
	      if (!loggedSubject.isDeleted())
		{		  
		  if (loggedSubject.dereference() == subject.dereference())
		    {
		      return null;
		    }
		}	      
            }
	  }	
        }
      }

    // create time and status objects
    DataElement status = _dataStore.createObject(commandObject, 
						 _dataStore.getLocalizedString("model.status"), 
						 _dataStore.getLocalizedString("model.start"), "", 
						 commandObject.getId() + _dataStore.getLocalizedString("model.status"));
    
    if (_dataStore.logTimes())
    {
	DataElement timeObject = _dataStore.createObject(status, _dataStore.getLocalizedString("model.time"), _dataStore.getLocalizedString("model.command_time"));
      DataElement startTime = _dataStore.createObject(timeObject, _dataStore.getLocalizedString("model.property"), _dataStore.getLocalizedString("model.start_time"));
      startTime.setAttribute(DE.A_VALUE, new String(System.currentTimeMillis() + ""));
    }
    
    _log.addNestedData(commandObject, false);
    _dataStore.refresh(_log);

    return commandObject;
  }

    public DataElement createCommand(DataElement commandDescriptor)
    {
      return _dataStore.createObject(null,
				commandDescriptor.getName(),
				commandDescriptor.getValue(),
				commandDescriptor.getSource());	
    }  

  public DataElement generateCommand(DataElement commandDescriptor,
				     ArrayList arguments,
				     DataElement dataObject,
				     boolean refArg)
    {
      DataElement commandObject = createCommand(commandDescriptor);
      commandObject.setAttribute(DE.A_VALUE, commandDescriptor.getName());

      if (refArg)
	  {
	      _dataStore.createReference(commandObject, dataObject, _dataStore.getLocalizedString("model.arguments"));
	  }
      else
	  {
	      commandObject.addNestedData(dataObject, false);
	  }

      if (arguments != null)
      {
        commandObject.addNestedData(arguments, false);
      }

      return logCommand(commandObject);
    }

  public DataElement generateCommand(DataElement commandDescriptor,
				     DataElement objectDescriptor,
				     DataElement dataObject,
				     boolean refArg)
    {
      String id = _log.getId() + "." + _id++;
      
      DataElement commandObject = createCommand(commandDescriptor);
      commandObject.setAttribute(DE.A_VALUE, commandDescriptor.getName());

      if (refArg)
	  {
	      _dataStore.createReference(commandObject, dataObject, _dataStore.getLocalizedString("model.arguments"));
	  }
      else
	  {
	      commandObject.addNestedData(dataObject, false);
	  }

      commandObject.addNestedData(objectDescriptor, false);

      return logCommand(commandObject);
    }

  public DataElement generateCommand(DataElement commandDescriptor, DataElement dataObject, boolean refArg)
    {
      String id = _log.getId() + "." + _id++;

      DataElement commandObject = createCommand(commandDescriptor);
      commandObject.setAttribute(DE.A_VALUE, commandDescriptor.getName());

      if (refArg)
	  {
	      _dataStore.createReference(commandObject, dataObject, _dataStore.getLocalizedString("model.arguments"));
	  }
      else
	  {
	      commandObject.addNestedData(dataObject, false);
	  }

      return logCommand(commandObject);
    }


  public DataElement generateResponse(DataElement document, ArrayList objects)
    {
      document.addNestedData(objects, false);
      return document;
    }

  public DataElement generateResponse(String responseType, DataElement dataObject)
    {
      if (dataObject != null)
	{	
	  DataElement commandObject = _dataStore.createObject(null, "RESPONSE", responseType);
	  commandObject.addNestedData(dataObject, true);
	  return commandObject;
	}
      else
	{
	  return null;
	}
    }

  public DataElement generateResponse(String responseType)
    {
      DataElement commandObject = _dataStore.createObject(null, "RESPONSE", responseType);
      return commandObject;
    }
}
