package com.ibm.cpp.miners.managedproject;
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


import com.ibm.dstore.core.model.*;

import java.io.*;
import java.lang.Runtime;
import java.util.*;

public class TargetManager {
	ProjectStructureManager structureManager;
	String cygwinPrefix = new String("sh ");

	public TargetManager()
	{
	}
	public void buildTarget(DataElement target, DataElement status,AutoconfManager autoconfmanager)
	{
		File parent = target.getFileObject().getParentFile();
		/*
		DataElement parentData = target.getParent();
		int i =0;
		while(!parent.getAbsolutePath().equals(autoconfmanager.getWorkSpaceLocation()))
		{
				if(i>0)
					parentData = parentData.getParent();
				parent = parent.getParentFile();
				i++;
		}
		System.out.println(parentData);
		autoconfmanager.runSupportScript(parentData,status);
		autoconfmanager.runConfigureScript(parentData,status);
		*/
		DataElement dirElement = target.getDataStore().createObject(null, "directory", 
			parent.getName(),
			parent.getAbsolutePath());
		runCommand(dirElement, status, "gmake " + target.getName());
		
		/*
		// here the only file that might be available is the Makefile.am
		File parent = dir.getFileObject().getParentFile();
		System.out.println("\n Dir = "+ parent.getName());
		File Makefile = new File(parent,"Makefile");
		if(Makefile.exists())
		{
			// if no Makefile then we should cretae configure
			// and then run configure
			if(getOS().equals("Linux"))
				runCommand(dir.getParent(), status, "make");
			else
				runCommand(dir.getParent(), status, cygwinPrefix+"make");
			// check if there is a configure script - then run it
		}
		else
		{
			int i = 0;
			File file = parent;
			while(!file.getAbsolutePath().equals(autoconfmanager.getWorkSpaceLocation()))
			{
				if(i>0)
					parent = parent.getParentFile();
				file = file.getParentFile();
				i++;
			}
			// now parnet is the project
			File configure = new File(parent,"configure");
			DataElement project = new DataElement();
			if(configure.exists())
				autoconfmanager.runConfigureScript(project,status);
			else
			{
				autoconfmanager.runSupportScript(project,status);
				autoconfmanager.runConfigureScript(project,status);
			}		
		}*/
	}
	public void executeTarget(DataElement target, DataElement status,String workSpacePath)
	{
		File parent = target.getFileObject().getParentFile();
	
		DataElement dirElement = target.getDataStore().createObject(null, "directory", 
			parent.getName(),
			parent.getAbsolutePath());
	
	    String invocation = target.getName();
	    
	    if(getOS().equals("Linux"))
			invocation = "./"+invocation;
		else
			invocation = cygwinPrefix+"./"+invocation;

	
		runCommand(dirElement, status, invocation);
	} 
	
	
	public void runCommand(DataElement project, DataElement status, String invocation)
	{
		DataStore ds = status.getDataStore();
		DataElement invocationElement = ds.createObject(null,"invocation",invocation);
		System.out.println("\n Descriptor = "+ project.getDescriptor());
		DataElement cmdD = ds.localDescriptorQuery(project.getDescriptor(),"C_COMMAND");
		if(cmdD!=null)
		{
			ArrayList args = new ArrayList();
			args.add(invocationElement);
			args.add(status);
			ds.command(cmdD,args,project);
		}		
	}
	protected String getOS()
	{
		String OS = new String(System.getProperty("os.name")); 
		return OS;
	}
}

