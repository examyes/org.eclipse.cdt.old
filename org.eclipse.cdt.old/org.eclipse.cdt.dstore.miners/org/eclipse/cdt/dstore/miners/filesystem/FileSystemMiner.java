package com.ibm.dstore.miners.filesystem;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */


import com.ibm.dstore.core.miners.miner.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.util.*;

import java.lang.*;
import java.io.*;
import java.util.*;

public class FileSystemMiner extends Miner
{
  public FileSystemMiner ()
      {
	super();
      };


  public void finish()
      {
	super.finish();	
      }

    public ResourceBundle getResourceBundle()  
    {
	ResourceBundle resourceBundle = null;
	// setup resource bundle
	try
	    {
		resourceBundle = ResourceBundle.getBundle(getName());
	    }
	catch (MissingResourceException mre)
	    {
	    }	  	

	return resourceBundle;
    }

  public void load(DataElement status)
      {
	  // load persisted information
	  DataElement host =  _dataStore.getHostRoot();
	  DataElement minerRoot = null;
	  String dir = host.getSource();
	  String dir2 = dir.substring(0,1).toUpperCase()+dir.substring(1,dir.length());	
	  File hostFile = new File(dir2);
	  	 
	  File[] deviceList = File.listRoots();
	  for(int i=0;i<deviceList.length;i++)
	      {		  
		  DataElement temp=_dataStore.createObject(_minerData, getLocalizedString("model.device"),deviceList[i].getPath(),deviceList[i].getPath());
	      }	 

	  DataElement currentDirectory = findFile(_minerData, hostFile);
	  if (currentDirectory != null)
	      {
		  System.out.println("currentDir:" + currentDirectory.getName());
		  DataElement ref = _dataStore.createReference(host,currentDirectory);
	      }
	  else
	      {
		  status.setAttribute(DE.A_NAME, getLocalizedString("model.incomplete"));
		  _dataStore.createObject(status, "message", "Specified host directory does not exist.  Please correct the specification and reconnect.");
	      }
      }

    public void updateMinerInfo()
    {
        DataElement host =  _dataStore.getHostRoot();
	_dataStore.refresh(host, true);

	DataElement cwd = host.get(0).dereference();
	DataElement theElement = cwd;
	_dataStore.refresh(_minerData);
	refreshFrom(cwd);
    }

    private void refreshFrom(DataElement child)
    {
	DataElement parent = child.getParent();
	if (parent != _minerData)
	    {
		refreshFrom(parent);
	    }

	_dataStore.refresh(child);
    }


    private DataElement findFile(DataElement root, File path)
    {	
	DataElement result = null;
	if (root.getType().equals("data"))
	    {
		for (int i =0; i < root.getNestedSize(); i++)
		    {
			DataElement device= root.get(i);
			String path_device=path.getPath();		      		
			if(device.getName().equals(path_device))
			    return device;			
			if(device.getSource().startsWith(path_device.substring(0,path_device.indexOf(File.separator))))
			    {				
				DataElement tempstatus=_dataStore.createObject(null,"status","start");
				handleQuery(device,tempstatus);
				result = findFile(device, path);
			    }	  			 
		    }
	    }
	else
	    {
		for (int i = 0; i < root.getNestedSize(); i++)
		    {
			DataElement directory = root.get(i);					
			if (directory.getSource().equals(path.getPath()))
			    {
				return directory;
			    }
			if (path.getPath().startsWith(directory.getSource()+File.separator))
			    {			
				DataElement tempstatus=_dataStore.createObject(null,"status","start");
				handleQuery(directory,tempstatus);				
				result= findFile(directory,path);
				break;
			    }			
		    }
	    }
	
	return result;
    }
    

  public void extendSchema(DataElement schemaRoot)
      {
        DataElement fsObject  = _dataStore.find(schemaRoot, DE.A_NAME, "Filesystem Objects", 1);
        DataElement deviceD   = _dataStore.find(schemaRoot, DE.A_NAME, getLocalizedString("model.device"), 1);
     
	DataElement dirD      = _dataStore.find(schemaRoot, DE.A_NAME, getLocalizedString("model.directory"), 1);
        DataElement hostD     = _dataStore.find(schemaRoot, DE.A_NAME, getLocalizedString("model.host"), 1);
        DataElement fileD     = _dataStore.find(schemaRoot, DE.A_NAME, getLocalizedString("model.file"), 1);
	DataElement rootD     = _dataStore.find(schemaRoot, DE.A_NAME, getLocalizedString("model.root"), 1);
	DataElement projectD  = _dataStore.find(schemaRoot, DE.A_NAME, getLocalizedString("model.project"), 1);
	DataElement containsD = _dataStore.find(schemaRoot, DE.A_NAME, getLocalizedString("model.contents"), 1);
	DataElement parentD   = _dataStore.find(schemaRoot, DE.A_NAME, getLocalizedString("model.parent"), 1);	

 	DataElement dirQuery  = createCommandDescriptor(fsObject, getLocalizedString("model.Query"), "C_QUERY", false);
	DataElement refresh   = createCommandDescriptor(fsObject, getLocalizedString("model.Refresh"), "C_REFRESH");

 	DataElement fileQuery  = createCommandDescriptor(fileD,    getLocalizedString("model.Query"), "C_QUERY", false);
	fileQuery.setAttribute(DE.A_SOURCE, "-");
	DataElement fileRefresh   = createCommandDescriptor(fileD, getLocalizedString("model.Refresh"), "C_REFRESH", false);
	fileRefresh.setAttribute(DE.A_SOURCE, "-");

	//      DataElement createD   = createCommandDescriptor(dirD, "Create", "C_CREATE", false);
 	DataElement fdateD    = createCommandDescriptor(fsObject, "Get Date", "C_DATE", false);
 	DataElement setDateD  = createCommandDescriptor(fsObject,  "Set Date", "C_SET_DATE", false);

       	DataElement open      = createCommandDescriptor(fileD, getLocalizedString("model.Open"), "C_OPEN");
        DataElement move      = createCommandDescriptor(fsObject, getLocalizedString("model.Move"), "C_MOVE", false);
	//        DataElement delete    = createCommandDescriptor(fsObject, getLocalizedString("model.Delete"), "C_DELETE");        
      	///////////////////////////
	//renaming files and dirs
	DataElement renF = createCommandDescriptor(fileD, getLocalizedString("model.Rename"), "C_RENAME_FILE");
	renF.setDepth(1);
	DataElement inRenF = _dataStore.createObject(renF,"input", "Enter the New Name");

	DataElement renD= createCommandDescriptor(dirD, getLocalizedString("model.Rename"), "C_RENAME_DIR");
	DataElement inrenD = _dataStore.createObject(renD,"input", "Enter the New Name");

	DataElement findD = createCommandDescriptor(dirD, "Find", "C_FIND_FILE", false);

	//deleting files and dirs
	DataElement del = createCommandDescriptor(fsObject,getLocalizedString("model.Delete"),"C_DELETE");

	//creating new files and dirs
	DataElement newFD=createAbstractCommandDescriptor(dirD,getLocalizedString("model.Create"),"C_NEW");

       	DataElement newF = createCommandDescriptor(newFD, getLocalizedString("model.Create_File"),"C_CREATE_FILE");
       	DataElement inNewF = _dataStore.createObject(newF, "input", "Enter Name for New file");

       	DataElement newD = createCommandDescriptor(newFD,getLocalizedString("model.Create_Directory"),"C_CREATE_DIR");
	DataElement inNewD = _dataStore.createObject(newD, "input", "Enter Name for New Directory");
	//////////////////////////

      }

  public DataElement handleCommand (DataElement theElement)
   {	
     String name         = getCommandName(theElement);    

     DataElement status  = getCommandStatus(theElement);

     DataElement subject = getCommandArgument(theElement, 0);
     if (name.equals("C_QUERY"))
       {
         if (subject != null)
         {
	     status = handleQuery(subject, status);
         }
       }
     else if (name.equals("C_DATE"))
	 {
	     status = handleDate(subject, status);
	 }
     else if (name.equals("C_SET_DATE"))
	 {
	     status = handleSetDate(subject, getCommandArgument(theElement, 1), status);
	 }
     else if (name.equals("C_REFRESH"))
     {
	 status = handleRefresh(subject, status);
     }
     else if (name.equals("C_FIND_FILE"))
       {
	 DataElement subElement1 = getCommandArgument(theElement, 0);
	 DataElement subElement2 = getCommandArgument(theElement, 1);
	 status = handleFind(subElement1.dereference(), subElement2.getName(), status);	 
       }
     else if (name.equals("C_SET_TYPE"))
       {
	 DataElement subElement1 = getCommandArgument(theElement, 0);
	 DataElement subElement2 = getCommandArgument(theElement, 1);
	 status = handleSetType(subElement1.dereference(), subElement2.dereference(), status);	 
       }
     else if (name.equals("C_IMPORT"))
     {
	 DataElement subElement1 = getCommandArgument(theElement, 0);
	 DataElement subElement2 = getCommandArgument(theElement, 1);
	 status = handleImport(subElement1.dereference(), subElement2, status);	 
     }
     else if (name.equals("C_OPEN"))
       {
	 status = handleOpen(subject, status);
       }
     else if (name.equals("C_MOVE"))
     {
       DataElement moveSource = getCommandArgument(theElement, 1);
       status = handleMove(subject.dereference(), moveSource.dereference(), status);
     }

      //////////////////////
     else if ((name.equals("C_RENAME_FILE"))||(name.equals("C_RENAME_DIR")))
	 {
	     DataElement newName = getCommandArgument(theElement,1);	     
	     status = handleRename(subject,newName,status);
	 }  
     else if ((name.equals("C_DELETE")))
	 {
	     status = handleDeleteFileDir(subject, status);
	 }
     else if (name.equals("C_CREATE_FILE"))
	 {
	     DataElement newName = getCommandArgument(theElement,1);
	     status = handleCreateFile(subject,newName,status);
	 }
      else if (name.equals("C_CREATE_DIR"))
	 {
	     DataElement newName = getCommandArgument(theElement,1);
	     status = handleCreateDir(subject,newName,status);
	 }
     return status;
   }
    
    //////////////////////    
    private DataElement handleRename(DataElement subject, DataElement newName, DataElement status)
    {
	File toBeRenamed = new File(subject.getSource());
	
	//construct a string with the new name
	StringBuffer path = new StringBuffer(toBeRenamed.getParent()); //get its directory
	path.append(File.separator);
	path.append(newName.getName());// construct the full path to the new filename
	
	File newFile = new File(path.toString());
	if(newFile.exists())
	    {
		//A file with the same new name already exists:Abort renaming
		status.setAttribute(DE.A_NAME,getLocalizedString("model.done"));
		return status;
	    }	    
	boolean success = toBeRenamed.renameTo(newFile);//Do the actual renaming
	if(success)
	    {		     		    
		//No need to remove the object and the add it back: just update its attributes
		String objName = newName.getName();
		subject.setAttribute(DE.A_NAME,objName);
		
		// Adjust the 'source' attribute of all children.
		updateSourceHelper(subject);
		
		_dataStore.refresh(subject, true);		      
	    }
	status.setAttribute(DE.A_NAME,getLocalizedString("model.done"));
	return status;
    }
    
    // recursively update the 'source' attribute of all DataElements rooted at 'subject'
    private void updateSourceHelper(DataElement subject)
    {
	StringBuffer parentName = new StringBuffer(subject.getParent().getAttribute(DE.A_SOURCE));
	String newName;
	
	//if my parent already has a slash("/" or "d:\"), then I don't need to add another.
	if (parentName.substring(parentName.length()-1).equals(File.separator))
	    {				
		newName = new String(parentName.toString()+subject.getAttribute(DE.A_NAME));		
	    }
	else
	    {
		newName = new String(parentName.toString()+File.separator+subject.getAttribute(DE.A_NAME));
	    }
	subject.setAttribute(DE.A_SOURCE,newName); // update my "source"

	//update 'source' for all my children. 
	int size = subject.getNestedSize();
	for(int i=0;i<size;i++)
	    {
		updateSourceHelper(subject.get(i));
	    }	
    }
     
     private DataElement handleDeleteFileDir(DataElement subject, DataElement status )
	 {
	     DataElement parent = subject.getParent();
     	     boolean success = deleteHelper(subject.getSource());// recursively delete all files rooted at 'subject'	
	     if (success)
		 { 		    
		     _dataStore.deleteObject(parent, subject);		     		   
		 }
       	    
	     status.setAttribute(DE.A_NAME,getLocalizedString("model.done"));
	     return status;
	 }
     
     // Recursively delete all directories and files rooted at 'fullPath' inclusive
     private boolean deleteHelper(String fullPath)
	 {
	     File filename = new File(fullPath);	    
	     if (filename.exists())
		 {
		     if(filename.isFile())
			 {
			     return filename.delete();
			 }
		     String[] names = filename.list(); 
		     boolean success=true;	     	     
		     for(int i=0;i<names.length;i++)
			 {
			     if (!deleteHelper(fullPath+File.separator+names[i]))
				 success = false;
			 }	
		     if(success)
			 {		
			     success=filename.delete();
			 }
		     return success;
		 }
	     else
		 {
		     return false;
		 }
	 }
     private DataElement handleCreateFile(DataElement subject, DataElement newName, DataElement status)
	 {	
	     StringBuffer newFileName = new StringBuffer(subject.getSource());
	     newFileName.append(File.separator+newName.getName());
	     
	     File toBeCreated = new File(newFileName.toString());
	     if (!toBeCreated.exists())
		 {
		     try
			 {
			     toBeCreated.createNewFile();
			 }
		     catch(IOException e)
			 {
			   status.setAttribute(DE.A_NAME,getLocalizedString("model.done"));
			   return status;  			     			   
			 }
		     
		     String objType  = new String(getLocalizedString("model.file"));
		     String objName = newName.getName();
		     DataElement newObject = _dataStore.createObject(subject,objType,objName,newFileName.toString());
		     newObject.setDepth(1);// the new file has no children
		     subject.setDepth(2);// my parent directory has the new file as its child
		     _dataStore.update(subject);
		 }
	     status.setAttribute(DE.A_NAME,getLocalizedString("model.done"));
	     return status;
	 }
     
     private DataElement handleCreateDir(DataElement subject, DataElement newName, DataElement status)
	 {	
	     StringBuffer newDirName = new StringBuffer(subject.getSource());
	     newDirName.append(File.separator+newName.getName());
	     
	     File toBeCreated = new File(newDirName.toString());
	     if (!toBeCreated.exists())
		 {
		     if(!toBeCreated.mkdir())
			 {
			     status.setAttribute(DE.A_NAME,getLocalizedString("model.done"));
			     return status;			    
			 }
		     String objType = new String(getLocalizedString("model.directory"));
		     String objName = newName.getName();
		     DataElement newObject=_dataStore.createObject(subject,objType,objName,newDirName.toString());
		     newObject.setDepth(1);//new directory is empty so it does not have any children(i.e.depth=1)
		     subject.setDepth(2);// the parent directory now has the new directory as its child.
		     _dataStore.update(subject);
		 }
	     status.setAttribute(DE.A_NAME,getLocalizedString("model.done"));
	     return status;
	 }
     
     /////////////////////   

  private DataElement handleMove(DataElement target, DataElement source, DataElement status)
      {
        // make sure target is expanded
        DataElement subStatus = _dataStore.createObject(null, getLocalizedString("status"), getLocalizedString("model.start"));
	_dataStore.moveObject(source, target);
	
        status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
        return status;
      }



  private DataElement handleOpen(DataElement theElement, DataElement status)
      {
	String sourceString = theElement.getSource();
	int indexOfLocation = sourceString.lastIndexOf(":");
	if (indexOfLocation > 1)
	  {	
	    sourceString = sourceString.substring(0, indexOfLocation);
	  }

	File file = new File(sourceString);
	if (file.exists())
	    {
		int maxSize = 5000000;
		int size = (int)file.length();
		try
		    {
			FileInputStream inFile = new FileInputStream(file);
			int written = 0;

			int bufferSize = (size > maxSize) ? maxSize : size;
			byte[] subBuffer = new byte[bufferSize];

			while (written < size)
			    {
				int subWritten = 0;

				while (written < size && subWritten < bufferSize)
				    {
					int available = inFile.available();
					available = (bufferSize > available) ? available : bufferSize;
					int read = inFile.read(subBuffer, subWritten, available);
					subWritten += read;
					written += subWritten;
				    }
				
				if (written <= maxSize)
				    {
					_dataStore.updateFile(sourceString, subBuffer, subWritten);
				    }
				else
				    {
					_dataStore.updateAppendFile(sourceString, subBuffer, subWritten);
				    }
			    }
			inFile.close();
		    }
		catch (IOException e)
		    {
			System.out.println(e);
		    }
	    }

        status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
        return status;        
      }

    private DataElement handleDate(DataElement theFile, DataElement status)
    {
	File file = new File(theFile.getSource());
	if (file.exists())
	    {
		long date = file.lastModified();
		DataElement dateObj = _dataStore.createObject(status, "date", "" + date);
		_dataStore.createReference(theFile, dateObj, "modified at");
	    }
	else
	    {
		DataElement dateObj = _dataStore.createObject(status, "date", "-1");
		_dataStore.createReference(theFile, dateObj, "modified at");		
	    }

        status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
	return status;
    }

    private DataElement handleSetDate(DataElement theFile, DataElement newDate, DataElement status)
    {
	File file = new File(theFile.getSource());
	if (file.exists())
	    {
		long date = new Long(newDate.getName()).longValue();
		file.setLastModified(date);
	    }
	
        status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
	return status;
    }

  private DataElement handleImport(DataElement theRoot, DataElement toImport, DataElement status)
      {
        _dataStore.createReference(theRoot, toImport);
        _dataStore.update(theRoot);
        status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
        return status;        
      }

  private DataElement handleSetType(DataElement theFile, DataElement newDescriptor, DataElement status)
      {
        theFile.setAttribute(DE.A_TYPE, newDescriptor.getName());
        _dataStore.update(theFile);
        status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
        return status;        
      }


  private DataElement handleQuery (DataElement theElement, DataElement status)
    {
	return handleQuery(theElement, status, false);
    }

  private DataElement handleQuery (DataElement theElement, DataElement status, boolean force)
      {
	  theElement = theElement.dereference();
	  if (force || (!theElement.isExpanded() || (theElement.getNestedSize() == 0)))
	      {
		  try
		      {

			  String type = (String)theElement.getElementProperty(DE.P_TYPE);	   
			  File theFile = new File (theElement.getSource());
			  StringBuffer path = new StringBuffer (theFile.getPath());

			  if (!type.equals("device"))
			      {
				  path.append(File.separator);
			      }
			  
			  String[] list= theFile.list();
			  if (list != null)
			      {
				  String objType  = new String(getLocalizedString("model.directory"));
				  for (int i= 0; i < list.length; i++)
				      {
					  String filePath = path.toString() + list[i];
					  String objName = list[i];
					  
					  DataElement newObject = _dataStore.createObject (theElement, objType, objName, filePath);
					  File f = new File (filePath);
					  if (!f.isDirectory())
					      {
						  newObject.setAttribute(DE.A_TYPE, getLocalizedString("model.file"));
						  newObject.setDepth(1);
					      }		      
				      }

				  _dataStore.refresh(theElement);
			      }

		      }	
		  catch (Exception e)
		      {
			  System.out.println(e);
			  e.printStackTrace();
		      }
	      }

	  if (status != null)
	      status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));	 

        return status;
      }

    public DataElement handleRefresh(DataElement theElement, DataElement status)
    {
	try
	    {		
		String type = (String)theElement.getElementProperty(DE.P_TYPE);	   
		File theFile = new File (theElement.getSource());
		StringBuffer path = new StringBuffer (theFile.getPath());
		
		if (!type.equals("device"))
		    {
			path.append(File.separator);
		    }
		
		// check for deleted
		for (int i = 0; i < theElement.getNestedSize(); i++)
		    {
			DataElement child = theElement.get(i);
			File childFile = new File(child.getSource());
			if (!childFile.exists())
			    {
				_dataStore.deleteObject(theElement, child);
			    }
		    }


		// query
		String[] list= theFile.list();
		if (list != null)
		    {
			String objType  = new String(getLocalizedString("model.directory"));
			for (int i= 0; i < list.length; i++)
			    {
				String filePath = path.toString() + list[i];
				String objName = list[i];
				
				DataElement newObject = _dataStore.find(theElement, DE.A_NAME, objName, 1);
				if (newObject == null || newObject.isDeleted())
				    {
					newObject = _dataStore.createObject (theElement, objType, 
											 objName, filePath);
					File f = new File (filePath);
					if (!f.isDirectory())
					    {
						newObject.setAttribute(DE.A_TYPE, getLocalizedString("model.file"));
						newObject.setDepth(1);
					    }		      
				    }
			    }			
		    }
		
	    }	
	catch (Exception e)
	    {
		System.out.println(e);
		e.printStackTrace();
	    }
	
	_dataStore.refresh(theElement);

	status.setAttribute(DE.A_NAME, "done");
	return status;
    }

    public DataElement findFile (DataElement root, String matchStr, DataElement status)
    {
	return handleFind(root, matchStr, status);
    }
    
    public DataElement handleFind (DataElement root, String patternStr, DataElement status)
    {
	status.setAttribute(DE.A_NAME, getLocalizedString("model.progress"));
	_dataStore.refresh(status);
	

	handleFindHelper(root, patternStr, status);

	status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
	return status;
    }
    
    private void handleFindHelper (DataElement root, String patternStr, DataElement status)
    {
	if (compareString(patternStr, root.getName(), true))
	    {
		_dataStore.createReference(status, root, "contents");
		_dataStore.refresh(root);
		_dataStore.refresh(status);
	    }
	
	int nestedSize = root.getNestedSize();       
	if ((nestedSize == 0) && (root.getType().equals(getLocalizedString("model.directory"))))
	    {	
		handleQuery(root, null);
		nestedSize = root.getNestedSize();
	    }
	
	// search next depth
	for (int j = 0; j < nestedSize; j++)
	    {
		DataElement child = (DataElement)root.get(j);
		handleFindHelper(child, patternStr, status);
	    }     
    }
    
    private boolean compareString(String patternStr, String compareStr, boolean ignoreCase)
    {
	return StringCompare.compare(patternStr, compareStr, ignoreCase);
    }
}




