package com.ibm.dstore.miners.cvstest;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;
import java.util.*;
import java.lang.*;
import java.io.*;


public class CVSMiner extends Miner
{   
 private DataElement commandD;
 
 public void load()
 {
 }
    
 public void extendSchema(DataElement schemaRoot)
    {
	DataElement fsD         = _dataStore.find(schemaRoot, DE.A_NAME, "Filesystem Objects", 1);
	commandD = _dataStore.find(fsD, DE.A_VALUE, "C_COMMAND");
  
	DataElement cvsD      = createAbstractCommandDescriptor(fsD, "CVS", "C_CVS");

	DataElement updateD   = createCommandDescriptor(cvsD, "update",   "C_CVS_UPDATE", false);	
	DataElement checkoutD = createCommandDescriptor(cvsD, "checkout", "C_CVS_CHECKOUT", false);	
	DataElement commitD   = createCommandDescriptor(cvsD, "commit",   "C_CVS_COMMIT", false);	
	DataElement loginD    = createCommandDescriptor(cvsD, "login",    "C_CVS_LOGIN");	
	DataElement linputD   = _dataStore.createObject(loginD, "input", "Enter Password");	
    }
    
   public DataElement handleCommand(DataElement theCommand)
    {
     String name          = getCommandName(theCommand);
     DataElement  status  = getCommandStatus(theCommand);
     DataElement  subject = getCommandArgument(theCommand, 0); 
     DataElement  arg1 = getCommandArgument(theCommand, 1); 
     
 
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

    private DataElement getParentDirectory(DataElement resource)
    {
	File file = new File(resource.getSource());
	String parentStr = file.getParent();

	DataElement parent = _dataStore.createObject(null, "directory", parentStr, parentStr);
	return parent;
    }

    public void handleLogin(DataElement subject, DataElement password, DataElement status)
    {
	execute("cvs login", subject, status);
    }

    public void handleCheckout(DataElement subject, DataElement status)
    {
	execute("cvs checkout " +  getArgument(subject), getParentDirectory(subject), status);
    }
    
    public void handleUpdate(DataElement subject, DataElement status)
    {	
	execute("cvs update " + getArgument(subject), getParentDirectory(subject), status);
    }

    public void handleCommit(DataElement subject, DataElement status)
    {
	execute("cvs commit -m \"test\" ", subject, status);
    }
    
    private void execute(String theCommand, DataElement theSubject, DataElement status)
    {
     ArrayList args = new ArrayList();
     args.add(_dataStore.createObject(null, "invocation", theCommand));
     args.add(theSubject);  
     args.add(status);         
     _dataStore.command(commandD, args, theSubject);     
    }
}






