package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.model.*;

import com.ibm.linux.help.*;

public class HelpAction extends CustomAction
{ 
    public HelpAction(DataElement subject, String label, DataElement command, DataStore dataStore)
    {	
        super(subject, label, command, dataStore);
    }

    public void run()
    {
	String theKey = getKey(_subject.getName());
	if (theKey != null)
	    {
		HelpPlugin.getDefault().showMatches(theKey); //find the docs
	    }
    }
    
    private String getKey(String name)
    {
	if (name==null || name.length()==0 || name=="")
	    return null;

	int end = name.length();
	int begin = name.lastIndexOf("::");
	    
	if (begin < end)
	    {
		if (begin != -1)
		    {			
			return name.substring(begin+2);
		    }
		else
		    {			
			return name;
		    }
	    }
	return null;
    }
}


