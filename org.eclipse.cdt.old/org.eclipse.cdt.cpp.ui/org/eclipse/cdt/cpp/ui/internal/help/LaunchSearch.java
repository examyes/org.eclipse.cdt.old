package org.eclipse.cdt.cpp.ui.internal.help;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.help.AppServer;

import org.eclipse.cdt.dstore.core.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;

import java.util.*;
import java.io.*;
import java.net.*;

public class LaunchSearch
{
    public static LaunchSearch _launchSearch = null;

    DataElement status;

    String _hostname="localhost";
    boolean _isHelpWebAppRegistered=false;
    
    private LaunchSearch()
    {
	try
	    {
		_hostname = InetAddress.getLocalHost().getHostName();
	    }
	catch(UnknownHostException e)
	    {
		//e.printStackTrace();
	    }
    }

    public static LaunchSearch getDefault()
    {
	if(_launchSearch == null)
	    {
		_launchSearch = new LaunchSearch();
	    }
	return _launchSearch;
    }


    public void doSearch(String key, String optSearchType)
    {
	
	DataStore dataStore = DataStoreCorePlugin.getDefault().getCurrentDataStore();

	DataElement helpObject = dataStore.createObject(null,"Project","linuxhelp_command");

	DataStore helpDataStore = helpObject.getDataStore();
	DataElement argKey = helpDataStore.createObject(null,"help_key",key);
	DataElement argOptSearchType;
	if(optSearchType==null)
	    {
		argOptSearchType = helpDataStore.createObject(null,"help_optsearchtype","null");
	    }
	else
	    {
		argOptSearchType = helpDataStore.createObject(null,"help_optsearchtype", optSearchType);
	    }
  
	boolean isRemote = dataStore.isVirtual();	
	HelpSettings settings = new HelpSettings(isRemote);
	settings.read();
	String helpSettings = settings.settingsToString();
	DataElement argSettings = helpDataStore.createObject(null,"help_settings",helpSettings);

	DataElement argHostname = helpDataStore.createObject(null,"hostname",_hostname);

	DataElement helpDescriptor = helpDataStore.localDescriptorQuery(helpObject.getDescriptor(),"C_HELPSEARCH");

	if (helpDescriptor!=null)
	    {
		ArrayList args = new ArrayList();
		args.add(argKey);
		args.add(argOptSearchType);
		args.add(argSettings);
		args.add(argHostname);

	        status = helpDataStore.command(helpDescriptor, args, helpObject);
		
		 getHelpView().setInput(status);
		 /******
		WorkbenchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().syncExec(new Runnable()
		    {
			public void run()
			{
			    //    HelpPlugin.getDefault().getView().setExpression(_keyText);
			    //HelpPlugin.getDefault().getView().setInput(status);
			    getHelpView().setInput(status);
			}
		    });
		 ******/

	  //-----moved the registration for local projects to ResultsViewPart(i.e.upon double click) ------------
		//Attempt to register the WebApp for LOCAL projects only if applicable
		//		if(!isRemote() && settings.getBoolean(IHelpSearchConstants.HELP_SEARCH_BROWSER_DEFAULT))
		//               registerHelpWebApp();
	    }
    }

    public void showHelpView()
    {
	IWorkbench desktop = WorkbenchPlugin.getDefault().getWorkbench();
	IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();
	IWorkbenchPage persp= win.getActivePage();
	try{
	    persp.showView("org.eclipse.cdt.linux.help.views.ResultsViewPart");
	}catch(PartInitException pie){
	    pie.printStackTrace();
	}
    }

    public IHelpInput getHelpView()
    {
	IWorkbench desktop = WorkbenchPlugin.getDefault().getWorkbench();
	IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();
	IWorkbenchPage persp= win.getActivePage();
	return (IHelpInput)persp.findView("org.eclipse.cdt.linux.help.views.ResultsViewPart");
    }

     public void registerHelpWebApp()
    {

	boolean success=false;
	if(!_isHelpWebAppRegistered)
	    {
		success=AppServer.add("cdthelp","org.eclipse.cdt.cpp.miners","org/eclipse/cdt/cpp/miners/help/helpwebapp");
		_isHelpWebAppRegistered=success;
	    }
    }

    private boolean isRemote()
    {
	return DataStoreCorePlugin.getDefault().getCurrentDataStore().isVirtual();
    }
}
