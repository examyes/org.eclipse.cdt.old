package com.ibm.cpp.miners.managedproject;
/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
import com.ibm.dstore.core.model.*;
import java.io.*;

public class MakefileAmManager {

	DataElement project;
	String[] subdirs;
	String SUBDIRS = new String("SUBDIRS");
	ProjectStructureManager structureManager;
	/**
	 * Constructor for MakefileAmManager
	 */
	public MakefileAmManager(DataElement aProject) {
		
		this.project = aProject;
		structureManager = new ProjectStructureManager( project.getFileObject());
		subdirs = structureManager.getSubdirWorkspacePath();
		
	}
	protected void manageMakefile_am()
	{
		// update top level Makefile.am - basically updating the SUBDIR variable definition
		updateTopLevelMakefile_am(new File(project.getSource(),"Makefile.am"));
		//updateMakefile_am(new File(project.getSource(),"Makefile.am"));
	}
	private void updateTopLevelMakefile_am(File makefile_am)
	{
		if(makefile_am.exists())
		{
			File modMakefile_am = new File(project.getSource(),"mod_Makefile.am");// this is the tope level Makefile.am
			String line;
			boolean found = false;
			try
			{
				// searching for the subdir line
				BufferedReader in = new BufferedReader(new FileReader(makefile_am));
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
				modMakefile_am.renameTo(makefile_am);
			}catch(FileNotFoundException e){System.out.println(e);}
			catch(IOException e){System.out.println(e);}
			if(!found)
				insertSubdirVaiableDefAtFirstLine(makefile_am);
		}
			
		
	}
	private void insertSubdirVaiableDefAtFirstLine(File makefile_am)
	{
		File modMakefile_am = new File(project.getSource(),"mod_Makefile.am");// this is the tope level Makefile.am
		String line;
		try
		{
			// searching for the subdir line
			BufferedReader in = new BufferedReader(new FileReader(makefile_am));
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
			modMakefile_am.renameTo(makefile_am);
		}catch(FileNotFoundException e){System.out.println(e);}
		catch(IOException e){System.out.println(e);}
	}
	private String insertSubdirValueDef()
	{
		String childrenOfTopDir = new String();
		// get subdirectories of depth one of the top level dir
		String[][] dirWithDepth = structureManager.getSubdirWithDepth();
		for(int i=0; i < dirWithDepth.length; i ++)
			if(dirWithDepth[i][1].equals("1"))
				childrenOfTopDir =	childrenOfTopDir.concat(" "+dirWithDepth[i][0]);
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
}

