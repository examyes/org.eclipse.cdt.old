package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.internal.misc.ContainerSelectionGroup;

import com.ibm.cpp.ui.internal.*;

public class TargetSelectionDialog extends SelectionDialog {
	// the widget group;
	private ContainerSelectionGroup group;

	// the root resource to populate the viewer with
	private IContainer initialSelection;

	// allow the user to type in a new container name
	private boolean allowNewContainerName = true;
	
	// the validation message
	private Label statusMessage;

	//for validating the selection
	private ISelectionValidator validator;

	// sizing constants
	private static final int	SIZING_SELECTION_PANE_HEIGHT = 250;
	private static final int	SIZING_SELECTION_PANE_WIDTH = 300;
	
// NL enablement
	private CppPlugin pluginInstance = CppPlugin.getPlugin();
	private String TITLE = "TargetsViewer.Selection_Dialog.Title";
	private String MESSAGE = "TargetsViewer.Selection_Dialog.Message";

public TargetSelectionDialog(Shell parentShell, IContainer initialRoot, boolean allowNewContainerName, String message) {
	super(parentShell);
	setTitle(pluginInstance.getLocalizedString(TITLE));
	this.initialSelection = initialRoot;
	this.validator = validator;
	this.allowNewContainerName = allowNewContainerName;
	if (message != null)
		setMessage(message);
	else
		setMessage(pluginInstance.getLocalizedString(MESSAGE));
}
protected Control createDialogArea(Composite parent) {
	// create composite 
	Composite dialogArea = (Composite)super.createDialogArea(parent);

	Listener listener = new Listener() {
		public void handleEvent (Event event) {
			if (statusMessage != null && validator != null) {
				String errorMsg = validator.isValid(group.getContainerFullPath());
				if (errorMsg == null || errorMsg.equals("")) {
					statusMessage.setText("");
					getOkButton().setEnabled(true);
				} else {
					statusMessage.setForeground(statusMessage.getDisplay().getSystemColor(SWT.COLOR_RED));
					statusMessage.setText(errorMsg);
					getOkButton().setEnabled(false);
				}
			}
		}
	};
	
	// container selection group
	group = new ContainerSelectionGroup(dialogArea, listener, allowNewContainerName, getMessage());
	if (initialSelection != null) {
		group.setSelectedContainer(initialSelection);
	}

	statusMessage = new Label(parent, SWT.NONE);
	statusMessage.setLayoutData(new GridData(GridData.FILL_BOTH));

	return dialogArea;
}
protected IContainer getResource()
{
	return initialSelection;
}

protected void okPressed() {
	List chosenContainerPathList = new ArrayList();
		chosenContainerPathList.add(group.getContainerFullPath());
	setResult(chosenContainerPathList);
	super.okPressed();
}
/**
 * Sets the validator to use.
 */
public void setValidator(ISelectionValidator validator) {
	this.validator = validator;
}
}
