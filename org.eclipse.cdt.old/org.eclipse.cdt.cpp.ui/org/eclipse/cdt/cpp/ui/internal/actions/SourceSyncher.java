package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.vcm.*;

import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.ui.connections.*;

import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.ui.resource.*;
import com.ibm.dstore.core.model.*;

import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.action.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*; 

import java.io.*;
import java.util.*;
import java.lang.*;

public class SourceSyncher extends AbstractSourceSyncher
{
    public SourceSyncher(ArrayList projects)
    {
	super(projects);
    }
    
    public void run()
    {
	if (_projects.size() > 0)
	    {
		for (int i = 0; i < _projects.size(); i++)
		    {
			Repository src = (Repository)_projects.get(i);
			for (int j = 0; j < _projects.size(); j++)
			    {
				if (j != i)
				    {
					Repository target = (Repository)_projects.get(j);
					performSynchronization(src, target);
				    }
			    }
		    }
		
		System.out.println("changes = " + _changes.size());
		_changes.clear();
	    }	    	    
    }
    
}
