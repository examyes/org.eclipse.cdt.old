package com.ibm.dstore.hosts.views;
 
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.hosts.*;
import com.ibm.dstore.core.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.views.*;

import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*; 
import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.action.*;

import org.eclipse.ui.*;

public class HostsDetailsViewPart extends DetailsViewPart
{
    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	return new ObjectWindow(parent, 0, null, new ImageRegistry(), loader, true);
    }
    
    public IActionLoader getActionLoader()
    {
	IActionLoader loader = HostsPlugin.getInstance().getActionLoader();
	return loader;
    }

}










