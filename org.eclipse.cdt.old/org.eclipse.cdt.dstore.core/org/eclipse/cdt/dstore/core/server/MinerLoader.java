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
    private ArrayList            _minerList;
    private ArrayList            _minerFileList;
    private ArrayList            _loaders;
    
    public MinerLoader(DataStore dataStore, ArrayList loaders)
    {
	_dataStore = dataStore;
	_loaders = loaders;
	_miners = new ArrayList();
	_minerList = new ArrayList();
	_minerFileList = new ArrayList();
    }

    public void loadMiners()
    {
	// load the miners
	String pluginDir = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH);
	ArrayList minerLocations = _dataStore.getMinersLocation();
	
	for (int i = 0; i < minerLocations.size(); i++)
	    {
		String minersDir = (String)minerLocations.get(i);		
		String minerFile = null;
		if (minersDir.endsWith(".dat"))
		    {
			minerFile = pluginDir + File.separator + minersDir;		
		    }
		else
		    {
			minerFile = pluginDir + File.separator + minersDir + File.separator + "minerFile.dat";
		    }
		//_dataStore.trace("load miners for " + minerFile);
		if (!_minerFileList.contains(minerFile))
		{
			loadMiners(minerFile);
			_minerFileList.add(minerFile);
		}
	    }
    }

    public ArrayList loadMiners(String minerFile)
    {
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
				if (!_minerList.contains(name))
				    {
					// only load new miners 
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
								_minerList.add(name);
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
				else
				    {
					//System.out.println("already have " + name);
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
	//extendSchema(_dataStore);
	return _miners;
    }


    private void connectMiners(ArrayList unconnectedMiners)
    {
	ArrayList connectedList = new ArrayList();
	// init list
	for (int i = 0; i < _miners.size(); i++)
	    {
		connectedList.add(((Miner)_miners.get(i)).getName());
	    }

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
		miner.extendSchema(_dataStore.getDescriptorRoot());
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

    public ArrayList getMiners()
    {
	return _miners;
    }

    public Miner getMiner(String name) 
    {
	for (int i = 0; i < _miners.size(); i++)
	    {
		Miner miner = (Miner)_miners.get(i);
		if (miner.getClass().getName().equals(name))
		    {
			return miner;	
		    }	
	    }
	
	return null;
    }

    public void finishMiner(String name)
    {
	Miner miner = getMiner(name);
	miner.finish();
	_miners.remove(miner);
    }

    public void finishMiners()
    {
	for (int i = 0; i < _miners.size(); i++)
	    {
		Miner miner = (Miner)_miners.get(i);
		miner.finish();	
	    }
    }
}
