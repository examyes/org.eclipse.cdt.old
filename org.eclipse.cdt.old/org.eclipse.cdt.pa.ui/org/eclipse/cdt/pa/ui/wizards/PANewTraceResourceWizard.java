package org.eclipse.cdt.pa.ui.wizards;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.swt.widgets.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.core.resources.*;

import org.eclipse.ui.*;
import org.eclipse.cdt.pa.ui.PAPlugin;


public class PANewTraceResourceWizard extends Wizard implements INewWizard {

    private IWorkbench                      _desktop;
    private IStructuredSelection            _selection;
    private PATraceTypeWizardPage 			_mainPage;
    private PAPlugin                        _plugin = PAPlugin.getDefault();
  

    public void addPages()
    {

	 super.addPages();
	 _mainPage = new PATraceTypeWizardPage("PA NewTraceTypePage");
	 _mainPage.setTitle("Trace Target");
	 _mainPage.setDescription("Create a new trace target");
	 _mainPage.setImageDescriptor(_plugin.getImageDescriptor("newproject"));
	 this.addPage(_mainPage);
	
	 /*
	 PATracePreferenceWizardPage preferencePage = new PATracePreferenceWizardPage("PA TracePreferencePage");
	 preferencePage.setTitle("Trace Preferences");
	 preferencePage.setDescription("Set the trace preferences");
	 preferencePage.setImageDescriptor(_plugin.getImageDescriptor("newproject"));
	 this.addPage(preferencePage);
	 */
	 
    }

    public PATraceTypeWizardPage getMainPage()
    {
	 return _mainPage;
    }


    public boolean performFinish()
    {
      _mainPage.finish();
      _plugin.getModelInterface().openPerspective();
	  return true;
    }


    public void init(IWorkbench aWorkbench,IStructuredSelection currentSelection) {
	 _desktop = aWorkbench;
	 _selection = currentSelection;
    }
    
}
