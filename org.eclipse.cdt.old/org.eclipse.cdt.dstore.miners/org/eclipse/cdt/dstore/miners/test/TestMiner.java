package com.ibm.dstore.miners.test;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;
import java.util.*;
import java.lang.*;
import java.io.*;


public class TestMiner extends Miner
  {
    
   public void load()
    {
    }


   public void extendSchema(DataElement schemaRoot)
    {
      DataElement dirD        = _dataStore.find(schemaRoot, DE.A_NAME, "directory", 1);

      DataElement testD = createAbstractCommandDescriptor(dirD, "Do Test", "C_TEST");

      DataElement addD = createCommandDescriptor(testD, "Object Add", "C_TEST_ADD");
      DataElement inputD    = _dataStore.createObject(addD, "input", "Enter object number");	

      DataElement rmD = createCommandDescriptor(testD, "Object Remove", "C_TEST_REMOVE");

      DataElement renD = createCommandDescriptor(testD, "Object Rename", "C_TEST_RENAME");
      DataElement renInputD    = _dataStore.createObject(renD, "input", "Enter New Name");	
    }
 
   public DataElement handleCommand(DataElement theCommand)
    {
     String name          = getCommandName(theCommand);
     DataElement  status  = getCommandStatus(theCommand);
     DataElement  subject = getCommandArgument(theCommand, 0);
 
     if (name.equals("C_TEST_ADD"))
     {
       DataElement  value   = getCommandArgument(theCommand, 1);
       return handleTestAdd(subject, value, status);	 
     }
     else if (name.equals("C_TEST_REMOVE"))
     {
       return handleTestRemove(subject, status);
     }
     else if (name.equals("C_TEST_RENAME"))
     {
       DataElement  newName   = getCommandArgument(theCommand, 1);       
       return handleTestRename(subject, newName, status);
     }
     status.setAttribute(DE.A_NAME, "done");
     return status;
    }

    public DataElement handleTestAdd(DataElement subject, DataElement value, DataElement status)
    {      
      status.setAttribute(DE.A_NAME, "progress");

      int numElements = (new Integer(value.getName())).intValue();
      
      long t1 = System.currentTimeMillis();
      subject.initializeNestedData(numElements);
      for (int i = 0; i < numElements; i++)
	{	  
	    String name1 = i + "dir";
	    DataElement n1 = _dataStore.createObject(subject, "directory", name1);
	    /*
	    _dataStore.createObject(n1, "file", "file1");
	    _dataStore.createObject(n1, "file", "file2");
	    _dataStore.createObject(n1, "file", "file3");
	    _dataStore.createObject(n1, "file", "file4");
*/
	}
      long t2 = System.currentTimeMillis();
      System.out.println("create time = " + (t2 - t1));
      _dataStore.update(subject);	

      status.setAttribute(DE.A_NAME, "done");
      return status;      
    } 

    public DataElement handleTestRemove(DataElement subject, DataElement status)
    {      
      DataElement parent = subject.getParent();
      _dataStore.deleteObject(parent, subject);

      status.setAttribute(DE.A_NAME, "done");
      return status;      
    } 

    public DataElement handleTestRename(DataElement subject, DataElement newName, DataElement status)
    {      
	String name = newName.getName();

	if (name.equals("bad"))
	    {
		status.setAttribute(DE.A_NAME, "incomplete");
		_dataStore.createObject(status, "input", "Invalid Name.  Please reenter.");
		return status;
	    }
	else
	    {
		subject.setAttribute(DE.A_NAME, name);
		_dataStore.update(subject);
		//		_dataStore.update(subject.getParent());	
		
		status.setAttribute(DE.A_NAME, "done");
	    }
      return status;      
    } 

    public DataElement handleSetBuffer(DataElement subject, DataElement newBuffer, DataElement status)
    {       
      System.out.println("handle set buffer");
      subject.appendToBuffer(newBuffer.getName());
      _dataStore.update(subject.getParent());	
      status.setAttribute(DE.A_NAME, "done");
      return status;      
    } 
    
  }

