package com.ibm.linux.help.display;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.*;
import java.lang.*;

public class runHelpBrowser extends Thread
{
    String _command;
    Runtime theRuntime;

    public runHelpBrowser(String theURL)
    {
	_command=theURL;
	theRuntime=Runtime.getRuntime();	    
    }

    public void run()
    {	
	try
	    {
		Process p=theRuntime.exec("gnome-help-browser "+ _command);
	    }

	catch(Exception e)
	    {
		e.printStackTrace();
	    }
    }  
}
