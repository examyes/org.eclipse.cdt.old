package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.vcm.*;

import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.ui.connections.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.util.*;
import java.lang.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;

import org.eclipse.jface.action.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;

import org.eclipse.swt.widgets.*;

public class MergeFiles extends SynchronizeFiles
{
    public void run(IAction action)
    {
	super.run(action);
	
	SourceSyncher synch = new SourceSyncher(_projects);
	synch.start();
    }
}
