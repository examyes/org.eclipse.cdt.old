package org.eclipse.cdt.cpp.ui.internal.builder;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.dialogs.FileExtensionDialog;
import org.eclipse.cdt.cpp.ui.internal.wizards.*;
import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.omg.stub.java.rmi._Remote_Stub;

import java.io.FileWriter;
import java.util.*;

public class AutoconfBuilderPropertyPageControl extends Composite
{
   
    private Button _debuggableButton;
    private Button _optimizedButton;
    private Button _addButton;
    private Button _removeButton;

    private Combo _compilerList;
    private Group _optionGroup;
    private Table _fileExtensionList;
 
    private CppPlugin _plugin;

    public AutoconfBuilderPropertyPageControl(Composite cnr, int style)
    {
		super(cnr, style);
		
		
		
		_plugin = CppPlugin.getDefault();
		
		GridLayout layout = new GridLayout();
	   	layout.numColumns = 1;
	   	
	   	setLayout(layout);
		// group #1 - executable group
		
		_optionGroup = new Group(this,SWT.NONE);
		_optionGroup.setText("Compiler debugging and optimization options");
		GridLayout g1Layout = new GridLayout();
	   	g1Layout.numColumns = 1;
	   	
		_optionGroup.setLayout(g1Layout);
		_optionGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

		// comp for options group 		
		Composite execComp = new Composite(_optionGroup,SWT.NONE);
		
		GridLayout execCompLayout = new GridLayout();
	   	execCompLayout.numColumns = 1;
		execComp.setLayout(execCompLayout);
		execComp.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

		// options contents
		_debuggableButton = new Button(execComp, SWT.RADIO);
		_debuggableButton.setText("Debug");	   	

		_optimizedButton = new Button(execComp, SWT.RADIO);
		_optimizedButton.setText("Optimized");
		
		// group #2 Extra Dist selection
		Group distGroup = new Group(this,SWT.NONE);
		distGroup.setText("Distribution control settings:");
		GridLayout distLayout = new GridLayout();
		distLayout.numColumns = 1;
		distGroup.setLayout(distLayout);
		distGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

		// composite for distribution control group
		Composite distComp = new Composite(distGroup,SWT.NONE);
		
		GridLayout distCompLayout = new GridLayout();
	   	distCompLayout.numColumns = 2;
		distComp.setLayout(distCompLayout);
		distComp.setLayoutData(new GridData (GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
 
   		// widgets
   		Label typeLbl = new Label(distComp,SWT.NONE);
   		typeLbl.setText("File types:");
   		
   		new Label(distComp,SWT.NONE);
   		   		   		
   		_fileExtensionList = new Table(distComp,SWT.BORDER|SWT.MULTI|SWT.H_SCROLL|SWT.V_SCROLL|SWT.FULL_SELECTION);
   //		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = _fileExtensionList.getItemHeight()*12;
		_fileExtensionList.setLayoutData(data);
		
		
		
	//	gd.verticalAlignment = gd.FILL;
	//	gd.grabExcessHorizontalSpace = false;
	//	_fileExtensionList.setLayoutData(gd);
   		
		// button composite
		
		Composite buttonComp = new Composite(distComp,SWT.NONE);
		buttonComp.setLayout(new GridLayout());		

   		_addButton = new Button(buttonComp,SWT.PUSH);
   		_addButton.setText("Add...");
   		
   		/* Add the listeners */
		SelectionListener addSelectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				addFileExtension();
			};
		};
		_addButton.addSelectionListener(addSelectionListener);
   		
   		_removeButton = new Button(buttonComp,SWT.PUSH);
   		_removeButton.setText("Remove");
   		
   		/* Add the listeners */
		SelectionListener removeSelectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				removeFileExtension();
			};
		};
		_removeButton.addSelectionListener(removeSelectionListener);
    }
    
    public void addFileExtension()
    {
    	// persist extension list
    	promptForResourceType();
    }
    public void removeFileExtension()
    {
     		if(_fileExtensionList.getSelectionIndices().length>1)
    			_fileExtensionList.remove(_fileExtensionList.getSelectionIndices());
    		else if(_fileExtensionList.getSelectionIndex()!=-1)
    		_fileExtensionList.remove(_fileExtensionList.getSelectionIndex());
    	
    		
    }

	public void promptForResourceType() {
		FileExtensionDialog dialog = new FileExtensionDialog(this.getShell());
		if (dialog.open() == dialog.OK) {
		String name = dialog.getName();
		String extension = dialog.getExtension();
		
		// Create the new type and insert it
		FileEditorMapping resourceType = new FileEditorMapping(name, extension);
		TableItem item = newResourceTableItem(resourceType,true);
		_fileExtensionList.setFocus();
		_fileExtensionList.showItem(item);
		}
	}

	protected TableItem newResourceTableItem(IFileEditorMapping mapping, boolean selected) {
	Image image = mapping.getImageDescriptor().createImage(false);
	//if (image != null)
	//	imagesToDispose.add(image);
	
	//TableItem item = new TableItem(_fileExtensionList, SWT.NULL, index);
	TableItem item = new TableItem(_fileExtensionList, SWT.NULL);
	if (image != null) {
		item.setImage(image);
	}
	item.setText(mapping.getLabel());
	item.setData(mapping);
//	if (selected) {
//		_fileExtensionList.setSelection(index);
	//}

	return item;
}
	// gets
     
    public boolean getDebugButtonSelection()
    {
		return _debuggableButton.getSelection();
    }
    public boolean getOptimizedButtonSelection()
    {
		return _optimizedButton.getSelection();
    }
    
    // sets
    

    public void setDebugButtonSelection(boolean flag)
    {
		_debuggableButton.setSelection(flag);
    }
   
    public void setOptimizedButtonSelection(boolean flag)
    {
		_optimizedButton.setSelection(flag);
    }
    
}
