package org.eclipse.cdt.dstore.ui.connections;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.ui.dialogs.*;
import org.eclipse.cdt.dstore.ui.connections.*;

import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.resource.*;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;

import java.util.*;
import java.io.*;

public abstract class EditConnectionAction extends ConnectActionDelegate
{
	private DataStoreUIPlugin _plugin = DataStoreUIPlugin.getDefault();

	protected void checkEnabledState(IAction action, Connection connection)
	{
		action.setEnabled(!connection.isConnected());	
	}
	
	
	public void run()
	{
		if (_connection != null)
		{
			ConnectDialog dialog = new ConnectDialog(_plugin.getLocalizedString("Edit_Connection"), _connection);
			dialog.open();
			if (dialog.getReturnCode() != dialog.OK)
				return;

			String name = dialog.getName();
			String host = dialog.getHostIP();
			String port = dialog.getPort();
			String dir = dialog.getHostDirectory();

			_connection.setName(name);
			_connection.setHost(host);
			_connection.setPort(port);
			_connection.setDir(dir);
			_connection.setIsLocal(dialog.isLocal());
			_connection.setIsUsingDaemon(dialog.isUsingDaemon());

			_dataStore.refresh(_dataStore.getHostRoot());
		}
	}
}