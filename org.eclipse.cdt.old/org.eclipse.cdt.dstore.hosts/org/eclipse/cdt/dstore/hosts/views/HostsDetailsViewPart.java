package org.eclipse.cdt.dstore.hosts.views;
 
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.hosts.*;
import org.eclipse.cdt.dstore.core.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.views.*;

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
	return new ObjectWindow(parent, ObjectWindow.TABLE, null, new ImageRegistry(), loader);
    }
    
    public IActionLoader getActionLoader()
    {
	IActionLoader loader = HostsPlugin.getInstance().getActionLoader();
	return loader;
    }

}










