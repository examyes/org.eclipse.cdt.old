package com.ibm.linux.help.display;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.*;
import java.lang.*;

public class HelpBrowserUtil
{
    public static boolean existsCommand(String command)
    {
	String[] arg=new String[4];
	arg[0]="sh";
	arg[1]="-c";
	arg[2]="which";
	arg[3]=command;
	Process p;
	try{
	    p=Runtime.getRuntime().exec(arg);
	    
	    BufferedReader in= new BufferedReader(new InputStreamReader(p.getInputStream()));
	    String line=in.readLine();	    	    
	    if(line!= null &&line.endsWith(command) && line.indexOf(" ")==-1)
		return true;
	    else
		return false;
	}catch(Exception e){
	    e.printStackTrace();
	    return false;
	}
    }

    

}
