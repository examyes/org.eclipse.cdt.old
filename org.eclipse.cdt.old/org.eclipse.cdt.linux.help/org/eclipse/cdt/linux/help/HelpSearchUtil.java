package org.eclipse.cdt.linux.help;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.*;
import java.lang.*;

public class HelpSearchUtil
{
 
    public static String getDefaultWhatisPath()
    {
	String filename="/var/cache/man/whatis";
	File path= new File(filename);
	if (path.exists())
	    return filename;
	else
	    return null;
    }

    public static String getDefaultDirPath()
    {
	//FIXME
	String filename="/usr/share/info/dir";
	File path= new File(filename);
	if (path.exists())
	    return filename;
	else
	    return null;
    }

   
}
