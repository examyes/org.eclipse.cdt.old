package org.eclipse.cdt.cpp.ui.internal.vcm;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.connections.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;


import java.util.*;
import java.io.*;

public class PlatformVCMProvider 
{
  protected IWorkspace workspace;
  protected ArrayList _repositories = new ArrayList();
  private static PlatformVCMProvider _instance;

  public PlatformVCMProvider() 
  {
    startup();
    _instance = this; 
  }

  public static PlatformVCMProvider getInstance()
  {
    if (_instance == null)
      {
	new PlatformVCMProvider();		
      }
    
    return _instance;    
  }
  

public IProject[] getKnownProjects() 
  {
    IProject[] result = new IProject[_repositories.size()];
    int i = 0;
    for (i = 0; i < _repositories.size(); i++) 
      {
	result[i] = (IProject)_repositories.get(i);
      }
    return result;
  }




public IWorkspace getWorkspace() 
  {
    return workspace;
  }

  public Repository createRepository(Connection connection, DataElement root) 
  {
    // create new project connection
    Connection repConnection = new Connection(connection, root);
    Repository nRepository = new Repository(repConnection, root);
    addRepository(nRepository);
    return nRepository;
  }

  public Repository createRepository(Connection connection) 
  {
    Repository nRepository = new Repository(connection);
    addRepository(nRepository);
    return nRepository;
  }

    public void addRepository(Repository repository)
    {
	_repositories.add(repository);
	writeRepositories();	
    }

  public void deleteRepository(Repository r)
  {
    for (int i = 0; i < _repositories.size(); i++)
      {
	Repository found = (Repository)_repositories.get(i);
	if (found == r)
	  {
	      try
		  {
		      found.close(null);
		  }
	      catch (CoreException e)
		  {
		  }
	    _repositories.remove(i);

	    if (_repositories.size() == 0)
		{
		    String projectFilePath = CppPlugin.getDefault().getStateLocation().append(".repositories").toOSString();
		    String repositoryFile = new String(projectFilePath + File.separator + "repositories.dat");
		    File file = new File(repositoryFile);
		    
		    if (file.exists())
			{
			    file.delete();		    
			}	
		}

	    return;	    
	  }	
      }
  }  



public void setWorkspace(IWorkspace value) 
  {
    workspace = value;
  }

public void shutdown(IProgressMonitor monitor) 
  {
    writeRepositories();    
  }

  public void writeRepositories()
  { 
    // we've got to save this information somewhere
    String projectFilePath = CppPlugin.getDefault().getStateLocation().append(".repositories").toOSString();
    String repositoryFile = new String(projectFilePath + File.separator + "repositories.dat");
    File file = new File(repositoryFile);
      
    if (_repositories.size() == 0)
      {
	if (file.exists())
	  {
	    file.delete();		    
	  }	
      }
    else
    {
      if (!file.exists())
      {
	File dir = new File(file.getParent());
	if (!dir.exists())
	  dir.mkdirs(); 
      }
      
      try
      {
	FileOutputStream fileStream = new FileOutputStream(file);
	StringBuffer buffer = new StringBuffer();
	
	// save the repositories
	for (int i = 0; i < _repositories.size(); i++)
        {
          Repository r = (Repository)_repositories.get(i);

	  // create directory for each
	  String repDirStr = projectFilePath + File.separator + r.getName();	  
	  File repDir = new File(repDirStr);
	  if (!repDir.exists())
	    {
	      repDir.mkdir();	      
	    }	  

          Connection connection = r.getConnection();
          if (connection != null)
          {		 
            String conStr = connection.toString();          
            buffer.append(conStr + "\n");
          }
        }	
	
        fileStream.write(buffer.toString().getBytes());            
        fileStream.close();
      }
      catch (IOException e)
      {
	System.out.println(e);
      }        
    }
  }
 
public void startup() 
  {
    workspace = ResourcesPlugin.getWorkspace();
    getRepositories();
  }

  public void getRepositories()
      {    
        if (_repositories.size() == 0)
        {
          // load the repositories if there are any
	    CppPlugin plugin = CppPlugin.getDefault();
	    if (plugin != null)
		{
		    String projectFilePath = plugin.getStateLocation().append(".repositories").toOSString();
		    String repositoryFile = new String(projectFilePath + File.separator + "repositories.dat");
		    
		    File file = new File(repositoryFile);
		    if (file.exists())
			{
			    try
				{
				    FileInputStream inFile = new FileInputStream(file);    
				    BufferedReader in = new BufferedReader(new InputStreamReader(inFile));
				    
				    String connection = null;
				    ConnectionManager mgr = ConnectionManager.getInstance(); 
				    while ((connection = in.readLine()) != null)
					{
					    ArrayList args = new ArrayList();
					    StringTokenizer tokenizer = new StringTokenizer(connection); 
					    
					    String name = (String)tokenizer.nextElement();
					    while (tokenizer.hasMoreElements())
						{
						    String token = (String)tokenizer.nextElement();
						    args.add(token);
						}
					    
					    DataStore ds = CppPlugin.getDefault().getDataStore();	    
					    DataElement root = ds.getRoot();
					    Connection con = new Connection(name, args, root);
					    Repository newPrj = createRepository(con, con.getRoot());	   
					}        
				    
				    inFile.close();
				}
			    catch (FileNotFoundException e)
				{
				    System.out.println(e);
				}
			    catch (IOException e)
				{
				    System.out.println(e);
				}
			}  
		}
        }      
      }
 

    
}
