package org.eclipse.cdt.dstore.core.util;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import java.lang.*;
import org.eclipse.cdt.dstore.core.model.*;


public class Sorter
{
    public static ArrayList sort(ArrayList list)
    {
	ArrayList sortedList = new ArrayList(list.size());
	while (list.size() > 0)
	    {
		DataElement first = findFirst(list);
		sortedList.add(first);
	    }
	
	return sortedList;
    }
    
    
    private static DataElement findFirst(ArrayList list)
    {
	DataElement result = null;
	for (int i = 0; i < list.size(); i++)
	    {
		DataElement item = (DataElement)list.get(i);
		int depth = item.depth();
		if ((result == null) || (depth > result.depth()))
		    {
			result = item;
		    }			
	    }
	
	list.remove(result);
	return result;			
    }
}
