package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import org.eclipse.core.resources.*;
import java.util.*;

public class ProjectParseAction extends ProjectAction
{ 
 	public ProjectParseAction(DataElement subject, String label, DataElement command, DataStore dataStore)
 	{	
     	super(subject, label, command, dataStore);
	}
	
	public ProjectParseAction(java.util.List subjects, String label, DataElement command, DataStore dataStore)
	{
		super(subjects, label, command, dataStore);
	}
       
    public void run()
    {
    	for (int i = 0; i < _subjects.size(); i++)
    	{
	    	DataElement subject = (DataElement)_subjects.get(i);
	    	DataStore dataStore = subject.getDataStore();
	    		    	
      		DataElement parseCommand = dataStore.localDescriptorQuery(subject.getDescriptor(), "C_PARSE");
    	
			if (parseCommand != null)
	    	{
				DataElement status = null;
				DataElement projectElement = _api.getProjectFor(subject);
				if (projectElement != null)
	        	{
			 		ArrayList args = new ArrayList();
			 		args.add(projectElement); 
			 		status = _dataStore.command(parseCommand, args, subject);		
			 	_api.monitorStatus(status);
				}
	    	}
    	}
    }
  
}


