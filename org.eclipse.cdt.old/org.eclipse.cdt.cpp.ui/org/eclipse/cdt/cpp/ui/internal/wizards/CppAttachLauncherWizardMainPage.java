package org.eclipse.cdt.cpp.ui.internal.wizards;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.CppProjectAttributes;
import org.eclipse.cdt.cpp.ui.internal.CppPlugin;

import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.*;

import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.model.*;
import org.eclipse.ui.actions.*;


/**
 * Main page for the debug attach wizard.
 */

public class CppAttachLauncherWizardMainPage extends WizardPage implements Listener
{

    // initial value stores
    private String initialProgramFieldValue;
    private String initialProcessIDFieldValue;

    // widgets
    private Text                             programNameField;
    private Text                             processIDField;

    // constants
    private static final int SIZING_TEXT_FIELD_WIDTH = 300;
    protected CppPlugin    _plugin = CppPlugin.getPlugin();
    protected ArrayList    _history;

    private  String        _programName;
    private  String        _processID;

    /**
     * Creates a <code>WizardNewProjectCreationPage</code> instance.
     *
     * @param pageId this page's internal name
     * @param desktop the current desktop
     */
    public CppAttachLauncherWizardMainPage(String pageId, String currentSelectionName) {
	super(pageId);
   _programName = currentSelectionName;
//	setPageComplete(false);
	setPageComplete(true);
    }

    /**
     * Returns this page's initial visual components.
     *
     * @param parent a <code>Composite</code> that is to be used as the parent of this
     *     page's collection of visual components
     * @return a <code>Control</code> that contains this page's collection of visual
     *     components
     * @see org.eclipse.swt.widgets.Composite
     * @see org.eclipse.swt.widgets.Control
     */
    public void createControl(Composite parent)
    {
   	Composite composite = new Composite(parent, SWT.NULL);
   	
   	
   	composite.addHelpListener(new HelpListener()
   	    {
   		public void helpRequested(HelpEvent event)
   		{
   		    performHelp();
   		}
   	    });
   	
   	composite.setLayout(new GridLayout());
   	composite.setLayoutData(new GridData(GridData.FILL_BOTH));
   	
   	createProgramNameGroup(composite);
   	createProcessIDGroup(composite);

   	createSpacer(composite);

   	processIDField.setFocus();
   	
   	setControl(composite);
    }

    /**
     * Field for entering the name of the program to execute.
     * We populate it with the user's current selection in the navigator,
     * and make it read only.  Essentially forcing user to preselect its executable
     * in the navigator before invoking the debug action.
     * @param parent a <code>Composite</code> that is to be used as the parent
     *     of this group's collection of visual components
     * @see org.eclipse.swt.widgets.Composite
     */
    protected final void createProgramNameGroup(Composite parent)
    {
   	// project specification group
   	Composite programGroup = new Composite(parent,SWT.NONE);
   	GridLayout layout = new GridLayout();
   	layout.numColumns = 2;
   	programGroup.setLayout(layout);
   	programGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
   	
   	// new program label
   	Label programLabel = new Label(programGroup,SWT.NONE);
   	programLabel.setText(_plugin.getLocalizedString("debugAttachLauncherMain.ProgramName"));
   	
   	// new program name entry field
   	programNameField = new Text(programGroup, SWT.BORDER);
   	GridData data = new GridData(GridData.FILL_HORIZONTAL);
   	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
      programNameField.setText(_programName);
   	programNameField.setLayoutData(data);
   	programNameField.setEnabled(false);
   	
    }

    /**
     * Field for entering the processID of the program to attach to in debug mode.
     * @param parent a <code>Composite</code> that is to be used as the parent
     *     of this group's collection of visual components
     * @see org.eclipse.swt.widgets.Composite
     */
    protected final void createProcessIDGroup(Composite parent)
    {
    	// project specification group
    	Composite processIDGroup = new Composite(parent,SWT.NONE);
    	GridLayout layout = new GridLayout();
    	layout.numColumns = 2;
    	processIDGroup.setLayout(layout);
    	processIDGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
    	
    	// new processID label
    	Label processIDLabel = new Label(processIDGroup,SWT.NONE);
    	processIDLabel.setText(_plugin.getLocalizedString("debugAttachLauncherMain.ProcessID"));
    	
    	// new processID name entry field
    	processIDField = new Text(processIDGroup, SWT.BORDER);
    	processIDField.addListener(SWT.Modify, this);
    	GridData data = new GridData(GridData.FILL_HORIZONTAL);
    	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
    	processIDField.setLayoutData(data);
    	processIDField.setEnabled(true);
     /*
   	// source name entry field
   	sourceNameField = new Combo(sourceContainerGroup,SWT.BORDER);
   	sourceNameField.addListener(SWT.Modify,this);
   	sourceNameField.addListener(SWT.Selection,this);
   	_history = _plugin.readProperty(CppProjectAttributes.LOCATION_LOCAL);
   	int size = _history.size();
   	for (int i = 0; i < size; i++)
   	    {
   		String item = (String)_history.get(size - i - 1);
   		if (item != null)
   		    {
   			sourceNameField.add(item, i);
   		    }
   		
   		if (i == 0)
   		    {	
   			sourceNameField.setText(item);
   		    }
   	    }

   	GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
   	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
   	sourceNameField.setLayoutData(data);
    */


    	
    	if (initialProcessIDFieldValue != null)
    	    processIDField.setText(initialProcessIDFieldValue);



    }


    /**
     * Performs setup code that is to be invoked whenever the user enters this
     * page in the wizard.
     *
     * @param int the direction that this page was entered from.  Possible values
     *     are <code>Wizard.CANCEL</code>, <code>Wizard.PREVIOUS</code>, <code>Wizard.NEXT</code>
     *     and <code>Wizard.FINISH</code>.
     * @see org.eclipse.jface.wizard.Wizard
     */
    protected void enter(int direction)
    {
	setPageComplete(validatePage());
    }

    /**
     * Performs this page's finish code which is invoked when the user presses the Finish
     * button on the parent wizard.  Returns a <code>boolean</code> indicating successful
     * completion, which also acts as permission for the parent wizard to close.  For this
     * page the task to perform at finish time is creation of a new project resource as
     * dictated by the current values of the page's visual components.
     *
     * @return <code>boolean</code> indicating successful completion of this page's finish code
     */
    public boolean finish()
    {

      _processID = processIDField.getText();

   	return true;
    }

    public String getProcessID()
    {
		return _processID;
    }



/**
 * Returns the current contents of the project name field, or
 * its set initial value if it does not exist yet (which could
 * be <code>null</code>).
 *
 * @return the project name field's current value or anticipated initial value, or <code>null</code>
 */
public String getProgramFieldValue() {
	if (programNameField == null)
		return initialProgramFieldValue;
		
	return programNameField.getText();
}


    /**
     * Handles all events and enablements for visual components on this page.
     * <b>Subclasses</b> may wish to override this method if they hook listeners
     * to their own visual components.  However, they must ensure that this method
     * is <b>always</b> invoked, even if the event source is a visual component
     * defined in a subclass.
     *
     * @param ev the visual component event
     */
    public void handleEvent(Event ev)
    {
	Widget source = ev.widget;
	
	if ((source == programNameField) || (source == processIDField))
	    {
		//resetSelection();
	    }
	this.setPageComplete(this.validatePage());
    }

    public void performHelp()
    {
	// System.out.println("HELP");
    }

    /**
     * Returns a <code>boolean</code> indicating whether this page's visual
     * components currently all contain valid values.
     *
     * @return <code>boolean</code> indicating validity of all visual components on this page
     */
    protected boolean validatePage()
    {
	return validateProgramNameGroup();
    }

    /**
     *	Display an error dialog with the specified message.
     *
     *	@param message java.lang.String
     */
    protected void displayErrorDialog(String message)
    {
	//***	MessageDialog.openError(getShell(),"Export Problems",message);
    }

    /**
     * Returns a <code>boolean</code> indicating whether this page's project name
     * specification group's visual components currently all contain valid values.
     *
     * @return <code>boolean</code> indicating validity of all visual components
     *     in the project name specification group
     */
    protected boolean validateProgramNameGroup()
    {
	IWorkspace workspace = _plugin.getPluginWorkspace();
	String programFieldContents = programNameField.getText();
	if (programFieldContents.equals(""))
	    return false;
	
	IStatus result = workspace.validateName(programFieldContents,IResource.PROJECT);
	if (!result.isOK()) {
	    //**displayErrorMessage("Invalid project name: " + result.getMessage());
	    return false;
	}
	
	
	return true;
    }
    private void getDefaults(String attribute, Combo control)
    {
	ArrayList history = _plugin.readProperty(attribute);
	int size = history.size();
	for (int i = 0; i < size; i++)
	    {
		String item = (String)history.get(i);
		if (item != null)
		    {
			control.add(item, i);
		    }
		
		if (i == 0)
		    {	
			control.setText(item);
		    }
	    }
    }

    private void setDefaults(String attribute, Combo control)
    {
	ArrayList history = new ArrayList();
	history.add(control.getText());
	for (int i = 0; i < control.getItemCount(); i++)
	    {
		String item = (String)control.getItem(i);
		if (!history.contains(item))
		    history.add(item);
	    }	
	
	_plugin.writeProperty(attribute, history);
    }

    /**
     *	Reset the selected resources collection and update the ui appropriately
     */
//    protected void resetSelection()
//    {
//	selectedResource = null;
//    }

    /**
     *	Respond to the user selecting/deselecting items in the
     *	extensions list
     *
     *	@param selection ISelection
     */
    public void selectionChanged(SelectionChangedEvent event)
    {
	    //resetSelection();
    }


    /**
     * Creates a horizontal spacer line that fills the width of its container.
     */
    protected void createSpacer(Composite parent)
    {
   	//	Label spacer = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_OUT);
   	Label spacer = new Label(parent, SWT.NONE);
   	GridData data = new GridData();
   	data.horizontalAlignment = GridData.FILL;
   	data.verticalAlignment = GridData.BEGINNING;
   	spacer.setLayoutData(data);
    }

    /**
     * Creates a new label with a bold font.
     */
   /*
    protected Label createBoldLabel(Composite parent, String text)
    {
   	Label label = new Label(parent, SWT.NONE);
   	label.setFont(JFaceResources.getBannerFont());
   	label.setText(text);
   	GridData data = new GridData();
   	data.verticalAlignment = GridData.FILL;
   	data.horizontalAlignment = GridData.FILL;
   	label.setLayoutData(data);
   	return label;
    }
   */

    }
