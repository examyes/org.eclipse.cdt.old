package com.ibm.cpp.miners.managedproject;
/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;

import java.io.*;
import java.lang.Runtime;
import java.util.*;

public class AutoconfManager {
	String autoconf = new String("autoconf");
	String automake = new String("automake");
	String aclocal = new String("aclocal");
	String autoheader = new String("autoheader");
	String autoscan = new String ("autoscan");
	ProjectStructureManager structureManager;
	ConfigureInManager configureInManager;
	MakefileAmManager makefileAmManager; 
	static Object O = new Object();
	String cygwinPrefix = new String("sh -c ");

	public AutoconfManager()
	{
		configureInManager = new ConfigureInManager();
		makefileAmManager = new MakefileAmManager();
	}

	protected void manageProject(DataElement project, DataElement status)
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
		{	// chechk if there are existing autoconf files anf if there are then
			//prompt the user that any existing autoconf files will be regenerated
			// and existing files will be renamed as *.old
			generateAutoconfFiles(project,status,true);
			getAutoconfScript(project);
			if(getOS().equals("Linux"))
				runCommand(project,status,"./script.batch;./configure");
			else
				runCommand(project, status, cygwinPrefix+"script.batch;"+cygwinPrefix+"configure");
		}
		//check // autoloca	// autoheader // automake // autoconf 
		// else notify the user with the missed packages
	}
	protected void generateAutoconfFiles(DataElement project, DataElement status, boolean actionIsManagedProject)
	{
		configureInManager.generateConfigureIn(project);
		makefileAmManager.generateMakefileAm(project);
	}
	protected void updateAutoconfFiles(DataElement project, DataElement status, boolean actionIsManagedProject)
	{
		configureInManager.updateConfigureIn(project,actionIsManagedProject);
		makefileAmManager.updateMakefileAm(project,actionIsManagedProject);
	}
	protected void runSupportScript(DataElement project,DataElement status)
	{
		getAutoconfScript(project);
		createConfigureScript(project,status);
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
		// check if there is an existing script - calls for aclocal, autoheader,automake and autoconf
		File script = new File (project.getSource(),"script.batch");
		if(!script.exists())
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
	}
	private void createConfigureScript(DataElement project, DataElement status)
	{
		
		if(getOS().equals("Linux"))
			runCommand(project, status, "./script.batch");
		else
			runCommand(project, status, cygwinPrefix+"script.batch");
	}
	public void runConfigureScript(DataElement project, DataElement status)
	{
		if(getOS().equals("Linux"))
			runCommand(project, status, "./configure");
		else
			runCommand(project, status, cygwinPrefix+"configure");
	} 
	
	public void runCommand(DataElement project,DataElement status, String invocation)
	{
		DataStore ds = status.getDataStore();
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
		return makefileAmManager;
	}
}

