package com.ibm.cpp.miners.managedproject;
/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
import java.util.*;
public class MakefileAmClassifier {
	
	private static String DEFAULT = "DEFAULT";
	private static String TOPLEVEL = "TOPLEVEL";
	private static String STATICLIB = "STATICLIB";
	private static String SHAREDLIB = "SHAREDLIB";
	protected Hashtable classifier = new Hashtable();
	private Vector toplevelAm = new Vector(5,5);
	private Vector defaultlAm = new Vector(5,5);
	private Vector staticlAm = new Vector(5,5);
	private Vector sharedlAm = new Vector(5,5);
	
	private static String templateLocation = "workspace/com.ibm.cpp.miners/autoconf_templates/";
	
	public MakefileAmClassifier()
	{
	}
	protected void generateMakefileLayout()
	{
		// building the vectors
		buildToplevelLayout();
		buildDefaulLayout();
		buildStaticLayout();
		buildSharedLayout();
	}
	private void buildToplevelLayout()
	{
		
	}
	private	void buildDefaulLayout()
	{
	}
	private void buildStaticLayout()
	{
	}
	private void buildSharedLayout()
	{
	}

}

