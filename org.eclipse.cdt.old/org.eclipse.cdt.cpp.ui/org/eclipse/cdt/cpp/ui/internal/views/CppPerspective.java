package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;

import org.eclipse.ui.*;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.search.ui.SearchUI;

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
    topLeftRight.addView("com.ibm.cpp.ui.ParsedSourceViewPart");

    IFolderLayout bbottomRight = factory.createFolder("bbottomRight", IPageLayout.BOTTOM, (float)0.70,
						      editorArea);
    bbottomRight.addView("com.ibm.cpp.ui.CppOutputViewPart");

    factory.addActionSet(IDebugUIConstants.DEBUG_ACTION_SET);
		
    // views - c++
    factory.addShowViewShortcut("com.ibm.cpp.ui.CppProjectsViewPart");
    factory.addShowViewShortcut("com.ibm.cpp.ui.DetailsViewPart");
    factory.addShowViewShortcut("com.ibm.cpp.ui.ProjectObjectsViewPart");
    factory.addShowViewShortcut("com.ibm.cpp.ui.SystemObjectsViewPart");
    factory.addShowViewShortcut("com.ibm.cpp.ui.ParsedSourceViewPart");
    factory.addShowViewShortcut("com.ibm.cpp.ui.CppCommandViewPart");
    factory.addShowViewShortcut("com.ibm.cpp.ui.CppOutputViewPart");
    factory.addShowViewShortcut("com.ibm.cpp.ui.SuperDetailsViewPart");    
    factory.addShowViewShortcut("com.ibm.cpp.ui.Targets");

    // search
    factory.addShowViewShortcut(SearchUI.SEARCH_RESULT_VIEW_ID);
    
    // views - standard workbench
    factory.addShowViewShortcut(IPageLayout.ID_OUTLINE);
    factory.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
    factory.addShowViewShortcut(IPageLayout.ID_RES_NAV);
    
    // new actions - c++ project creation wizard
    factory.addNewWizardShortcut("com.ibm.cpp.ui.wizards.CppNewProjectResourceWizard"); //$NON-NLS-1$
  }
}
















