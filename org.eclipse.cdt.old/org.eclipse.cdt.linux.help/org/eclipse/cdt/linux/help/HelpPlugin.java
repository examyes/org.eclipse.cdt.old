package org.eclipse.cdt.linux.help;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.*;
import java.util.*;
import java.util.ResourceBundle;
import java.io.*;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.cdt.linux.help.views.ResultsViewPart;
import org.eclipse.cdt.linux.help.filter.HelpFilter;

import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.*;

import org.eclipse.core.internal.plugins.*;

public class HelpPlugin extends AbstractUIPlugin
{
    private static HelpPlugin _instance=null;
    private static HelpSearch _search=null;

    private static HelpFilter _filter = null;

    private static String _installLocation=null;   

    private ResourceBundle _resourceBundle;
    
    private Thread workerThread;
    
    public HelpPlugin(IPluginDescriptor descriptor)
    {
	super(descriptor);
	
	if (_instance==null)
	    _instance=this;

	try
	    {
		_resourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.linux.help.PluginResources");
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

     public String getInstallLocation()
    {
	if(_installLocation==null)
	    {
	        _installLocation = ((PluginDescriptor)getDescriptor()).getInstallURLInternal().getFile();
	    }
	return _installLocation;
    }

    public Image getImage(String name)
    {
	ImageRegistry reg = getImageRegistry();
	Image image = reg.get(getInstallLocation()+"icons"+File.separator+name);
	if(image==null)
	    {
		ImageDescriptor des = ImageDescriptor.createFromFile(null, getInstallLocation()+"icons"+File.separator+ name);
		image = des.createImage();
		reg.put(name, des);		
	    }
	return image;
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
	    persp.showView("org.eclipse.cdt.linux.help.views.ResultsViewPart"); //FIXME hardcoded view id
	}catch(PartInitException pie){
	    pie.printStackTrace();
	}	
	
	ResultsViewPart theView=(ResultsViewPart)persp.findView("org.eclipse.cdt.linux.help.views.ResultsViewPart");
	
	workerThread=new HelpSearchThread(key,theView);
	workerThread.start();
    }

    public ResultsViewPart getView()
    {
	IWorkbench desktop = WorkbenchPlugin.getDefault().getWorkbench();
	IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();
	IWorkbenchPage persp= win.getActivePage();
	return (ResultsViewPart)persp.findView("org.eclipse.cdt.linux.help.views.ResultsViewPart");
    }

    public HelpFilter getFilter()
    {
	if(_filter == null)
	    _filter = new HelpFilter();
	
	return _filter;
    }
}
