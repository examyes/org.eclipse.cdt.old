package com.ibm.dstore.core.server;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.server.*;
import com.ibm.dstore.core.model.*;

import com.ibm.dstore.core.miners.miner.*;

import java.io.*;
import java.util.*;
import java.lang.*;

public class Loader implements ILoader
{
    public Loader()
    {
    }

    public Miner loadMiner(String name)
    {
	Miner miner = null;
	try
	    {					
		miner = (Miner)Class.forName(name).newInstance();
	    }
	catch (ClassNotFoundException e)
	    {
		System.out.println(e);
	    }
	catch (InstantiationException e)
	    {
		System.out.println(e);
	    }
	catch (IllegalAccessException e)
	    {
		System.out.println(e);
	    }

	return miner;
    }
}
