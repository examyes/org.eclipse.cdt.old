package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.
 */

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;

import org.eclipse.ui.*;
import org.eclipse.debug.ui.IDebugUIConstants;

public class CppPerspective implements IPerspectiveFactory
{
  public CppPerspective()
  {
    super();
  }

  public void createInitialLayout(IPageLayout factory )
  {
    String editorArea = factory.getEditorArea();

    IFolderLayout bottomLeft = factory.createFolder("bottomLeft", IPageLayout.LEFT, (float)0.50,
					     editorArea);
    bottomLeft.addView("com.ibm.cpp.ui.Targets");
    bottomLeft.addView("com.ibm.cpp.ui.CppCommandViewPart");
    bottomLeft.addView("com.ibm.cpp.ui.DetailsViewPart");

    IFolderLayout topLeft = factory.createFolder("topLeft", IPageLayout.TOP, (float)0.60,
    					  "bottomLeft");
    topLeft.addView(IPageLayout.ID_RES_NAV);
    topLeft.addView("com.ibm.cpp.ui.CppProjectsViewPart");

    IFolderLayout topLeftRight = factory.createFolder("topLeftRight", IPageLayout.RIGHT, (float)0.45,
						      "topLeft");
    topLeftRight.addView("com.ibm.cpp.ui.ProjectObjectsViewPart");
    topLeftRight.addView("com.ibm.cpp.ui.SystemObjectsViewPart");
    topLeftRight.addView("com.ibm.cpp.ui.ParsedSourceViewPart");

    IFolderLayout bbottomRight = factory.createFolder("bbottomRight", IPageLayout.BOTTOM, (float)0.70,
						      editorArea);
    bbottomRight.addView("com.ibm.cpp.ui.CppOutputViewPart");
    bbottomRight.addView("com.ibm.cpp.ui.SuperDetailsViewPart");

    factory.addActionSet(IDebugUIConstants.DEBUG_ACTION_SET);
  }
}
















