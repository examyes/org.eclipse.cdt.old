package org.eclipse.cdt.dstore.core.server;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.server.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.cdt.dstore.core.miners.miner.*;

import java.io.*;
import java.util.*;
import java.lang.*;

public class MinerLoader
{
    private ArrayList            _miners;
    private DataStore            _dataStore;
    private ILoader              _loader;

    public MinerLoader(DataStore dataStore, ILoader loader)
    {
	_dataStore = dataStore;
	_loader = loader;
	_miners = new ArrayList();
    }

    public ArrayList loadMiners()
    {
	// load the miners
	String pluginDir = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH);
	String minersDir = _dataStore.getMinersLocation();

	String minerFile = null;
	if (minersDir.endsWith(".dat"))
	    {
		minerFile = pluginDir + File.separator + minersDir;		
	    }
	else
	    {
		minerFile = pluginDir + File.separator + minersDir + File.separator + "minerFile.dat";
	    }

	// load the miners
	ArrayList unconnectedMiners = new ArrayList();
	File file = new File(minerFile);	
	try
	    {
		FileInputStream inFile = new FileInputStream(file);
		BufferedReader in = new BufferedReader(new InputStreamReader(inFile));
		

		String name = null;
		while ((name = in.readLine()) != null)
		    {
			// check name
			name = name.trim();

			if (!name.startsWith("#") && 
			    (name.length() > 5))

			    {
				Miner miner = _loader.loadMiner(name);
				if (miner != null)
				    {
					unconnectedMiners.add(miner);
				    }
			    }
		    }
	    }
	catch (FileNotFoundException e)
	    {
		System.out.println(e);
	    }
	catch (IOException e)
	    {
		System.out.println(e);
	    }
	
	
	connectMiners(unconnectedMiners);
	return _miners;
    }

    private void connectMiners(ArrayList unconnectedMiners)
    {
	ArrayList connectedList = new ArrayList();
	while (unconnectedMiners.size() > 0)
	    {
		Miner miner = (Miner)unconnectedMiners.get(0);
		unconnectedMiners.remove(miner);					
		if (connectMiner(connectedList, miner))
		    {
			_miners.add(miner);
			connectedList.add(miner.getName());
			_dataStore.trace("connected " + miner.getName());
		    }
		else
		    {
			unconnectedMiners.add(miner);					
		    }
	    }
    }
    
    private boolean connectMiner(ArrayList connectedList, Miner miner)
    {
	boolean canConnect = true;
	ArrayList dependencies = miner.getMinerDependencies();
	for (int i = 0; i < dependencies.size(); i++)
	    {
		String dependency = (String)dependencies.get(i);
		if (!connectedList.contains(dependency))
		    {
			canConnect = false;
		    }
	    }
	
	if (canConnect)
	    {
		// get the schema for the miner 
		miner.setDataStore(_dataStore);
	    }
	return canConnect;
    }
}
