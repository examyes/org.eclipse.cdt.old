package com.ibm.linux.help.display;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.*;
import java.lang.*;

public class runKonq extends Thread
{
    String _command;
    Runtime theRuntime;

    public runKonq(String theURL)
    {
	_command=theURL;
	theRuntime=Runtime.getRuntime();
    }

    public String getName(String thecommand,String thekey)
	{
	    String s;
	    try
	    {
		Process p = theRuntime.exec(thecommand);
		BufferedReader in= new BufferedReader(new InputStreamReader(p.getInputStream()));
		
		while ((s=in.readLine())!=null)
		    {
		       if(s.indexOf(thekey)!= -1)
			   {	
			       //		       in.close();p.destroy();
			       return s;
			       
			   }
		    }
		/*		in.close();
				p.destroy();*/
	    }
	    catch(Exception e)
		{
		    e.printStackTrace();
		}
	    return null;
	}

    public void run()
    {
	String name= getName("dcop","konqueror");
	if(name==null)
	    {
		//Konq is not yet running
		try{		    
      		    Process p= theRuntime.exec("konqueror "+ _command);
		    int timeout=0;
		    do
			{			    
			    name=getName("dcop","konqueror");
			    Thread.sleep(500);
			    if(timeout>10) return;//cannot bring konqueror so abort.
			    timeout++;
			}while(name==null);

		    theRuntime.exec("dcop konqueror KonquerorIface getWindows");

		    String windowName;
		    timeout=0;
		    do
			{			   
			    windowName=getName("dcop konqueror","konqueror-mainwindow");
			   
			    Thread.sleep(200);
			    if(timeout>15) return;// cannot get window so abort.
			    timeout++;
			}while(windowName==null);
		    
		    String args[]=new String[4];
		    args[0]="dcop";
		    args[1]="konqueror";
		    args[2]="qt/konqueror-mainwindow#1/mainToolBar";
		    args[3]="hide";
		    theRuntime.exec(args);
		    
		    args[2]="qt/konqueror-mainwindow#1/extraToolBar";
		    theRuntime.exec(args);
		    
		    args[2]="qt/konqueror-mainwindow#1/locationToolBar";
		    theRuntime.exec(args);
		    
		    args[2]="qt/konqueror-mainwindow#1/bookmarkToolBar";
		    theRuntime.exec(args);	
		    
		    args[2]=windowName+"/action/showmenubar"; 
		    args[3]="activate";
		    theRuntime.exec(args);

      		    //the geometry
		    String geo[]=new String[6];
		    geo[0]="dcop";
		    geo[1]="konqueror";
		    geo[2]="qt/konqueror";
		    geo[3]="setProperty";
		    geo[4]="geometry";
		    geo[5]="QRect(20,20,400,300)";
		    theRuntime.exec(geo);
		   
		}catch(Exception e)
		    {
			e.printStackTrace();
			return;
		    }
		}
	else
	    {
		//konq running so run it in the currently running konq process
		String windowName = getName("dcop konqueror","konqueror-mainwindow");
		if(windowName ==  null)
		    {			
			//konq windows not yet available so get it and hide the bars.
			try{
       			    Process p=theRuntime.exec("dcop konqueror KonquerorIface getWindows");
			    int timeout=0;
			    do
				{				   
				    windowName=getName("dcop konqueror","konqueror-mainwindow");
      				    Thread.sleep(200);
				    if (timeout>15) return; //cannot get window so abort.
				    timeout++;
				}while(windowName==null);
			    
			   String args[]=new String[4];
			   args[0]="dcop";
			   args[1]="konqueror";
			   args[2]="qt/konqueror-mainwindow#1/mainToolBar";
			   args[3]="hide";
			   theRuntime.exec(args);

			   args[2]="qt/konqueror-mainwindow#1/extraToolBar";
			   theRuntime.exec(args);

			   args[2]="qt/konqueror-mainwindow#1/locationToolBar";
			   theRuntime.exec(args);
			   
			   args[2]="qt/konqueror-mainwindow#1/bookmarkToolBar";
			   theRuntime.exec(args);

			   args[2]=windowName+"/action/showmenubar"; 
			   args[3]="activate";
			   theRuntime.exec(args);	
		 
			   args[2]="qt/konqueror";
			   args[3]="raise";
			   theRuntime.exec(args);
  
			}catch(Exception e)
			    {
       				e.printStackTrace();
				return;
		
			    }			
		    }	    		
		if(windowName!=null)
		    {
			String args[]=new String[5];
			args[0]="dcop";
			args[1]="konqueror";
			args[2]=windowName;
			args[3]="openURL";
			args[4]=_command;    

			try{		    
      			    Process p=theRuntime.exec(args);

			    String topargs[]= new String[4];
			    topargs[0]="dcop";
			    topargs[1]="konqueror";
			    topargs[2]="qt/konqueror";
			    topargs[3]="raise";
			    theRuntime.exec(topargs);

			}catch(Exception e){			    
			    e.printStackTrace();
			    return;
			}	
		    }
	    }       
    }

}
