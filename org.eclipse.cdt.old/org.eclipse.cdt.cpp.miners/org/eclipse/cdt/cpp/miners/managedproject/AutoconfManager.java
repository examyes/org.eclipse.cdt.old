package com.ibm.cpp.miners.managedproject;
/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

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
	
	
	// will be grouped under Configure_in Manager
	String version = new String("@VERSION@");
	String pack = new String("@PACKAGE@");
	String subdir = new String ("@SUBDIR@");
	int[] delimPosition = {-1,-1,-1,-1};
	char delim = '@';
	
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
			}
			//ModelInterface api = CppPlugin.getModelInterface();	
			//DataElement status = api.command(project.getSource(), autoconf);
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
		// update Makefile.am
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
					line = trimTargetLine(line);// replace this line with the new values
					line = insertPackageName(line.toCharArray(),delimPosition[0]);
					line = insertVersionName(line.toCharArray(),delimPosition[1]);
				}
				
				if(line.indexOf(subdir)!=-1)
				{
					line = trimTargetLine(line);
					line = insertSubdirs(line.toCharArray(),delimPosition[0]);
					
				}
				// needed at the end of each line when writing  the modified file
				out.write(line+"\n"); 
			}
			in.close();
			out.close();
			modFile.renameTo(configure_in);
		}catch(FileNotFoundException e){System.out.println(e);}
		catch(IOException e){System.out.println(e);}
		
	}
	private String trimTargetLine(String line)
	{
		char[] originalLine = line.toCharArray();
		char[] modLine= new char[line.length()];
		boolean found = true;
		int counter = 0;
		int loc = 0;
		for(int i = 0; i < originalLine.length; i++)
		{
			if(originalLine[i] != delim)
			{
				modLine[counter]=originalLine[i];
				counter++;
			}
			else
			{
				
				delimPosition[loc++] = i++;
				System.out.println("\n pos = "+delimPosition[loc-1]);
				while(originalLine[i]!=delim)
					i++;
			}
		}
		return new String(modLine);
	}
	private String insertPackageName(char[] line, int position)
	{
		int k=0;
		int counetrForModLine = 0;
		char[] modLine = new char[256];
		System.out.println("\n line length = "+line.length);
		int i = 0;
		while(line[i]!= '\0')
		{
			if(i == position)
				for(int j=0; j<project.getName().toCharArray().length; j++)
					modLine[counetrForModLine++]=project.getName().toCharArray()[j];
			modLine[counetrForModLine++]=line[i];
			i++;
		}
		int extra_chars = delimPosition[1] - delimPosition[0] - pack.length(); // to account for any characters between @PACKAGE@ and @VERSIONS@
		delimPosition[1] = position+project.getName().toCharArray().length+extra_chars;
		return new String(modLine);
	}
	private String insertVersionName(char[] line, int pos)
	{
		System.out.println("\n line length = "+line.length);
		String version = new String("0.1");
		int k=0;
		int counetrForModLine = 0;
		char[] modLine = new char[256];
		int i = 0;
		while(line[i]!= '\0')
		{
			if(i == pos)
				for(int j=0; j<version.toCharArray().length; j++)
					modLine[counetrForModLine++]=version.toCharArray()[j];
			modLine[counetrForModLine++]=line[i];
			i++;
		}
		return new String(modLine);
	}
	private String insertSubdirs(char[] line, int position)
	{
		int k=0;
		int counetrForModLine = 0;
		char[] modLine = new char[256];
		System.out.println("\n line length = "+line.length);
		int i = 0;
		while(line[i]!= '\0')
		{
			if(i == position)
				for(int j=0; j<project.getName().toCharArray().length; j++)
					modLine[counetrForModLine++]=project.getName().toCharArray()[j];
			modLine[counetrForModLine++]=line[i];
			i++;
		}
		int extra_chars = delimPosition[1] - delimPosition[0] - pack.length(); // to account for any characters between @PACKAGE@ and @VERSIONS@
		delimPosition[1] = position+project.getName().toCharArray().length+extra_chars;
		return new String(modLine);
	}	
	
}

