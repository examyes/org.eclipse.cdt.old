package com.ibm.dstore.core.server;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
