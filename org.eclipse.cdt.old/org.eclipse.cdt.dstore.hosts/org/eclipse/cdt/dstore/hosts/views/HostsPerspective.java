package org.eclipse.cdt.dstore.hosts.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
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

      IFolderLayout topright = factory.createFolder("topright", IPageLayout.TOP, (float)0.50, editorArea);
      topright.addView("org.eclipse.cdt.dstore.hosts.views.HostsDetailsViewPart");
      
      IFolderLayout topleft = factory.createFolder("topLeft", IPageLayout.LEFT, (float)0.40, "topright");
      topleft.addView("org.eclipse.cdt.dstore.hosts.views.HostsViewPart");

      IFolderLayout a = factory.createFolder("a", IPageLayout.LEFT, (float)0.50, editorArea);
      a.addView("org.eclipse.cdt.dstore.hosts.views.CommandViewPart");

      IFolderLayout b = factory.createFolder("b", IPageLayout.BOTTOM, (float)0.40, "a");
      b.addView("org.eclipse.cdt.dstore.hosts.views.OutputViewPart");


  }
}
















