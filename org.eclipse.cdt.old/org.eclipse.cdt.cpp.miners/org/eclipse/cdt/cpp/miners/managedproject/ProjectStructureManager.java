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
	public ProjectStructureManager(File project_object)
	{
		project = project_object;
		analyze(project);
	}
	private void analyze(File project)
	{
		// do the work
		File[] contents = project.listFiles();
		for(int i = 0;i<contents.length; i++ )
		{
			if(contents[i].isDirectory())
			{
				subdirs.add(contents[i]);
				analyze(contents[i]);
			}
			else
			{
				files.add(contents[i]);
			}
		}
	}
	public File[] getSubdirs()
	{
		// analyze fresh
		subdirs.removeAllElements();
		files.removeAllElements();
		// analyze
		analyze(project);
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
		analyze(project);
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
	public String[] getSubdirWprkspacePath()
	{
		// analyze fresh
		subdirs.removeAllElements();
		files.removeAllElements();
		// analyze
		analyze(project);
		String[] locations = new String[subdirs.size()];
		for(int i = 0; i < subdirs.size(); i ++)
		{
			locations[i]= ((File)subdirs.elementAt(i)).getPath().substring(getProjectLocation().length()+1);
			System.out.println("\n location = "+locations[i]);
		}
		return locations;
	}
}

