package org.eclipse.cdt.cpp.miners.managedproject.amparser;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;

public class Am
{
	//Attribute Types ...Make sure these start at 0 and go up...they are used as indexes in Target.java
	public static final int ATTRIBUTE_START    = 0;
	public static final int SOURCES            = 0;
	public static final int LDADD              = 1;
	public static final int LDFLAGS            = 2;
	public static final int DEPENDENCIES       = 3;
	public static final int LIBADD             = 4;
	public static final int ATTRIBUTE_END      = 4;
 	
	//Target Types
	public static final int TARGETTYPE_START   = 10;
	public static final int PROGRAMS           = 10;
	public static final int LIBRARIES          = 11;
	public static final int LTLIBRARIES        = 12;
	public static final int SCRIPTS            = 13;
	public static final int DATA               = 14;
	public static final int HEADERS            = 15;
	public static final int MANS               = 16;
	public static final int TEXINFOS           = 17;
	public static final int TARGETTYPE_END     = 17;
	
	//Other keywords
	public static final String SUBDIRS         = "SUBDIRS";

	//DataElement Types
	public static final String MANAGED_PROJECT  = "Managed Project";
	public static final String PROJECT_TARGET   = "Project Target";
	public static final String TARGET_ATTRIBUTE_TYPE = "Target Attribute Type";
	public static final String TARGET_ATTRIBUTE = "Target Attribute";
	public static final String TARGET_OPTION    = "Target Option";
	 
	public static String getString(int num)
	{
		switch(num)
 		{
			case SOURCES      : return "SOURCES";
			case LDADD        : return "LDADD";
			case LDFLAGS      : return "LDFLAGS";
			case DEPENDENCIES : return "DEPENDENCIES";
			case LIBADD       : return "LIBADD";
			case PROGRAMS     : return "PROGRAMS";
			case LIBRARIES    : return "LIBRARIES";
			case LTLIBRARIES  : return "LTLIBRARIES";
			case SCRIPTS      : return "SCRIPTS";
			case DATA         : return "DATA";
			case HEADERS      : return "HEADERS";
			case MANS         : return "MANS";
			case TEXINFOS     : return "TEXINFOS";
		}
		return "UNKNOWN TYPE";
	}

	public static String getType(int num)
	{
		switch (num)
		{
			case SOURCES       : return "Project File";
			case LDFLAGS       : return TARGET_OPTION;
			case DEPENDENCIES  : return PROJECT_TARGET;
		}
		return TARGET_ATTRIBUTE;
	}
}

