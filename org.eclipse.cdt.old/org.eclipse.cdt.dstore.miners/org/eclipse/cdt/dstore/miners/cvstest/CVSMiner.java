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
  DataElement dirD         = _dataStore.find(schemaRoot, DE.A_NAME, "directory", 1);
  commandD = _dataStore.find(dirD, DE.A_VALUE, "C_COMMAND");
  
  DataElement fileD        = _dataStore.find(schemaRoot, DE.A_NAME, "file", 1);
	
  DataElement cvsD      = createAbstractCommandDescriptor(fsD, "CVS", "C_CVS");
  DataElement updateD   = createCommandDescriptor(cvsD, "update",   "C_CVS_UPDATE");	
  DataElement checkoutD = createCommandDescriptor(cvsD, "checkout", "C_CVS_CHECKOUT");	
  _dataStore.createObject(checkoutD, "input", "Enter argument: ");
	

  DataElement commitD   = createCommandDescriptor(cvsD, "commit",   "C_CVS_COMMIT");	
  createReference(fileD, cvsD);
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
	     handleCheckout(subject, arg1, status);
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
	execute("cvs login", subject);
    }

 public void handleCheckout(DataElement subject, DataElement arg1, DataElement status)
 {
  execute("cvs checkout " + arg1.getName(), subject);
 }

    public void handleUpdate(DataElement subject, DataElement status)
    {
	String args = getArgument(subject);
	execute("cvs update " + args, subject);
    }

    public void handleCommit(DataElement subject, DataElement status)
    {
	String args = getArgument(subject);
	execute("cvs commit -m \"test\" " + args, subject);
    }
    
    private void execute(String theCommand, DataElement theSubject)
    {
     ArrayList args = new ArrayList();
     args.add(_dataStore.createObject(null, "invocation", theCommand));
     args.add(theSubject);  
         
     DataElement status = _dataStore.command(commandD, args, theSubject, true);
     //while (!status.getValue().equals("done"))
      System.out.println("STATUS => " + status.getValue());
     theSubject.refresh(true);
    }
}






