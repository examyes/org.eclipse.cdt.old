package org.eclipse.cdt.cpp.miners.managedproject;
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


import org.eclipse.cdt.dstore.core.model.*;

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
		File Makefile = new File(parent,"Makefile");
		/*if(!Makefile.exists())
		{
			DataElement parentData = target.getParent();
			int i =0;
			while(!parent.getAbsolutePath().equals(autoconfmanager.getWorkSpaceLocation()))
			{
					if(i>0)
						parentData = parentData.getParent();
					parent = parent.getParentFile();
					i++;
			}
			autoconfmanager.getAutoconfScript(parentData);
			if(getOS().equals("Linux"))
				runCommand(parentData,status,"./bootstrap.sc;./configure");
			else
				runCommand(parentData, status, cygwinPrefix+"bootstrap.sc;"+cygwinPrefix+"configure");
			autoconfmanager.runConfigureScript(parentData,status);
			autoconfmanager.runSupportScript(parentData,status);
		}*/
		DataElement dirElement = target.getDataStore().createObject(null, "directory", 
			parent.getName(),
			parent.getAbsolutePath());
		runCommand(dirElement, status, "make " +target.getName());
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

