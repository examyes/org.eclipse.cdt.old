package org.eclipse.cdt.dstore.ui.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;

import org.eclipse.cdt.dstore.ui.actions.RemoteOperation;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.io.*;
import java.util.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;

public class ImportAction extends Action
{
  private DataElement _object;

  public ImportAction(String label, DataElement object)
  {
    super(label);
    _object = object;
  }

  public void run()
  {
    DataStore dataStore = _object.getDataStore();

    FileDialog fdlg = new FileDialog(new Shell());
    String[] filterExtensions = new String[1];
    filterExtensions[0] = "*.xml";

    String filterPath = dataStore.getAttribute(ClientAttributes.A_LOCAL_PATH);

    fdlg.setFilterPath(filterPath);
    fdlg.setFilterExtensions(filterExtensions);
    String fileName = fdlg.open();
    if (fileName != null)
      {
	System.out.println("IM: opening " + fileName);
 	dataStore.load(_object, fileName);
      }
  }
}
