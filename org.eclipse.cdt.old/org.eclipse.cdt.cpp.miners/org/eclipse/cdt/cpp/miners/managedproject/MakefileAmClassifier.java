package com.ibm.cpp.miners.managedproject;
/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
import java.util.*;
import java.io.*;
public class MakefileAmClassifier {
	private final int TOPLEVEL = 1;
	private final int PROGRAMS = 2;
	private final int STATICLIB = 3;
	private final int SHAREDLIB = 4;
	
	protected Hashtable classifier = new Hashtable();
	
	private Vector toplevelAm = new Vector(5,5);
	private Vector programsAm = new Vector(5,5);
	private Vector staticlAm = new Vector(5,5);
	private Vector sharedlAm = new Vector(5,5);
	
	private final String MAKEFILE_AM = "Makefile.am";
	private final String templateLocation = "workspace/com.ibm.cpp.miners/autoconf_templates/";
	
	// Member Variables which can be defined in Makefile.am
	final String _PROGRAMS = new String ("_PROGRAMS");
	final String _LDADD = new String("_LDADD");
	final String _SOURCES = new String("_SOURCES");
	final String EXTRA_DIST = new String("EXTA_DIST");
	final String INCLUDES = new String("INCLUDES");
	final String _LDFLAGS= new String("_LDFLAGS");
	
	//top level Makefile.am
	final String SUBDIRS = new String("SUBDIRS");
	final String AUTOMAKE_OPTIONS = new String("AUTOMAKE_OPTIONS");
	
	//for static lib files
	final String _LIBRARIES = new String("_LIBRARIES");
	final String _a_SOURCES = new String("_a_SOURCES");
	//for shared lib files
	final String _LTLIBRARIES = new String("_LTLIBRARIES");
	final String _la_SOURCES = new String("_la_SOURCES"); 	
	final String _la_LDFLAGS= new String("_la_LDFLAGS");
	final String _la_LIBADD= new String("_la_LIBADD");
	final String _HEADERS = new String("_HEADERS");
	
	public MakefileAmClassifier()
	{
		generateMakefileAmLayout();
	}
	protected void generateMakefileAmLayout()
	{
		// building the vectors
		buildToplevelLayout();
		buildProgramsLayout();
		buildStaticLayout();
		buildSharedLayout();
	}
	private void buildToplevelLayout()
	{
		
		File Makefile_am = new File(templateLocation,"Makefile.am");
		if(Makefile_am.exists())
		{
			String line;
			try
			{
				// searching for the subdir line
				BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
				while((line=in.readLine())!=null)
				{
					if(line.indexOf(SUBDIRS)!=-1)
					{
						toplevelAm.addElement(line);
					}
					if(line.indexOf(AUTOMAKE_OPTIONS)!=-1)
					{
						toplevelAm.addElement(line);
					}
				}
				in.close();
			}catch(IOException e){System.out.println(e);}
		}
	}
	private	void buildProgramsLayout()
	{
		File Makefile_am = new File(templateLocation+"sub/","Makefile.am");
		if(Makefile_am.exists())
		{
			String line;
			try
			{
				// searching for the subdir line
				BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
				while((line=in.readLine())!=null)
				{
					if(line.indexOf(_PROGRAMS)!=-1)
					{
						programsAm.addElement(line);
					}
					if(line.indexOf(_SOURCES)!=-1 && line.indexOf(_a_SOURCES)==-1 && line.indexOf(_la_SOURCES)==-1)
					{
						programsAm.addElement(line);
					}
					if(line.indexOf(_LDADD)!=-1)
					{
						programsAm.addElement(line);
					}
					if(line.indexOf(SUBDIRS)!=-1)
					{
						programsAm.addElement(line);
					}
					if(line.indexOf(EXTRA_DIST)!=-1)
					{
						programsAm.addElement(line);
					}
					if(line.indexOf(INCLUDES)!=-1)
					{
						programsAm.addElement(line);
					}
					if(line.indexOf(_LDFLAGS)!=-1 && line.indexOf(_la_LDFLAGS)==-1 )
					{
						programsAm.addElement(line);
					}

				}
				in.close();
			}catch(IOException e){System.out.println(e);}
		}
	}
	private void buildStaticLayout()
	{
		File Makefile_am = new File(templateLocation+"sub/static/","Makefile.am");
		if(Makefile_am.exists())
		{
			String line;
			try
			{
				// searching for the subdir line
				BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
				while((line=in.readLine())!=null)
				{
					if(line.indexOf(_LIBRARIES)!=-1)
					{
						staticlAm.addElement(line);
					}
					if(line.indexOf(_a_SOURCES)!=-1)
					{
						staticlAm.addElement(line);
					}
					if(line.indexOf(SUBDIRS)!=-1)
					{
						staticlAm.addElement(line);
					}
					if(line.indexOf(EXTRA_DIST)!=-1)
					{
						staticlAm.addElement(line);
					}
				}
				in.close();
			}catch(IOException e){System.out.println(e);}
		}
	}
	private void buildSharedLayout()
	{
		File Makefile_am = new File(templateLocation+"sub/shared/","Makefile.am");
		if(Makefile_am.exists())
		{
			String line;
			try
			{
				// searching for the subdir line
				BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
				while((line=in.readLine())!=null)
				{
					if(line.indexOf(_LTLIBRARIES)!=-1)
					{
						sharedlAm.addElement(line);
					}
					if(line.indexOf(_la_SOURCES)!=-1)
					{
						sharedlAm.addElement(line);
					}
					if(line.indexOf(SUBDIRS)!=-1)
					{
						sharedlAm.addElement(line);
					}
					if(line.indexOf(EXTRA_DIST)!=-1)
					{
						sharedlAm.addElement(line);
					}
					if(line.indexOf(_HEADERS)!=-1)
					{
						sharedlAm.addElement(line);
					}
					if(line.indexOf(_la_LDFLAGS)!=-1)
					{
						sharedlAm.addElement(line);
					}
					if(line.indexOf(_la_LIBADD)!=-1)
					{
						sharedlAm.addElement(line);
					}
				}
				in.close();
			}catch(IOException e){System.out.println(e);}
		}
	}
	public int classify(File Makefile_am)
	{
		Vector profile = new Vector(3,3);
		
		profile = getProfile(Makefile_am);
		
		return getClassification(profile);
	}
	private int getClassification(Vector profile)
	{	
		int classification = 0;
		int unique = 0;
		for(int i = 0; i < profile.size(); i++)
		{
			String line = (String)profile.elementAt(i);
			if(line.indexOf(_PROGRAMS)!=-1)
			{
				classification = PROGRAMS;
				unique++;
			}
			if(line.indexOf(_LIBRARIES)!=-1)
			{
				classification = STATICLIB;
				unique++;
			}
			if(line.indexOf(_LTLIBRARIES)!=-1)
			{
				classification = SHAREDLIB;
				unique++;
			}
			if(line.indexOf(AUTOMAKE_OPTIONS)!=-1)
			{
				classification = TOPLEVEL;	
				unique++;
			}
		}
			
		if(unique ==1)
			return classification= checkProfileLength(profile,classification);
		else
			return 0;
	}
	private int checkProfileLength(Vector profile,int classification)
	{	
		int profileLength = profile.size();
		int topLevelLenght = toplevelAm.size();
		int programsLength = programsAm.size();
		int staticLenght = staticlAm.size();
		int sharedLength = sharedlAm.size();
		
		if(classification==TOPLEVEL&& (profileLength>=topLevelLenght-1&&profileLength<=topLevelLenght+1))
			return TOPLEVEL;
		if(classification==PROGRAMS&& (profileLength>=programsLength-1&&profileLength<=programsLength+1))
			return PROGRAMS;
		if(classification==STATICLIB&& (profileLength>=staticLenght-1&&profileLength<=staticLenght+1))
			return STATICLIB;
		if(classification==SHAREDLIB&& (profileLength>=sharedLength-1&&profileLength<=sharedLength+1))
			return SHAREDLIB;				
		return 0;
	}
	private Vector getProfile(File Makefile_am)
	{
		Vector profile = new Vector(3,3);
		String line;
		try
		{
			// searching for the subdir line
			BufferedReader in = new BufferedReader(new FileReader(Makefile_am));
			while((line=in.readLine())!=null)
			{
				if(line.indexOf(SUBDIRS)!=-1||line.indexOf(AUTOMAKE_OPTIONS)!=-1||line.indexOf(_PROGRAMS)!=-1
				||line.indexOf(_SOURCES)!=-1||line.indexOf(_LDADD)!=-1||line.indexOf(EXTRA_DIST)!=-1
				||line.indexOf(INCLUDES)!=-1||line.indexOf(_LDFLAGS)!=-1||line.indexOf(_LIBRARIES)!=-1
				||line.indexOf(_a_SOURCES)!=-1||line.indexOf(_LTLIBRARIES)!=-1||line.indexOf(_la_SOURCES)!=-1
				||line.indexOf(_la_LDFLAGS)!=-1||line.indexOf(_la_LIBADD)!=-1||line.indexOf(_HEADERS)!=-1)
					profile.addElement(line);
			}
			in.close();
		}catch(IOException e){System.out.println(e);}
		print(profile);
		return profile;
	}
	private void print(Vector vec)
	{
		System.out.println("\n***************************");
		for(int i = 0; i<vec.size(); i++)
			System.out.println(vec.elementAt(i));
		System.out.println("***************************\n");
	}

}

