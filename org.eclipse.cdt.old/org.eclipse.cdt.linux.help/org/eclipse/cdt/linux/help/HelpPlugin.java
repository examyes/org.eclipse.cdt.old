package com.ibm.linux.help;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.*;
import java.util.*;
import java.util.ResourceBundle;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import com.ibm.linux.help.views.ResultsViewPart;

public class HelpPlugin extends AbstractUIPlugin
{
    private static HelpPlugin _instance=null;
    private static HelpSearch _search=null;

    ////Mirrors the list of elements in the view table////
    //   private static ArrayList _itemElementList = new ArrayList();

    private ResourceBundle _resourceBundle;
    
    private Thread workerThread;
    
    public HelpPlugin(IPluginDescriptor descriptor)
    {
	super(descriptor);
	
	if (_instance==null)
	    _instance=this;

	try
	    {
		_resourceBundle = ResourceBundle.getBundle("com.ibm.linux.help.PluginResources");
	    }
	catch(MissingResourceException mre)
	    {
		_resourceBundle = null;
	    }

    }
    
    public static String getLocalizedString(String key)
    {
	try
	    {
		if(_instance._resourceBundle != null && key != null)
		    return _instance._resourceBundle.getString(key);
	    }
	catch(MissingResourceException mre)
	    {
	    }
	return "";
    }

    static public HelpPlugin getPlugin()
    {
	return _instance;
    }
    static public HelpPlugin getDefault()
    {
	return _instance;
    }

    static public ArrayList getListElements(String key)
    {
	if(_search == null)
	    {		
		_search= new HelpSearch();
	    }
	return _search.FindListOfMatches(key);
    }

    public void showMatches(String key)
    {
	if(_search == null)
	    {		
		_search= new HelpSearch();
	    }
	IWorkbench desktop = WorkbenchPlugin.getDefault().getWorkbench();
	IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();
	IWorkbenchPage persp= win.getActivePage();	
	try{
	    persp.showView("com.ibm.linux.help.views.ResultsViewPart"); //FIXME hardcoded view id
	}catch(PartInitException pie){
	    pie.printStackTrace();
	}
	
	
	ResultsViewPart theView=(ResultsViewPart)persp.findView("com.ibm.linux.help.views.ResultsViewPart");
	
	workerThread=new HelpSearchThread(key,theView);
	workerThread.start();
    }

    public ResultsViewPart getView()
    {
	IWorkbench desktop = WorkbenchPlugin.getDefault().getWorkbench();
	IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();
	IWorkbenchPage persp= win.getActivePage();
	return (ResultsViewPart)persp.findView("com.ibm.linux.help.views.ResultsViewPart");
    }   
}
