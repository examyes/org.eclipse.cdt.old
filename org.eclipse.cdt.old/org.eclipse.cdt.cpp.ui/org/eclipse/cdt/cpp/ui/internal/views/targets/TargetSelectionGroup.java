package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.util.*;
import java.util.List;
import org.eclipse.ui.internal.misc.UIHackFinder;
import com.ibm.cpp.ui.internal.*;

/**
 * Workbench-level composite for choosing a container.
 */
public class TargetSelectionGroup extends Composite {
	// The listener to notify of events
	private Listener listener;

	// Enable user to type in new container name
	private boolean allowNewContainerName = true;

	// Last selection made by user
	private IContainer selectedContainer;
	
	// handle on parts
	private Text containerNameField;
	private TreeViewer treeViewer;

// NL enablement
	private CppPlugin pluginInstance = CppPlugin.getPlugin();
	// the message to display at the top of this dialog
	private static final String DEFAULT_MSG_NEW_ALLOWED = "TargetsViewer.Selection_Group.Message.New_Allowed";
	private static final String DEFAULT_MSG_SELECT_ONLY = "TargetsViewer.Selection_Group.Message.Select_Only";


	// sizing constants
	private static final int SIZING_SELECTION_PANE_HEIGHT = 125;
	private static final int SIZING_SELECTION_PANE_WIDTH = 200;
/**
 * Creates a new instance of the widget.
 *
 * @param parent The parent widget of the group.
 * @param listener A listener to forward events to. Can be null if
 *	 no listener is required.
 * @param allowNewContainerName Enable the user to type in a new container
 *  name instead of just selecting from the existing ones.
 */
public TargetSelectionGroup (Composite parent, Listener listener, boolean allowNewContainerName) {
	this(parent, listener, allowNewContainerName, null);
}
/**
 * Creates a new instance of the widget.
 *
 * @param parent The parent widget of the group.
 * @param listener A listener to forward events to.  Can be null if
 *	 no listener is required.
 * @param allowNewContainerName Enable the user to type in a new container
 *  name instead of just selecting from the existing ones.
 * @param message The text to present to the user.
 */
public TargetSelectionGroup (Composite parent, Listener listener, boolean allowNewContainerName, String message) {
	super (parent, SWT.NONE);
	this.listener = listener;
	this.allowNewContainerName = allowNewContainerName;
	this.setFont(parent.getFont());
	if (message != null)
		createContents(message);
	else if (allowNewContainerName)
		createContents(pluginInstance.getLocalizedString(DEFAULT_MSG_NEW_ALLOWED));
	else
		createContents(pluginInstance.getLocalizedString(DEFAULT_MSG_SELECT_ONLY));
}
/**
 * The container selection has changed in the
 * tree view. Update the container name field
 * value and notify all listeners.
 */
public void containerSelectionChanged(IContainer container) {
	selectedContainer = container;
	
	if (allowNewContainerName) {
		if (container == null)
			containerNameField.setText("");
		else
			containerNameField.setText(container.getFullPath().toString());
	}

	// fire an event so the parent can update its controls
	if (listener != null) {
		Event changeEvent = new Event();
		changeEvent.type = SWT.Selection;
		changeEvent.widget = this;
		listener.handleEvent(changeEvent);
	}
}
/**
 * Creates the contents of the composite.
 */
public void createContents(String message) {
	GridLayout layout = new GridLayout();
	layout.marginWidth = 0;
	setLayout(layout);
	setLayoutData(new GridData(GridData.FILL_BOTH));

	Label label = new Label(this,SWT.WRAP);
	label.setText(message);
	label.setFont(this.getFont());

	if (allowNewContainerName) {
		containerNameField = new Text(this, SWT.SINGLE | SWT.BORDER);
		containerNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		containerNameField.addListener(SWT.Modify, listener);
	}
	else {
		// filler...
		new Label(this, SWT.NONE);
	}

	createTreeViewer();
}
/**
 *
 */
protected void createTreeViewer() {
	// Create drill down.
	DrillDownComposite drillDown = new DrillDownComposite(this, SWT.BORDER);
	GridData spec = new GridData(
		GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL |
		GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
	spec.widthHint = SIZING_SELECTION_PANE_WIDTH;
	spec.heightHint = SIZING_SELECTION_PANE_HEIGHT;
	drillDown.setLayoutData(spec);

	// Create tree viewer inside drill down.
	treeViewer = new TreeViewer(drillDown, SWT.NONE);
	drillDown.setChildTree(treeViewer);
	treeViewer.setContentProvider(new org.eclipse.ui.model.WorkbenchContentProvider());
	treeViewer.setLabelProvider(new WorkbenchLabelProvider());
	treeViewer.addSelectionChangedListener(
		new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				containerSelectionChanged((IContainer) selection.getFirstElement()); // allow null
			}
		});


	// This has to be done after the viewer has been laid out
	treeViewer.setInput(ResourcesPlugin.getWorkspace());
}
/**
 * Returns the currently entered container name.
 * Null if the field is empty. Note that the
 * container may not exist yet if the user
 * entered a new container name in the field.
 */
public IPath getContainerFullPath() {
	if (allowNewContainerName) {
		String pathName = containerNameField.getText();
		if (pathName == null || pathName.length() < 1)
			return null;
		else
			//The user may not have made this absolute so do it for them
			return (new Path(pathName)).makeAbsolute();
	} else {
		if (selectedContainer == null)
			return null;
		else
			return selectedContainer.getFullPath();
	}
}
/**
 * Returns the currently selected resource.
 */
public IContainer getResource() {
	return selectedContainer;
}
/**
 * Sets the selected existing container.
 */
public void setSelectedContainer(IContainer container) {
	selectedContainer = container;
	
	//expand to and select the specified container
	List itemsToExpand = new ArrayList();
	IContainer parent = container.getParent();
	while (parent != null) {
		itemsToExpand.add(0,parent);
		parent = parent.getParent();
	}
	treeViewer.setExpandedElements(itemsToExpand.toArray()); 
	treeViewer.setSelection(new StructuredSelection(container));
}
}
