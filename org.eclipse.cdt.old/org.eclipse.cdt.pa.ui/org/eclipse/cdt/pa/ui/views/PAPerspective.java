package org.eclipse.cdt.pa.ui.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;

import org.eclipse.ui.*;

public class PAPerspective implements IPerspectiveFactory
{
  public PAPerspective()
  {
  }
  
  public void createInitialLayout(IPageLayout factory )
  {
    String editorArea = factory.getEditorArea();

    IFolderLayout topRight = factory.createFolder("topRight", IPageLayout.TOP, (float)0.55,
						      editorArea);
	topRight.addView("org.eclipse.cdt.pa.ui.FunctionStatisticsViewPart");
    topRight.addView("org.eclipse.cdt.pa.ui.ClassStatisticsViewPart");
    
    IFolderLayout topCenter = factory.createFolder("topCenter", IPageLayout.LEFT, (float)0.45,
					     "topRight");
    topCenter.addView("org.eclipse.cdt.pa.ui.TraceFilesViewPart");
					     	
    IFolderLayout topLeft = factory.createFolder("topLeft", IPageLayout.LEFT, (float)0.55,
    					  "topCenter");
    //topLeft.addView("org.eclipse.cdt.cpp.ui.CppProjectsViewPart");
    topLeft.addView("org.eclipse.cdt.pa.ui.PAProjectsViewPart");    					  
	
    IFolderLayout bottomLeft = factory.createFolder("bottomLeft", IPageLayout.LEFT, (float)0.50,
    					  editorArea);
    bottomLeft.addView("org.eclipse.cdt.pa.ui.CallTreeViewPart");
    bottomLeft.addView("org.eclipse.cdt.pa.ui.CallersViewPart");
    bottomLeft.addView("org.eclipse.cdt.pa.ui.CalleesViewPart");
    bottomLeft.addView(IPageLayout.ID_PROP_SHEET);
    bottomLeft.addView("org.eclipse.cdt.cpp.ui.CppOutputViewPart");
        
    // views - PA
    factory.addShowViewShortcut("org.eclipse.cdt.pa.ui.CallersViewPart");
    factory.addShowViewShortcut("org.eclipse.cdt.pa.ui.CalleesViewPart");
    factory.addShowViewShortcut("org.eclipse.cdt.pa.ui.TraceFilesViewPart");
    factory.addShowViewShortcut("org.eclipse.cdt.pa.ui.FunctionStatisticsViewPart");
    factory.addShowViewShortcut("org.eclipse.cdt.pa.ui.ClassStatisticsViewPart");
    factory.addShowViewShortcut("org.eclipse.cdt.pa.ui.CallTreeViewPart");
    
    // views - standard workbench
    factory.addShowViewShortcut(IPageLayout.ID_OUTLINE);
    factory.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
    factory.addShowViewShortcut(IPageLayout.ID_PROP_SHEET); 
    
    // new actions - c++ project creation wizard
    factory.addNewWizardShortcut("org.eclipse.cdt.pa.ui.wizards.PANewTraceResourceWizard");
  
  }
  
}