package com.ibm.cpp.miners.managedproject;
/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
import com.ibm.dstore.core.model.*;
import java.io.*;
import java.lang.*;
import java.util.*;

public class MakefileAmManager {

	private final int TOPLEVEL = 1;
	private final int PROGRAMS = 2;
	private final int STATICLIB = 3;
	private final int SHAREDLIB = 4;
	
	DataElement project;
	ProjectStructureManager structureManager;
	
	public static Hashtable timeStamps = new Hashtable();
	private final String MAKEFILE_AM = "Makefile.am";
	private final String TEMPLOCATION = "workspace/com.ibm.cpp.miners/autoconf_templates/";
	
	// Member Variables which can be defined in Makefile.am
	final String SUBDIRS = new String("SUBDIRS");
	final String bin_PROGRAMS = new String ("bin_PROGRAMS");
	final String _LDADD = new String("_LDADD");
	final String _SOURCES = new String("_SOURCES");
	final String EXTRA_DIST = new String("EXTA_DIST");
	final String INCLUDES = new String("INCLUDES");
	final String _LDFLAGS= new String("_LDFLAGS");
	
	// needed to for creating Makefile.am's
	String[] subdirs;
	
	//for static lib files
	final String _LIBRARIES = new String("_LIBRARIES");
	final String _a_SOURCES = new String("_a_SOURCES");
	//for shared lib files
	final String _LTLIBRARIES = new String("_LTLIBRARIES");
	final String _la_SOURCES = new String("_la_SOURCES"); 	
	// updating data
	final String TARGET = new String("!TARGET!");
	final char delim = '!';
	
	// to identify Makefile.am identity
	private static MakefileAmClassifier classifier = new MakefileAmClassifier();
	
	// default values in Makefile.am
	//String targetSuffix = new String("_target");
	String targetSuffix = new String("");
	/**
	 * Constructor for MakefileAmManager
	 */
	public MakefileAmManager(DataElement aProject) {
		
		this.project = aProject;
		structureManager = new ProjectStructureManager( project.getFileObject());
		subdirs = structureManager.getSubdirWorkspacePath();
		
	}
	protected void generateMakefileAm()
	{
		getMakefileAmTemplateFiles(project);
		initializeMakefileAm();
	}
	protected void getMakefileAmTemplateFiles(DataElement project)
	{
		Runtime rt = Runtime.getRuntime();
		//check the project structure
		File projectFile = project.getFileObject();
		if(projectFile.isDirectory()&& !(projectFile.getName().startsWith(".")))
		{
			// add configure.in template files only if not exist
			try{
				Process p;
				// check if exist then
				p= rt.exec("cp "+TEMPLOCATION+MAKEFILE_AM+" "+project.getSource());
				p.waitFor();
			}catch(IOException e){System.out.println(e);}
			catch(InterruptedException e){System.out.println(e);}	
		}
		// provide one makefile.am in each subdiectory
		for(int i =0; i < subdirs.length ; i++)
		{
			if(subdirs[i].indexOf(".")==-1)
			{
				StringTokenizer token = new StringTokenizer(subdirs[i],"/");
				if (token.countTokens()==1)
				{
					try{
						Process p = rt.exec("cp "+TEMPLOCATION+"/sub/Makefile.am "+project.getSource()+"/"+subdirs[i]);
						p.waitFor();
					}catch(IOException e){System.out.println(e);}
					catch(InterruptedException e){System.out.println(e);}
				}
				else
				{
					
					try{
						Process p= rt.exec("cp "+TEMPLOCATION+"/sub/static/Makefile.am "+project.getSource()+"/"+subdirs[i]);
						p.waitFor();
					}catch(IOException e){System.out.println(e);}
					catch(InterruptedException e){System.out.println(e);}
				}
			}
		}
	}
	private void initializeMakefileAm()
	{
		Object[][] projectStucture = structureManager.getProjectStructure();
		for(int i =0; i < projectStucture.length; i++)
		{
			Integer level = new Integer((String)projectStucture[i][1]);
			String absPath = new String("");
			switch (level.intValue())
			{
				case (0):
					// initialize top level Makefile.am - basically updating the SUBDIR variable definition
					initTopLevelMakefileAm((File)projectStucture[i][0]);
					absPath = ((File)projectStucture[i][0]).getAbsolutePath()+MAKEFILE_AM;
					timeStamps.put(absPath,new Long(getMakefileAmStamp(((File)projectStucture[i][0]))));
					break;
				case (1):
					// initialize First level Makefile.am - updating the bin_BROGRAMS,SUBDIR variable definition
					initProgramsMakefileAm((File)projectStucture[i][0]);
					absPath = ((File)projectStucture[i][0]).getAbsolutePath()+MAKEFILE_AM;
					timeStamps.put(absPath,new Long(getMakefileAmStamp(((File)projectStucture[i][0]))));
					break;
				default:
				// initialize all other files in the subdirs
					initStaticLibMakefileAm((File)projectStucture[i][0]);
					absPath = ((File)projectStucture[i][0]).getAbsolutePath()+MAKEFILE_AM;
					timeStamps.put(absPath,new Long(getMakefileAmStamp(((File)projectStucture[i][0]))));
			}
		}
	}
	private void initTopLevelMakefileAm(File parent)
	{
		File Makefile_am = new File(parent,"Makefile.am");
		if(Makefile_am.exists())
		{
			File modMakefile_am = new File(parent.getAbsolutePath(),"mod_Makefile.am");// this is the tope level Makefile.am
			String line;
			boolean found = false;
			try
			{
				// searching for the subdir line
				BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
				BufferedWriter out= new BufferedWriter(new FileWriter(modMakefile_am));
				out.write(getGeneratedStamp());
				out.newLine();
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
				File abstractPath = new File(Makefile_am.getAbsolutePath());
				Makefile_am.delete();
				modMakefile_am.renameTo(abstractPath);
				//modMakefile_am.renameTo(Makefile_am);
			}catch(FileNotFoundException e){System.out.println(e);}
			catch(IOException e){System.out.println(e);}
		}
	}
	private void initProgramsMakefileAm(File parent)
	{
		File Makefile_am = new File(parent,"Makefile.am");
		String line = new String();
		if(Makefile_am.exists())
		{
			File modMakefile_am = new File(parent,"mod_Makefile.am");
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
				out.write(getGeneratedStamp());
				out.newLine();
				while((line=in.readLine())!=null)
				{
					// searching for the bin_PROGRAMS line
					if(line.indexOf(bin_PROGRAMS)!=-1)
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
				File abstractPath = new File(Makefile_am.getAbsolutePath());
				Makefile_am.delete();
				modMakefile_am.renameTo(abstractPath);
				//modMakefile_am.renameTo(Makefile_am);
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
				//insertLdflagsLine(Makefile_am);
		}
	}
	private void initStaticLibMakefileAm(File parent)
	{
		File Makefile_am = new File(parent,"Makefile.am");
		String line = new String();
		if(Makefile_am.exists())
		{
			File modMakefile_am = new File(parent,"mod_Makefile.am");
			boolean found_SUBDIRS = false;
			boolean found_LIBRARIES = false;
			boolean found_a_SOURCES = false;
			boolean found_EXTRA_DIST = false;
			try
			{
				// searching for the subdir line
				BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
				BufferedWriter out= new BufferedWriter(new FileWriter(modMakefile_am));
				out.write(getGeneratedStamp());
				out.newLine();
				while((line=in.readLine())!=null)
				{
					// searching for the bin_PROGRAMS line

					if(line.indexOf(_a_SOURCES)!=-1)
					{
						found_a_SOURCES = true;
						line = updateASourcesLine(line, parent);
					}
					if(line.indexOf(_LIBRARIES)!=-1)
					{
						found_LIBRARIES= true;
						line = updateLibrariesLine(line, parent);
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
				
					out.write(line);
					out.newLine();
				}
				in.close();
				out.close();
				File abstractPath = new File(Makefile_am.getAbsolutePath());
				Makefile_am.delete();
				modMakefile_am.renameTo(abstractPath);
				//modMakefile_am.renameTo(Makefile_am);
			}catch(FileNotFoundException e){System.out.println(e);}
			catch(IOException e){System.out.println(e);}
			
			//if(!found_SUBDIRS)
				//insertSubdirsLine(Makefile_am);
			//if(!found_LIBRARIES)
				//insertLdaddLine(Makefile_am);
			//if(!found_SOURCES)
				//insertSourcesLine(Makefile_am,parent);

		}
	}
	private void initSharedLibMakefileAm(File parent)
	{
		File Makefile_am = new File(parent,"Makefile.am");
		String line = new String();
		if(Makefile_am.exists())
		{
			File modMakefile_am = new File(parent,"mod_Makefile.am");
			boolean found_LTLIBRARIES = false;
			boolean found_la_SOURCES = false;
			boolean found_SUBDIRS = false;
			boolean found_EXTRA_DIST = false;
			try
			{
				// searching for the subdir line
				BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
				BufferedWriter out= new BufferedWriter(new FileWriter(modMakefile_am));
				out.write(getGeneratedStamp());
				out.newLine();
				while((line=in.readLine())!=null)
				{
					// searching for the bin_PROGRAMS line
					if(line.indexOf(_LTLIBRARIES)!=-1)
					{
						found_LTLIBRARIES= true;
						line = updateLtlibrariesLine(line, parent);
					}
					if(line.indexOf(_la_SOURCES)!=-1)
					{
						found_la_SOURCES = true;
						line = updateAlSourcesLine(line, parent);
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
					out.write(line);
					out.newLine();
				}
				in.close();
				out.close();
				File abstractPath = new File(Makefile_am.getAbsolutePath());
				Makefile_am.delete();
				modMakefile_am.renameTo(abstractPath);
				//modMakefile_am.renameTo(Makefile_am);
			}catch(FileNotFoundException e){System.out.println(e);}
			catch(IOException e){System.out.println(e);}
			
			//if(!found_SUBDIRS)
				//insertSubdirsLine(Makefile_am);
			//if(!found_LIBRARIES)
				//insertLdaddLine(Makefile_am);
			//if(!found_SOURCES)
				//insertSourcesLine(Makefile_am,parent);

		}
	}	
	protected void updateMakefileAm(boolean actionIsManageProject)
	{
		Object[][] projectStucture = structureManager.getProjectStructure();
		// update Makefile in the top level directory
		// first check if it is ok to update
		for(int i =0; i < projectStucture.length; i++)
		{
			File Makefile_am = new File((File)projectStucture[i][0],MAKEFILE_AM);
			if(Makefile_am.exists())
			{
				//  need to figure out what kind of a target that I am dealing with
				// then update
				
		// this is for switching Makefiles.am's to specific targets
		//checkFileStamp();
		
		//if(notModified)
			//generateNewMakefilAm();
		//else
			//updateMakefileAmBasedOnClassification();
				int classification = classifier.classify(Makefile_am);
				switch (classification)
				{
					case (TOPLEVEL):
					updateTopLevelMakefileAm(Makefile_am.getParentFile());
					break;
					
					case (PROGRAMS):
					updateProgramsMakefileAm(Makefile_am.getParentFile());
					break;
					
					case (STATICLIB):
					break;
					
					case (SHAREDLIB):
					break;
					
					default:
					break;
				
				}
			}
		/*	Integer level = new Integer((String)projectStucture[i][1]);
			switch (level.intValue())
			{
				case (0):
					// update top level Makefile.am - basically updating the SUBDIR variable definition
					updateTopLevelMakefileAm(project.getFileObject());
					break;
				case (1):
					// update First level Makefile.am - updating the SUBDIR variable definition
					updateDefaultMakefileAm((File)projectStucture[i][0]);
					break;
				default:
				// update all other files in the subdirs
				// this will be changed as based on the file layout  - static or shared
					updateStaticLibMakefileAm((File)projectStucture[i][0]);
			}*/
		}
	}
	private void updateTopLevelMakefileAm(File parent)
	{
		initTopLevelMakefileAm(parent);
	}

	private void updateProgramsMakefileAm(File MakefileAm)
	{
	}
	private void updateStaticLibMakefileAm(File MakefileAm)
	{
	}

	private long getMakefileAmStamp(File parent)
	{
		// get time stamp
		File Makefile_am = new File (parent,"Makefile.am");
		return Makefile_am.lastModified();
	}
	private String getGeneratedStamp()
	{
		String stamp = new String(
		"# Generated by C/C++ IDE plugin - Do not change/delete if you wish the tool to manage your project\n");
		return stamp;
	}
	private boolean doesStampExist(File MakefileAm)
	{
		String line;
		try{// initializing Package Version and Subdir fields
			BufferedReader in = new BufferedReader(new FileReader(MakefileAm));
			while((line=in.readLine())!=null)
				if(line.indexOf("C/C++ IDE plugin")!=-1)
					return true;
			in.close();
		}catch(IOException e){System.out.println(e);}
		return false;		
	}	

	private String updateExtraDistLine(String line, File parent)
	{
		// add files to the EXTRA_DIST variable
		for(int i = 0; i <parent.listFiles().length; i++)
		{
			String name = parent.listFiles()[i].getName();
			if(name.endsWith(".c")|| name.endsWith(".h")||name.endsWith(".cpp") 
			||name.endsWith(".H") || name.endsWith(".C"))
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
			if(((String)subdirs[i][1]).equals("1") && !(((File)subdirs[i][0]).getName()).startsWith("."))
				line = line.concat(" "+((File)subdirs[i][0]).getName());
		return line;
		
	}
	private String updateLdflagsLine(String line, File parent)
	{
		// add the target name at the begining of the "_SOURCES"
		String mod = line.substring(line.lastIndexOf(_LDFLAGS));
		return line = parent.getName().concat(targetSuffix).concat(mod);
	}
	private String updateBinProgramsLine(String line, File parent)
	{
		if(line.indexOf(TARGET)!=-1)
		{
			line = new String(trimTargetLine(line));
			line = line.concat(" ").concat(parent.getName()).concat(targetSuffix);
		}
		return line;
	}
	private String updateSourcesLine(String line, File parent)
	{
		// add the target name at the begining of the "_SOURCES"
		String mod = line.substring(line.lastIndexOf(_SOURCES));
		line = parent.getName().concat(targetSuffix).concat(mod);
		// add files to the _SOURCES variable
		for(int i = 0; i <parent.listFiles().length; i++)
			if(!parent.listFiles()[i].isDirectory())
			{
				String name = parent.listFiles()[i].getName();
				if(line.indexOf(name)==-1 && 
					(name.endsWith(".c")|| name.endsWith(".cpp") || name.endsWith(".C")))
					line = line.concat(" ").concat(parent.listFiles()[i].getName());
			}
		return line;
	}
	private String updateLdaddLine(String line, File parent)
	{
		// add the target name at the begining of the "_LDADD"
		String mod = line.substring(line.lastIndexOf(_LDADD));
		line = parent.getName().concat(targetSuffix).concat(mod);
		// add libs to the _LDADD variable
		ProjectStructureManager dir_structure = new ProjectStructureManager( parent);
		String[] subNames = dir_structure.getSubdirWorkspacePath();
		for(int i = 0; i <subNames.length; i++)
		{
			// check if the directory starts with "."
			if(subNames[i].indexOf(".")==-1)
			{
				String tok = getLastToken(subNames[i]);
				String modTok = new String();
				String modName = new String("./").concat(subNames[i]).concat("/");
				modTok = modTok.concat("lib").concat(tok).concat(targetSuffix).concat(".a");
				modName = modName.concat(modTok);
				line = line.concat(" "+modName);
			}
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
	private String insertSubdirValueDef()
	{
		String childrenOfTopDir = new String();
		// get subdirectories of depth one of the top level dir
		Object[][] projectStructure = structureManager.getProjectStructure();
		for(int i=0; i < projectStructure.length; i ++)
			if(projectStructure[i][1].equals("1")&&!(((File)projectStructure[i][0]).getName()).startsWith("."))
				childrenOfTopDir =	childrenOfTopDir.concat(" "+((File)projectStructure[i][0]).getName());
		String line=new String (SUBDIRS+" ="+childrenOfTopDir);
		return line;
	}
	private String updateASourcesLine(String line, File parent)
	{
		// add the target name at the begining of the "_SOURCES"
		String mod = line.substring(line.lastIndexOf(_a_SOURCES));
		String lib = new String("lib");
		line = lib.concat(parent.getName()).concat(targetSuffix).concat(mod);
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
	private String updateAlSourcesLine(String line, File parent)
	{
		// add the target name at the begining of the "_SOURCES"
		String mod = line.substring(line.lastIndexOf(_la_SOURCES));
		String lib = new String("lib");
		line = lib.concat(parent.getName()).concat(targetSuffix).concat(mod);
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
	private String updateLibrariesLine(String line, File parent)
	{
		// add lib to the target name first
		String libName = new String("lib");
		return line = line.concat(libName).concat(parent.getName()).concat(targetSuffix).concat(".a");
	}
	private String updateLtlibrariesLine(String line, File parent)
	{
		// add lib to the target name first
		String libName = new String("lib");
		return line = line.concat(libName).concat(parent.getName()).concat(targetSuffix).concat(".la");
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
	protected void setMakefileAmToStaticLib(File parent ,DataElement status)
	{
		Runtime rt = Runtime.getRuntime();
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
		{
			// rename the existing Maekfile.am if exists
			try{
				Process p;
				// check if exist then
				p= rt.exec("mv Makefile.am Makefile.am.old ", null, parent);
				p.waitFor();
			}catch(IOException e){System.out.println(e);}
			catch(InterruptedException e){System.out.println(e);}	
		}
		//check the project structure
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
		{
			// add proper Makefile.am template files 
			try{
				Process p;
				// check if exist then
				p= rt.exec("cp "+TEMPLOCATION+"/sub/static/Makefile.am "+parent.getAbsolutePath());
				p.waitFor();
			}catch(IOException e){System.out.println(e);}
			catch(InterruptedException e){System.out.println(e);}	
		}
		initStaticLibMakefileAm(parent);
		timeStamps.put(parent.getAbsolutePath()+MAKEFILE_AM,new Long(getMakefileAmStamp(parent)));	
	}
	protected void setMakefileAmToPrograms(File parent ,DataElement status)
	{
		Runtime rt = Runtime.getRuntime();
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
		{
			// rename the existing Maekfile.am if exists
			try{
				Process p;
				// check if exist then
				p= rt.exec("mv Makefile.am Makefile.am.old ", null, parent);
				p.waitFor();
			}catch(IOException e){System.out.println(e);}
			catch(InterruptedException e){System.out.println(e);}	
		}
		//check the project structure
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
		{
			// add proper Makefile.am template files 
			try{
				Process p;
				// check if exist then
				p= rt.exec("cp "+TEMPLOCATION+"sub/Makefile.am "+parent.getAbsolutePath());
				p.waitFor();
			}catch(IOException e){System.out.println(e);}
			catch(InterruptedException e){System.out.println(e);}	
		}
		initProgramsMakefileAm(parent);
		timeStamps.put(parent.getAbsolutePath()+MAKEFILE_AM,new Long(getMakefileAmStamp(parent)));				
	}
	protected void setMakefileAmToTopLevel(File parent ,DataElement status)
	{
		Runtime rt = Runtime.getRuntime();
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
		{
			// rename the existing Maekfile.am if exists
			try{
				Process p;
				// check if exist then
				p= rt.exec("mv Makefile.am Makefile.am.old ", null, parent);
				p.waitFor();
			}catch(IOException e){System.out.println(e);}
			catch(InterruptedException e){System.out.println(e);}	
		}
		//check the project structure
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
		{
			// add proper Makefile.am template files 
			try{
				Process p;
				// check if exist then
				p= rt.exec("cp "+TEMPLOCATION+MAKEFILE_AM+" "+parent.getAbsolutePath());
				p.waitFor();
			}catch(IOException e){System.out.println(e);}
			catch(InterruptedException e){System.out.println(e);}	
		}
		initTopLevelMakefileAm(parent);
		timeStamps.put(parent.getAbsolutePath()+MAKEFILE_AM,new Long(getMakefileAmStamp(parent)));	
	}
	protected void setMakefileAmToSharedLib(File parent ,DataElement status)
	{
		Runtime rt = Runtime.getRuntime();
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
		{
			// rename the existing Makefile.am if exists
			try{
				Process p;
				// check if exist then
				p= rt.exec("mv Makefile.am Makefile.am.old ", null, parent);
				p.waitFor();
			}catch(IOException e){System.out.println(e);}
			catch(InterruptedException e){System.out.println(e);}	
		}
		//check the project structure
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
		{
			// add proper Makefile.am template files 
			try{
				Process p;
				// check if exist then
				p= rt.exec("cp "+TEMPLOCATION+"sub/shared/Makefile.am "+parent.getAbsolutePath());
				p.waitFor();
			}catch(IOException e){System.out.println(e);}
			catch(InterruptedException e){System.out.println(e);}	
		}
		initSharedLibMakefileAm(parent);
		timeStamps.put(parent.getAbsolutePath()+MAKEFILE_AM,new Long(getMakefileAmStamp(parent)));	
	}
	private void displayHassTableContents()
	{
		Enumeration enum = timeStamps.elements();
		while(enum.hasMoreElements())
			System.out.println(""+enum.nextElement());
	}
}

