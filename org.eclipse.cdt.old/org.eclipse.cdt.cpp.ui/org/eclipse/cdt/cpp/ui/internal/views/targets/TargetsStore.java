package com.ibm.cpp.ui.internal.views.targets;

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


    static public TargetsStore getInstance()
    {
	return _instance;
    }

    public Vector getProjectList()
    {
	return projectList;
    }
}
