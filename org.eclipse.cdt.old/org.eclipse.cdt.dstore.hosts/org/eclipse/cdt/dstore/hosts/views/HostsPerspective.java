package com.ibm.dstore.hosts.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;

import org.eclipse.ui.*;

public class HostsPerspective implements IPerspectiveFactory
{
  public HostsPerspective()
  {
    super(); 
  }

  public void createInitialLayout(IPageLayout factory )
  {
      String editorArea = factory.getEditorArea();

      IFolderLayout a = factory.createFolder("a", IPageLayout.LEFT, (float)0.60, editorArea);
      a.addView("com.ibm.dstore.hosts.views.CommandViewPart");

      IFolderLayout b = factory.createFolder("b", IPageLayout.BOTTOM, (float)0.80, "a");
      b.addView("com.ibm.dstore.hosts.views.OutputViewPart");

      IFolderLayout topLeft = factory.createFolder("topLeft", IPageLayout.TOP, (float)0.70, "a");
      topLeft.addView("com.ibm.dstore.hosts.views.HostsViewPart");

      IFolderLayout bottomLeft = factory.createFolder("bottomLeft", IPageLayout.RIGHT, (float)0.60, "topLeft");
      bottomLeft.addView("com.ibm.dstore.hosts.views.HostsDetailsViewPart");


  }
}
















