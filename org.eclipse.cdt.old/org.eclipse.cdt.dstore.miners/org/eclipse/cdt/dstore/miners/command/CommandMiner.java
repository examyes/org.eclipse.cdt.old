package com.ibm.dstore.miners.command;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.server.*;
import com.ibm.dstore.core.miners.miner.*;
import com.ibm.dstore.miners.filesystem.*;
import com.ibm.dstore.miners.environment.*;
import com.ibm.dstore.miners.command.patterns.*;

import java.io.*;
import java.util.*;
import java.lang.*;

public class CommandMiner extends Miner
{
 private Hashtable _threads = new Hashtable();
 private Patterns _patterns;
 
 public CommandMiner() { super();  }
 public void finish()  { super.finish(); }
 public void load()    
 {
  _patterns = new Patterns(_dataStore);
 }

 public void extendSchema(DataElement schemaRoot)
 {
     DataElement fsD           = _dataStore.find(schemaRoot, DE.A_NAME, "Filesystem Objects", 1);
     DataElement cancellable   = _dataStore.find(schemaRoot, DE.A_NAME, getLocalizedString("model.Cancellable"), 1);
     

     DataElement cmdD          = createCommandDescriptor(fsD, "Command", "C_COMMAND");
     _dataStore.createReference(cancellable, cmdD, "abstracts", "abstracted by");

     DataElement inputD    = _dataStore.createObject(cmdD, "input", "Enter command");	
     DataElement outputD   = _dataStore.createObject(cmdD, "output", "Command Output");
 }

 public DataElement handleCommand (DataElement theElement)
 {
  String          name = getCommandName(theElement);
  DataElement   status = getCommandStatus(theElement);
  DataElement  subject = getCommandArgument(theElement, 0);

  if (name.equals("C_COMMAND"))
      {
	  DataElement   invArg = getCommandArgument(theElement, 1);
	  if (invArg != null)
	      {
		  String    invocation = invArg.getName();
		  
		  //Remove all extra whitespace from the command
		  if (invocation.trim().length() > 0)
		  {
		   if (invocation.equals("?") || invocation.equals("help"))
		    invocation = "cat " + theElement.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + "/com.ibm.dstore.miners/patterns.dat";
		   
                   launchCommand(subject, invocation, status);
		  }
                  return status;
	      }
	  else
	      {
		  status.setAttribute(DE.A_NAME, "done");
	      }
      }
  if (name.equals("C_CANCEL"))
      {
	  DataElement de = (DataElement)subject.dereference().get(1);
	  DataElement cancelStatus = getCommandStatus(subject);
	  cancelCommand(de.getName().trim(),cancelStatus);
	  return status;
      }
  return status;
 }
    
 public void launchCommand (DataElement subject, String invocation, DataElement status)
 {
  //First Check to make sure that there are no "zombie" threads
  for (Enumeration e = _threads.keys() ; e.hasMoreElements() ;) 
  {
   String threadName = (String)e.nextElement();
   CommandMinerThread theThread = (CommandMinerThread)_threads.get(threadName);
   if ( (theThread == null) || (!theThread.isAlive())) 
   {
    _threads.remove(threadName);
    
   }
  }
  CommandMinerThread newCommand = new CommandMinerThread(subject, invocation, status, _patterns);
  _threads.put(invocation, newCommand);
  newCommand.start();
 }

 private void cancelCommand (String theCommand, DataElement status)
 {
  CommandMinerThread theThread = (CommandMinerThread)_threads.get(theCommand);
   
  if (theThread != null)
  {
      theThread.stopThread();
      boolean done = false;
      long stopIn = System.currentTimeMillis() + 3000;
      
      while (!done)
	  if ( (!theThread.isAlive()) || (stopIn < System.currentTimeMillis()) )
	      done = true;
  }
  _dataStore.createObject(status, "stdout", "Command Cancelled by User Request");
  _dataStore.update(status);
 }
}

class CommandMinerThread extends MinerThread
{
    public class OutputHandler extends Handler
    {
	private BufferedReader _reader;
	private String         _qualifier;

	public OutputHandler(BufferedReader reader, String qualifier)
	{
	    _reader = reader;
	    _qualifier = qualifier;
	}

	public void handle()
	{
	    String line = readLine();		    
	    
	    if (line != null)
		{
		    if (_qualifier != null)
			{
			    line = _qualifier + ": " + line;
			}

		    interpretLine(line);
		}
	    else
		{
		    finish();
		}
	}

	private String readLine ()
	{	    
	    StringBuffer theLine = new StringBuffer();
	    int ch;
	    boolean done = false;
	    while(!done && !isFinished())
		{
		    try
			{
			    ch = _reader.read();
			    switch (ch)
				{
				case -1    : if (theLine.length() == 0)       //End of Reader 
				    return null; 
				    done = true; 
				    break;                  
				case 65535 : if (theLine.length() == 0)       //Check why I keep getting this!!! 
				    return null; 
				    done = true; 
				    break;                     
				case 10    : done = true;                     //Newline
				    break;           
				case 9     : theLine.append("     ");         //Tab
				    break; 
				case 13    : break;                          //Carriage Return
				default    : theLine.append((char)ch);             //Any other character
				}
			}
		    catch (IOException e)
			{
			    return null;
			}
		}
	    return theLine.toString();
	}

    }


 private DataElement     _status;
 private DataStore       _dataStore;
 private String          _invocation;
 private BufferedReader  _stdInput;
 private BufferedReader  _stdError;
 private Patterns        _patterns;
 private FileSystemMiner _fileMiner;
 private Process         _theProcess;
 private String          _fileLocation;
    private DataElement _subject;

 private OutputHandler    _stdOutputHandler;
 private OutputHandler    _stdErrorHandler;
  
 public CommandMinerThread (DataElement theElement, String invocation, DataElement status, Patterns thePatterns)
 {
  
  _status     = status;
  _dataStore  =  theElement.getDataStore();
  _subject = theElement;
 
  _invocation = invocation.trim();
  _patterns = thePatterns;
  _patterns.refresh(_invocation);
  ServerCommandHandler sch = (ServerCommandHandler)(_dataStore.getCommandHandler());
  _fileMiner = (FileSystemMiner)(sch.getMiners("com.ibm.dstore.miners.filesystem.FileSystemMiner"));
  
  //This dataElement is where the handleQuerys from FileSystem Miner get put during a find file
  createObject("command", "> " + _invocation);
  createObject("stdout","");
  _dataStore.update(status);
  
   
   try
   {
    File theDirectory = new File(theElement.getSource().trim());
    if (!theDirectory.isDirectory())
	theDirectory = theDirectory.getParentFile();
    String theOS = System.getProperty("os.name");
    String theShell = null;    


    if (!theOS.toLowerCase().startsWith("win"))
	{
	    String property = "SHELL=";
	    String[] env = getEnvironment(_subject);
	    for (int i = 0; i < env.length; i++)
		{
		    String var = env[i];
		    if (var.startsWith(property))
			{
			    theShell = var.substring(property.length(), var.length());
			}
		}

	    if (theShell == null)
		{
		    theShell = "sh";
		}


	    String args[] = new String[3];
	    args[0] = theShell;
	    args[1] = "-c";
	    args[2] = _invocation;

	    _theProcess = Runtime.getRuntime().exec(args, env, theDirectory); 
	}
    else
	{
	    theShell = "cmd /c ";
	    _theProcess = Runtime.getRuntime().exec(theShell + _invocation, getEnvironment(_subject), theDirectory); 
	}
    
    _stdInput = new BufferedReader(new InputStreamReader(_theProcess.getInputStream()));
    _stdError = new BufferedReader(new InputStreamReader(_theProcess.getErrorStream()));
   }
   catch (IOException e) 
   {
    _theProcess = null;
    e.printStackTrace();
    System.out.println(e.getMessage());
    return;
   }
   
   status.setAttribute(DE.A_NAME, "progress");  
  
    _stdOutputHandler = new OutputHandler(_stdInput, null);
    _stdOutputHandler.setWaitTime(0);
    _stdOutputHandler.start();
    _stdErrorHandler  = new OutputHandler(_stdError, null);
    _stdErrorHandler.setWaitTime(0);
    _stdErrorHandler.start();  
 }
 
 
 private String[] getEnvironment(DataElement theSubject)
 {
  ArrayList theVars = new ArrayList();
 
  try
  {
   //First grab the system environment:
   DataElement envMiner  = _dataStore.findMinerInformation("com.ibm.dstore.miners.environment.EnvironmentMiner");
   DataElement systemEnv = _dataStore.find(envMiner, DE.A_NAME, "System Environment", 1);
   ArrayList systemVars  = systemEnv.getNestedData();
   int MAX = systemVars.size();
   for (int i=0; i<MAX; i++)
    theVars.add(((DataElement)systemVars.get(i)).getName());
  
   ArrayList prjEnv = theSubject.getAssociated("inhabits");
   if (prjEnv != null && (prjEnv.size() > 0))
       {
	   DataElement theEnvironment = (DataElement)prjEnv.get(0);
	   ArrayList varElements = theEnvironment.getNestedData();
	   MAX = varElements.size();
	   for (int i = 0; i<MAX; i++)
	       {
		   String var = ((DataElement)varElements.get(i)).getName();
		   theVars.add(var);
		   System.out.println(var);
	       }
       }
  
  }
  catch (Throwable e) 
      {
	  e.printStackTrace();	  
      }  
  
  int MAX = theVars.size();
  String[] env = new String[MAX];
  for(int i = 0; i< MAX; i++)
   {
    env[i] = (String)theVars.get(i);
   }
  
  return env;
 }
 
 public boolean doThreadedWork()
 {
   if (((_stdOutputHandler == null) || _stdOutputHandler.isFinished()) && 
       ((_stdErrorHandler == null) ||_stdErrorHandler.isFinished()))
     {
       return false;
     }
   else
     {
       return true;
     }
 }
 
 public void initializeThread()
 {
 }

    public void cleanupThread()
    {
	try
	    {
		_status.setAttribute(DE.A_NAME, "done");
		_dataStore.refresh(_status, true);
		_subject.refresh(false);
		
		_stdOutputHandler.finish();
		_stdErrorHandler.finish();

		_stdInput.close();
		_stdError.close();

		if (_theProcess != null)
		    {
			int exitcode;
			try 
			    {
				if (_isCancelled)
				    {
					_theProcess.destroy();
				    }
				else
				    {
					exitcode = _theProcess.exitValue();
					createObject("command", "> Command Completed (exit code = " + exitcode + ")");
				    }
			    }
			catch (IllegalThreadStateException e)
			    {
				e.printStackTrace();
				exitcode = -1;
				_theProcess.destroy();
			    }			
		    }		
	    }
	catch (IOException e) 
	    {
		e.printStackTrace();
	    }
    }


 


/************************************************************************************************
    private String readLine()

    It seems there is a bug in BufferedReader.readLine(), where it counts \r\n at the end of 
    a line as two lines (and nmake spits out lines with that combination at the end). So for 
    now I'll just do my own readLine().  Should investigate the following:
       1. If it is a real bug in readLine or I'm using it incorrectly
       2. If the cast to char in the while expression below is expensive or not.  Although
          I would have to cast it at some point anyway.  
       3. Figure out what to do about exceptions.  Should probably print or log something.
       4. It seems when I remove the yield(), the ability to Cancel threads is limited.  
 *************************************************************************************************/
 private String readLine ()
 {
  //First Check to see if the process is still alive
  //try
  //{
  if ( (_theProcess == null) || (_stdInput == null))
    return null;
  // int returnCode = _theProcess.exitValue();
   //If we get here, then the process has finished.
  // return null;
  // }
  //catch (IllegalThreadStateException e) {}
  

  //Next read lines from the error stream first

  StringBuffer theLine = new StringBuffer();
  int ch;
  boolean done = false;
  try
  {
   while(!done)
   {
    ch = _stdInput.read();
    switch (ch)
    {
     case -1    : if (theLine.length() == 0)       //End of Reader 
                   return null; 
                  done = true; 
                  break;                  
     case 65535 : if (theLine.length() == 0)       //Check why I keep getting this!!! 
                   return null; 
                  done = true; 
                  break;                     
     case 10    : done = true;                     //Newline
                  break;           
     case 9     : theLine.append("     ");         //Tab
                  break; 
     case 13    : break;                          //Carriage Return
     default    : theLine.append((char)ch);             //Any other character
    }
   }
  }
  catch (IOException e)  { e.printStackTrace(); }
  //yield();
  return theLine.toString();
 }
 
 

/************************************************************************************************
    private void interpretLine(String)

    Parse a line of output and, if it is not a special object (one macthing a format spec.), 
    then just create a simple object to show the line...Otherwise call the more elaborate
    object creation method.
 *************************************************************************************************/
 private void interpretLine(String line)
 {     
  ParsedOutput parsedMsg = _patterns.matchLine(removeWhitespace(line));
  if ((parsedMsg == null))
   createObject("stdout", line); 
  
  else
  {
   try
   {
    String fileName = parsedMsg.file;
    if (fileName.equals("PATTERNS.DAT"))
     fileName = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + "/com.ibm.dstore.miners/patterns.dat";
    createObject(parsedMsg.type, line, fileName, new Integer(parsedMsg.line), new Integer(parsedMsg.col));
   }
   catch (NumberFormatException e) 
       {
	   e.printStackTrace();
       }
   
  }
  _dataStore.update(_status);
 }

public String removeWhitespace(String theLine)
 {
  StringBuffer strippedLine = new StringBuffer();
  boolean inWhitespace = true;
  char curChar;
  for (int i=0; i<theLine.length(); i++)
  {
   curChar = theLine.charAt(i);
   if (curChar == '\t')
   {
    if (!inWhitespace) 
    {
     strippedLine.append(' ');
     inWhitespace = true;
    }
   }
   else if (curChar == ' ')
   {
    if (!inWhitespace) 
    {
     strippedLine.append(' ');
     inWhitespace = true;
    }
   }
   else
   {
    strippedLine.append(curChar);
    inWhitespace = false;
   }
  }
  return strippedLine.toString();
 }

 /************************************************************************************************
    private void createObject (String,String)
    Create a simple object with no source information
 *************************************************************************************************/
 private DataElement createObject (String type, String text)
 {
   return _dataStore.createObject(_status, type, text, "");
 }
 


 /************************************************************************************************
    private void createObject (String,String,String,Integer,Integer)
   
    Create an object that can contain file information as well as line an column.            
    Note: currently our editors do not support jumping to a column, so neither
    do we here.
 *************************************************************************************************/
 private DataElement createObject (String type, String text, String file, Integer line, Integer col)
 {
  if (file != null && file.length() > 0)
   {
       DataElement fileElement = null;
       // check for fully qualified file 
       File qfile = new File(file);
       if (!qfile.exists())
	   {
	       qfile = new File(_subject.getSource() + "/" + file);
	       if (!qfile.exists())
		   {
		       DataElement subStatus = _dataStore.createObject(null, "status", "find"); 
		       DataElement statusElement =_fileMiner.findFile(_subject, file, subStatus);
		       fileElement = statusElement.get(0);
		       if (fileElement != null)
			   {
			       String fileStr = fileElement.getSource();
			       if (fileStr.length() > 0)
				   {
				       file = fileStr;
				   }
			   }
		   }
	       else
		   {
		       file = _subject.getSource() + "/" + file;
		   }
	   }
   
       DataElement obj = null;
       if (line == null || (line.intValue() == 1))
	   {
	       obj =  _dataStore.createObject(_status, type, text, file);
	   }
       else
	   {
	       obj = _dataStore.createObject(_status, type, text, 
							 file + ":" + line.intValue());
	   }

       if (fileElement != null)
	   {
	       _dataStore.createReference(obj, fileElement);
	   }

       return obj;
   }
  else
      {
	  return createObject(type, text);
      }   
 }
}









