package com.ibm.cpp.miners.managedproject;
/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;

import java.io.*;
import java.lang.Runtime;
import java.util.*;

public class AutoconfManager {
	
	DataElement project;
	String autoconf = new String("autoconf");
	String automake = new String("automake");
	String aclocal = new String("aclocal");
	String autoheader = new String("autoheader");
	String autoscan = new String ("autoscan");
	ProjectStructureManager structureManager;
	String[] subdirs;
	
	public AutoconfManager(DataElement aProject)
	{
		this.project = aProject;
		ProjectStructureManager structureManager = new ProjectStructureManager( project.getFileObject());
		subdirs = structureManager.getSubdirWorkspacePath();
	}
	
	protected void manageProject()
	{
		// check if it is a unix like system
		if(getOS().equals("Linux")) // to be modified
		{
			String path = project.getSource().toString();

			//check if he tools are available	autolocal, autoheader, automake & autoconf
			if(!areAllNeededPackagesAvailable())
			{
				// should be a popup dialog
				System.out.println("neede package is missing to manage the project"
				+"\n ... the needed packages are  autolocal, autoheader, automake & autoconf");
			}
			else
			{
				// running autoconf support
				// add all the necessary needed template files
				getAutoconfSupprotFiles(project);
				initializeAutoconfSupprotFiles(project);
				createConfigureScript();
				
			}
				// autoloca
				// autoheader
				// automake
				// autoconf
			// else notify the user with the missed packages
		}
	}
	protected String getOS()
	{
		String OS = new String(System.getProperty("os.name")); 
		return OS;
	}
	protected boolean areAllNeededPackagesAvailable()
	{
		// check for autoconf
		// check for automake
		// check for aclocal
		// check for autoheader
		return true;
	}
	protected void getAutoconfSupprotFiles(DataElement project)
	{
		Runtime rt = Runtime.getRuntime();
		//check the project structure
		File projectFile = project.getFileObject();
		if(projectFile.isDirectory()&& !(projectFile.getName().startsWith(".")))
		{
			// add configure.in template files only if not exist
			try{
				// check if exist then
				rt.exec(
					"cp workspace/com.ibm.cpp.miners/autoconf_templates/configure.in "
						+project.getSource());
				// check if exist then
				rt.exec(
					"cp workspace/com.ibm.cpp.miners/autoconf_templates/Makefile.am "
						+project.getSource());
				// check if exist then
				rt.exec(
					"cp workspace/com.ibm.cpp.miners/autoconf_templates/support.dist "
						+project.getSource());
			}catch(IOException e){System.out.println(e);}	
		}
		// provide one makefile.am in each subdiectory
		for(int i =0; i < subdirs.length ; i++)
		{
			if(subdirs[i].indexOf(".")==-1)
			{
				StringTokenizer token = new StringTokenizer(subdirs[i],"/");
				if (token.countTokens()==1)
				{
					try{
						rt.exec(
							"cp workspace/com.ibm.cpp.miners/autoconf_templates/sub/Makefile.am "
							+project.getSource()+"/"+subdirs[i]);
					}catch(IOException e){System.out.println(e);}
				}
				else
				{
					try{
						rt.exec(
							"cp workspace/com.ibm.cpp.miners/autoconf_templates/sub/lib/Makefile.am "
							+project.getSource()+"/"+subdirs[i]);
					}catch(IOException e){System.out.println(e);}
				}
			}
		}
	}
	protected void initializeAutoconfSupprotFiles(DataElement project)
	{
		//udpdate configure.in
		ConfigureInManager configure_in_manager = new ConfigureInManager(project);
		configure_in_manager.manageConfigure_in();
		// update Makefile.am
		MakefileAmManager makefile_am_manager = new MakefileAmManager(project);
		makefile_am_manager.manageMakefile_am();
	}
	private void createConfigureScript()
	{
		Runtime rt = Runtime.getRuntime();
		try
		{
			rt.exec("sh -c ./support.dist", null, project.getFileObject());
		}catch(IOException e){e.printStackTrace();}	
	}

	public void runConfigureScript(DataElement status)
	{
		Runtime rt = Runtime.getRuntime();
		//		try
		    {
			DataStore ds = status.getDataStore();
			String invocation = "sh -c ./configure";
			DataElement invocationElement = ds.createObject(null, "invocation", invocation);

			//Process p = rt.exec("sh -c ./configure", null, project.getFileObject());
			DataElement cmdD = ds.localDescriptorQuery(project.getDescriptor(), "C_COMMAND");
			if (cmdD != null)
			    {
				ArrayList args = new ArrayList();
				args.add(invocationElement);
				DataElement s = ds.command(cmdD, args, project);
				status.addNestedData(s, true);
			    }
			
		    }
		/*
		catch(IOException e)
		    {
			System.out.println(e);
		    }
		*/	
	} 
}

