package org.eclipse.cdt.dstore.hosts.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.actions.*;

import org.eclipse.cdt.dstore.hosts.*;
import org.eclipse.cdt.dstore.hosts.actions.*;
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

public interface ITransferListener
{
    public void update(String message);
    public Shell getShell();
}
