package org.eclipse.cdt.dstore.miners.monitor;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;

import java.util.*;
import java.lang.*;
import java.io.*;

public class Monitor extends Handler
{
    public class TypeNumPair
    {
	public String _type;
	public int    _num;

	public TypeNumPair(String type)
	{	   
	    _type = type;
	    _num = 0;
	}

	public boolean matchType(String type)
	{
	    if (type.equals(_type))
		{
		    return true;
		}
	    else
		{
		    return false;
		}
	}

	public void increment()
	{
	    _num++;
	}	
    }


    private int _sizeThreshold;
    private HashMap _metrics;

    public Monitor()
    {
	_sizeThreshold = 1000;
	_metrics = new HashMap();
    }

    public void handle()
    {
	int size = _dataStore.getNumElements();
		
	if (size > _sizeThreshold)
	    {		
		//		System.out.println("deleting log");
		System.gc();
	    }
    }


    private synchronized void attemptClean()
    {
		/*
		DataElement logRoot = _dataStore.getLogRoot();
		for (int i = logRoot.getNestedSize() - 1; i >= 0; i--)
		    {
			DataElement command = logRoot.get(i);
			DataElement status =  _dataStore.find(command, DE.A_TYPE, "status", 1);
			if ((status != null) && status.getName().equals("done"))
			    {
				_dataStore.deleteObjects(status);
			    }
		    }
		*/
    }

    private synchronized void printResults()
    {
	System.out.println("");
	System.out.println("");
	System.out.println("TYPE: \t\t\t\t\t NUM: ");
	System.out.println("================================================================");
	Object values[] = _metrics.values().toArray();
	for (int i = 0; i < values.length; i++)
	    {
		TypeNumPair data = (TypeNumPair)values[i];
		String type = data._type;
		int length = type.length();
		
		String result = type;
		for (int j = length; j < 40; j++)
		    {
			result += " ";
		    }
		result += data._num;

		System.out.println(result);
	    }
    }

    private synchronized void examine(DataElement node)
    {
	int size = node.getNestedSize();	

	String type = node.getType();
	TypeNumPair metric = (TypeNumPair)_metrics.get(type);
	if (metric == null)
	    {
		_metrics.put(type, new TypeNumPair(type));
	    }
	else
	    {
		metric.increment();
	    }

	if (size > 0)
	    {
		for (int i = 0; i < size; i++)
		    {
			DataElement child = node.get(i);		
			examine(child);
		    }
	    }	
    }
}
