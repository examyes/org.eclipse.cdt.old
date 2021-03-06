package org.eclipse.cdt.dstore.miners.filesystem;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


import org.eclipse.cdt.dstore.core.miners.miner.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.util.*;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class FileSystemMiner extends Miner
{
    private DataElement _fsystemObjectDescriptor;
    private DataElement _fileDescriptor;
    private DataElement _hiddenFileDescriptor;
    private DataElement _directoryDescriptor;
    private DataElement _hiddenDirectoryDescriptor;
    private DataElement _deviceDescriptor;
    private DataElement _hostDescriptor;
    private DataElement _containsDescriptor;
    private DataElement _dateDescriptor;
    private DataElement _sizeDescriptor;
    private DataElement _attributesDescriptor;
    private DataElement _permissionsDescriptor;
    private DataElement _exeDescriptor;
    private DataElement _binexeDescriptor;

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
	  	 
	  String dir = host.getSource();	  


	  File[] deviceList = File.listRoots();
	  for(int i=0;i<deviceList.length;i++)
	      {		  
		  String devicePath = deviceList[i].getPath().replace('\\', '/');

		  _dataStore.createObject(_minerData, 
					  getLocalizedString("model.device"),
					  devicePath,
					  devicePath);
	      }	 

	  if (dir.length() == 0)
	      {
		  _dataStore.createReference(host, _minerData);
	      }
	  else
	      {
		  String dir2 = dir.substring(0,1).toUpperCase()+dir.substring(1,dir.length());	
		  
		  File hostFile = new File(dir2);
		  if (hostFile.exists())
		      {
			  DataElement currentDirectory = findFile(_minerData, hostFile);
			  if (currentDirectory != null)
			      {
				  DataElement ref = _dataStore.createReference(host,currentDirectory);
				  //host.addNestedData(currentDirectory, true);
			      }
		      }
	      }
      }

    public void updateMinerInfo()
    {
        DataElement host =  _dataStore.getHostRoot();
	_dataStore.refresh(host, true);

	DataElement cwdRef = host.get(0);
	if (cwdRef != null)
	    {
		_dataStore.refresh(_minerData);

		DataElement theElement = cwdRef.dereference();
		if (theElement != _minerData)
		    {
			refreshFrom(theElement);
		    }
	    }
    }

    private void refreshFrom(DataElement child)
    {
	if (child != null)
	    {
		DataElement parent = child.getParent();
		if (parent != _minerData)
		    {
			refreshFrom(parent);
		    }
		
		_dataStore.refresh(child);
	    }
    }


    private DataElement findFile(DataElement root, File path)
    {	
	DataElement result = root;
	if (root.getType().equals("data"))
	    {
		for (int i =0; i < root.getNestedSize(); i++)
		    {
			DataElement device= root.get(i);
			String path_device=path.getPath().replace('\\', '/');		      		
			String device_source = device.getSource(); 
			
			if(device_source.equals(path_device))
			    {
				return device;			
			    }

			if(path_device.startsWith(device_source))
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
			String type = directory.getType();
			if (type.equals("directory") || type.equals("device"))
			    {
				String path_dir = path.getPath().replace('\\', '/');
				String directory_source = directory.getSource();
				if (directory_source.equals(path_dir))
				    {
					return directory;
				    }
				
				if (path_dir.startsWith(directory_source))
				    {			
					DataElement tempstatus=_dataStore.createObject(null,"status","start");
					handleQuery(directory,tempstatus);				
					result= findFile(directory,path);
					break;
				    }			
			    }
		    }
	    }
	
	return result;
    }
    

  public void extendSchema(DataElement schemaRoot)
      {
	  _fsystemObjectDescriptor  = _dataStore.find(schemaRoot, DE.A_NAME, getLocalizedString("model.Filesystem_Objects"), 1);
	  _deviceDescriptor         = _dataStore.find(schemaRoot, DE.A_NAME, getLocalizedString("model.device"), 1);    
	  _directoryDescriptor      = _dataStore.find(schemaRoot, DE.A_NAME, getLocalizedString("model.directory"), 1);
	  _hostDescriptor           = _dataStore.find(schemaRoot, DE.A_NAME, getLocalizedString("model.host"), 1);
	  _fileDescriptor           = _dataStore.find(schemaRoot, DE.A_NAME, getLocalizedString("model.file"), 1);
	  _containsDescriptor       = _dataStore.find(schemaRoot, DE.A_NAME, getLocalizedString("model.contents"), 1);
	  _attributesDescriptor     = _dataStore.find(schemaRoot, DE.A_NAME, "attributes", 1);

	  _exeDescriptor            = createObjectDescriptor(schemaRoot, "executable");
	  _dataStore.createReference(_fileDescriptor, _exeDescriptor, "abstracts", "abstracted by");
	  _binexeDescriptor         = createObjectDescriptor(schemaRoot, "binary executable");
	  _dataStore.createReference(_exeDescriptor, _binexeDescriptor, "abstracts", "abstracted by");

	  DataElement intD          = _dataStore.find(schemaRoot, DE.A_NAME, "Integer", 1);
	  DataElement dateD         = _dataStore.find(schemaRoot, DE.A_NAME, "Date", 1);
	  

	  _sizeDescriptor           = createObjectDescriptor(schemaRoot, "size");
	  _dateDescriptor           = createObjectDescriptor(schemaRoot, "date");
	  _permissionsDescriptor     = createObjectDescriptor(schemaRoot, "permissions");
	  
	  _dataStore.createReference(_sizeDescriptor, intD, _attributesDescriptor);
	  _dataStore.createReference(_dateDescriptor, dateD, _attributesDescriptor);

	  _dataStore.createReference(_fileDescriptor, _sizeDescriptor, _attributesDescriptor);	  
	  _dataStore.createReference(_fileDescriptor, _dateDescriptor, _attributesDescriptor);	  

	  _dataStore.createReference(_directoryDescriptor, _sizeDescriptor, _attributesDescriptor);	  
	  _dataStore.createReference(_directoryDescriptor, _dateDescriptor, _attributesDescriptor);	  
	  
	  _hiddenFileDescriptor     = createObjectDescriptor(schemaRoot, "hidden file");
	  _dataStore.createReference(_fileDescriptor, _hiddenFileDescriptor, "abstracts", "abstracted by"); 
 
	  _hiddenDirectoryDescriptor     = createObjectDescriptor(schemaRoot, "hidden directory");
	  _dataStore.createReference(_directoryDescriptor, _hiddenDirectoryDescriptor, "abstracts", "abstracted by"); 
	  
	  DataElement queryAllD   = createCommandDescriptor(_fsystemObjectDescriptor, "Query All", "C_QUERY_ALL", false);

	  DataElement fdatesD   = createCommandDescriptor(_fsystemObjectDescriptor, "Get Dates", "C_DATES", false);
	  DataElement fdateD    = createCommandDescriptor(_fileDescriptor, "Get Date", "C_DATE", false);
	  DataElement setDateD  = createCommandDescriptor(_fileDescriptor,  "Set Date", "C_SET_DATE", false);
	  
	  DataElement permissionsD = createCommandDescriptor(_fileDescriptor, "Get Permissions", "C_PERMISSIONS", false);
	  
	  DataElement open      = createCommandDescriptor(_fileDescriptor, getLocalizedString("model.Open"), 
							  "C_OPEN", false);

	  DataElement move      = createCommandDescriptor(_fsystemObjectDescriptor, 
							  getLocalizedString("model.Move"), "C_MOVE", false);
	  
	  //renaming files and dirs
	  DataElement renF = createCommandDescriptor(_fileDescriptor, getLocalizedString("model.Rename"), "C_RENAME", false);
	  DataElement inRenF = _dataStore.createObject(renF,"input", "Enter the New Name");
	  
	  DataElement findD = createCommandDescriptor(_fsystemObjectDescriptor, "Find", "C_FIND_FILE", false);

	  //deleting dirs
	  DataElement del = createCommandDescriptor(_fileDescriptor, getLocalizedString("model.Delete"), "C_DELETE", false);
	  

	  //creating new files and dirs
	  DataElement newFD=createAbstractCommandDescriptor(_fsystemObjectDescriptor,getLocalizedString("model.Create"),
							    "C_NEW");
	  
	  DataElement newF = createCommandDescriptor(newFD, getLocalizedString("model.File___"),
						     "C_CREATE_FILE", false);

	  DataElement inNewF = _dataStore.createObject(newF, "input", 
						       "Enter Name for New file");
	  DataElement newD = createCommandDescriptor(newFD,getLocalizedString("model.Directory___"),
						     "C_CREATE_DIR");
	  DataElement inNewD = _dataStore.createObject(newD, "input", 
						       "Enter Name for New Directory");

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
     else if (name.equals("C_DATES"))
	 {
	     status = handleDates(subject, status);
	 }
     else if (name.equals("C_DATE"))
	 {
	     status = handleDate(subject, status, true);
	 }
     else if (name.equals("C_PERMISSIONS"))
	 {
	     status = handlePermissions(subject, status);	
	 }
     else if (name.equals("C_SET_DATE"))
	 {
	     DataElement newDate = _dataStore.find(theElement, DE.A_TYPE, "date", 1);
	     status = handleSetDate(subject, newDate, status);
	 }
     else if (name.equals("C_REFRESH"))
     {
	 status = handleQuery(subject, status, true);
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
	     String type = subject.getType();
	     DataElement des = subject.getDescriptor();
	     if (des != null && des.isOfType(_fsystemObjectDescriptor, true))
		 {
		     status = handleQueryAll(subject, status);
		 }
	     else
		 {
		     status = handleOpen(subject.dereference(), status);
		 }
	 }
     else if (name.equals("C_MOVE"))
     {
       DataElement moveSource = getCommandArgument(theElement, 1);
       status = handleMove(subject.dereference(), moveSource.dereference(), status);
     }

     else if (name.equals("C_RENAME"))
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

     status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
     return status;
   }
    
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
		_dataStore.refresh(subject.getParent());
	      
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
	int length = parentName.length();
	if (length > 0 && parentName.substring(length - 1).equals("/"))
	    {				
		newName = new String(parentName.toString()+subject.getAttribute(DE.A_NAME));		
	    }
	else
	    {
		newName = new String(parentName.toString()+"/"+subject.getAttribute(DE.A_NAME));
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
		     _dataStore.refresh(parent);
		 }
	     else
		 {
		     //System.out.println("Delete failed!");
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
	     String parentSource = subject.getSource();
	     StringBuffer newFileName = new StringBuffer(parentSource);
	     if (parentSource.charAt(parentSource.length() - 1) != '/')
		 {
		     newFileName.append("/");
		 }
	     newFileName.append(newName.getName());
	     
	     File toBeCreated = new File(newFileName.toString());
	     if (!toBeCreated.exists())
		 {
		     try
			 {
			     toBeCreated.createNewFile();
			     FileOutputStream fileStream = new FileOutputStream(toBeCreated);			     
			     fileStream.write((new String(" ")).getBytes());            
			     fileStream.close();
			 }
		     catch(IOException e)
			 {
			   status.setAttribute(DE.A_NAME,getLocalizedString("model.done"));
			   return status;  			     			   
			 }
		     
		     String objName = newName.getName();
		     DataElement type = _fileDescriptor;
		     boolean hidden = objName.charAt(0) == '.';
		     if (hidden)
		     {
		     	type = _hiddenFileDescriptor;	
		     }
		     
		     DataElement newObject = _dataStore.createObject(subject, type,
								     objName, newFileName.toString());
		     
		     if (!hidden)
			 {
			     handleSize(newObject, status);
			     handleDate(newObject, status, false);
			     newObject.setDepth(1);// the new file has no children
			     subject.setDepth(2);// my parent directory has the new file as its child
			 }
		     else
			 {
			     newObject.setDepth(0);
			 }
		     _dataStore.refresh(subject);
		 }
	     status.setAttribute(DE.A_NAME,getLocalizedString("model.done"));
	     return status;
	 }
     
     private DataElement handleCreateDir(DataElement subject, DataElement newName, DataElement status)
	 {	
	     String parentStr = subject.getSource();
	     StringBuffer newDirName = new StringBuffer(parentStr);
	    
	     if (parentStr.charAt(parentStr.length() - 1) != '/')
		 {
		     newDirName.append("/");
		 }

	     newDirName.append(newName.getName());
	     
	     File toBeCreated = new File(newDirName.toString());
	     if (!toBeCreated.exists())
		 {
		     if(!toBeCreated.mkdir())
			 {
			     status.setAttribute(DE.A_NAME,getLocalizedString("model.done"));
			     return status;			    
			 }

		     String objName = newName.getName();
		     DataElement type = _directoryDescriptor;
		     boolean hidden = objName.charAt(0) == '.';
		     if (hidden)
		     {
		     	type = _hiddenDirectoryDescriptor;	
		     }
		     
		     DataElement newObject=_dataStore.createObject(subject, type,
								   objName, newDirName.toString());
		     if (!hidden)
			 {
			     handleSize(newObject, status);
			     handleDate(newObject, status, false);
			     
			     newObject.setDepth(1);//new directory is empty so it does not have any children(i.e.depth=1)
			     subject.setDepth(2);// the parent directory now has the new directory as its child.
			 }
		     else
			 {
			     newObject.setDepth(0);
			 }

		     _dataStore.refresh(subject);
		 }
	     status.setAttribute(DE.A_NAME,getLocalizedString("model.done"));
	     return status;
	 }
     
  private DataElement handleMove(DataElement target, DataElement source, DataElement status)
      {
        // make sure target is expanded
        DataElement subStatus = _dataStore.createObject(null, getLocalizedString("status"), 
							getLocalizedString("model.start"));
	_dataStore.moveObject(source, target);
	
        status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
        return status;
      }



  private DataElement handleOpen(DataElement theElement, DataElement status)
      {
        String sourceString = theElement.getSource().replace('\\', '/');
		int indexOfLocation = sourceString.lastIndexOf(":");
		if (indexOfLocation > 1)
	  	{	
	   	 sourceString = sourceString.substring(0, indexOfLocation);
	  	}
 
 		_dataStore.sendFile(sourceString);	
        status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
        return status;        
      }

	private DataElement handlePermissions(DataElement theFile, DataElement status)
	{
		File file = new File(theFile.getSource());
		if (file.canWrite())
		    {
			DataElement permissions = _dataStore.createObject(status, _permissionsDescriptor, "rw");	
			_dataStore.createReference(theFile, permissions, _attributesDescriptor);

		    }
		else
		    {
			DataElement permissions = _dataStore.createObject(status, _permissionsDescriptor, "r");	
			_dataStore.createReference(theFile, permissions, _attributesDescriptor);
		    }	
		
		return status;
	}

    private DataElement handleSize(DataElement theFile, DataElement status)
    {
	ArrayList attributes = theFile.getAssociated(_attributesDescriptor);
	DataElement sizeObj = null;
	if (attributes.size() != 0)
	    {
		for (int i = 0; i < attributes.size(); i++)
		    {
			DataElement att = (DataElement)attributes.get(i);
			if (att.getType().equals("size"))
			    {
				sizeObj = att;					
			    }
		    }
	    }
	
	File file = new File(theFile.getSource());
	try 
	{
		file = file.getCanonicalFile();
	}
	catch(IOException e)
	{
	}
	if (file.exists())
	    {
		if (sizeObj == null)
		    {
			sizeObj = _dataStore.createObject(status, _sizeDescriptor, "" + file.length());
			_dataStore.createReference(theFile, sizeObj, _attributesDescriptor);
		    }
		else
		    {
			sizeObj.setAttribute(DE.A_NAME, "" + file.length());
		    }
 
	    }
	return status;
    }

    private DataElement handleDates(DataElement theDirectory, DataElement status)
    {
	handleDate(theDirectory, status, false);
	ArrayList contents = theDirectory.getAssociated(_containsDescriptor);
	for (int i = 0; i < contents.size(); i++)
	    {
		handleDate((DataElement)contents.get(i), status, false);
	    }

	_dataStore.refresh(theDirectory);
	return status;
    }

    private DataElement handleDate(DataElement theFile, DataElement status, boolean doRefresh)
    {
	ArrayList dateInfo = theFile.getAssociated(_attributesDescriptor);
	DataElement dateObj = null;
	if (dateInfo.size() != 0)
	    {
		for (int i = 0; i < dateInfo.size(); i++)
		    {
			DataElement att = (DataElement)dateInfo.get(i);
			if (att.getType().equals("date"))
			    {
				dateObj = att;
			    }
		    }
	    }
	
	File file = new File(theFile.getSource());
	try 
	{
		file = file.getCanonicalFile();
	}
	catch(IOException e)
	{
	}
	if (file.exists())
	    {
		long date = file.lastModified();
		Date dateC = new Date(date);
		SimpleDateFormat format = new SimpleDateFormat();
		String value = format.format(dateC);
		
		if (dateObj == null)
		    {
			dateObj = _dataStore.createObject(status, _dateDescriptor, "" + date);
			dateObj.setAttribute(DE.A_VALUE, value);
			_dataStore.createReference(theFile, dateObj, _attributesDescriptor);
		    }
		else
		    {
			dateObj.setAttribute(DE.A_NAME, "" + date);
			dateObj.setAttribute(DE.A_VALUE, value);
		    }
 
	    }
	else
	    {
		if (dateObj == null)
		    {
			dateObj = _dataStore.createObject(status, _dateDescriptor, "-1");
			_dataStore.createReference(theFile, dateObj, _attributesDescriptor);		
		    }
		else
		    {
			dateObj.setAttribute(DE.A_NAME, "-1");
		    }

	    }
	
	if (doRefresh)
	    {
		_dataStore.refresh(dateObj.getParent());
		_dataStore.refresh(theFile);
	    }

	return status;
    }

    private DataElement handleSetDate(DataElement theFile, DataElement newDate, DataElement status)
    {
	File file = new File(theFile.getSource());
	try
	{
	    file = file.getCanonicalFile();
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	}
	if (file.exists())
	    {
		if (newDate != null)
		    {
			newDate.setParent(status);
			try
			    {
				long date = new Long(newDate.getName()).longValue();
				Date dateC = new Date(date);
				SimpleDateFormat format = new SimpleDateFormat();
				String value = format.format(dateC);
				if (date > 0)
				    {
					file.setLastModified(date);
				    }
				    
				ArrayList attributes = theFile.getAssociated(_attributesDescriptor);
				DataElement dateObj = null;
				for (int a = 0; a < attributes.size() && dateObj == null; a++)
				    {
					DataElement attribute = (DataElement)attributes.get(a);
					if (attribute.getType().equals("date"))
					    {
						dateObj = attribute;
					    }
				    }
				if (dateObj == null)
				    {
					newDate.setAttribute(DE.A_VALUE, value);
					_dataStore.createReference(theFile, newDate, _attributesDescriptor);
				    }
				else
				    {
					dateObj.setAttribute(DE.A_NAME, newDate.getName());
					dateObj.setAttribute(DE.A_VALUE, value);
					_dataStore.refresh(dateObj);
					_dataStore.refresh(theFile);
				    }
			    }
			catch (Exception e)
			    {
				System.out.println("bad date " + newDate);
			    }
		    }
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


    private DataElement handleQueryAll(DataElement theElement, DataElement status)
    {
	handleQuery(theElement, status, true);
	for (int i = 0; i < theElement.getNestedSize(); i++)
	    {
		DataElement child = theElement.get(i);
		if (!child.isReference())
		    {
			handleQueryAll(child, status);
		    }
	    }

	return status;
    }   

    private DataElement handleQuery (DataElement theElement, DataElement status)
    {
	return handleQuery(theElement, status, false);
    }

    private DataElement handleQuery (DataElement theElement, DataElement status, boolean refresh)
    {
	String theOS = System.getProperty("os.name");
	if (!theOS.startsWith("Win"))
	    //if (true)  
	{
		return internalHandleLSQuery(theElement, status, refresh);
	    }
	else
	    {
		return internalHandleQuery(theElement, status, refresh);
	    }
    }


    private DataElement internalHandleLSQuery (DataElement theElement, DataElement status, boolean refresh)
    {
	theElement = theElement.dereference();
	if (theElement.isOfType(_fsystemObjectDescriptor))
	    {

		// check for deleted
		if (refresh)
		    {
			checkForDeleted(theElement);
		    }
		
		// use 'ls -F' command
		String cmd = "ls -F";
		try
		    {
			String rootSource = theElement.getSource();
			if (!theElement.getType().equals("device"))
			    {
				rootSource += "/";
			    }

			Process theProcess = Runtime.getRuntime().exec(cmd,null, new File(rootSource));	
			BufferedReader reader = new BufferedReader(new InputStreamReader(theProcess.getInputStream()));
			
			String line = null;
			while ((line = reader.readLine()) != null)
			    {
				String name = line;				
				int length = line.length();


				boolean hidden = name.charAt(0) == '.';
				DataElement type = (hidden ? _hiddenFileDescriptor : _fileDescriptor);				
				if (line.charAt(length - 1) == '/')
				    {
					if (hidden)
					    {
						type = _hiddenDirectoryDescriptor;
					    }
					else
					    {
						type = _directoryDescriptor;
					    }
					name = line.substring(0, length - 1);
				    }
				else if (line.charAt(length - 1) == '*')
				    {
					type = _exeDescriptor;
					name = line.substring(0, length - 1);
				    }
				
				String filePath = rootSource + name;
				
				DataElement newObject = _dataStore.find(theElement, DE.A_SOURCE, filePath, 1);
				if (newObject == null || newObject.isDeleted())
				    {
					newObject = _dataStore.createObject(theElement, type, name, filePath);
					
					if (!hidden)
					    {
						if (type == _directoryDescriptor)
						    {
							handleSize(newObject, status);
							handleDate(newObject, status, false);
						    }
						else
						    {
							handleSize(newObject, status);
							handleDate(newObject, status, false);
							classifyExecutable(newObject, false);
							newObject.setDepth(1);
						    }
					    }
					else
					    {
						newObject.setDepth(0);
					    }
				    }
				else
				    {
					if (refresh)
					    {
						if (!hidden)
						    {
							if (!newObject.isOfType(_directoryDescriptor))
							    {
								if (type != newObject.getDescriptor())
								    {
									newObject.setAttribute(DE.A_TYPE, type.getName());
								    }
								
								if (type == _exeDescriptor)
								    {
									classifyExecutable(newObject, false);
								    }
							    }
							else
							    {
								if (newObject.depth() == 1)
								    {
									if (newObject.getNestedSize() > 0)
									    {
										newObject.setDepth(2);
									    }
								    }
							    }
						    }
						else
						    {
							newObject.setDepth(0);
						    }
					    }
				    }
			    }
		    }
		catch (IOException e)
		    {
		    }
	    }
	if (status != null)
	    {
		status.setAttribute(DE.A_NAME, "done");
	    }
	return status;
    }

    private void classifyExecutable(DataElement theElement, boolean doRefresh)
    {
	if (theElement.isOfType(_exeDescriptor))
	    {
		try
		    {
			// next level query using "file"?
			// find out if binary exe
			Process theProcess = Runtime.getRuntime().exec("file " + theElement.getSource());	
			BufferedReader reader = new BufferedReader(new InputStreamReader(theProcess.getInputStream()));			
			String line = reader.readLine();
			if (line != null)
			    {
				if (line.indexOf("executable") > 0)
				    {
					theElement.setAttribute(DE.A_TYPE, "binary executable");
					
					if (doRefresh)
					    {
						_dataStore.refresh(theElement);
					    }
				    }
			    }
		    }
		catch (IOException e)
		    {
		    }
	    }	
    }

    private DataElement internalHandleQuery (DataElement theElement, DataElement status, boolean refresh)
    {
	theElement = theElement.dereference();
	if (theElement.getDescriptor() == null || 
	    theElement.isOfType(_fsystemObjectDescriptor))
	    {
		try
		    {
			
			String type = theElement.getAttribute(DE.A_TYPE);	   
			File theFile = new File (theElement.getSource());
			StringBuffer path = new StringBuffer (theFile.getPath());
			
			if (!type.equals("device"))
			    {
				path.append("/");
			    }
			
			// check for deleted
			if (refresh)
			    {
				checkForDeleted(theElement);
			    }
			
			
			File[] list= theFile.listFiles();
			if (list != null)
			    {
				for (int i= 0; i < list.length; i++)
				    {
				      	File f = list[i];
					String filePath = f.getAbsolutePath().replace('\\', '/');			
					String objName = f.getName();
					boolean hidden = f.isHidden()  || objName.charAt(0) == '.';
					
					DataElement newObject = _dataStore.find(theElement, DE.A_SOURCE, filePath, 1);
					if (newObject == null || newObject.isDeleted())
					    {
						DataElement objType = _directoryDescriptor;

						if (!f.isDirectory())
						    {
							objType  = _fileDescriptor;
							
							if (hidden)
							    {
								objType = _hiddenFileDescriptor;
							    }
						    }
						else
						    {
							if (hidden)
							    {
								objType = _hiddenDirectoryDescriptor;
							    }
						    }
						
						newObject = _dataStore.createObject (theElement, objType, 
										     objName, filePath);

						if (hidden)
						    {
							newObject.setDepth(0);
						    }
						else
						    {
							if (!f.isDirectory())
							    {
								handleSize(newObject, status);
								handleDate(newObject, status, false);
								
								//newObject.expandChildren();
								newObject.setDepth(1);

								// classify
								winClassifyFile(newObject);
							    }		      
							else
							    {
								File[] slist = f.listFiles();
								if (slist == null || slist.length == 0)
								    {
									newObject.setDepth(1);
								    }
								
								
								handleSize(newObject, status);
								handleDate(newObject, status, false);
							    }
						    }
					    }
					else
					    {
						// reclassify files on refresh
						if (refresh)
						    {
							if (!newObject.isOfType(_directoryDescriptor))
							    {
								if (hidden)
								    {
									newObject.setDepth(0);
								    }
								else
								    {
									winClassifyFile(newObject);	
								    }
							    }
							else
							    {
								if (hidden)
								    {
									newObject.setDepth(0);
								    }
								else
								    {
									if (newObject.depth() == 1)
									    {
										if (newObject.getNestedSize() > 0)
										    {
											newObject.setDepth(2);
										    }
									    }
								    }
							    }
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
	    }
	  if (status != null)
	      status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));	 

	  return status;
      }

    // hack for windows
    private void winClassifyFile(DataElement theFile)
    {
	// use 'exe' extension
	String name = theFile.getName();

	if (name.endsWith(".exe"))
	    {
		theFile.setAttribute(DE.A_TYPE, "binary executable");
	    }
    }


    private void checkForDeleted(DataElement theElement)
    {
	for (int i = 0; i < theElement.getNestedSize(); i++)
	    {
		DataElement child = theElement.get(i);
		if (child != null && !child.isReference() && !child.isDeleted())
		    {
			if (child.isOfType(_fileDescriptor))
			    {
				String src = child.getSource();
				if (src != null)
				    {
					File childFile = new File(src);
					if (!childFile.exists())
					    {
						_dataStore.deleteObject(theElement, child);
					    }
				    }
			    }
		    }
	    }
    }

    public DataElement findFile (DataElement root, String matchStr, DataElement status)
    {
	return handleFind(root, matchStr, status);
    }
    
    public DataElement handleFind (DataElement root, String patternStr, DataElement status)
    {
	status.setAttribute(DE.A_NAME, getLocalizedString("model.progress"));
	root = root.dereference();
	handleFindHelper(root, patternStr, status);

	status.setAttribute(DE.A_NAME, getLocalizedString("model.done"));
	return status;
    }
    
    private void handleFindHelper (DataElement root, String patternStr, DataElement status)
    {
	if (compareString(patternStr, root.getName(), true))
	    {
		_dataStore.createReference(status, root, getLocalizedString("model.contents"));
	    }
	
	int nestedSize = root.getNestedSize();       
	if (root.isOfType(_directoryDescriptor))
	    {	
		handleQuery(root, null);
		nestedSize = root.getNestedSize();
	    }
	
	// search next depth 
	for (int j = 0; j < nestedSize; j++)
	    {
		DataElement child = (DataElement)root.get(j);
		if (!child.isReference())
		    {
			handleFindHelper(child, patternStr, status);
		    }
	    }     
    }
    
    private boolean compareString(String patternStr, String compareStr, boolean ignoreCase)
    {
	return StringCompare.compare(patternStr, compareStr, ignoreCase);
    }



}




