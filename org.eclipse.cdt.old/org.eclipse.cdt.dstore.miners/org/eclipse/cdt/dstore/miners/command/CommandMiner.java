package org.eclipse.cdt.dstore.miners.command;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.server.*;
import org.eclipse.cdt.dstore.core.miners.miner.*;
import org.eclipse.cdt.dstore.miners.filesystem.*;
import org.eclipse.cdt.dstore.miners.environment.*;
import org.eclipse.cdt.dstore.miners.command.patterns.*;

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
     

     DataElement cmdD          = createCommandDescriptor(fsD, "Command", "C_COMMAND", false);
     _dataStore.createReference(cancellable, cmdD, "abstracts", "abstracted by");
     
     DataElement shellD        = createCommandDescriptor(fsD, "Shell", "C_SHELL", false);
     _dataStore.createReference(cancellable, shellD, "abstracts", "abstracted by");

     DataElement inputD    = _dataStore.createObject(cmdD, "input", "Enter command");	
     DataElement outputD   = _dataStore.createObject(cmdD, "output", "Command Output");
     _dataStore.createObject(schemaRoot, "stdout", "stdout","org.eclipse.cdt.dstore.miners");
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
		  
		  //Remove All extra whitespace from the command
		  if (invocation.trim().length() > 0)
		  {
		   if (invocation.equals("?") || invocation.equals("help"))
		    invocation = "cat " + theElement.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + "/org.eclipse.cdt.dstore.miners/patterns.dat";
		   launchCommand(subject, invocation, status);
  
		  }
                  return status;
	      }
	  else
	      {
		  status.setAttribute(DE.A_NAME, "done");
	      }
      }
  else if (name.equals("C_SHELL"))
  {
   	String invocation = ">";
  	launchCommand(subject, invocation, status);
  }
  else if (name.equals("C_SEND_INPUT"))
  {
  	DataElement input = getCommandArgument(theElement, 1);
  	DataElement de = (DataElement)subject.dereference().get(1);
  	sendInputToCommand(de.getName().trim(), input.getName(), getCommandStatus(subject));
  }
  else if (name.equals("C_CANCEL"))
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
  _threads.put(status.getAttribute(DE.A_ID), newCommand);
  newCommand.start();
 }

 private void sendInputToCommand(String theCommand, String input, DataElement status)
 {
  CommandMinerThread theThread = (CommandMinerThread)_threads.get(status.getAttribute(DE.A_ID));

     
   if (theThread != null)
   {
   	  theThread.sendInput(input);  
  	}

 }

 private void cancelCommand (String theCommand, DataElement status)
 {
  CommandMinerThread theThread = (CommandMinerThread)_threads.get(status.getAttribute(DE.A_ID));

  if (theThread != null)
  { 	
      theThread.stopThread();
      boolean done = false;
      long stopIn = System.currentTimeMillis() + 3000;
      
      while (!done)
	  if ( (!theThread.isAlive()) || (stopIn < System.currentTimeMillis()) )
	      done = true;

  	_dataStore.createObject(status, "stdout", "Command Cancelled by User Request");
  	_dataStore.refresh(status);
 }
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
 
 private boolean          _isShell;
 private boolean          _isDone;
  
 public CommandMinerThread (DataElement theElement, String invocation, DataElement status, Patterns thePatterns)
 {
  _isShell = false; 
  _isDone  = false;
  _status     = status;
  _dataStore  =  theElement.getDataStore();
  _subject = theElement;
 
  _invocation = invocation.trim();
  _patterns = thePatterns;
  _patterns.refresh(_invocation);
  ServerCommandHandler sch = (ServerCommandHandler)(_dataStore.getCommandHandler());
  _fileMiner = (FileSystemMiner)(sch.getMiners("org.eclipse.cdt.dstore.miners.filesystem.FileSystemMiner"));
  
  
     
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

		if (_invocation.equals(">"))
		{
			_invocation = theShell;
		}


	    String args[] = new String[3];
	    args[0] = theShell;
	    args[1] = "-c";
	    args[2] = _invocation;

	    _theProcess = Runtime.getRuntime().exec(args, env, theDirectory); 
	}
    else
	{
	    theShell = "cmd";
	    if (_invocation.equals(">"))
	    {
	    	_invocation = theShell;
	    	_isShell = true;
	    }
	
	    _theProcess = Runtime.getRuntime().exec(theShell + " /C " + _invocation, getEnvironment(_subject), theDirectory); 
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
   
   //This dataElement is where the handleQuerys from FileSystem Miner get put during a find file
  createObject("command", "> " + _invocation);
  createObject("stdout","");

  status.setAttribute(DE.A_NAME, "progress");  
   _dataStore.update(status);
    
    _stdOutputHandler = new OutputHandler(_stdInput, null);
    _stdOutputHandler.setWaitTime(0);
    _stdOutputHandler.start();
    _stdErrorHandler = new OutputHandler(_stdError, null);
    _stdErrorHandler.setWaitTime(0);
    _stdErrorHandler.start();  
 }
 
 public Process getProcess()
 {
 	return _theProcess;
 }
 
 public void sendInput(String input)
 {
 	if (!_isDone)
 	{
 	byte[] intoout = input.getBytes();
 	 
	 OutputStream output = _theProcess.getOutputStream();
   	 try
   		{   	  	 	
   	  	 	output.write(intoout, 0, input.length());
   	  	 	output.write('\n');
   	  	 	output.flush();
   	  	 	
   	  	 	createObject("command", input);
   	  	 	
   	  	 	if (_isShell)
   	  	 	{
   	  	 	  _patterns.refresh(input);
   	  	 	}
   	  	 }
   	  	 catch (IOException e)
   	  	 {
   	  	 	System.out.println(e);
   	  	 }	
 	}
 }
 
 
 private String[] getEnvironment(DataElement theSubject)
 {
  //Grab the system environment:
  DataElement envMiner  = _dataStore.findMinerInformation("org.eclipse.cdt.dstore.miners.environment.EnvironmentMiner");
  DataElement systemEnv = _dataStore.find(envMiner, DE.A_NAME, "System Environment", 1);
  
  //Grab the project environment
  ArrayList projectEnvReference = theSubject.getAssociated("inhabits");
  DataElement projectEnv = null;
  if (projectEnvReference != null && (projectEnvReference.size() > 0))
   projectEnv = (DataElement)projectEnvReference.get(0);
  String[] theEnv = mergeEnvironments(systemEnv, projectEnv);
  return theEnv;
 }

 private String[] mergeEnvironments(DataElement systemEnv, DataElement projectEnv)
 {
  ArrayList prjVars = null;
  ArrayList sysVars = null;

  //Fill the ArrayLists with the environment variables
  if (systemEnv != null)   sysVars= systemEnv.getNestedData();
  if (projectEnv != null)  prjVars = projectEnv.getNestedData();

  //If one or both of the ArrayLists are null, exit early:
  if ( (sysVars == null) || (sysVars.size() == 0) )
   return listToArray(prjVars);
  if ( (prjVars == null) || (prjVars.size() == 0) )
   return listToArray(sysVars);


  //If we get here, then we have both system and project variables...to make merging the 2 lists easier, we'll
  //use a Hashtable (Variable Names are the keys, Variables Values are the values):
  Hashtable varTable = new Hashtable();
  
  //First fill the varTable with the sysVars
  varTable.putAll(mapVars(sysVars));
  
  //Now for every project variable, check to see if it already exists, and if the value contains other variables:
  for (int i = 0; i < prjVars.size(); i++)
  {
   DataElement envElement = (DataElement)prjVars.get(i);
   if (!envElement.getType().equals("Environment Variable"))
    continue;
   String theVariable = envElement.getName();
   String theKey      = getKey(theVariable);
   String theValue    = getValue(theVariable);
   theValue = calculateValue(theValue, varTable);
   varTable.put(theKey, theValue);
  }
  return tableToArray(varTable);
 }
 
 //This method is responsible for replacing variable references with their values.
 //We support 3 methods of referencing a variable (assume we are referencing a variable called FOO):
 // 1. $FOO     - common to most shells (must be followed by a non-alphanumeric or nothing...in other words, we
 //               always construct the longest name after the $)
 // 2. ${FOO}   - used when you want do something like ${FOO}bar, since $FOObar means a variable named FOObar not 
 //               the value of FOO followed by "bar". 
 // 3. %FOO%    - Windows command interpreter
 private String calculateValue(String value, Hashtable theTable)
 {
  StringBuffer theValue = new StringBuffer(value); 
  try
  {
   int index = 0;
   char c;
   while (index < theValue.length())
   {  
    c = theValue.charAt(index);
    //If the current char is a $, then look for a { or just match alphanumerics
    if (c == '$')
    {
     int nextIndex = index + 1;
     if (nextIndex < theValue.length())
     {
      c = theValue.charAt(nextIndex);
       //If there is a { then we just look for the closing }, and replace the span with the variable value
      if (c == '{')
      {
       int next = theValue.toString().indexOf("}",nextIndex);
       if (next > 0)
       {
        String replacementValue = findValue(theValue.substring(nextIndex+1, next), theTable, true);
        theValue.replace(index, next+1, replacementValue);
        index += replacementValue.length() - 1;
       }
      }
      //If there is no { then we just keep matching alphanumerics to construct the longest possible variable name
      else
      {
       if (Character.isJavaIdentifierStart(c))
       {
        while ((nextIndex < theValue.length()) && (Character.isJavaIdentifierPart(c)))
         c = theValue.charAt(++nextIndex);
        String replacementValue = findValue(theValue.substring(index+1, nextIndex),theTable, true);
        theValue.replace(index, nextIndex, replacementValue);
        index += replacementValue.length() - 1;
       }
      }
     } 
    }
    //If the current char is a %, then simply look for a matching %
    else if (c == '%')
    {
     int next = theValue.toString().indexOf("%",index+1);
     if (next > 0)
     {
      String replacementValue = findValue(theValue.substring(index+1, next), theTable, false);
      theValue.replace(index, next+1, replacementValue);
      index += replacementValue.length() - 1;
     }
    }
    index++; 
   }
  }
  catch (Throwable e) {e.printStackTrace();}  
  return theValue.toString();
 }

 private String findValue(String key, Hashtable theTable, boolean caseSensitive)
 {
  Object theValue = null;
  if (caseSensitive)
   theValue = theTable.get(key);
  else
  {
   String matchString = key.toUpperCase();
   for (Enumeration e=theTable.keys(); e.hasMoreElements();)
   {
    String theKey = (String)e.nextElement();
    if (matchString.equals(theKey.toUpperCase()))
     theValue = (String)theTable.get(theKey);
   }
  }
  if (theValue == null)
   return "";
  return (String)theValue;
 } 

 private String getKey(String var)
 {
  int index = var.indexOf("=");
  if (index < 0)
   return var;
  return var.substring(0,index);
 }

 private String getValue(String var)
 {
  int index     = var.indexOf("=") + 1;
  int varLength = var.length();
  if ( (index < 1) || (index == var.length()))
   return "";
  return var.substring(index, varLength);
 }
 
 private Hashtable mapVars(ArrayList theVars)
 {
  Hashtable theTable = new Hashtable();
  int theSize = theVars.size();
  for (int i = 0; i < theSize; i++)
  {
   String theVar = ((DataElement)theVars.get(i)).getName();
   theTable.put(getKey(theVar), getValue(theVar));
  }
  return theTable;
 }

 private String[] listToArray(ArrayList theList)
 {
  if (theList == null)
   theList = new ArrayList();
  int theSize = theList.size();
  String theArray[] = new String[theSize];
  for (int i = 0; i < theSize; i++)
   theArray[i] = ((DataElement)theList.get(i)).getName(); 
  return theArray;
 } 

 private String[] tableToArray(Hashtable theTable)
 {
  if (theTable == null)
   theTable = new Hashtable();
  int theSize = theTable.size();
  String theArray[] = new String[theSize];
  int i = 0;
  for (Enumeration e = theTable.keys() ; e.hasMoreElements() ;) 
  {
   String theKey = (String)e.nextElement();
   String theValue = (String)theTable.get(theKey);
   theArray[i++] = theKey + "=" + theValue;
  }
  return theArray;
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
    	_isDone = true;
    	if (_isShell)
    	{
    		sendInput("exit");
    	}
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
				//e.printStackTrace();
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
     fileName = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + "/org.eclipse.cdt.dstore.miners/patterns.dat";
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
 public DataElement createObject (String type, String text)
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









