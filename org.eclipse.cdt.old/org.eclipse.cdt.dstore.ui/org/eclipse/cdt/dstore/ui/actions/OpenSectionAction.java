package com.ibm.dstore.ui.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.ui.widgets.*;
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

public class OpenSectionAction extends Action
{
  private DataElement  _object;
  private ObjectWindow _window;

  public OpenSectionAction(String label, DataElement object, ObjectWindow window)
  {
    super(label);
    _object = object;
    _window = window;
  }

  public void run()
  {
    _window.openSection(_object);
  }
}

