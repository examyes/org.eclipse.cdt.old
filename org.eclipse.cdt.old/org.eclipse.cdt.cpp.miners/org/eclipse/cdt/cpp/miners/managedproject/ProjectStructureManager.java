package com.ibm.cpp.miners.managedproject;
/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
import java.io.*;
import java.util.*;

public class ProjectStructureManager {

	File project;
	private Vector files = new Vector(5,5);
	private Vector subdirs = new Vector(5,5); 
	private Vector subdirs_depth = new Vector(5,5);
	int depth;
	
	
	public ProjectStructureManager(File project_object)
	{
		project = project_object;
	}
	private void analyze(File project, int depth)
	{
		// do the work
		
		if (project != null)
		{
                 depth++;
		 
                 if (project.isDirectory())
                 {
                   File[] contents = project.listFiles();
                   for(int i = 0;i<contents.length; i++ )
		   {
		    if(contents[i].isDirectory())
		    {
		     subdirs.add(contents[i]);
		     subdirs_depth.add(new Integer(depth));
		     analyze(contents[i],depth);
		    }
		    else
		    {
		     files.add(contents[i]);
		    }
		   }
		 }
		 else
                  files.add(project);
	        }
	}

    public File[] getSubdirs()
    {
	// analyze fresh
		subdirs.removeAllElements();
		files.removeAllElements();
		// analyze
		analyze(project,0);
		File[] subDirsList = new File[subdirs.size()];
		for(int i =0; i < subdirs.size(); i++)
		{
			subDirsList[i]=(File)subdirs.elementAt(i);
			System.out.println("\ndirs and subdirs = "+subDirsList[i].getPath());
		}
		return subDirsList;
	}
	public File[] getFiles()
	{
		// analyze fresh
		subdirs.removeAllElements();
		files.removeAllElements();
		analyze(project,0);
		File[] filesList = new File[files.size()];
		for(int i =0; i < files.size(); i++)
		{
			filesList[i]=(File)files.elementAt(i);
			System.out.println("\nfiles = "+filesList[i].getName());
		}
		return filesList;
	}
	public String getProjectLocation()
	{
		return project.getPath();
	}
	public String[] getSubdirWorkspacePath()
	{
		// analyze fresh
		subdirs.removeAllElements();
		files.removeAllElements();
		// analyze
		analyze(project,0);
		String[] locations = new String[subdirs.size()];
		for(int i = 0; i < subdirs.size(); i ++)
		{
			locations[i]= ((File)subdirs.elementAt(i)).getPath().substring(getProjectLocation().length()+1);
			System.out.println("\n location = "+locations[i]);
		}
		return locations;
	}
	protected Object[][] getProjectStructure()
	{
		// analyze fresh
		subdirs.removeAllElements();
		files.removeAllElements();
		// analyze
		analyze(project,0);
		Object[][] projectStructure = new Object [subdirs.size()+1][2];
		projectStructure[0][1]=project;
		projectStructure[0][1]="0";
		for(int i = 0; i < subdirs.size(); i ++)
		{
			projectStructure[i+1][0]=(File)(subdirs.elementAt(i));
			projectStructure[i+1][1]=subdirs_depth.elementAt(i).toString();
		}
		return projectStructure;
	}
}

