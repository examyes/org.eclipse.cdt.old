package org.eclipse.cdt.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.ui.internal.misc.ContainerSelectionGroup;

import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.core.model.*;

public class NewTargetGroup implements Listener 
{
    // the client to notify of changes
    private Listener			client;
    
    // whether to allow existing resources
    private boolean allowExistingResources = false;
    
    // problem indicator
    private String				problemMessage = "";
    
    // widgets
    private ObjectWindow _viewer;

    //private Button				browseButton;
    private Text				targetNameField;
    private Text				invocationNameField;
    // constants
    private static final int	SIZING_TEXT_FIELD_WIDTH = 250;
    
    // NL enablement
    private CppPlugin pluginInstance = CppPlugin.getPlugin();
    private String GROUP_TITLE = "TargetsViewer.Container.Group.Title";
    private String GROUP_TARGET_INVOCATION = "TargetsViewer.Container.Group.Target_Invocation";
    private String PROBLEM_MESSAGE_TARGET_CONTAINER = "TargetsViewer.Container.GroupProblem.Target_Container";
    private String PROBLEM_MESSAGE_TARGET_PATH = "TargetsViewer.Container.GroupProblem.Target_Path";
    private String PROBLEM_MESSAGE_TARGET_INVOCATION = "TargetsViewer.Container.GroupProblem.Target_Invocation";
    private String PROBLEM_MESSAGE_TARGET_NAME = "TargetsViewer.Container.GroupProblem.Target_Name";

    public NewTargetGroup(Composite parent,Listener client,String resourceLabelString) 
    {
	super();
	createContents(parent,resourceLabelString);
	this.client = client;
    }

    /**
     * Returns a boolean indicating whether all controls in this group
     * contain valid values.
     *
     * @return boolean
     */
    public boolean areAllValuesValid() 
    {
	return problemMessage.equals("");
    }

    /**
     * Creates this object's visual components.
     *
     * @param parent com.ibm.swt.widgets.Composite
     */
    protected void createContents(Composite parent,String resourceLabelString) 
    {
	// server name group
	Composite composite = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.marginWidth = 0;
	layout.marginHeight = 0;
	composite.setLayout(layout);
	composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
	
	// container group
	CppPlugin plugin = CppPlugin.getDefault();
	ModelInterface api = plugin.getModelInterface();
	DataElement input = api.findWorkspaceElement();
	DataStore dataStore = plugin.getDataStore();
	_viewer = new ObjectWindow(composite, ObjectWindow.TREE, dataStore, plugin.getImageRegistry(), org.eclipse.cdt.cpp.ui.internal.views.CppActionLoader.getInstance());	
	_viewer.setInput(input);
	_viewer.fixateOnRelationType("contents");
	_viewer.fixateOnObjectType("Project Containers");
	
	GridLayout vlayout= new GridLayout();
	vlayout.numColumns = 1;
	vlayout.marginHeight = 2;
	vlayout.marginWidth = 2;
	_viewer.setLayout(vlayout);
	
	GridData tgrid = new GridData(GridData.FILL_BOTH);
	tgrid.heightHint = 200;
	tgrid.widthHint = 200;
	_viewer.setLayoutData(tgrid);
	

	Composite nameGroup = new Composite(composite,SWT.NONE);
	layout = new GridLayout();
	layout.numColumns = 2;
	layout.marginWidth = 0;
	nameGroup.setLayout(layout);
	GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	nameGroup.setLayoutData(data);

	new Label(nameGroup,SWT.NONE).setText(resourceLabelString);

	// target name entry field
	targetNameField = new Text(nameGroup,SWT.BORDER);
	targetNameField.addListener(SWT.KeyDown,this);
	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	targetNameField.setLayoutData(data);

	new Label(nameGroup,SWT.NONE).setText(pluginInstance.getLocalizedString(GROUP_TARGET_INVOCATION));
	// Invocation entry field
	invocationNameField = new Text(nameGroup,SWT.BORDER);
	invocationNameField.addListener(SWT.KeyDown,this);
	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	invocationNameField.setLayoutData(data);
	
	validateControls();
}
/**
 * Returns the path of the currently selected container
 * or null if no container has been selected. Note that
 * the container may not exist yet if the user entered
 * a new container name in the field.
 */
public String getContainerFullPath() 
    {
	DataElement selection = _viewer.getSelected();
	if (selection != null)
	    {
		return selection.getSource();
	    }
	else
	    {
		return null;
	    }
    }

    /**
     * Returns a string that is the path of the currently selected
     * container.  Returns an empty string if no container has been
     * selected.
     */
    public String getInvocation() 
    {
	return invocationNameField.getText();
    }
    
    /**
     * Returns an error message indicating the current problem with the value
     * of a control in the group, or an empty message if all controls in the
     * group contain valid values.
     *
     * @return java.lang.String
     */
    public String getProblemMessage() 
    {
	return problemMessage;
    }

    /**
     * Returns a string that is the path of the currently selected
     * container.  Returns an empty string if no container has been
     * selected.
     */
    public String getResource() 
    {
	return targetNameField.getText();
    }

    public DataElement getResourceElement()
    {
	return _viewer.getSelected();
    }

    /**
     * Handles events for all controls in the group.
     *
     * @param e com.ibm.swt.widgets.Event
     */
    public void handleEvent(Event e) 
    {
	validateControls();
	if (client != null) {
	    client.handleEvent(e);
	}
    }

    /**
     * Sets the flag indicating whether existing resources are permitted.
     */
    public void setAllowExistingResources(boolean value) 
    {
	allowExistingResources = value;
    }

    /**
     * Sets the value of this page's container.
     *
     * @param value Full path to the container.
     */
    public void setContainerFullPath(IPath path) 
    {
	System.out.println("set container " + path);
    }

    /**
     * Gives focus to one of the widgets in the group, as determined by the group.
     */
    public void setFocus() 
    {
	targetNameField.setFocus();
    }

    /**
     * Sets the value of this page's resource name.
     *
     * @param value new value
     */
    public void setInvocation(String value) 
    {
	invocationNameField.setText(value);
	validateControls();
    }

    /**
     * Sets the value of this page's resource name.
     *
     * @param value new value
     */
    public void setTargetName(String value) 
    {
	targetNameField.setText(value);
	validateControls();
    }

    /**
     * Returns a <code>boolean</code> indicating whether a container name represents
     * a valid container resource in the workbench.  An error message is stored for
     * future reference if the name does not represent a valid container.
     *
     * @return <code>boolean</code> indicating validity of the container name
     */
    protected boolean validateContainer() 
    {
	DataElement selection = _viewer.getSelected();
	if (selection == null)
	    {
		problemMessage = pluginInstance.getLocalizedString(PROBLEM_MESSAGE_TARGET_CONTAINER);
		return false;
	    }
	return true;
    }

    /**
     * Validates the values for each of the group's controls.  If an invalid
     * value is found then a descriptive error message is stored for later
     * reference.  Returns a boolean indicating the validity of all of the
     * controls in the group.
     */
    protected boolean validateControls() 
    {
	// don't attempt to validate controls until they have been created
	if (_viewer == null) 
	    {
		return false;
	    }

	problemMessage = "";
	
	if (!validateResourceName() || !validateContainer() || !validateInvocation())
	    return false;
	
	
	return validateContainer();	
    }
    
    /**
     * Returns a <code>boolean</code> indicating whether the specified resource
     * path represents a valid new resource in the workbench.  An error message
     * is stored for future reference if the path  does not represent a valid
     * new resource path.
     *
     * @param containerName the container name to validate
     * @return <code>boolean</code> indicating validity of the resource path
     */
    protected boolean validateFullResourcePath(IPath resourcePath) 
    {
	return true;
    }

    /**
     * Returns a <code>boolean</code> indicating whether the resource name rep-
     * resents a valid resource name in the workbench.  An error message is stored
     * for future reference if the name does not represent a valid resource name.
     *
     * @return <code>boolean</code> indicating validity of the resource name
     */    
    protected boolean validateInvocation() 
    {
	String invocation = invocationNameField.getText();
	
	if (invocation.equals(""))
	    {
		problemMessage =pluginInstance.getLocalizedString(PROBLEM_MESSAGE_TARGET_INVOCATION);
		return false;
	}

	return true;
    }

    /**
     * Returns a <code>boolean</code> indicating whether the resource name rep-
     * resents a valid resource name in the workbench.  An error message is stored
     * for future reference if the name does not represent a valid resource name.
     *
     * @return <code>boolean</code> indicating validity of the resource name
     */
    protected boolean validateResourceName() 
    {
	String resourceName = targetNameField.getText();
	
	if (resourceName.equals("") || resourceName.indexOf(IPath.SEPARATOR) != -1) {
	    problemMessage = pluginInstance.getLocalizedString(PROBLEM_MESSAGE_TARGET_NAME);
	    return false;
	}
	
	return true;
    }
}
