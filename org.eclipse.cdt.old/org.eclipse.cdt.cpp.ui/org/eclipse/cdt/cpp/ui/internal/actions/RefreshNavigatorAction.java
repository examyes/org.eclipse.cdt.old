package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.vcm.*;
import com.ibm.cpp.ui.internal.views.*;

import com.ibm.dstore.ui.widgets.ExtendedTreeViewer;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.ui.connections.*;

import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;


import java.io.*;
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;

import org.eclipse.core.resources.*;
import org.eclipse.ui.views.contentoutline.*;

import org.eclipse.ui.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.ui.internal.*;


public class RefreshNavigatorAction extends Action
{
  private CppPlugin   _plugin = CppPlugin.getDefault();
  public class RefreshAction implements Runnable
  {
    private TreeViewer _viewer;

    public RefreshAction(TreeViewer viewer)
    {
      _viewer = viewer;
    }

    public void run()
    {
      _viewer.refresh();
    }
  }


  public RefreshNavigatorAction(String label)
      {	
        super(label);
      }

  public void run()
      {
	  RemoteProjectViewPart instance = RemoteProjectViewPart.getInstance();
	  if (instance != null)
	      {
		  instance.resetView();		  
	      }
      }
}


