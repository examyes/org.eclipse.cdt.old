package org.eclipse.cdt.cpp.ui.internal.builder;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.ArrayList;

import org.eclipse.cdt.cpp.ui.internal.dialogs.FileExtensionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.internal.registry.FileEditorMapping;

public class AutoconfBuilderPropertyPageControl extends Composite
{
   
    private Button _debuggableButton;
    private Button _optimizedButton;
    private Button _addButton;
    private Button _removeButton;

    private Combo _compilerList;
    private Group _optionGroup;
    private Table _fileExtensionList;
 
	//private ArrayList extensionList = new ArrayList();
	
	
    public AutoconfBuilderPropertyPageControl(Composite cnr, int style)
    {
		super(cnr, style);
		
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
   		typeLbl.setText("File extensions:");
   		
   		new Label(distComp,SWT.NONE);
   		   		   		
   		_fileExtensionList = new Table(distComp,SWT.BORDER|SWT.MULTI|SWT.H_SCROLL|SWT.V_SCROLL|SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = _fileExtensionList.getItemHeight()*5;
		_fileExtensionList.setLayoutData(data);
		
		// button composite
		
		Composite buttonComp = new Composite(distComp,SWT.NONE);
		buttonComp.setLayout(new GridLayout());		

   		_addButton = new Button(buttonComp,SWT.PUSH);
   		_addButton.setText("Add...");
   		_addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
   		
   		/* Add the listeners */
		SelectionListener addSelectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				addFileExtension();
			};
		};
		_addButton.addSelectionListener(addSelectionListener);
   		
   		_removeButton = new Button(buttonComp,SWT.PUSH);
   		_removeButton.setText("Remove");
   		_removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
   		
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
    		else 
    			if(_fileExtensionList.getSelectionIndex()!=-1)
    				_fileExtensionList.remove(_fileExtensionList.getSelectionIndex());
    	
    		
    }

	public void promptForResourceType() {
		FileExtensionDialog dialog = new FileExtensionDialog(this.getShell());
		if (dialog.open() == dialog.OK) {
		String name = dialog.getName();
		String extension = dialog.getExtension();
		
		// Create the new type and insert it
		FileEditorMapping resourceType = new FileEditorMapping(name, extension);
		TableItem item = newResourceTableItem(_fileExtensionList, resourceType,true);
		_fileExtensionList.setFocus();
		_fileExtensionList.showItem(item);
		
		}
	}

	protected TableItem newResourceTableItem(Table extList,IFileEditorMapping mapping, boolean selected) 
	{
		Image image = mapping.getImageDescriptor().createImage(false);
	//if (image != null)
	//	imagesToDispose.add(image);
	
	//TableItem item = new TableItem(_fileExtensionList, SWT.NULL, index);
		TableItem item = new TableItem(extList, SWT.NULL);
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
    public ArrayList getExtensionList()
    {
    	
    	ArrayList list = new ArrayList();
    	TableItem[] items = _fileExtensionList.getItems();
    	for(int i = 0; i < items.length; i++ )
    	{
    		list.add(items[i].getText());
    	}
    	return list;
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
    public void setTableItems(ArrayList list)
    {
    	for(int i = 0; i < list.size(); i++)
    	{
    		String extension = (String)list.get(i);
    		extension = extension.substring(extension.indexOf(".")+1);

			// Create the new type and insert it
			FileEditorMapping resourceType = new FileEditorMapping("*",extension);
			TableItem item = newResourceTableItem(_fileExtensionList,resourceType,true);
			_fileExtensionList.showItem(item);
    	}
    }
    
}
