package com.ibm.dstore.ui.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */ 

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;

import org.eclipse.ui.*;

public class ObjectsPerspective implements IPerspectiveFactory
{
  public ObjectsPerspective()
  {
    super(); 
  } 

  public void createInitialLayout(IPageLayout factory )
  {
      String editorArea = factory.getEditorArea();
      
      IFolderLayout topLeft = factory.createFolder("topLeft", IPageLayout.LEFT, (float)0.50,
						   editorArea);
      topLeft.addView("com.ibm.dstore.ui.views.GenericViewPart");
      
      IFolderLayout bottomLeft = factory.createFolder("bottomLeft", IPageLayout.BOTTOM, (float)0.40,
						      "topLeft");
      bottomLeft.addView("com.ibm.dstore.ui.views.DetailsViewPart");
  }
}
















