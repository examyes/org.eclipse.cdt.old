package org.eclipse.cdt.linux.help;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.linux.help.views.ResultsViewPart;
import org.eclipse.cdt.linux.help.filter.HelpFilter;
import java.lang.*;
import java.util.*;
import org.eclipse.swt.widgets.Display;

public class HelpSearchThread extends Thread
{
    ResultsViewPart theView;
    String theKey;
	String optSearchType;
    ArrayList result;


    public HelpSearchThread(String key, ResultsViewPart view, String searchType)
    {
	theKey=key;
	theView=view;
    optSearchType=searchType;
    }

    public void run()
    {
	// Always return FALSE for Windows
	String theOs = System.getProperty("os.name");
	if (!theOs.toLowerCase().startsWith("window"))
	    {
		while(!HelpSearch.finishLoading())
		    {		
			//Thread.sleep(100);
			yield();
		    }
	    }
		
	result = HelpPlugin.getDefault().getFilter().doSearch(theKey,optSearchType);

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
