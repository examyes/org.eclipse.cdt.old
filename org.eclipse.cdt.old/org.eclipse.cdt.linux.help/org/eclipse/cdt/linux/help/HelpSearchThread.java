package com.ibm.linux.help;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.linux.help.views.ResultsViewPart;
import java.lang.*;
import java.util.*;
import org.eclipse.swt.widgets.Display;

public class HelpSearchThread extends Thread
{
    ResultsViewPart theView;
    String theKey;
    ArrayList result;


    public HelpSearchThread(String key, ResultsViewPart view)
    {
	theKey=key;
	theView=view;
    }

    public void run()
    {
	while(!HelpSearch.finishLoading())
	    {		
		//Thread.sleep(50);
		yield();
	    }	
	result= HelpPlugin.getListElements(theKey);
	if (result==null)
	    return;// no results	
	
	theView.getSite().getShell().getDisplay().syncExec(new Runnable()
	    {
		public void run()
		{  
		    theView.populate(result);
		    theView.setExpression(theKey);		   
		}
	    });
	yield();
    }


}
