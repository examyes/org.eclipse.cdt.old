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
	ConfigureInManager configure_in_manager;
	MakefileAmManager makefile_am_manager; 

	public AutoconfManager(DataElement aProject)
	{
		this.project = aProject;
				//manage configure.in
		configure_in_manager = new ConfigureInManager(project);
		// manage Makefile.am
		makefile_am_manager = new MakefileAmManager(project);
		// check if it is a unix like system
	}
	protected void manageProject(DataElement status)
	{

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
			{	// check if there is an existing script - calls for aclocal, autoheader,automake and autoconf
				File script = new File (project.getSource(),"script.batch");
				if(!script.exists())
					getAutoconfScript(project);
				configure_in_manager.manageConfigure_in();
				makefile_am_manager.manageMakefile_am();
				createConfigureScript(status);
			}
			//check // autoloca	// autoheader // automake // autoconf // else notify the user with the missed packages
		}
	}
	protected String getOS()
	{
		String OS = new String(System.getProperty("os.name")); 
		return OS;
	}
	protected boolean areAllNeededPackagesAvailable()
	{
		// check for autoconf  // check for automake // check for aclocal	// check for autoheader
		return true;
	}
	protected void getAutoconfScript(DataElement project)
	{
		Runtime rt = Runtime.getRuntime();
		//check the project structure
		File projectFile = project.getFileObject();
		if(projectFile.isDirectory()&& !(projectFile.getName().startsWith(".")))
		{
			// add configure.in template files only if not exist
			try{
				Process p;	
				p = rt.exec(
					"cp workspace/com.ibm.cpp.miners/autoconf_templates/script.batch "
						+project.getSource());
				p.waitFor();
				//System.out.println("\n p3 exit value = "+p3.exitValue());
			}catch(IOException e){System.out.println(e);}
			catch(InterruptedException e){System.out.println(e);}	
		}	
	}

/*	private void createConfigureScript()
	{
		Runtime rt = Runtime.getRuntime();
		try
		{
			Process pro = rt.exec("sh -c ./support.dist", null, project.getFileObject());
			BufferedReader _stdInput = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			while (_stdInput.readLine()!=null)
				System.out.println("\n INPUT\n"+_stdInput.readLine());
			BufferedReader _stdError = new BufferedReader(new InputStreamReader(pro.getErrorStream()));
			while (_stdError.readLine()!=null)
				System.out.println("\n ERROR\n"+_stdError.readLine());
			BufferedWriter _stdOutput = new BufferedWriter(new OutputStreamWriter(pro.getOutputStream()));
			System.out.println("\n OUTPUT\n"+_stdOutput.toString());
		}catch(IOException e){e.printStackTrace();}	
	}*/
	public void runConfigureScript(DataElement status)
	{
		DataStore ds = status.getDataStore();
		String invocation = new String("./configure");
		DataElement invocationElement = ds.createObject(null,"invocation",invocation);
		DataElement cmdD = ds.localDescriptorQuery(project.getDescriptor(),"C_COMMAND");
		if(cmdD!=null)
		{
			ArrayList args = new ArrayList();
			args.add(invocationElement);
			args.add(status);
			ds.command(cmdD,args,project);
		}	
	} 
	private void createConfigureScript(DataElement status)
	{
		DataStore ds = status.getDataStore();
		String invocation = new String("./script.batch");
		DataElement invocationElement = ds.createObject(null,"invocation",invocation);
		DataElement cmdD = ds.localDescriptorQuery(project.getDescriptor(),"C_COMMAND");
		if(cmdD!=null)
		{
			ArrayList args = new ArrayList();
			args.add(invocationElement);
			args.add(status);
			ds.command(cmdD,args,project);
		}	
	}
	public MakefileAmManager getMakeFileAmManager()
	{
		return makefile_am_manager;
	}
}

