package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.ui.*;

public class MinersPerspective implements IPerspectiveFactory
{
  public MinersPerspective()
  {
    super(); 
  } 

  public void createInitialLayout(IPageLayout factory )
  {
      String editorArea = factory.getEditorArea();

    // Top left.
    IFolderLayout topLeft = factory.createFolder("topLeft", IPageLayout.LEFT 
                                                 , (float)0.60
                                                 , editorArea
                                                 );
    topLeft.addView("com.ibm.cpp.ui.SchemaViewPart");
    topLeft.addView("com.ibm.cpp.ui.MinersViewPart");
    topLeft.addView("com.ibm.cpp.ui.LogViewPart");
     
    // Bottom left.
    IFolderLayout bottomLeft = factory.createFolder("bottomLeft", IPageLayout.BOTTOM, (float)0.60,
					     "topLeft");
    bottomLeft.addView("com.ibm.cpp.ui.DetailsViewPart");
  }
}
















