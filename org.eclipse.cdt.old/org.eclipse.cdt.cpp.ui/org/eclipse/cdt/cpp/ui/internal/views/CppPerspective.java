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

public class CppPerspective implements IPerspectiveFactory
{
  public CppPerspective()
  {
    super();
  }

  public void createInitialLayout(IPageLayout factory )
  {
    String editorArea = factory.getEditorArea();

    IFolderLayout bbottomRight = factory.createFolder("bbottomRight", IPageLayout.BOTTOM, (float)0.65,
						      editorArea);
    bbottomRight.addView("org.eclipse.cdt.cpp.ui.CppOutputViewPart");
    bbottomRight.addView(IPageLayout.ID_TASK_LIST);

    IFolderLayout bottomLeft = factory.createFolder("bottomLeft", IPageLayout.LEFT, (float)0.50, "bbottomRight");
    bottomLeft.addView("org.eclipse.cdt.cpp.ui.DetailsViewPart");
    bottomLeft.addView("org.eclipse.cdt.cpp.ui.CppCommandViewPart");
    bottomLeft.addView("org.eclipse.cdt.cpp.ui.Targets");

    IFolderLayout topLeft = factory.createFolder("topLeft", IPageLayout.LEFT, (float)0.20,
    					  editorArea);
    topLeft.addView("org.eclipse.cdt.cpp.ui.CppProjectsViewPart");

    IFolderLayout topRight = factory.createFolder("topRight", IPageLayout.RIGHT, (float)0.75,
						  editorArea);
    topRight.addView(IPageLayout.ID_OUTLINE);


    factory.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
    factory.addActionSet(IDebugUIConstants.DEBUG_ACTION_SET);
		
    // views - c++
    factory.addShowViewShortcut("org.eclipse.cdt.cpp.ui.CppProjectsViewPart");
    factory.addShowViewShortcut("org.eclipse.cdt.cpp.ui.DetailsViewPart");
    factory.addShowViewShortcut("org.eclipse.cdt.cpp.ui.ProjectObjectsViewPart");
    factory.addShowViewShortcut("org.eclipse.cdt.cpp.ui.SystemObjectsViewPart");
    factory.addShowViewShortcut("org.eclipse.cdt.cpp.ui.ParsedSourceViewPart");
    factory.addShowViewShortcut("org.eclipse.cdt.cpp.ui.CppCommandViewPart");
    factory.addShowViewShortcut("org.eclipse.cdt.cpp.ui.CppOutputViewPart");
    factory.addShowViewShortcut("org.eclipse.cdt.cpp.ui.Targets");
    factory.addShowViewShortcut("org.eclipse.cdt.linux.help.views.ResultsViewPart");

    // search
    factory.addShowViewShortcut(SearchUI.SEARCH_RESULT_VIEW_ID);
    
    // views - standard workbench
    factory.addShowViewShortcut(IPageLayout.ID_OUTLINE);
    factory.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
    factory.addShowViewShortcut(IPageLayout.ID_RES_NAV);
    factory.addShowViewShortcut(IPageLayout.ID_PROP_SHEET); 
    
    // new actions - c++ project creation wizard
    factory.addNewWizardShortcut("org.eclipse.cdt.cpp.ui.wizards.CppNewProjectResourceWizard"); //$NON-NLS-1$
  }
}
















