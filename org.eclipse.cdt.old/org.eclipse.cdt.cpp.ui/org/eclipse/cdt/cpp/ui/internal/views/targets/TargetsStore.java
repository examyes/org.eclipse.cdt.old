package org.eclipse.cdt.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import java.util.*;
/**
 */
public class TargetsStore 
{
    static private TargetsStore _instance = new TargetsStore();

    public Vector projectList;
    
    /**
     * TargetsStore constructor comment.
     */
    public TargetsStore() 
    {
	super();
	projectList = new Vector();
    }

    public void persist()
    {
	java.util.Vector projects =  projectList;
	for(int i = 0;i < projects.size(); i++)
	    {
		java.util.ArrayList list = new java.util.ArrayList();
		int listCounter = 0;
			
		RootElement root = (RootElement)projects.elementAt(i);
		for (int y=0; y < root.getTargets().size(); y++)
		    {
			TargetElement target = (TargetElement)root.getTargets().elementAt(y);
			list.add(listCounter++,target.getTargetName());
			list.add(listCounter++,target.getWorkingDirectory()); 
			list.add(listCounter++,target.getMakeInvocation());
		    }

		org.eclipse.cdt.cpp.ui.internal.CppPlugin.writeProperty(root.getRoot(),root.getName(),list);
		
		// check if persistence has been worked properly 
		//java.util.ArrayList savedList = org.eclipse.cdt.cpp.ui.internal.CppPlugin.readProperty(root.getRoot(),root.getName());
	    }
    }

    static public TargetsStore getInstance()
    {
	return _instance;
    }

    public Vector getProjectList()
    {
	return projectList;
    }
}
