package org.eclipse.cdt.dstore.hosts.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.hosts.*;
import org.eclipse.cdt.dstore.hosts.dialogs.*;
import org.eclipse.cdt.dstore.ui.dialogs.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*; 

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public class SelectFileAction extends CustomAction
{
    private HostsPlugin _plugin;
  public SelectFileAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
	  super(subject, label, command, dataStore);
	  _plugin = HostsPlugin.getInstance();
      }

  public void run()
      {
	  DataElementFileDialog fd = new DataElementFileDialog(_plugin.getLocalizedString("actions.Select_File"), _subject);
	  fd.open();
      }    
}

