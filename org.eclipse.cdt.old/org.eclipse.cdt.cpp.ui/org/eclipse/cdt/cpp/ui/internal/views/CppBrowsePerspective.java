package org.eclipse.cdt.cpp.ui.internal.views;
 
/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;

import org.eclipse.ui.*;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.search.ui.SearchUI;

public class CppBrowsePerspective implements IPerspectiveFactory
{
  public CppBrowsePerspective()
  {
    super();
  }

  public void createInitialLayout(IPageLayout factory )
  {
    String editorArea = factory.getEditorArea();

    IFolderLayout topLeft = factory.createFolder("topLeft", IPageLayout.TOP, (float)0.45,
    					  editorArea);
    topLeft.addView("org.eclipse.cdt.cpp.ui.CppProjectsViewPart");

    IFolderLayout topLeftRight = factory.createFolder("topLeftRight", IPageLayout.RIGHT, (float)0.25,
						      "topLeft");
    topLeftRight.addView("org.eclipse.cdt.cpp.ui.ProjectObjectsViewPart");
    topLeftRight.addView("org.eclipse.cdt.cpp.ui.ParsedSourceViewPart");

    IFolderLayout topRight = factory.createFolder("topRight", IPageLayout.RIGHT, (float)0.65,
						  "topLeftRight");
    topRight.addView("org.eclipse.cdt.cpp.ui.SuperDetailsViewPart");    

    // search
    factory.addShowViewShortcut(SearchUI.SEARCH_RESULT_VIEW_ID);
    
    
    // new actions - c++ project creation wizard
    factory.addNewWizardShortcut("org.eclipse.cdt.cpp.ui.wizards.CppNewProjectResourceWizard"); //$NON-NLS-1$
  }
}
















