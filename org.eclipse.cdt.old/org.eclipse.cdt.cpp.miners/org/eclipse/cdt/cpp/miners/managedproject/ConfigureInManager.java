package com.ibm.cpp.miners.managedproject;
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import com.ibm.dstore.core.model.*;
import java.io.*;

public class ConfigureInManager {
	String amKey = new String("AM_INIT_AUTOMAKE");
	String acKey = new String("AC_OUTPUT");
	String pack = new String("@PACKAGE@");
	String makefile = new String("/Makefile");
	int[] delimPosition = {-1,-1,-1};
	char delim = '@';
	static long timeStamp=-1;
	/**
	 * Constructor for ConfigureInManager
	 */
	public ConfigureInManager() 
	{

	}
	protected void generateConfigureIn(DataElement project)
	{
		File configure_in = new File (project.getSource(),"configure.in");
		getConfigureInTemplateFile(project);
		initializeConfigureIn(project,configure_in);
	}
	private void initializeConfigureIn(DataElement project,File configure_in)
	{
		ProjectStructureManager structureManager = new ProjectStructureManager( project.getFileObject());
		String[] subdirs = structureManager.getSubdirWorkspacePath();
		File modFile = new File(project.getSource(),"mod.in");
		// reading configure.in
		String line;
		try{// initializing Package Version and Subdir fields
			BufferedReader in = new BufferedReader(new FileReader(configure_in));
			BufferedWriter out= new BufferedWriter(new FileWriter(modFile));
			out.write(getGeneratedStamp());
			out.newLine();
			while((line=in.readLine())!=null)
			{
				if(line.indexOf(amKey)!=-1)
				{
					line = trimTargetLine(line);// replace this line with the new values
					line = insertPackageName(project.getName(),line.toCharArray(),delimPosition[0]);
					line = insertVersionName(line.toCharArray(),delimPosition[1]);
				}
				if(line.indexOf(acKey)!=-1)
					line = updateAcoutputMacroLine(subdirs,line);
					
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
	private String getGeneratedStamp()
	{
		String stamp = new String(
		"dnl Generated by C/C++ IDE plugin \n");
		return stamp;
	}
	private boolean doesStampExist(File configureIn)
	{
		String line;
		try{// initializing Package Version and Subdir fields
			BufferedReader in = new BufferedReader(new FileReader(configureIn));
			while((line=in.readLine())!=null)
				if(line.indexOf("C/C++ IDE plugin")!=-1)
					return true;
			in.close();
		}catch(IOException e){System.out.println(e);}
		return false;		
	}	
	protected void updateConfigureIn(DataElement project,boolean forceGenerate)
	{
		// notify the user, using a popup dilalog, that the file will be updated - only 2 macros
		// the rest of the file will be te same
		File configure_in = new File (project.getSource(),"configure.in");
		//&&doesStampExist(configure_in)&&timeStamp==configure_in.lastModified()
		//update anyway even if the file is imported from outside the tool
		// updating envolves only the AC_OUTPUT macro
		if(configure_in.exists())
		{
			createDotOldFileFor(configure_in);
			updateConfigureIn(project,configure_in);
			compareOldAndNew(configure_in.getParentFile());
		}
		else if(forceGenerate)
			generateConfigureIn(project);
	}
	private void updateConfigureIn(DataElement project,File configure_in)
	{
		ProjectStructureManager structureManager = new ProjectStructureManager( project.getFileObject());
		String[] subdirs = structureManager.getSubdirWorkspacePath();

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
						line = insertPackageName(project.getName(),line.toCharArray(),delimPosition[0]);
						line = insertVersionName(line.toCharArray(),delimPosition[1]);
					}
				}
				if(line.indexOf(acKey)!=-1)
					line = updateAcoutputMacroLine(subdirs,line);
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
	private String insertPackageName(String name,char[] line, int position)
	{
		int k=0;
		int counetrForModLine = 0;
		char[] modLine = new char[256];
		int i = 0;
		while(line[i]!= '\0')
		{
			if(i == position)
				for(int j=0; j<name.toCharArray().length; j++)
					modLine[counetrForModLine++]=name.toCharArray()[j];
			modLine[counetrForModLine++]=line[i];
			i++;
		}
		int extra_chars = delimPosition[1] - delimPosition[0] - pack.length(); // to account for any characters between @PACKAGE@ and @VERSIONS@
		delimPosition[1] = position+name.toCharArray().length+extra_chars;
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
	private String updateAcoutputMacroLine(String[] subdirs,String line)
	{
		line = line.substring(0,line.indexOf('('));
		StringBuffer buff = new StringBuffer(line);
		buff.append("(Makefile"+" ");
		for(int j = 0; j< subdirs.length; j++)
			if(subdirs[j].indexOf(".")==-1)// check that the path doesnot have any  hidden dirs
				buff.append(" "+subdirs[j]).append(makefile);
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
				Process p = rt.exec("cp "+ project.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH)
				 					+"/"+"com.ibm.cpp.miners/autoconf_templates/configure.in "
						+project.getSource());
				p.waitFor();
			}catch(IOException e){System.out.println(e);}
			catch(InterruptedException e){System.out.println(e);}	
		}
	}
	private void compareOldAndNew(File parent)
	{
		File _new = new File(parent,"configure.in");
		File _old = new File(parent,"configure.in.old");
		String updatedLine;
		String oldLine;
		boolean remove = true; 
		try
		{
			// searching for the subdir line
			BufferedReader updated = new BufferedReader(new FileReader(_new));
			BufferedReader old= new BufferedReader(new FileReader(_old));
			oldLine = old.readLine();
			updatedLine = updated.readLine();
			while(updatedLine!=null || oldLine!=null)
			{
				if(!updatedLine.equals(oldLine))
				{
					remove = false;
					break;
				}
				oldLine = old.readLine();
				updatedLine = updated.readLine();
			}
			updated.close();
			old.close();
		}catch(FileNotFoundException e){System.out.println(e);}
		catch(IOException e){System.out.println(e);}
		if(remove)
			_old.delete();		
	}
	private void createDotOldFileFor(File aFile)
	{
		Runtime rt = Runtime.getRuntime();
		// rename the existing Makefile.am if exists
		try{
			Process p;
			// check if exist then
			p= rt.exec("cp "+aFile.getName()+" "+aFile.getName()+".old ", null, aFile.getParentFile());
			p.waitFor();
		}catch(IOException e){System.out.println(e);}
		catch(InterruptedException e){System.out.println(e);}	
	}
}


