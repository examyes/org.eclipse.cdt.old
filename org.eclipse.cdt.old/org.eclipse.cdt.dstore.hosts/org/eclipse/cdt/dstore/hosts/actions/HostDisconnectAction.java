package org.eclipse.cdt.dstore.hosts.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.hosts.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.connections.*;

public class HostDisconnectAction extends DisconnectAction
{
	protected ConnectionManager getConnectionManager()
	{
		return HostsPlugin.getInstance().getConnectionManager();	
	}
 
}