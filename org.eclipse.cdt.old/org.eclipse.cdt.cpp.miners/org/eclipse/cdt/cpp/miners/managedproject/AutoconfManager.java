package org.eclipse.cdt.cpp.miners.managedproject;
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

/*
 * comment
 * */
import org.eclipse.cdt.dstore.core.model.*;

import java.io.*;
import java.lang.Runtime;
import java.util.*;

public class AutoconfManager {
	final String AUTOCONF = new String("autoconf");
	final String AUTOMAKE = new String("automake");
	final String ACLOCAL = new String("aclocal");
	final String AUTOHEADER = new String("autoheader");
	final String AUTOSCAN = new String ("autoscan");
	ProjectStructureManager structureManager;
	ConfigureInManager configureInManager;
	MakefileAmManager makefileAmManager; 
	static Object O = new Object();
	String cygwinPrefix = new String("sh -c ");
	
	String targetKey = "Target_Type";

	public AutoconfManager()
	{
		configureInManager = new ConfigureInManager();
		makefileAmManager = new MakefileAmManager();
	}
	
	void setWorkspaceLocation(String location)
	{
		makefileAmManager.setWorkspaceLocation(location);
	}
	
/*	protected void manageProject(DataElement project, DataElement status,MakefileAmClassifier classifier)
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
			//generateAutoconfFiles(project,status,true);
			updateAutoconfFiles(project,status,true,classifier);
			getAutoconfScript(project);
			if(getOS().equals("Linux"))
				runCommand(project,status,"./bootstrap.sc"+"&&"+"./configure");
			else
				runCommand(project, status, cygwinPrefix+"bootstrap.sc"+"&&"+cygwinPrefix+"configure");
		}
		//check // autoloca	// autoheader // automake // autoconf 
		// else notify the user with the missed packages
	}*/
	protected void updateAutoconfFiles(DataElement project, DataElement status,MakefileAmClassifier classifier)
	{
		configureInManager.updateConfigureIn(project,true);
		makefileAmManager.updateAllMakefileAm(project,classifier);
	}
	
	protected void configure(DataElement project,DataElement status, boolean update,MakefileAmClassifier classifier)
	{		
		getAutoconfScript(project);
		
		// perform an update in case user has not updated the dependencies 
		//in the Makefile.am's and configure.in
		if(update)
		{
		
			configureInManager.updateConfigureIn(project,true);
			makefileAmManager.updateAllMakefileAm(project,classifier);
		}
		// it will check if configure exists and if not runConfigure will generate it
		generateAndRunConfigure(project,status,update,classifier);
	}

	
	public void generateAndRunConfigure(DataElement project, DataElement status, boolean update,MakefileAmClassifier classifier)
	{
		// if configure is not found then create it first
		File configure = new File (project.getSource(),"configure");
		File script = new File (project.getSource(),"bootstrap.sc");
		if(!configure.exists())
		{
			if(!script.exists())
				getAutoconfScript(project);
			if(getOS().equals("Linux"))
				runCommand(project, status,"./bootstrap.sc"+"&&"+"./configure"+"&&"+"touch -m "+"configure");
			else
			runCommand(project, status,cygwinPrefix+"bootstrap.sc"+"&&"+cygwinPrefix+"configure"+"&&"+
			cygwinPrefix+"\'"+"touch -m "+"configure"+"\'");
		}
		else if(configureIsUptodate(project))
		{
			if(getOS().equals("Linux"))
				runCommand(project, status,"./configure"+"&&"+"touch -m "+"configure");
			else
				runCommand(project, status,cygwinPrefix+"configure"+"&&"+cygwinPrefix+"\'"+"touch -m "+"configure"+"\'");
		}
		else 
		{
			if(getOS().equals("Linux"))
				runCommand(project, status,"./bootstrap.sc"+"&&"+"./configure"+"&&"+"touch -m "+"configure");
			else
				runCommand(project, status,cygwinPrefix+"bootstrap.sc"+"&&"+cygwinPrefix+"configure"+"&&"+cygwinPrefix+"\'"+"touch -m "+"configure"+"\'");
		}
	} 	

	protected void configureTarget(DataElement project,DataElement status, boolean update,MakefileAmClassifier classifier, int projectTarget)
	{		
		getAutoconfScript(project);
		
		// perform an update in case user has not updated the dependencies 
		//in the Makefile.am's and configure.in
		if(update)
		{
			configureInManager.updateConfigureIn(project,true);
			makefileAmManager.updateAllMakefileAm(project,classifier,projectTarget);
		}
		// it will check if configure exists and if not runConfigure will generate it
		generateAndRunConfigure(project,status,update,classifier);
	}

	protected void createConfigure(DataElement project,DataElement status, boolean update,MakefileAmClassifier classifier)
	{
		getAutoconfScript(project);
		
		// perform an update in case user has not updated the dependencies 
		//in the Makefile.am's and configure.in
		if(update)
		{
			configureInManager.updateConfigureIn(project,true);
			makefileAmManager.updateAllMakefileAm(project,classifier);
		}
		
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
		File projectFile = project.getFileObject();
		Process p;	
		Runtime rt = Runtime.getRuntime();
		// check if there is an existing script - calls for aclocal, autoheader,automake and autoconf
		File script = new File (project.getSource(),"bootstrap.sc");
		if(!script.exists())
		{
			if(projectFile.isDirectory()&& !(projectFile.getName().startsWith(".")))
			{
				try{// add bootstrap.sc only if not exist
					p = rt.exec(
						"cp "+project.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH)+
						"/org.eclipse.cdt.cpp.miners/autoconf_templates/bootstrap.sc "+project.getSource());
					p.waitFor();
				}catch(IOException e){System.out.println(e);}
				catch(InterruptedException e){System.out.println(e);}	
			}
		}
		modifyScript(script,projectFile);
		try
		{
			p = rt.exec("chmod +x "+project.getSource()+"/bootstrap.sc ");
			p.waitFor();	
		}catch(IOException e){System.out.println(e);}
		catch(InterruptedException e){System.out.println(e);}	
	}
	private void modifyScript(File script, File parent)
	{
		//check for macros or m4 and point to it
		structureManager = new ProjectStructureManager(parent);
		File [] dirs = structureManager.getSubdirs();
		for(int i = 0;i<dirs.length; i++ )
			if(dirs[i].getName().equals("macros")||dirs[i].getName().equals("m4"))
				modifyAclocalArgument(script, dirs[i]);
	}
	private void modifyAclocalArgument(File script, File dir)
	{
		File mod = new File(script.getParent(),"mod");
		String line;
		boolean found = false;
		try
		{
			// searching for the a clocal line
			BufferedReader in = new BufferedReader(new FileReader(script));
			BufferedWriter out= new BufferedWriter(new FileWriter(mod));
			while((line=in.readLine())!=null)
			{
				if(line.indexOf(ACLOCAL)!=-1)
				{
					String path = dir.getPath().replace('\"','\\');
					String modPath = new String("");
					int j =0;
					for(int i = 0; i < path.length(); i++)
					{
						if(path.charAt(i)=='\\')
						{
							modPath = modPath+path.charAt(i);
							modPath = modPath+'"';
						}
						else
							modPath = modPath+path.charAt(i);
					}
					// check if it does exixt before adding it
					if(!contains(line,modPath))
							line = line+ " -I "+modPath;
				}
				out.write(line);
				out.newLine();
			}
			in.close();
			out.close();
			File abstractPath = new File(script.getAbsolutePath());
			script.delete();
			mod.renameTo(abstractPath);
		}catch(FileNotFoundException e){System.out.println(e);}
		catch(IOException e){System.out.println(e);}
	}
	private boolean contains(String line,String str)
	{
		StringTokenizer tokenizer = new StringTokenizer(line);
		while(tokenizer.hasMoreTokens())
			if(tokenizer.nextToken().equals(str))
				return true;
		return false;
	}
	private void createConfigureScript(DataElement project, DataElement status)
	{
		if(getOS().equals("Linux"))
			runCommand(project, status, "./bootstrap.sc"+"&&"+"touch -m configure");
		else 
			runCommand(project, status, cygwinPrefix+"bootstrap.sc"+"&&"+cygwinPrefix+"\'touch -m configure\'");
	}
	public void runConfigure(DataElement project, DataElement status, boolean update,MakefileAmClassifier classifier)
	{
		// if configure is not found then create it first
		File configure = new File (project.getSource(),"configure");
		File script = new File (project.getSource(),"bootstrap.sc");
		if(!configure.exists())
		{
			if(!script.exists())
				getAutoconfScript(project);
			if(getOS().equals("Linux"))
				runCommand(project, status,"./bootstrap.sc"+"&&"+"./configure"+"&&"+"touch -m "+
				"configure");
			else
			runCommand(project, status,cygwinPrefix+"bootstrap.sc"+"&&"+cygwinPrefix+"configure"+"&&"+
			cygwinPrefix+"\'"+"touch -m "+"configure"+"\'");
		}
		else
		{ 
			// check that configure is up to date
			if(!update)
			{
				// setting time stamp to all Makefile.am and Makefiles.in if cuorrupted when imported
				if(getOS().equals("Linux"))
					runCommand(project, status, "./configure"+"&&"+"touch -m "+"configure");
				else
					runCommand(project, status, cygwinPrefix+"configure"+"&&"+cygwinPrefix+"\'"+"touch -m "+"configure"+"\'");
			}
			else
			{
				// perform an update in case user has not updated the dependencies 
				//in the Makefile.am's and configure.in
				if(update)
				{
					configureInManager.updateConfigureIn(project,true);
					makefileAmManager.updateAllMakefileAm(project,classifier);
				}
				
				if(getOS().equals("Linux"))
					runCommand(project, status,"./bootstrap.sc"+"&&"+"./configure"+"&&"+"touch -m "+"configure");
				else
					runCommand(project, status,cygwinPrefix+"bootstrap.sc"+"&&"+cygwinPrefix+"configure"+"&&"+cygwinPrefix+"\'"+"touch -m "+"configure"+"\'");
			}
		}
	} 
	public void distClean(DataElement project, DataElement status)
	{
		if(getOS().equals("Linux"))
			runCommand(project, status, "make distclean");
		else
			runCommand(project, status, cygwinPrefix+"make distclean");
	}	
	public void install(DataElement project, DataElement status)
	{
		if(getOS().equals("Linux"))
			runCommand(project, status, "make install");
		else
			runCommand(project, status, cygwinPrefix+"make install");
	}	
	public void maintainerClean(DataElement project, DataElement status)
	{
		if(getOS().equals("Linux"))
			runCommand(project, status, "make maintainer-clean");
		else
			runCommand(project, status, cygwinPrefix+"make maintainer-clean");
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
	/*private boolean configureIsUptodate(DataElement root)
	{
		DataElement cmdD = root.getDataStore().localDescriptorQuery(root.getDescriptor(), "C_CHECK_UPDATE_STATE", 4);
		
		if (cmdD != null)
		{
			DataElement status = root.getDataStore().synchronizedCommand(cmdD, root);		
			DataElement updateState = (DataElement)status.get(0);
		    String state = updateState.getName();
		    if(state.equals("uptodate"))
		    	return true;
		}
		return false;
	}*/
	private boolean configureIsUptodate(DataElement subject)
	{
		File configure = getfile(subject.getFileObject(),"configure");
		long configureTimeStamp = configure.lastModified();
		System.out.println("\n My configure file = "+configureTimeStamp);
		
		File rootObject = subject.getFileObject();
		ProjectStructureManager manager = new ProjectStructureManager(rootObject);
		
		File[] list = manager.getFiles();
		for(int i = 0; i < list.length; i++)
			if(!list[i].getName().equals("configure"))
			{
				System.out.println("\n"+list[i].getName()+" = "+list[i].lastModified());
				if(list[i].lastModified()>configureTimeStamp)
					return false;
			}
		return true;
	}
	private File getfile(File dir,String name)
	{
		File[] list = dir.listFiles();
		for(int i = 0; i < list.length; i++)
			if(list[i].getName().equals(name))
				return list[i];
		return null;
	}
}

