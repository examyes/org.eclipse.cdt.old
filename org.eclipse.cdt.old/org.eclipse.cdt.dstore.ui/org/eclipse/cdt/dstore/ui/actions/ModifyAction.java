package com.ibm.dstore.ui.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.ui.actions.RemoteOperation;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;

import java.io.*;
import java.util.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;

public class ModifyAction extends Action
{
  private DataElement _object;
  private TestUI      _parent;

  public ModifyAction(String label, DataElement object, TestUI parent)
  {
    super(label);
    _object = object;
    _parent = parent;
  }

  public void run()
  {
    DataStore dataStore = _object.getDataStore();    
    ModifyDialog dlg = new ModifyDialog(_object);
    dlg.open();
    
  }
}
