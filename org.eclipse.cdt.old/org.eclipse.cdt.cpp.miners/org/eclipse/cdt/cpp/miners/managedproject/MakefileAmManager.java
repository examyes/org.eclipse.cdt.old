package org.eclipse.cdt.cpp.miners.managedproject;
/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.cdt.dstore.core.model.DataElement;
import org.eclipse.cdt.dstore.core.model.DataStoreAttributes;

public class MakefileAmManager {


	private String _workspaceLocation;
	private final int TOPLEVEL = 1;
	private final int PROGRAMS = 2;
	private final int STATICLIB = 3;
	private final int SHAREDLIB = 4;

	private final String MAKEFILE_AM = "Makefile.am";
	
	// Member Variables which can be defined in Makefile.am
	final String SUBDIRS = new String("SUBDIRS");
	final String bin_PROGRAMS = new String ("bin_PROGRAMS");
	final String _LDADD = new String("_LDADD");
	final String _SOURCES = new String("_SOURCES");
	final String EXTRA_DIST = new String("EXTRA_DIST");
	final String INCLUDES = new String("INCLUDES");
	final String _LDFLAGS= new String("_LDFLAGS");
	
	//for static lib files
	final String _LIBRARIES = new String("_LIBRARIES");
	final String _a_SOURCES = new String("_a_SOURCES");
	//for shared lib files
	final String _LTLIBRARIES = new String("_LTLIBRARIES");
	final String _la_SOURCES = new String("_la_SOURCES");
	final String _la_LDFLAGS = new String("_la_LDFLAGS");
	final String _la_LIBADD = new String("_la_LIBADD"); 	
	// updating data
	final String TARGET = new String("!TARGET!");
	final char delim = '!';
	
	final String TEMP_MAKEFILE_AM_NAME = "mod_Makefile.am";
	
	//String targetSuffix = new String("_target");
	String targetSuffix = new String("");
	
	private ArrayList extradistExtensions;
	/**
	 * Constructor for MakefileAmManager
	 */
	public MakefileAmManager() 
	{
	}
	
	public void setWorkspaceLocation(String location)
	{
		_workspaceLocation = location;	
	}
	
	protected void generateMakefileAm(DataElement project)
	{
		ProjectStructureManager structureManager = new ProjectStructureManager( project.getFileObject());
		getMakefileAmTemplateFiles(project,structureManager);
		initializeMakefileAm(structureManager);
	}
	protected void getMakefileAmTemplateFiles(DataElement project,ProjectStructureManager structureManager)
	{
		String[] subdirs = structureManager.getSubdirWorkspacePath();
		Runtime rt = Runtime.getRuntime();
		//check the project structure
		File projectFile = project.getFileObject();
		if(projectFile.isDirectory()&& !(projectFile.getName().startsWith(".")))
			copyMakefileFromTempDir(project.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH),
			"/org.eclipse.cdt.cpp.miners/autoconf_templates/",project.getSource());
		// provide one makefile.am in each subdiectory
		for(int i =0; i < subdirs.length ; i++)
		{
			if(subdirs[i].indexOf(".")==-1)
			{
				StringTokenizer token = new StringTokenizer(subdirs[i],"/");
				if (token.countTokens()==1)
					copyMakefileFromTempDir(project.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH),
					 "/org.eclipse.cdt.cpp.miners/autoconf_templates/sub/",project.getSource()+"/"+subdirs[i]);
				else
					copyMakefileFromTempDir(project.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH),
					 "/org.eclipse.cdt.cpp.miners/autoconf_templates/sub/static/",project.getSource()+"/"+subdirs[i]);
			}
		}
	}
	private void initializeMakefileAm(ProjectStructureManager structureManager)
	{
		Object[][] projectStucture = structureManager.getProjectStructure();
		for(int i =0; i < projectStucture.length; i++)
		{
			if(!((File)projectStucture[i][0]).getName().startsWith("."))
			{
				Integer level = new Integer((String)projectStucture[i][1]);
				String absPath = new String("");
				switch (level.intValue())
				{
					case (0):
					// initialize top level Makefile.am - basically updating the SUBDIR variable definition
					initializeTopLevelMakefileAm((File)projectStucture[i][0],structureManager,true);
					absPath = ((File)projectStucture[i][0]).getAbsolutePath()+MAKEFILE_AM;
					break;
					case (1):
					// initialize First level Makefile.am - updating the bin_BROGRAMS,SUBDIR variable definition
					initializeProgramsMakefileAm((File)projectStucture[i][0]);
					absPath = ((File)projectStucture[i][0]).getAbsolutePath()+MAKEFILE_AM;
					break;
					default:
					// initialize all other files in the subdirs
					initializeStaticLibMakefileAm((File)projectStucture[i][0]);
					absPath = ((File)projectStucture[i][0]).getAbsolutePath()+MAKEFILE_AM;
				}
			}
		}
	}
	private void initializeTopLevelMakefileAm(File parent, ProjectStructureManager structureManager, boolean stampit)
	{
		File Makefile_am = new File(parent,"Makefile.am");
		if(Makefile_am.exists())
		{
			File modMakefile_am = new File(parent.getAbsolutePath(),TEMP_MAKEFILE_AM_NAME);// this is the tope level Makefile.am
			String line;
			boolean found = false;
			try
			{
				// searching for the subdir line
				BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
				BufferedWriter out= new BufferedWriter(new FileWriter(modMakefile_am));
				if(stampit)
				{
					//out.write(getGeneratedStamp());
					//out.newLine();
				}
				while((line=in.readLine())!=null)
				{
					if(line.indexOf(SUBDIRS)!=-1)
					{
						found = true;
						removeIfMoreThanOneLine(line,in);
						line = insertSubdirValueDef(structureManager, out);
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
	private void initializeProgramsMakefileAm(File parent)
	{
		File Makefile_am = new File(parent,"Makefile.am");
		String line = new String();
		if(Makefile_am.exists())
		{
			File modMakefile_am = new File(parent,TEMP_MAKEFILE_AM_NAME);
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
				//out.write(getGeneratedStamp());
				//out.newLine();
				while((line=in.readLine())!=null)
				{
					// searching for the bin_PROGRAMS line
					if(line.indexOf(bin_PROGRAMS)!=-1)
					{
						found_bin_PROGRAMS = true;
						line = initBinProgramsLine(line, parent);
					}
					if(line.indexOf(_SOURCES)!=-1)
					{
						found_SOURCES = true;
						line = initSourcesLine(line, parent, out);
					}
					if(line.indexOf(_LDADD)!=-1)
					{
						found_LDADD= true;
						line = initLdaddLine(line, parent);
					}
					if(line.indexOf(SUBDIRS)!=-1)
					{
						found_SUBDIRS = true;
						line = initSubdirsLine(line, parent, out);
					}
					if(line.indexOf(EXTRA_DIST)!=-1)
					{
						found_EXTRA_DIST = true;
						line = initExtraDistLine(line, parent,out);
					}
					if(line.indexOf(_LDFLAGS)!=-1)
					{
						found_LDFLAGS= true;
						line = initLdflagsLine(line, parent);
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
	private void initializeStaticLibMakefileAm(File parent)
	{
		File Makefile_am = new File(parent,"Makefile.am");
		String line = new String();
		if(Makefile_am.exists())
		{
			File modMakefile_am = new File(parent,TEMP_MAKEFILE_AM_NAME);
			boolean found_SUBDIRS = false;
			boolean found_LIBRARIES = false;
			boolean found_a_SOURCES = false;
			boolean found_EXTRA_DIST = false;
			try
			{
				BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
				BufferedWriter out= new BufferedWriter(new FileWriter(modMakefile_am));
				//out.write(getGeneratedStamp());
				//out.newLine();
				while((line=in.readLine())!=null)
				{
					if(line.indexOf(_a_SOURCES)!=-1)
					{
						found_a_SOURCES = true;
						line = initASourcesLine(line, parent, out);
					}
					if(line.indexOf(_LIBRARIES)!=-1)
					{
						found_LIBRARIES= true;
						line = initLibrariesLine(line, parent);
					}
					if(line.indexOf(SUBDIRS)!=-1)
					{
						found_SUBDIRS = true;
						line = initSubdirsLine(line, parent, out);
					}
					if(line.indexOf(EXTRA_DIST)!=-1)
					{
						found_EXTRA_DIST = true;
						line = initExtraDistLine(line, parent,out);
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
	private void initializeSharedLibMakefileAm(File parent)
	{
		File Makefile_am = new File(parent,"Makefile.am");
		String line = new String();
		if(Makefile_am.exists())
		{
			File modMakefile_am = new File(parent,TEMP_MAKEFILE_AM_NAME);
			boolean found_LTLIBRARIES = false;
			boolean found_la_SOURCES = false;
			boolean found_SUBDIRS = false;
			boolean found_EXTRA_DIST = false;
			boolean found_la_LDFLAGS = false;
			boolean found_la_LIBADD = false;
			try
			{
				// searching for the subdir line
				BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
				BufferedWriter out= new BufferedWriter(new FileWriter(modMakefile_am));
				//out.write(getGeneratedStamp());
				//out.newLine();
				while((line=in.readLine())!=null)
				{
					// searching for the bin_PROGRAMS line
					if(line.indexOf(_LTLIBRARIES)!=-1)
					{
						found_LTLIBRARIES= true;
						line = initLtlibrariesLine(line, parent);
					}
					if(line.indexOf(_la_SOURCES)!=-1)
					{
						found_la_SOURCES = true;
						line = initLaSourcesLine(line, parent, out);
					}
					if(line.indexOf(SUBDIRS)!=-1)
					{
						found_SUBDIRS = true;
						line = initSubdirsLine(line, parent, out);
					}
					if(line.indexOf(EXTRA_DIST)!=-1)
					{
						found_EXTRA_DIST = true;
						line = initExtraDistLine(line, parent,out);
					}
					if(line.indexOf(_la_LDFLAGS)!=-1)
					{
						found_la_LDFLAGS = true;
						line = initLaldflagsLine(line, parent);
					}
					if(line.indexOf(_la_LIBADD)!=-1)
					{
						found_la_LIBADD = true;
						line = initLalibaddLine(line, parent);
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
	protected void updateMakefileAm(DataElement subject,boolean actionIsManageProject, MakefileAmClassifier classifier)
	{
		ProjectStructureManager structureManager = new ProjectStructureManager( subject.getFileObject());
		File Makefile_am = new File(subject.getFileObject(),MAKEFILE_AM);
				
		if(Makefile_am.exists()&&!subject.getFileObject().getName().startsWith("."))
		{
			int classification = classifier.classify(Makefile_am);
			switch (classification)
			{
				case (TOPLEVEL):
				createDotOldFileFor(Makefile_am);
				updateTopLevelMakefileAm(Makefile_am.getParentFile(),structureManager);
				updateMakefileAmDependency(Makefile_am.getParentFile(),classifier);
				compareOldAndNew(Makefile_am.getParentFile());
				break;
				
				case (PROGRAMS):
				createDotOldFileFor(Makefile_am);
				updateProgramsMakefileAm(Makefile_am.getParentFile(),classifier);
				updateMakefileAmDependency(Makefile_am.getParentFile(),classifier);
				compareOldAndNew(Makefile_am.getParentFile());
				break;
				
				case (STATICLIB):
				createDotOldFileFor(Makefile_am);
				updateStaticLibMakefileAm(Makefile_am.getParentFile());
				updateMakefileAmDependency(Makefile_am.getParentFile(),classifier);
				compareOldAndNew(Makefile_am.getParentFile());
				break;
					
				case (SHAREDLIB):
				createDotOldFileFor(Makefile_am);
				updateSharedLibMakefileAm(Makefile_am.getParentFile());
				updateMakefileAmDependency(Makefile_am.getParentFile(),classifier);
				compareOldAndNew(Makefile_am.getParentFile());
				break;
					
				default:
				break;
			}
		}
	}
	protected void updateAllMakefileAm(DataElement project,MakefileAmClassifier classifier, int projectType)
	{
		ProjectStructureManager structureManager = new ProjectStructureManager( project.getFileObject());
		String[] subdirs = structureManager.getSubdirWorkspacePath();

		Object[][] projectStucture = structureManager.getProjectStructure();
		if(projectType == 0) // i.e  a program target
		{
			for(int i =0; i < projectStucture.length; i++)
			{
				File Makefile_am = new File((File)projectStucture[i][0],MAKEFILE_AM);
				Integer level = new Integer((String)projectStucture[i][1]);
				switch (level.intValue())
				{
					case (0):
					copyMakefileFromTempDir(project.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH),
						 	"/org.eclipse.cdt.cpp.miners/autoconf_templates/sub/",Makefile_am.getParentFile().getAbsolutePath());
					initializeProgramsMakefileAm((File)projectStucture[i][0]);
					break;
					default:
						copyMakefileFromTempDir(project.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH),
						"/org.eclipse.cdt.cpp.miners/autoconf_templates/sub/static/",Makefile_am.getParentFile().getAbsolutePath());
					initializeStaticLibMakefileAm((File)projectStucture[i][0]);
					break;
				}
			}
		}
		if(projectType == 1) // i.e a static target
		{
			for(int i =0; i < projectStucture.length; i++)
			{
				File Makefile_am = new File((File)projectStucture[i][0],MAKEFILE_AM);
				Integer level = new Integer((String)projectStucture[i][1]);
				switch (level.intValue())
				{
					default:
						copyMakefileFromTempDir(project.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH),
						"/org.eclipse.cdt.cpp.miners/autoconf_templates/sub/static/",Makefile_am.getParentFile().getAbsolutePath());
					initializeStaticLibMakefileAm((File)projectStucture[i][0]);
					break;
				}
			}
		}
		if(projectType == 2) // i.e  a shared target
		{
			for(int i =0; i < projectStucture.length; i++)
			{
				File Makefile_am = new File((File)projectStucture[i][0],MAKEFILE_AM);
				Integer level = new Integer((String)projectStucture[i][1]);
				switch (level.intValue())
				{
					case (0):
					copyMakefileFromTempDir(project.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH),
						 	"/org.eclipse.cdt.cpp.miners/autoconf_templates/sub/shared/",Makefile_am.getParentFile().getAbsolutePath());
					initializeSharedLibMakefileAm((File)projectStucture[i][0]);
					break;
					default:
						copyMakefileFromTempDir(project.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH),
						"/org.eclipse.cdt.cpp.miners/autoconf_templates/sub/static/",Makefile_am.getParentFile().getAbsolutePath());
					initializeStaticLibMakefileAm((File)projectStucture[i][0]);
					break;
				}
			}
		}
	}
	protected void updateAllMakefileAm(DataElement project,MakefileAmClassifier classifier)
	{
		ProjectStructureManager structureManager = new ProjectStructureManager( project.getFileObject());
		String[] subdirs = structureManager.getSubdirWorkspacePath();

		Object[][] projectStucture = structureManager.getProjectStructure();
		for(int i =0; i < projectStucture.length; i++)
		{
			File Makefile_am = new File((File)projectStucture[i][0],MAKEFILE_AM);
			
			if(Makefile_am.exists()&&!((File)projectStucture[i][0]).getName().startsWith("."))
			{
				int classification = classifier.classify(Makefile_am);
				switch (classification)
				{
					case (TOPLEVEL):
					createDotOldFileFor(Makefile_am);
					updateTopLevelMakefileAm(Makefile_am.getParentFile(),structureManager);
					updateMakefileAmDependency(Makefile_am.getParentFile(),classifier);
					compareOldAndNew(Makefile_am.getParentFile());
					break;
					
					case (PROGRAMS):
					createDotOldFileFor(Makefile_am);
					updateProgramsMakefileAm(Makefile_am.getParentFile(),classifier);
					updateMakefileAmDependency(Makefile_am.getParentFile(),classifier);
					compareOldAndNew(Makefile_am.getParentFile());
					break;
					
					case (STATICLIB):
					createDotOldFileFor(Makefile_am);
					updateStaticLibMakefileAm(Makefile_am.getParentFile());
					updateMakefileAmDependency(Makefile_am.getParentFile(),classifier);
					compareOldAndNew(Makefile_am.getParentFile());
					break;
					
					case (SHAREDLIB):
					createDotOldFileFor(Makefile_am);
					updateSharedLibMakefileAm(Makefile_am.getParentFile());
					updateMakefileAmDependency(Makefile_am.getParentFile(),classifier);
					compareOldAndNew(Makefile_am.getParentFile());
					break;
					
					default:
					break;
				}
			}
			else
			{
				Integer level = new Integer((String)projectStucture[i][1]);
				switch (level.intValue())
				{
					case (0):
					copyMakefileFromTempDir(project.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH),
						"/org.eclipse.cdt.cpp.miners/autoconf_templates/",project.getSource());
					initializeTopLevelMakefileAm((File)projectStucture[i][0],structureManager,true);
					break;
					case (1):
					copyMakefileFromTempDir(project.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH),
					 	"/org.eclipse.cdt.cpp.miners/autoconf_templates/sub/",Makefile_am.getParentFile().getAbsolutePath());
					initializeProgramsMakefileAm((File)projectStucture[i][0]);
					break;
					default:
					copyMakefileFromTempDir(project.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH),
						"/org.eclipse.cdt.cpp.miners/autoconf_templates/sub/static/",Makefile_am.getParentFile().getAbsolutePath());
					initializeStaticLibMakefileAm((File)projectStucture[i][0]);
					break;
				}
			}
		}
	}
	private void updateTopLevelMakefileAm(File parent, ProjectStructureManager structureManager)
	{
		initializeTopLevelMakefileAm(parent,structureManager,false);
	}

	private void updateProgramsMakefileAm(File parent,MakefileAmClassifier classifier)
	{
		File Makefile_am = new File(parent,"Makefile.am");
		String line = new String();
		File modMakefile_am = new File(parent,TEMP_MAKEFILE_AM_NAME);
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
			BufferedWriter out= new BufferedWriter(new FileWriter(modMakefile_am));
			while((line=in.readLine())!=null)
			{
				if(line.indexOf(_SOURCES)!=-1)
				{
					removeIfMoreThanOneLine(line,in);
					line = updateSourcesLine(line, parent, out);
				}
				if(line.indexOf(_LDADD)!=-1)
					line = updateLdaddLine(line, parent, classifier);
				if(line.indexOf(SUBDIRS)!=-1)
				{
					removeIfMoreThanOneLine(line,in);
					line = updateSubdirsLine(line, parent, out);
				}
				if(line.indexOf(EXTRA_DIST)!=-1)
				{
					removeIfMoreThanOneLine(line,in);
					line = updateExtraDistLine(line, parent, out);
				}
				if(line.indexOf(_LDFLAGS)!=-1)
					line = updateLdflagsLine(line, parent);
				out.write(line);
				out.newLine();
			}
			in.close();
			out.close();
			File abstractPath = new File(Makefile_am.getAbsolutePath());
			Makefile_am.delete();
			modMakefile_am.renameTo(abstractPath);
		}catch(FileNotFoundException e){System.out.println(e);}
		catch(IOException e){System.out.println(e);}
	}
	private void updateStaticLibMakefileAm(File parent)
	{
		File Makefile_am = new File(parent,"Makefile.am");
		String line = new String();
		File modMakefile_am = new File(parent,TEMP_MAKEFILE_AM_NAME);
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
			BufferedWriter out= new BufferedWriter(new FileWriter(modMakefile_am));
			while((line=in.readLine())!=null)
			{
				if(line.indexOf(_a_SOURCES)!=-1)
				{
					removeIfMoreThanOneLine(line,in);
					line = updateASourcesLine(line, parent, out);
				}
				if(line.indexOf(SUBDIRS)!=-1)
				{
					removeIfMoreThanOneLine(line,in);
					line = updateSubdirsLine(line, parent, out);
				}
				if(line.indexOf(EXTRA_DIST)!=-1)
				{
					removeIfMoreThanOneLine(line,in);
					line = updateExtraDistLine(line, parent, out);
				}
				out.write(line);
				out.newLine();
			}
			in.close();
			out.close();
			File abstractPath = new File(Makefile_am.getAbsolutePath());
			Makefile_am.delete();
			modMakefile_am.renameTo(abstractPath);
		}catch(FileNotFoundException e){System.out.println(e);}
		catch(IOException e){System.out.println(e);}
	}
	private void updateSharedLibMakefileAm(File parent)
	{
		File Makefile_am = new File(parent,"Makefile.am");
		String line = new String();
		if(Makefile_am.exists())
		{
			File modMakefile_am = new File(parent,TEMP_MAKEFILE_AM_NAME);
			try
			{
				// searching for the subdir line
				BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
				BufferedWriter out= new BufferedWriter(new FileWriter(modMakefile_am));
				while((line=in.readLine())!=null)
				{
					if(line.indexOf(_la_SOURCES)!=-1)
					{
						removeIfMoreThanOneLine(line,in);
						line = updateLaSourcesLine(line, parent, out);
					}
					if(line.indexOf(SUBDIRS)!=-1)
					{
						removeIfMoreThanOneLine(line,in);
						line = updateSubdirsLine(line, parent, out);
					}
					if(line.indexOf(EXTRA_DIST)!=-1)
					{
						removeIfMoreThanOneLine(line,in);
						line = updateExtraDistLine(line, parent,out);
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
/*	private long getMakefileAmStamp(File parent)
	{
		// get time stamp
		File Makefile_am = new File (parent,"Makefile.am");
		return Makefile_am.lastModified();
	}
	private String getGeneratedStamp()
	{
		String stamp = new String(
		"# Generated by C/C++ IDE plugin \n");
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
	}	*/

	private String initExtraDistLine(String line, File parent, BufferedWriter out)
	{
		try
		{
			line = line.substring(0,line.lastIndexOf("=")+1);
			out.write(line);
			// add files to the EXTRA_DIST variable
			for(int i = 0; i <parent.listFiles().length; i++)
			{
				if(!parent.listFiles()[i].isDirectory())
				{
					String name = parent.listFiles()[i].getName();
					/*if(!name.endsWith(".c")&& !name.endsWith(".C")&&!name.endsWith(".cpp")&&!name.endsWith(".CPP")
					&&!name.endsWith(".cc")&&!name.endsWith(".CC")&&!name.endsWith(".cxx")&&!name.endsWith(".CXX")
					&&!name.endsWith(".c++")&&!name.endsWith(".C++")
					&&!name.endsWith(".h") && !name.endsWith(".H")&&!name.endsWith(".hpp")&&!name.endsWith(".HPP")
					&&!name.endsWith(".hh")&&!name.endsWith(".HH")&&!name.endsWith(".hxx")&&!name.endsWith(".HXX")
					&&!name.endsWith(".h++")&&!name.endsWith(".H++")
					&&!name.startsWith(".")&&!name.endsWith(".am") && !name.endsWith(".in")&& (name.indexOf(".")!=-1)
					&&!name.endsWith(".o")&&!name.endsWith(".old")&&!name.equals("Makefile")
					&&!name.equals("makefile")&&!name.equals("configur"))*/
					if(foundInExtraList(name))
					{
						out.write(" "+"\\");
						out.newLine();
						out.write(name);
					}
				}
			}
			
		}catch (IOException e){}
		return "\n";
	}
	private String updateExtraDistLine(String line, File parent, BufferedWriter out)
	{
		boolean update = false;
		try
		{
			line = line.substring(0,line.lastIndexOf("=")+1);
			out.write(line);
			// add files to the EXTRA_DIST variable
			for(int i = 0; i <parent.listFiles().length; i++)
			{
				if(!parent.listFiles()[i].isDirectory())
				{
					String name = parent.listFiles()[i].getName();
					/*if(!name.endsWith(".c")&& !name.endsWith(".C")&&!name.endsWith(".cpp")&&!name.endsWith(".CPP")
					&&!name.endsWith(".cc")&&!name.endsWith(".CC")&&!name.endsWith(".cxx")&&!name.endsWith(".CXX")
					&&!name.endsWith(".c++")&&!name.endsWith(".C++")
					&&!name.endsWith(".h") && !name.endsWith(".H")&&!name.endsWith(".hpp")&&!name.endsWith(".HPP")
					&&!name.endsWith(".hh")&&!name.endsWith(".HH")&&!name.endsWith(".hxx")&&!name.endsWith(".HXX")
					&&!name.endsWith(".h++")&&!name.endsWith(".H++")
					&&!name.startsWith(".")&&!name.endsWith(".am") && !name.endsWith(".in")&&(name.indexOf(".")!=-1)
					&&!name.endsWith(".o")&&!name.endsWith(".old")&&!name.equals("Makefile") &&!name.equals("makefile")
					&&!name.equals("configur"))*/
					if(foundInExtraList(name))
					{
						update = true;
						out.write(" "+"\\");
						out.newLine();
						out.write(name);
					}
				}
			}
			
		}catch (IOException e){}
		if(update)
			return "\n";
		return "";
	}
	private String initSubdirsLine(String line, File parent, BufferedWriter out)
	{
		line = SUBDIRS+" =";
		// add subdire to the SUBDIRS variable
		try
		{
			out.write(line.trim());
		}catch(IOException e){}
		
		ProjectStructureManager dir_structure = new ProjectStructureManager( parent);
		Object[][] subdirs = dir_structure.getProjectStructure();
		for(int i = 0; i <subdirs.length; i++)
			if(((String)subdirs[i][1]).equals("1") && !(((File)subdirs[i][0]).getName()).startsWith("."))
			{
				try
				{
					out.write(" "+"\\");out.newLine();
					out.write(((File)subdirs[i][0]).getName());
				}catch(IOException e){}
			}
		return "\n";
		
	}
	private String updateSubdirsLine(String line, File parent, BufferedWriter out)
	{
		line = SUBDIRS+" =";
		boolean update = false;
		// add subdire to the SUBDIRS variable
		try
		{
			out.write(line.trim());
		}catch(IOException e){}
		
		ProjectStructureManager dir_structure = new ProjectStructureManager( parent);
		Object[][] subdirs = dir_structure.getProjectStructure();
		for(int i = 0; i <subdirs.length; i++)
			if(((String)subdirs[i][1]).equals("1") && !(((File)subdirs[i][0]).getName()).startsWith("."))
			{
				try
				{
					update = true;
					out.write(" "+"\\");out.newLine();
					out.write(((File)subdirs[i][0]).getName());
				}catch(IOException e){}
			}
		if(update)
			return "\n";
		return "";
	}
	private String initLdflagsLine(String line, File parent)
	{
		// add the target name at the begining of the "_LDFLAGS"
		String mod = line.substring(line.lastIndexOf(_LDFLAGS)).trim();
		return line = parent.getName().concat(targetSuffix).concat(mod);
	}
	private String updateLdflagsLine(String line, File parent)
	{
		return line;
	}
	private String initBinProgramsLine(String line, File parent)
	{
		if(line.indexOf(TARGET)!=-1)
		{
			line = new String(trimTargetLine(line));
			line = line.concat(" ").concat(parent.getName()).concat(targetSuffix);
		}
		return line.trim();
	}
	private String initSourcesLine(String line, File parent, BufferedWriter out)
	{
		// add the target name at the begining of the "_SOURCES"
		String mod = line.substring(line.lastIndexOf(_SOURCES)).trim();
		line = parent.getName().concat(targetSuffix).concat(mod);
		try
		{
			out.write(line.trim());
		}catch(IOException e){}
		
		// add files to the _SOURCES variable
		for(int i = 0; i <parent.listFiles().length; i++)
			if(!parent.listFiles()[i].isDirectory())
			{
				String name = parent.listFiles()[i].getName();
				if(line.indexOf(name)==-1 
				&&(name.endsWith(".c")|| name.endsWith(".C")|| name.endsWith(".cpp") || name.endsWith(".CPP")
				|| name.endsWith(".cc")|| name.endsWith(".CC")||name.endsWith(".c++")|| name.endsWith(".C++")
				|| name.endsWith(".cxx")||name.endsWith(".CXX")))
					{
						try
						{
							out.write(" "+"\\");out.newLine();
							out.write(parent.listFiles()[i].getName());
						}catch(IOException e){}
					}
			}
		return "\n";
	}
	private String updateSourcesLine(String line, File parent, BufferedWriter out)
	{
		line = line.substring(0,line.lastIndexOf("=")+1);
		try
		{
			out.write(line.trim());
		}catch(IOException e){}
		// add files to the _SOURCES variable
		for(int i = 0; i <parent.listFiles().length; i++)
			if(!parent.listFiles()[i].isDirectory())
			{
				String name = parent.listFiles()[i].getName();
				if(line.indexOf(name)==-1 
				&&(name.endsWith(".c")|| name.endsWith(".C")|| name.endsWith(".cpp") || name.endsWith(".CPP")
				|| name.endsWith(".cc")|| name.endsWith(".CC")||name.endsWith(".c++")|| name.endsWith(".C++")
				|| name.endsWith(".cxx")||name.endsWith(".CXX")))
					{
						try
						{
							out.write(" "+"\\");out.newLine();
							out.write(parent.listFiles()[i].getName());
						}catch(IOException e){}
					}
			}
		return "\n";
	}
	private String initLdaddLine(String line, File parent)
	{
		// add the target name at the begining of the "_LDADD"
		String mod = line.substring(line.lastIndexOf(_LDADD)).trim();
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
		return line.trim()+" "; // space is needed for matching purposes
	}
	private String updateLdaddLine(String line, File parent,MakefileAmClassifier classifier)
	{
		String libName = new String();
		File Makefile_am = new File(parent,"Makefile.am");
		if(Makefile_am.exists())
		{
			// add libs to the _LDADD variable
			ProjectStructureManager dir_structure = new ProjectStructureManager( parent);
			File[] subdirs = dir_structure.getSubdirs();
			for(int i = 0; i < subdirs.length; i++)
			{
				// check if the directory starts with "."
				if(subdirs[i].getName().indexOf(".")==-1)
				{
					File MakefileAm = new File(subdirs[i],"Makefile.am");
					if(MakefileAm.exists())
					{
						int fileClass = classifier.classify(MakefileAm);
						String absPath = MakefileAm.getAbsolutePath();
						String parentPath = parent.getAbsolutePath();
						String loc = absPath.substring(parentPath.length());
						String path= loc.substring(0,loc.lastIndexOf("Makefile.am"));
						if(fileClass == STATICLIB)
						{
							libName = "." + path + getLibName(MakefileAm,_LIBRARIES);
							libName = libName.replace('\\', '/');
							// check if this value already defined before adding it to the _LDADD variable definition
							if(line.indexOf(" "+libName+" ")==-1)
								line = line+" "+libName+" ";
						}
						if(fileClass == SHAREDLIB)
						{
							libName = "."+path+getLibName(MakefileAm,_LTLIBRARIES);
							libName = libName.replace('\\', '/');
							// check if this value already defined before adding it to the _LDADD variable definition
							if(line.indexOf(" "+libName+" ")==-1)
								line = line+" "+libName+" ";
						}
					}
				}
			}
		}
		return line.trim();
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
	private String insertSubdirValueDef(ProjectStructureManager structureManager, BufferedWriter out)
	{
		try
		{
			out.write(SUBDIRS+" =");
			String childrenOfTopDir = new String();
			// get subdirectories of depth one of the top level dir
			Object[][] projectStructure = structureManager.getProjectStructure();
			for(int i=0; i < projectStructure.length; i ++)
				if(projectStructure[i][1].equals("1")&&!(((File)projectStructure[i][0]).getName()).startsWith("."))
				{
					out.write(" "+"\\");out.newLine();
					out.write(((File)projectStructure[i][0]).getName());
				}
		}catch(IOException e){}
		return "\n";
	}
	private String initASourcesLine(String line, File parent, BufferedWriter out)
	{
		// add the target name at the begining of the "_SOURCES"
		String mod = line.substring(line.lastIndexOf(_a_SOURCES));
		String lib = new String("lib");
		line = lib.concat(parent.getName()).concat(targetSuffix).concat(mod);
		try
		{
			out.write(line.trim());
		}catch(IOException e){}
		// add files to the _SOURCES variable
		for(int i = 0; i <parent.listFiles().length; i++)
			if(!parent.listFiles()[i].isDirectory())
			{
				String name = parent.listFiles()[i].getName();
				if(line.indexOf(name)==-1 
				&&(name.endsWith(".c")|| name.endsWith(".C")|| name.endsWith(".cpp") || name.endsWith(".CPP")
				|| name.endsWith(".cc")|| name.endsWith(".CC")||name.endsWith(".c++")|| name.endsWith(".C++")
				|| name.endsWith(".cxx")||name.endsWith(".CXX")))
						{
							try
							{
								out.write(" "+"\\");out.newLine();
								out.write(parent.listFiles()[i].getName());
							}catch(IOException e){}
						}
			}
		return "\n";
	}
	private String updateASourcesLine(String line, File parent, BufferedWriter out)
	{
		line = line.substring(0,line.lastIndexOf("=")+1);
		try
		{
			out.write(line.trim());
		}catch(IOException e){}

		// add files to the _SOURCES variable
		for(int i = 0; i <parent.listFiles().length; i++)
			if(!parent.listFiles()[i].isDirectory())
			{
				String name = parent.listFiles()[i].getName();
				if(line.indexOf(name)==-1
				&&(name.endsWith(".c")|| name.endsWith(".C")|| name.endsWith(".cpp") || name.endsWith(".CPP")
				|| name.endsWith(".cc")|| name.endsWith(".CC")||name.endsWith(".c++")|| name.endsWith(".C++")
				|| name.endsWith(".cxx")||name.endsWith(".CXX")))
						{
							try
							{
								out.write(" "+"\\");out.newLine();
								out.write(parent.listFiles()[i].getName());
							}catch(IOException e){}
						}
			}
		return "\n";
	}
	private String initLaSourcesLine(String line, File parent, BufferedWriter out)
	{
		// add the target name at the begining of the "_SOURCES"
		String mod = line.substring(line.lastIndexOf(_la_SOURCES)).trim();
		String lib = new String("lib");
		line = lib.concat(parent.getName()).concat(targetSuffix).concat(mod);
		try
		{
			out.write(line.trim());
		}catch(IOException e){}
		// add files to the _SOURCES variable
		for(int i = 0; i <parent.listFiles().length; i++)
			if(!parent.listFiles()[i].isDirectory())
			{
				String name = parent.listFiles()[i].getName();
				if(line.indexOf(name)==-1
				&&(name.endsWith(".c")|| name.endsWith(".C")|| name.endsWith(".cpp") || name.endsWith(".CPP")
				|| name.endsWith(".cc")|| name.endsWith(".CC")||name.endsWith(".c++")|| name.endsWith(".C++")
				|| name.endsWith(".cxx")||name.endsWith(".CXX")))
						{
							try
							{
								out.write(" "+"\\");out.newLine();
								out.write(parent.listFiles()[i].getName());
							}catch(IOException e){}
						}
			}
		return "\n";
	}
	private String updateLaSourcesLine(String line, File parent,BufferedWriter out)
	{
		line = line.substring(0,line.lastIndexOf("=")+1);
		try
		{
			out.write(line.trim());
		}catch(IOException e){}
		// add files to the _SOURCES variable
		for(int i = 0; i <parent.listFiles().length; i++)
			if(!parent.listFiles()[i].isDirectory())
			{
				String name = parent.listFiles()[i].getName();
				if(line.indexOf(name)==-1 				
				&&(name.endsWith(".c")|| name.endsWith(".C")|| name.endsWith(".cpp") || name.endsWith(".CPP")
				|| name.endsWith(".cc")|| name.endsWith(".CC")||name.endsWith(".c++")|| name.endsWith(".C++")
				|| name.endsWith(".cxx")||name.endsWith(".CXX")))
						{
							try
							{
								out.write(" "+"\\");out.newLine();
								out.write(parent.listFiles()[i].getName());
							}catch(IOException e){}
						}
			}
		return "\n";
	}
	private String initLibrariesLine(String line, File parent)
	{
		// add lib to the target name first
		String libName = new String("lib");
		return line = line.concat(" ").concat(libName).concat(parent.getName()).concat(targetSuffix).concat(".a").trim();
	}
	private String initLtlibrariesLine(String line, File parent)
	{
		// add lib to the target name first
		String libName = new String("lib");
		return line = line.concat(" ").concat(libName).concat(parent.getName()).concat(targetSuffix).concat(".la").trim();
	}
	private String initLaldflagsLine(String line, File parent)
	{
		// add the target name at the begining of the "_la_LDFLAGS"
		String mod = line.substring(line.lastIndexOf(_la_LDFLAGS)).trim();
		String lib = new String("lib");
		return lib.concat(parent.getName()).concat(targetSuffix).concat(mod).trim();
		
	}
	private String initLalibaddLine(String line, File parent)
	{
		// add the target name at the begining of the "_la_LDFLAGS"
		String mod = line.substring(line.lastIndexOf(_la_LIBADD)).trim();
		String lib = new String("lib");
		return lib.concat(parent.getName()).concat(targetSuffix).concat(mod).trim();
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
	protected void setMakefileAmToStaticLib(File parent ,DataElement status,MakefileAmClassifier classifier)
	{
		File Makefile_am = new File(parent,"Makefile.am");
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
			createDotOldFileFor(Makefile_am);
		//check the project structure
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
			copyMakefileFromTempDir(status.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH),
			"/org.eclipse.cdt.cpp.miners/autoconf_templates/sub/static/",parent.getAbsolutePath());
		initializeStaticLibMakefileAm(parent);	
		updateMakefileAmDependency(parent,classifier);
	}
	protected void setMakefileAmToPrograms(File parent ,DataElement status,MakefileAmClassifier classifier)
	{
		File Makefile_am = new File(parent,"Makefile.am");
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
			createDotOldFileFor(Makefile_am);
		//check the project structure
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
			copyMakefileFromTempDir(status.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH),
			"/org.eclipse.cdt.cpp.miners/autoconf_templates/sub/",parent.getAbsolutePath());
		initializeProgramsMakefileAm(parent);
		updateMakefileAmDependency(parent,classifier);				
	}
	protected void setMakefileAmToTopLevel(DataElement project,DataElement status,MakefileAmClassifier classifier)
	{
		ProjectStructureManager structureManager = new ProjectStructureManager( project.getFileObject());
		File parent = project.getFileObject();
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
		{
			File Makefile_am = new File(parent,"Makefile.am");
			createDotOldFileFor(Makefile_am);
		}
		//check the project structure
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
			copyMakefileFromTempDir(project.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH),
			"/org.eclipse.cdt.cpp.miners/autoconf_templates/",parent.getAbsolutePath());
		initializeTopLevelMakefileAm(parent,structureManager,true);
		updateMakefileAmDependency(parent,classifier);	
	}	
	protected void setMakefileAmToSharedLib(File parent ,DataElement status,MakefileAmClassifier classifier)
	{
		File Makefile_am = new File(parent,"Makefile.am");
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
			createDotOldFileFor(Makefile_am);
		//check the project structure
		if(parent.isDirectory()&& !(parent.getName().startsWith(".")))
			copyMakefileFromTempDir(status.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH),
			"/org.eclipse.cdt.cpp.miners/autoconf_templates/sub/shared/",parent.getAbsolutePath());
		initializeSharedLibMakefileAm(parent);
		updateMakefileAmDependency(parent,classifier);	
	}
	private void updateMakefileAmDependency(File parent,MakefileAmClassifier classifier)
	{
		File dir = parent.getParentFile();
		ArrayList list = new ArrayList();
		int counter = 0;
		//find the first level parent
		String path = dir.getAbsolutePath();
		if (path != null)
		{
			path = path.replace('\\', '/');
		}
		String wpath = _workspaceLocation.replace('\\', '/');
		
		while(dir != null &&  path != null && !path.equals(wpath))
		{
			list.add(counter,dir);counter++;
			dir = dir.getParentFile();
			
			if (dir != null)
			{		
				path = dir.getAbsolutePath();
				if (path != null)
				{
					path = path.replace('\\', '/');
				}
			}
		}
		//boolean found = false;
		for(int i = 0; i < list.size(); i++)
		{
			File parent_Makefile_am = new File((File)list.get(i),"Makefile.am");
			int parentClass = classifier.classify(parent_Makefile_am);
		
			if(parentClass==PROGRAMS)
			{
				File Makefile_am = new File(parent,"Makefile.am");
				try
				{
					// searching for the subdir line
					String line ;
					File modMakefile_am = new File((File)list.get(i),"mod.am");
					BufferedReader in = new BufferedReader(new FileReader(parent_Makefile_am));
					BufferedWriter out= new BufferedWriter(new FileWriter(modMakefile_am));
					while((line=in.readLine())!=null)
					{
						if(line.indexOf(_LDADD)!=-1)
							line = updateDependenciesLine(line, Makefile_am,parent_Makefile_am,classifier);
						out.write(line);
						out.newLine();
					}
					in.close();
					out.close();
					File abstractPath = new File(parent_Makefile_am.getAbsolutePath());
					parent_Makefile_am.delete();
					modMakefile_am.renameTo(abstractPath);
				}catch(FileNotFoundException e){System.out.println(e);}
				catch(IOException e){System.out.println(e);}
			}
		}
	}
	private String updateDependenciesLine(String line,File Makefile_am,File ProgramsMakefile,MakefileAmClassifier classifier)
	{
		StringBuffer modLine = new StringBuffer();
		StringTokenizer tokenizer = new StringTokenizer(line);
		int classification = classifier.classify(Makefile_am);
		boolean found = false;
		while (tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();
			if(token.indexOf(Makefile_am.getParentFile().getName())!=-1&&token.indexOf("/")!=-1)
			{
				if(isRightTokenToModify(Makefile_am.getParentFile().getName(),token) )
				{
					found = true;
					if(classification==STATICLIB)
						token = getModifiedLibString(token,getLibName(Makefile_am,_LIBRARIES));
						
					else if (classification==SHAREDLIB)
						token = getModifiedLibString(token,getLibName(Makefile_am,_LTLIBRARIES));
					else
						token = "";
				}
			}
			modLine.append(token+" ");
		}
		if(!found)
		{
			String path = new String("");
			String name =ProgramsMakefile.getParentFile().getName();
			File file = Makefile_am.getParentFile();
			while(!name.equals(file.getName()))
			{
				path = "/"+file.getName()+path;
				file = file.getParentFile();
			}
			if(classification==STATICLIB)
				path = "."+path+"/"+"lib"+Makefile_am.getParentFile().getName()+".a";
			else if(classification==SHAREDLIB)
				path = "."+path+"/"+"lib"+Makefile_am.getParentFile().getName()+".la";
			else
				path = "";
			modLine.append(path+"");
		}
		return modLine.toString();
	}
	private boolean isRightTokenToModify(String dir, String tok)
	{
		ArrayList list = new ArrayList();
		StringTokenizer tokenizer = new StringTokenizer(tok,"/");
		int counter = tokenizer.countTokens();
		for(int i = 0; i<counter ; i++)
			list.add(i,tokenizer.nextToken());
		if(list.get(counter-2).toString().equals(dir))
			return true;
		
		return false;
	}
	private String getLibName(File Makefile_am, String key)
	{
		String target = new String();
		try
		{
			String line ;
			BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
			while((line=in.readLine())!=null)
				if(line.indexOf(key)!=-1)
					target = line.substring(line.indexOf("=")+2);
			in.close();
		}catch(IOException e){System.out.println(e);}
		return target;
	}
	private String getModifiedLibString(String tok, String target)
	{
		StringBuffer mod = new StringBuffer();
		StringTokenizer tokenizer = new StringTokenizer(tok,"/");
		int counter = tokenizer.countTokens();
		for(int i = 0; i< ( counter-1); i++)
			mod.append(tokenizer.nextToken()).append("/");
		return mod.append(target).toString();
	}
	private void compareOldAndNew(File parent)
	{
		File _new = new File(parent,"Makefile.am");
		File _old = new File(parent,"Makefile.am.old");
		String updatedLine;
		String oldLine;
		boolean remove = true; 
		try
		{
			// searching for the subdir line
			BufferedReader updated = new BufferedReader(new FileReader(_new));
			BufferedReader old= new BufferedReader(new FileReader(_old));
			oldLine = old.readLine().trim();
			updatedLine = updated.readLine().trim();
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
	private void copyMakefileFromTempDir(String pluginLocation, String makefileLocation,String copyLocation)
	{
		Runtime rt = Runtime.getRuntime();
		try{
			Process p;
			// check if exist then
			p= rt.exec("cp "+ pluginLocation + makefileLocation + MAKEFILE_AM +" "+copyLocation);
			p.waitFor();
		}catch(IOException e){System.out.println(e);}
		catch(InterruptedException e){System.out.println(e);}	
	}
	private void removeIfMoreThanOneLine(String line, BufferedReader in)
	{
		String deleteLine = new String(line);
		if(deleteLine.indexOf("\\")!=-1)
		{
			try
			{
				while(deleteLine.indexOf("\\") !=-1 )
					deleteLine = in.readLine();
				in.readLine();
			}catch (IOException e){}
		}
	}
	public void setCompilerFlag(DataElement project, String option)
	{

		String name = "Makefile.am";
		
		ProjectStructureManager structureManager = new ProjectStructureManager( project.getFileObject());
		
		File topLevelMakefileAm = new File( project.getFileObject(),name);
		if(topLevelMakefileAm.exists())
		{
			updateCflagValue(topLevelMakefileAm,option);
		}	
		
		File[] projectDirs = structureManager.getSubdirs();
		for (int i = 0; i<projectDirs.length; i++)
		{
			//System.out.println("\n name = "+projectDirs[i].getName());
			File makefile = new File(projectDirs[i],name);
			if(makefile.exists())
			{
				updateCflagValue(makefile,option);
			}	
		}
	}
	public void updateCflagValue(File makefileAm, String option)
	{
		try
		{
			// searching for the subdir line
			String line ;			
			File modMakefile_am = new File(makefileAm.getParentFile(),"mod.am");
			BufferedReader in = new BufferedReader(new FileReader(makefileAm));
			BufferedWriter out= new BufferedWriter(new FileWriter(modMakefile_am));
				while((line=in.readLine())!=null)
				{
					if(line.indexOf("CFLAGS")!=-1&&line.indexOf("$(CFLAGS)")==-1)
						line = "CFLAGS = "+option;
					out.write(line);
					out.newLine();
				}
				in.close();
				out.close();
				File abstractPath = new File(makefileAm.getAbsolutePath());
				makefileAm.delete();
				modMakefile_am.renameTo(abstractPath);
		}catch(FileNotFoundException e){System.out.println(e);}
		catch(IOException e){System.out.println(e);}
		
	}
	public void setExtradistExtensions(ArrayList extensionList,DataElement project, String commandDescriptor)
	{
		extradistExtensions = new ArrayList();
		for(int i = 0; i < extensionList.size(); i ++)
					extradistExtensions.add(((DataElement)extensionList.get(i)).getName());
		// open a makefile.am to force an update for configure - hack
		if(commandDescriptor.equals("C_SET_EXTRA_DIST_EXTENSIONS"))
		{
			//System.out.println("\n Modified");
			ProjectStructureManager structureManager = new ProjectStructureManager( project.getFileObject());
			String[] subdirs = structureManager.getSubdirWorkspacePath();
			Object[][] projectStucture = structureManager.getProjectStructure();
			for(int i =0; i < projectStucture.length; i++)
			{
				File Makefile_am = new File((File)projectStucture[i][0],MAKEFILE_AM);
				
				if(Makefile_am.exists()&&!((File)projectStucture[i][0]).getName().startsWith("."))
				{
					Makefile_am.setLastModified(System.currentTimeMillis());
					break;
				}
			}
		}
	}
	private boolean foundInExtraList(String name)
	{

		for(int i= 0; i < extradistExtensions.size(); i++)
		{
			String item = (String)extradistExtensions.get(i);
			String ext = item.substring(item.indexOf("."));
			//System.out.println("\n name = "+name+"\n > ext = "+ext);
			if((name.endsWith(ext)||name.equals(item))&&!name.equals(TEMP_MAKEFILE_AM_NAME))
				return true;
		}
		return false;
	}
}

