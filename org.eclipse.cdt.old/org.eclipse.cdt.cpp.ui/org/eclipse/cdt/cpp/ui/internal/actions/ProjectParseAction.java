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
 DataElement _parseCommand;
 
 public ProjectParseAction(DataElement subject, String label, DataElement command, DataStore dataStore)
 {	
     super(subject, label, command, dataStore);
     _parseCommand = _dataStore.localDescriptorQuery(subject.getDescriptor(), "C_PARSE");
 }
    
    public void run()
    {
	if (_parseCommand != null)
	    {
		DataElement status = null;
		if (_subject.getType().equals("Project"))
		    {
			status = _dataStore.command(_parseCommand, _subject);
			_api.monitorStatus(status);
		    }
		else
		    {		
			
			DataElement projectElement = getProjectFor(_subject);
			if (projectElement != null)
			    {
				ArrayList args = new ArrayList();
				args.add(projectElement); 
				status = _dataStore.command(_parseCommand, args, _subject);		
				_api.monitorStatus(status);
			    }
		    }
	    }
    }

    private DataElement getProjectFor(DataElement subject)
    {
	DataElement parent = subject.getParent();
	if (parent == null)
	    {
		return null;
	    }

	String type = parent.getType();
	if (type.equals("Project"))
	    {
		return parent;
	    }
	else if (type.equals("Data"))
	    {
		return null;
	    }
	else 
	    {
		return getProjectFor(parent);
	    }
    }
}


