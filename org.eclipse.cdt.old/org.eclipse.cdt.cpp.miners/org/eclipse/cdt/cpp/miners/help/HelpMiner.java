package org.eclipse.cdt.cpp.miners.help;
/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.miners.miner.*;

import org.eclipse.cdt.cpp.miners.help.search.*;
import org.eclipse.cdt.cpp.miners.help.preferences.*;

import org.eclipse.cdt.cpp.miners.help.server.TomcatContainer;

import java.util.*;
import java.io.*;
import java.net.*;

public class HelpMiner extends Miner
{
    private HelpSearch _helpSearch = null;
    private HelpSettings _helpSettings = null;

    private TomcatContainer _tomcatContainer = null;

    private boolean _isRemote = false; //used to start tomcat only for remote projects
    private boolean _serverRunning = false;

    public void load()
    {
	if(_helpSearch == null)
	    {
		_helpSearch = HelpSearch.getDefault();
	    }
	
	if(_helpSettings == null)
	    {
		_helpSettings = HelpSettings.getDefault();
	    }
	
	if(_tomcatContainer==null)
	    {   
		_tomcatContainer = TomcatContainer.getDefault();
	    }
    }

    public void updateMinerInfo()
    {
    }

    public void extendSchema(DataElement schemaRoot)
    {
       	DataElement projD = _dataStore.find(schemaRoot, DE.A_NAME, "Project", 1);

	DataElement helpD = createCommandDescriptor(projD, "HelpSearch", "C_HELPSEARCH",false);
	DataElement helpIndexD = createCommandDescriptor(projD,"HelpIndex", "C_HELPCREATEINDEX",false);
    }

    public DataElement handleCommand(DataElement theCommand)
    {
	String name = getCommandName(theCommand);
	DataElement status = getCommandStatus(theCommand);
	DataElement subject = getCommandArgument(theCommand,0);

	DataElement argKey,argOptSearchType,argSettings,argHostname;
	if (name.equals("C_HELPSEARCH"))
	    {
		argKey = getCommandArgument(theCommand,1);
		argOptSearchType = getCommandArgument(theCommand,2);
		argSettings = getCommandArgument(theCommand,3);
		argHostname = getCommandArgument(theCommand,4);

		_helpSettings.loadSettingsFromString(argSettings.getName());
	
		while(!HelpSearch.finishLoading())
		    {
			//Thread.sleep(100);
			Thread.yield();
		    }
		//Do the search
		String optsearchtype;
		if(argOptSearchType.getName().equals("null"))
		    {
			optsearchtype = null;
		    }
		else
		    {
			optsearchtype = argOptSearchType.getName();
		    }

		ArrayList itemElementList = _helpSearch.FindListOfMatches(argKey.getName(),optsearchtype);
		ArrayList stringList = DataElementMapper.convertToString(itemElementList);
		
		for(int i=0; i<stringList.size();i++)
		    {
			//add results to status
			status.getDataStore().createObject(status,"helpresult",(String)stringList.get(i));
		    }
		
		//the one before last element is the hostname
		String minerHostname = "localhost";
		try
		    {
			minerHostname = InetAddress.getLocalHost().getHostName();
		    }
		catch(UnknownHostException e)
		    {
			//e.printStackTrace();
		    }
		status.getDataStore().createObject(status,"hostname",minerHostname);
		
		// The last element is the 'key'
		status.getDataStore().createObject(status,"key",argKey.getName());

		_isRemote = !minerHostname.equals(argHostname.getName());

		if(_isRemote)
		    {
			// Start Tomcat only for remote projects(i.e. for different client and miner hostnames)
			int port = Integer.parseInt(_helpSettings.get(IHelpSearchConstants.HELP_TOMCAT_PORT));
			_tomcatContainer.start(port);
			_serverRunning = true;
		    }
	    }	
	else if(name.equals("C_HELPCREATEINDEX"))
	    {
		argSettings=getCommandArgument(theCommand, 1);
		_helpSettings.loadSettingsFromString(argSettings.getName());

		//Create the index
		//FIXME:/tmp or in the metadata?
		File indexPath = new File("/tmp",IHelpSearchConstants.HELP_SETTINGS_INDEXLOCATION);
		if(!indexPath.exists())
		    {
			if(!indexPath.mkdir())
			    {
				//do something with status to indicate error
			    }
		    }
		String indexPathName=null;
		try{
		    indexPathName = indexPath.getCanonicalPath();
		}catch(Exception e){
		    e.printStackTrace();
		    //do something to status to indicate error
		}

		ArrayList pathList = readPathsToIndex();
		if(pathList==null)
		    {
			//do something with status to indicate error
		    }

		SearchHtml searchBox = new SearchHtml();
		searchBox.createIndex(indexPathName,pathList);
	    }

	status.setAttribute(DE.A_NAME,"done");

	return status;
    }

    private ArrayList readPathsToIndex()
    {
	ArrayList pathList = new ArrayList();	
	HelpSettings settings = HelpSettings.getDefault();
	String paths = settings.get(IHelpSearchConstants.HELP_SETTINGS_PATHSTOINDEX);	

	//remember the initial paths
	////originalPaths = paths;

	if(paths==null) return null;
	StringTokenizer tokenizer = new StringTokenizer(paths,"##");
	while(tokenizer.hasMoreTokens())
	    {
		pathList.add(tokenizer.nextToken());
	    }
	return pathList;
    }   

    public void finish()
    {
	if(_isRemote && _serverRunning)
	    {
		_tomcatContainer.stop();
	    }
	super.finish();
    }

}
