package com.ibm.cpp.miners.managedproject;

import com.ibm.dstore.core.model.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import java.io.*;
import java.util.ArrayList;
import java.lang.Runtime;

public class AutoconfManager {
	
	DataElement project;
	String autoconf = new String("autoconf");
	String automake = new String("automake");
	String aclocal = new String("aclocal");
	String autoheader = new String("autoheader");
	String autoscan = new String ("autoscan");
	
	
	
	String version = new String("@VERSION@");
	String pack = new String("@PACKAGE@");
	String subdir = new String ("@SUBDIR@");
	
	
	
	protected void manageProject(DataElement proj)
	{
		project = proj;
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
					/*try
					{	
						// creating configure.in 
						//File configure_in = new File(path,"configure.in");
						//FileWriter out = new FileWriter(configure_in);
						//out.write("");
						//out.close();
						// check for support packages
						// if existe then proceed
						//Runtime rt = Runtime.getRuntime();
						//Process pro = rt.exec("autoscan",null,new File(path));
						//pro.destroy();
					
					}catch(IOException e){System.out.println(e.toString());}*/
				}
		
		}
			//ModelInterface api = CppPlugin.getModelInterface();	
			//DataElement status = api.command(project.getSource(), autoconf);
				// autoloca
				// autoheader
				// automake
				// autoconf
			// else notify the user with the missed packages
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
		if(projectFile.isDirectory())
		{
			// add configure.in template
			try{
				rt.exec(
					"cp workspace/com.ibm.cpp.miners/autoconf_templates/configure.in "
						+project.getSource());
				rt.exec(
					"cp workspace/com.ibm.cpp.miners/autoconf_templates/Makefile.am "
						+project.getSource());
			}catch(IOException e){System.out.println(e);}	
		}
		// provide one makefile.am in each subdiectory
		File[] structure = projectFile.listFiles();
		for(int i =0; i < structure.length; i++)
		{
			if(structure[i].isDirectory())
			{
				try{
					rt.exec(
						"cp workspace/com.ibm.cpp.miners/autoconf_templates/sub/Makefile.am "
						+structure[i].getPath());
				}catch(IOException e){System.out.println(e);}
			}
		}
	}
	protected void initializeAutoconfSupprotFiles(DataElement project)
	{
		//udpdate configure.in
		updateConfigure_in(new File(project.getSource(),"configure.in"));
	}
	protected void updateConfigure_in(File configure_in)
	{
		File modFile = new File(project.getSource(),"mod.in");
		// reading configure.in
		String line;
		try{
			// initializing Package Version and Subdir fields
			BufferedReader in = new BufferedReader(new FileReader(configure_in));
			BufferedWriter out= new BufferedWriter(new FileWriter(modFile));
			while((line=in.readLine())!=null)
			{
				if(line.indexOf(pack)!=-1)
				{
					// replace this line with the new values
					line = modifyLine(line);
					//line = new String("***********************************");
				}
				out.write(line+"\n");
				//System.out.println(line);
				//System.out.println("\nPackage Index is = "+line.indexOf(pack));
			}
			in.close();
			out.close();
			modFile.renameTo(configure_in);
		}catch(FileNotFoundException e){System.out.println(e);}
		catch(IOException e){System.out.println(e);}
		
	}
	private String modifyLine(String line)
	{
		String modLine= new String();
		java.util.StringTokenizer token = new java.util.StringTokenizer(line);
		while(token.hasMoreTokens())
		{
			String buff = new String(token.nextToken());
			if(buff.equals(pack))
				buff = new String(project.getName());
			if(buff.equals(version))
				buff = new String("0.1");
			modLine.concat(buff);
		}
		return modLine;
	}
}

