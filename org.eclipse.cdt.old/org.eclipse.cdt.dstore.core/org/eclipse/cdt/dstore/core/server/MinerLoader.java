package org.eclipse.cdt.dstore.core.server;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.util.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.cdt.dstore.core.miners.miner.*;

import java.io.*;
import java.util.*;
import java.lang.*;

public class MinerLoader implements ISchemaRegistry
{
    private DataStore            _dataStore;
    private ArrayList            _miners;
    private ArrayList            _loaders;

    public MinerLoader(DataStore dataStore, ArrayList loaders)
    {
	_dataStore = dataStore;
	_loaders = loaders;
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
				try
				    {
					ExternalLoader loader = getLoaderFor(name);
					if (loader != null)
					    {
						Class theClass = loader.loadClass(name);
						Miner miner = (Miner)theClass.newInstance();
						if (miner != null)
						    {
							unconnectedMiners.add(miner);
							miner.setExternalLoader(loader);
						    }
					    }
				    }
				catch (ClassNotFoundException e)
				    {
				    }
				catch (InstantiationException e)
				    {
				    }
				catch (IllegalAccessException e)
				    {
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
	extendSchema(_dataStore);
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
		// set the datastore for the miner 
		miner.setDataStore(_dataStore);
	    }
	return canConnect;
    }

    // for now, do nothing here
    public void registerSchemaExtender(ISchemaExtender extender)
    {
    }

    public void extendSchema(DataStore dataStore)
    {
	DataElement schemaRoot = dataStore.getDescriptorRoot();
	for (int i = 0; i < _miners.size(); i++)
	    {
		Miner miner = (Miner)_miners.get(i);
		miner.extendSchema(schemaRoot);
	    }
    }

    // for now, do nothing here
    public ExternalLoader getLoaderFor(String source)
    {
	for (int i = 0; i < _loaders.size(); i++)
	    {
		ExternalLoader loader = (ExternalLoader)_loaders.get(i);
		if (loader.canLoad(source))
		    {
			return loader;
		    }
		else
		    {
		    }
	    }

	return null;
    }

}
