package com.ibm.dstore.miners.cvstest;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;
import java.util.*;
import java.lang.*;
import java.io.*;


public class CVSMiner extends Miner
{    
    public void load()
    {
    }
    
   public void extendSchema(DataElement schemaRoot)
    {
	DataElement fsD         = _dataStore.find(schemaRoot, DE.A_NAME, "Filesystem Objects", 1);
	DataElement dirD         = _dataStore.find(schemaRoot, DE.A_NAME, "directory", 1);
	DataElement fileD        = _dataStore.find(schemaRoot, DE.A_NAME, "file", 1);
	
	DataElement cvsD      = createAbstractCommandDescriptor(fsD, "CVS", "C_CVS");
	
	DataElement updateD   = createCommandDescriptor(cvsD, "update",   "C_CVS_UPDATE");	
	DataElement checkoutD = createCommandDescriptor(cvsD, "checkout", "C_CVS_CHECKOUT");	
	DataElement commitD   = createCommandDescriptor(cvsD, "commit",   "C_CVS_COMMIT");	

	DataElement loginD    = createCommandDescriptor(cvsD, "login",    "C_CVS_LOGIN");	
	DataElement linputD   = _dataStore.createObject(loginD, "input", "Enter Password");	
    }
 
   public DataElement handleCommand(DataElement theCommand)
    {
     String name          = getCommandName(theCommand);
     DataElement  status  = getCommandStatus(theCommand);
     DataElement  subject = getCommandArgument(theCommand, 0);
 
     if (name.equals("C_CVS_LOGIN"))
	 {
	     handleLogin(subject, getCommandArgument(theCommand, 1), status);
	 }
     else if (name.equals("C_CVS_CHECKOUT"))
	 {
	     handleCheckout(subject, status);
	 }
     else if (name.equals("C_CVS_UPDATE"))
	 {
	     handleUpdate(subject, status);	     
	 }
     else if (name.equals("C_CVS_COMMIT"))
	 {
	     handleCommit(subject, status);	     	     
	 }

     status.setAttribute(DE.A_NAME, "done");
     return status;
    }

    private String getArgument(DataElement object)
    {
	String args = object.getName();
	if (object.getType().equals("file"))
	    {
		args = "\"" + args + "\"";
	    }

	return args;
    }

    public void handleLogin(DataElement subject, DataElement password, DataElement status)
    {
	System.out.println("cvs login");
    }

    public void handleCheckout(DataElement subject, DataElement status)
    {
	String args = getArgument(subject);
	System.out.println("cvs checkout " + args);
    }

    public void handleUpdate(DataElement subject, DataElement status)
    {
	String args = getArgument(subject);
	System.out.println("cvs update " + args);
    }

    public void handleCommit(DataElement subject, DataElement status)
    {
	String args = getArgument(subject);
	System.out.println("cvs commit -m \"test\" " + args);
    }

  }

