package org.eclipse.cdt.cpp.miners.help.search;
/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.cdt.dstore.core.model.*;

import java.util.*;

public class DataElementMapper
{
    public static ArrayList convertToItemElement(ArrayList dataElementList)
    {
	ArrayList itemElementList = new ArrayList();
	for(int i=0;i<dataElementList.size();i++)
	    {
		DataElement element = (DataElement)dataElementList.get(i);

		if(element.getType().equals("helpresult"))
		    {
			StringTokenizer st = new StringTokenizer(element.getName(),"|||");
			if(st.countTokens()==6)
			    {
		
				ItemElement item= new ItemElement(st.nextToken(),
								  st.nextToken(),
								  st.nextToken(),
								  st.nextToken(),
								  st.nextToken(),
								  st.nextToken());
				itemElementList.add(item);
			    }
			else
			    {
				//System.out.println("insufficient tokens="+ st.countTokens());
			    }
		    }
	    }
	return itemElementList;
    }
    
 public static ArrayList convertToString(ArrayList itemElementList)
    {
	ArrayList stringList = new ArrayList();
	for(int i=0;i<itemElementList.size();i++)
	    {
		ItemElement item = (ItemElement)itemElementList.get(i);

		String contents = new String(item.getKey()+ "|||"+
					     item.getName()+"|||"+
					     item.getContent()+"|||"+
					     item.getInvocation()+"|||"+
					     item.getSection()+"|||"+
					     item.getType());
		

		stringList.add(contents);
	    }
	return stringList;

    }

    

}
