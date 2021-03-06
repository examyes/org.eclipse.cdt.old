package org.eclipse.cdt.dstore.core.model;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.util.*; 

import java.util.*;
import java.lang.*;
import java.io.*;


// this will be the new class for abstracting out file read and write


public class ByteStreamHandler
{
	protected DataStore _dataStore;
	
	public ByteStreamHandler(DataStore dataStore)
	{
		_dataStore = dataStore;
	}
			    
  
    /**
     * Save a file in the specified location   
     *
     * @param localPath the path where to save the file
     * @param file the file to save
     */         
    public void receiveBytes(String localPath, File file)
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
    public void receiveBytes(String remotePath, byte[] buffer, int size, boolean binary)
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
            
            //boolean binary = false;
            if (binary)
            {
	            fileStream.write(buffer, 0, size);
            }    
            else
            {
            	String bufferString = new String(buffer, 0,size, "UTF-8");			
		
		OutputStreamWriter writer = new OutputStreamWriter(fileStream);
		writer.write(bufferString, 0, size);
		writer.flush();
            }
            
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
    public void receiveAppendedBytes(String remotePath, byte[] buffer, int size, boolean binary)
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
		    
		    //boolean binary = false;
		    if (binary)
			{
			    fileStream.write(buffer, 0, size);
			}    
		    else
			{
			    String bufferString = new String(buffer, 0,size, "UTF-8");			
			    
			    OutputStreamWriter writer = new OutputStreamWriter(fileStream);
			    writer.write(bufferString, 0, size);
			    writer.flush();
			}
		    
		    fileStream.close();
		}
	    else
		{
		    // need to reorganize this so that we don't use up all the memory
		    // divide appendedBuffer into chunks
		    // at > 50M this kills Eclipse
		    File oldFile = new File(fileName);
		    File newFile = new File(fileName + ".new");
		    newFile.createNewFile();
		    
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
			    //boolean binary = false;
			    if (binary)
				{
				    newFileStream.write(buffer, 0, size);
				}    
			    else
				{
				    String bufferString = new String(buffer, 0,size, "UTF-8");			
				    
				    OutputStreamWriter writer = new OutputStreamWriter(newFileStream);
				    writer.write(bufferString, 0, size);
				    writer.flush();
				}
			    
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


	public void sendBytes(String path)
	{
		File file = new File(path);
		try
		{
		 file = file.getCanonicalFile();
		}
		catch (IOException e)
		{
		}
		if (!file.isDirectory() && file.exists())
	    {

		int maxSize = 5000000;
		int size = (int)file.length();
		try
		    {
			FileInputStream inFile = new FileInputStream(file);
			int written = 0;

			int bufferSize = (size > maxSize) ? maxSize : size;
			if (bufferSize == 0)
			    {
				bufferSize = 1;
			    }

			byte[] subBuffer = new byte[bufferSize];

			boolean binary = false; 
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
				
				if (!binary)
				    {
					// hack to find out if this is binary or not!
					try 
					    {
						String dummy = new String(subBuffer, "UTF-8");
						if (dummy.length() != subWritten)
						    {
							binary = true;
						    }
					    }
					catch (Exception e)
					    {
						binary = true;
					    }
				    }

				if (written <= maxSize)
				    {
				    	internalSendBytes(path, subBuffer, subWritten, binary);
				    }
				else
				    {
					internalSendAppendBytes(path, subBuffer, subWritten, binary);
				    }
			    }
			// special case for empty files
			if (written == 0)
			    {
					subBuffer[0] = ' ';
					internalSendBytes(path, subBuffer, 1, true);
			    }

			
			inFile.close();
		    }
		catch (IOException e)
		    {
			System.out.println(e);
			e.printStackTrace();			
		    }
	    }
	}
	
	protected void internalSendBytes(String path, byte[] bytes, int size, boolean binary)
	{
		if (_dataStore.isVirtual())
		{
			_dataStore.replaceFile(path, bytes, size, binary);
		}
		else	
		{
			_dataStore.updateFile(path, bytes, size, binary);		
		}
	}	
	
	protected void internalSendAppendBytes(String path, byte[] bytes, int size, boolean binary)
	{
		if (_dataStore.isVirtual())
		{
			_dataStore.replaceAppendFile(path, bytes, size, binary);
		}
		else
		{
			_dataStore.updateAppendFile(path, bytes, size, binary);		
		}
	}
	
}
