package com.ibm.dstore.hosts.views;
 
/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
	return new ObjectWindow(parent, 0, null, new ImageRegistry(), HostsPlugin.getInstance().getActionLoader(), true);
    }
}










