package com.ibm.cpp.miners.managedproject;
/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
import com.ibm.dstore.core.model.*;
import java.io.*;

public class ConfigureInManager {

	
	DataElement project;
		// will be grouped under Configure_in Manager
	String version = new String("@VERSION@");
	String pack = new String("@PACKAGE@");
	String subdir = new String ("@SUBDIR/Makefile@");
	String makefile = new String("/Makefile ");
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
	protected void manageConfigure_in()
	{
		updateConfigure_in(new File(project.getSource(),"configure.in"));
	}
	private void updateConfigure_in(File configure_in)
	{
		File modFile = new File(project.getSource(),"mod_configure.in");
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
					
					if(subdirs.length>0)
					{
						line = trimTargetLine(line);
						line = insertSubdirs(line.toCharArray(),delimPosition[0]);
					}
				}
				
				out.write(line);
				out.newLine();// needed at the end of each line when writing  the modified file
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
		int counetrForModLine = 0;
		char[] modLine = new char[256];
		int i = 0;
		while(line[i]!= '\0')
		{
			if(i == position)
			{
				
				for(int j = 0; j< subdirs.length; j++)
				{
					for(int k=0; k< subdirs[j].toCharArray().length; k++)
						modLine[counetrForModLine++]=subdirs[j].toCharArray()[k];
					for(int l=0; l< makefile.length(); l++)
						modLine[counetrForModLine++]=makefile.toCharArray()[l];
				}
				
			}
			modLine[counetrForModLine++]=line[i];
			i++;
		}
		int insertPosition = 0;
		
		return new String(modLine);
	}		
}


