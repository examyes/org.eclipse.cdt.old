package com.ibm.cpp.miners.managedproject;
/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
import com.ibm.dstore.core.model.*;
import java.io.*;
import java.lang.*;

public class MakefileAmManager {

	DataElement project;
	ProjectStructureManager structureManager;
	
	// Member Variables which can be defined in Makefile.am
	String SUBDIRS = new String("SUBDIRS");
	String bin_BROGRAMS = new String ("bin_PROGRAMS");
	String _LDADD = new String("_LDADD");
	String _SOURCES = new String("_SOURCES");
	String EXTRA_DIST = new String("EXTA_DIST");
	String INCLUDES = new String("INCLUDES");
	String _LDFLAGS= new String("_LDFLAGS");
	
	// data insertion
	String TARGET = new String("!TARGET!");
	char delim = '!';
	
	/**
	 * Constructor for MakefileAmManager
	 */
	public MakefileAmManager(DataElement aProject) {
		
		this.project = aProject;
		structureManager = new ProjectStructureManager( project.getFileObject());
		
	}
	protected void manageMakefile_am()
	{
		
		Object[][] projectStucture = structureManager.getProjectStructure();
		for(int i =0; i < projectStucture.length; i++)
		{
			Integer level = new Integer((String)projectStucture[i][1]);
			switch (level.intValue())
			{
				case (0):
					// update top level Makefile.am - basically updating the SUBDIR variable definition
					updateTopLevelMakefile_am(new File(project.getSource(),"Makefile.am"));
				case (1):
					// update First level Makefile.am - updating the bin_BROGRAMS,SUBDIR variable definition
					updateFirstLevelMakefile_am((File)projectStucture[i][0]);
				default:
					//updateMakeFile_am();
			}
		}
	}
	private void updateFirstLevelMakefile_am(File parent)
	{
		File Makefile_am = new File(parent,"Makefile.am");
		String line = new String();
		if(Makefile_am.exists())
		{
			File modMakefile_am = new File(parent,"mod_Makefile.am");// this is the top level Makefile.am
			boolean found_SUBDIRS = false;
			boolean found_bin_PROGRAMS = false;
			boolean found_LDADD = false;
			boolean found_SOURCES = false;
			boolean found_EXTRA_DIST = false;
			boolean found_LDFLAGS = false;
			try
			{
				// searching for the subdir line
				BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
				BufferedWriter out= new BufferedWriter(new FileWriter(modMakefile_am));
				while((line=in.readLine())!=null)
				{
					// searching for the bin_PROGRAMS line
					if(line.indexOf(bin_BROGRAMS)!=-1)
					{
						found_bin_PROGRAMS = true;
						line = updateBinProgramsLine(line, parent);
					}
					if(line.indexOf(_SOURCES)!=-1)
					{
						found_SOURCES = true;
						line = updateSourcesLine(line, parent);
					}
					if(line.indexOf(_LDADD)!=-1)
					{
						found_LDADD= true;
						line = updateLdaddLine(line, parent);
					}
					if(line.indexOf(SUBDIRS)!=-1)
					{
						found_SUBDIRS = true;
						line = updateSubdirsLine(line, parent);
					}
	

					if(line.indexOf(EXTRA_DIST)!=-1)
					{
						found_EXTRA_DIST = true;
						line = updateExtraDistLine(line, parent);
					}
					if(line.indexOf(_LDFLAGS)!=-1)
					{
						found_LDFLAGS= true;
						line = updateLdflagsLine(line, parent);
					}
				
					out.write(line);
					out.newLine();
				}
				in.close();
				out.close();
				modMakefile_am.renameTo(Makefile_am);
			}catch(FileNotFoundException e){System.out.println(e);}
			catch(IOException e){System.out.println(e);}
			
			//if(!found_SUBDIRS)
				//insertSubdirsLine(Makefile_am);
			//if(!found_bin_PROGRAMS)
				//insertBinProgramLine(Makefile_am);
			//if(!found_LDADD)
				//insertLdaddLine(Makefile_am);
			//if(!found_SOURCES)
				//insertSourcesLine(Makefile_am,parent);
			//if(!found_INCLUDES)
				//insertIncludesLine(Makefile_am);
			//if(!found_LDFLAGS)
				//insertLdflagsLine(Makefile_am);*/

		}
	}
	private String updateExtraDistLine(String line, File parent)
	{
		// add files to the EXTRA_DIST variable
		for(int i = 0; i <parent.listFiles().length; i++)
		{
			String name = parent.listFiles()[i].getName();
			if(name.endsWith(".c")|| name.endsWith(".h")||name.endsWith(".cpp") ||name.endsWith(".H") || name.endsWith(".C"))
				line = line.concat(" "+name);
		}
		return line;
	}
	private String updateSubdirsLine(String line, File parent)
	{
		// add subdire to the SUBDIRS variable
		ProjectStructureManager dir_structure = new ProjectStructureManager( parent);
		Object[][] subdirs = dir_structure.getProjectStructure();
		for(int i = 0; i <subdirs.length; i++)
			if(((String)subdirs[i][1]).equals("1"))
				line = line.concat(" "+((File)subdirs[i][0]).getName());
		return line;
		
	}
	private String updateLdflagsLine(String line, File parent)
	{
		// add the target name at the begining of the "_SOURCES"
		String mod = line.substring(line.lastIndexOf(_LDFLAGS));
		return line = parent.getName().concat(mod);
	}
	private String updateBinProgramsLine(String line, File parent)
	{
		if(line.indexOf(TARGET)!=-1)
		{
			line = new String(trimTargetLine(line));
			line = line.concat(" ").concat(parent.getName());
		}
		return line;
	}
	private String updateSourcesLine(String line, File parent)
	{
		// add the target name at the begining of the "_SOURCES"
		String mod = line.substring(line.lastIndexOf(_SOURCES));
		line = parent.getName().concat(mod);
		// add files to the _SOURCES variable
		for(int i = 0; i <parent.listFiles().length; i++)
			if(!parent.listFiles()[i].isDirectory())
			{
				String name = parent.listFiles()[i].getName();
				if(line.indexOf(name)==-1 && 
					(name.endsWith(".c")|| name.endsWith(".h")||name.endsWith(".cpp") ||
						name.endsWith(".H") || name.endsWith(".C")))
					line = line.concat(" ").concat(parent.listFiles()[i].getName());
			}
		return line;
	}
	private String updateLdaddLine(String line, File parent)
	{
		// add the target name at the begining of the "_LDADD"
		String mod = line.substring(line.lastIndexOf(_LDADD));
		line = parent.getName().concat(mod);
		// add libs to the _LDADD variable
		ProjectStructureManager dir_structure = new ProjectStructureManager( parent);
		String[] subNames = dir_structure.getSubdirWorkspacePath();
		for(int i = 0; i <subNames.length; i++)
		{
			String tok = getLastToken(subNames[i]);
			String modTok = new String();
			String modName = subNames[i].substring(0,subNames[i].lastIndexOf(tok));
			modTok = modTok.concat("lib").concat(tok).concat(".a");
			modName = modName.concat(modTok);
			line = line.concat(" "+modName);
		}
		return line;
	}		
	private String getLastToken(String aName)
	{
		String name = new String(aName);
		String last_dir_name = new String();
		java.util.StringTokenizer token = new java.util.StringTokenizer(name,"/");
		while(token.hasMoreTokens())
			last_dir_name = token.nextToken();
		return last_dir_name;
		
	}		
	private void updateTopLevelMakefile_am(File Makefile_am)
	{
		if(Makefile_am.exists())
		{
			File modMakefile_am = new File(project.getSource(),"mod_Makefile.am");// this is the tope level Makefile.am
			String line;
			boolean found = false;
			try
			{
				// searching for the subdir line
				BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
				BufferedWriter out= new BufferedWriter(new FileWriter(modMakefile_am));
				while((line=in.readLine())!=null)
				{
					if(line.indexOf(SUBDIRS)!=-1)
					{
						found = true;
						line = insertSubdirValueDef();
					}
				
					out.write(line);
					out.newLine();
				}
				in.close();
				out.close();
				modMakefile_am.renameTo(Makefile_am);
			}catch(FileNotFoundException e){System.out.println(e);}
			catch(IOException e){System.out.println(e);}
			if(!found)
				insertSubdirVaiableDefAtFirstLine(Makefile_am);
		}
			
		
	}
	private void insertSubdirVaiableDefAtFirstLine(File Makefile_am)
	{
		File modMakefile_am = new File(project.getSource(),"mod_Makefile.am");// this is the tope level Makefile.am
		String line;
		try
		{
			// searching for the subdir line
			BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
			BufferedWriter out= new BufferedWriter(new FileWriter(modMakefile_am));
			// insert the SUBDIR line @ the begining of the file
			out.write(insertSubdirValueDef());
			out.newLine();
			while((line=in.readLine())!=null)
			{
				out.write(line);
				out.newLine();
			}
			in.close();
			out.close();
			modMakefile_am.renameTo(Makefile_am);
		}catch(FileNotFoundException e){System.out.println(e);}
		catch(IOException e){System.out.println(e);}
	}
	private String insertSubdirValueDef()
	{
		String childrenOfTopDir = new String();
		// get subdirectories of depth one of the top level dir
		Object[][] projectStructure = structureManager.getProjectStructure();
		for(int i=0; i < projectStructure.length; i ++)
			if(projectStructure[i][1].equals("1"))
				childrenOfTopDir =	childrenOfTopDir.concat(" "+((File)projectStructure[i][0]).getName());
		String line=new String (SUBDIRS+" ="+childrenOfTopDir);
		return line;
	}
	private void updateMakefile_am(File Makefile_am)
	{
		File modFile = new File(project.getSource(),"mod_Makefile.am");
		// reading configure.in
		String line;
		try{
			// initializing Package Version and Subdir fields
			BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
			BufferedWriter out= new BufferedWriter(new FileWriter(modFile));
		/*	while((line=in.readLine())!=null)
			{
				if(line.indexOf(pack)!=-1)
				{
					line = trimTargetLine(line);// replace this line with the new values
					line = insertPackageName(line.toCharArray(),delimPosition[0]);
					line = insertVersionName(line.toCharArray(),delimPosition[1]);
				}
				
				if(line.indexOf(subdir)!=-1)
				{
					
					if(subdirs.length>0)
					{
						line = trimTargetLine(line);
						line = insertSubdirs(line.toCharArray(),delimPosition[0]);
					}
				}
				// needed at the end of each line when writing  the modified file
				out.write(line+"\n"); 
			}*/
			in.close();
			out.close();
			modFile.renameTo(Makefile_am);
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
				i++;// advance to by pass the delim
				while(originalLine[i]!=delim)
					i++;
			}
		}
		return (new String(modLine)).trim();
	}
}

