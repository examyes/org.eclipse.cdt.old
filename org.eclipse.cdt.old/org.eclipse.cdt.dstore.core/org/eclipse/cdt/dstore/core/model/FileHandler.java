package org.eclipse.cdt.dstore.core.model;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.util.*; 
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.core.server.ILoader;

import java.util.*;
import java.lang.*;
import java.io.*;


// this will be the new class for abstracting out file read and write


public class FileHandler
{
	private DataStore _dataStore;
	
	public FileHandler(DataStore dataStore)
	{
		_dataStore = dataStore;
	}
			    
  
    /**
     * Save a file in the specified location   
     *
     * @param localPath the path where to save the file
     * @param file the file to save
     */         
    public void saveFile(String localPath, File file)
    {
	File newFile = new File(localPath);
	if (!newFile.exists())
	{
		try
		{
		newFile.createNewFile();
		}
		catch (IOException e)
		{
		}
	}

	try
	    {
		FileOutputStream newFileStream = new FileOutputStream(newFile);

		if (file != null && !file.isDirectory() && file.exists())
		    {
			int maxSize = 5000000;
			int size = (int)file.length();

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
				
				newFileStream.write(subBuffer, 0, subWritten);
			    }
			
			inFile.close();
			newFileStream.close();
		    }
	    }
	catch (IOException e)
	    {
		System.out.println(e);
		e.printStackTrace();			
	    }
    }
	
    /**
     * Save a file in the specified location   
     *
     * @param remotePath the path where to save the file
     * @param buffer the buffer to save in the file
     */         
    public void saveFile(String remotePath, byte[] buffer, int size)
    {
        remotePath = new String(remotePath.replace('\\', '/'));
        String fileName = _dataStore.mapToLocalPath(remotePath);
	
        if (fileName != null)
        {
          try
          {
            // need to create directories as well
            File file = new File(fileName);
            if (!file.exists())
            {
	      File parent = new File(file.getParent());	      
	      parent.mkdirs();
            }
            else
            {
            }

            File newFile = new File(fileName);
            FileOutputStream fileStream = new FileOutputStream(newFile);
            //fileStream.write(buffer);
            fileStream.write(buffer, 0, size);
            fileStream.close();
          }
          catch (IOException e)
          {
            System.out.println(e);
          }
        }
    }   

    /**
     * Append a file to the specified location   
     *
     * @param remotePath the path where to save the file
     * @param buffer the buffer to append into the file
     */         
    public void appendToFile(String remotePath, byte[] buffer, int size)
    {
        remotePath = new String(remotePath.replace('\\', '/'));
        String fileName = _dataStore.mapToLocalPath(remotePath);
	
        if (fileName != null)
        {
          try 
          {
            // need to create directories as well
            File file = new File(fileName);
            if (!file.exists())
		{
		    File parent = new File(file.getParent());	      
		    parent.mkdirs();

		    File newFile = new File(fileName);
		    FileOutputStream fileStream = new FileOutputStream(newFile);
		    fileStream.write(buffer, 0, size);
		    fileStream.close();
		}
	    else
		{
		    // need to reorganize this so that we don't use up all the memory
		    // divide appendedBuffer into chunks
		    // at > 50M this kills Eclipse
		    File oldFile = new File(fileName);
		    File newFile = new File(fileName + ".new");

		    FileInputStream  oldFileStream = new FileInputStream(oldFile);            
		    FileOutputStream newFileStream = new FileOutputStream(newFile);

		    // write old file to new file
		    int maxSize = 5000000;
		    int written = 0;
		    int oldSize = (int)oldFile.length();		    
		    int bufferSize = (oldSize > maxSize) ? maxSize : oldSize;
		    byte[] subBuffer = new byte[bufferSize];

		    while (written < oldSize)
			{
			    int subWritten = 0;
			    
			    while (written < oldSize && subWritten < bufferSize)
				{
				    int available = oldFileStream.available();
				    available = (bufferSize > available) ? available : bufferSize;
				    int read = oldFileStream.read(subBuffer, subWritten, available);
				    subWritten += read;
				    written += subWritten;
				}
			    
			    newFileStream.write(subBuffer, 0, subWritten);
			}
		    
		    oldFileStream.close();		    		    

		    // write new buffer to new file
		    newFileStream.write(buffer, 0, size);
		    newFileStream.close();

		    // remote old file
		    oldFile.delete();

		    // rename new file 
		    newFile.renameTo(oldFile);
		} 
          }
          catch (IOException e)
          {
            System.out.println(e);
          }
        }
    }   


	
}