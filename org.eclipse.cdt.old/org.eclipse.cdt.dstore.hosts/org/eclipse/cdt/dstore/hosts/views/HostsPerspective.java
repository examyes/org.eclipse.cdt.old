package com.ibm.dstore.hosts.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
      bottomLeft.addView("com.ibm.dstore.ui.views.DetailsViewPart");


  }
}
















