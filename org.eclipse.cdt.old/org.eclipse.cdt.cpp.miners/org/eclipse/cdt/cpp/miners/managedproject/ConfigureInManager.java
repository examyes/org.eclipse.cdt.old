package com.ibm.cpp.miners.managedproject;
/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
import com.ibm.dstore.core.model.*;
import java.io.*;

public class ConfigureInManager {
	DataElement project;
	String amKey = new String("AM_INIT_AUTOMAKE");
	String acKey = new String("AC_OUTPUT");
	String pack = new String("@PACKAGE@");
	String makefile = new String("/Makefile ");// note the space @ the end
	int[] delimPosition = {-1,-1,-1};
	char delim = '@';
	String[] subdirs;
	/**
	 * Constructor for ConfigureInManager
	 */
	public ConfigureInManager(DataElement aProject) {
		
		this.project = aProject;
		ProjectStructureManager structureManager = new ProjectStructureManager( project.getFileObject());
		subdirs = structureManager.getSubdirWorkspacePath();
		
	}
	protected void manageConfigureIn( boolean actionIsManageProject)
	{
		// check if there is an existing configure.in
		File configure_in = new File (project.getSource(),"configure.in");
		if(!configure_in.exists())
		{
			getConfigureInTemplateFile(project);
			initializeConfigureIn(new File(project.getSource(),"configure.in"));
		}
		else // it does exist
		{
			// if the action was mangeProject then
			if(actionIsManageProject)
			{
				System.out.println("************************************************"+
									"\n* configure.in will be updated"+ 
									"\n* existing file will be named configure.in.old"+
									"\n* Never prompt me again! - check box"+
									"***********************************************");
			}
			// notify the user, using a popup dilalog, that the file will be updated - only 2 macros
			// the rest of the file will be te same
			Runtime rt = Runtime.getRuntime();
			// copy the old configure.in to configure.in.old
			try{
				Process p = rt.exec("cp configure.in configure.in.old ", null, project.getFileObject());
				p.waitFor();
			}catch(IOException e){System.out.println(e);}
			catch(InterruptedException e){System.out.println(e);}	
			// update configure.in
			updateConfigureIn(new File(project.getSource(),"configure.in"));
		}
	}
	private void updateConfigureIn(File configure_in)
	{
		File modFile = new File(project.getSource(),"mod.in");
		// reading configure.in
		String line;
		try{// initializing Package Version and Subdir fields
			BufferedReader in = new BufferedReader(new FileReader(configure_in));
			BufferedWriter out= new BufferedWriter(new FileWriter(modFile));
			while((line=in.readLine())!=null)
			{
				if(line.indexOf(amKey)!=-1)
				{
					// just make sure that the package name has a name diferent than @PACKAGE@
					if(line.indexOf(pack)!=-1)
					{
						line = trimTargetLine(line);// replace this line with the new values
						line = insertPackageName(line.toCharArray(),delimPosition[0]);
						line = insertVersionName(line.toCharArray(),delimPosition[1]);
					}
				}
				if(line.indexOf(acKey)!=-1)
				{
					if(subdirs.length>0)
						line = updateAcoutputMacroLine(line);
				}
				out.write(line);
				out.newLine();// needed at the end of each line when writing  the modified file
			}
			in.close();
			out.close();
			// because rename does not work properly on windows cygwin
			File abstractPath = new File(configure_in.getAbsolutePath());
			configure_in.delete();
			modFile.renameTo(abstractPath);
		}catch(FileNotFoundException e){System.out.println(e);}
		catch(IOException e){System.out.println(e);}
	}
	private void initializeConfigureIn(File configure_in)
	{
		File modFile = new File(project.getSource(),"mod.in");
		// reading configure.in
		String line;
		try{// initializing Package Version and Subdir fields
			BufferedReader in = new BufferedReader(new FileReader(configure_in));
			BufferedWriter out= new BufferedWriter(new FileWriter(modFile));
			while((line=in.readLine())!=null)
			{
				if(line.indexOf(amKey)!=-1)
				{
					line = trimTargetLine(line);// replace this line with the new values
					line = insertPackageName(line.toCharArray(),delimPosition[0]);
					line = insertVersionName(line.toCharArray(),delimPosition[1]);
				}
				if(line.indexOf(acKey)!=-1)
				{
					if(subdirs.length>0)
						line = updateAcoutputMacroLine(line);
				}
				out.write(line);
				out.newLine();// needed at the end of each line when writing  the modified file
			}
			in.close();
			out.close();
			// because rename does not work properly on windows cygwin
			File abstractPath = new File(configure_in.getAbsolutePath());
			configure_in.delete();
			modFile.renameTo(abstractPath);
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
		String packageName = (new String(modLine)); 
		return packageName;
	}
	private String insertVersionName(char[] line, int pos)
	{
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
		String versionName = (new String(modLine)).trim();
		return versionName;
	}
	private String updateAcoutputMacroLine(String line)
	{
		line = line.substring(0,line.indexOf('('));
		StringBuffer buff = new StringBuffer(line);
		buff.append("( Makefile");
		for(int j = 0; j< subdirs.length; j++)
			if(subdirs[j].indexOf(".")==-1)// check that the path doesnot have any  hidden dirs
				buff.append(subdirs[j]).append(makefile).append('\\');
		buff.append(')');	
		return buff.toString();
	}
	protected void getConfigureInTemplateFile(DataElement project)
	{
		Runtime rt = Runtime.getRuntime();
		//check the project structure
		File projectFile = project.getFileObject();
		if(projectFile.isDirectory()&& !(projectFile.getName().startsWith(".")))
		{
			// add configure.in template files only if not exist
			try{
				Process p = rt.exec(
					"cp workspace/com.ibm.cpp.miners/autoconf_templates/configure.in "
						+project.getSource());
				p.waitFor();
			}catch(IOException e){System.out.println(e);}
			catch(InterruptedException e){System.out.println(e);}	
		}
	}
}


