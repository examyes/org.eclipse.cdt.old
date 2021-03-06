package org.eclipse.cdt.cpp.ui.internal.dialogs;
/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.ArrayList;

import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.actions.AdvancedConfigureAction;
import org.eclipse.cdt.dstore.ui.actions.CustomAction;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 *
 */
public class CustomMessageDialog extends MessageDialog{
	
	private final int DEFAULT_BUTTON_TYPE = 1<<5; // SWT.CHECK
	private Button[] buttons;
	private String[] buttonLabels;
	private String[] extraButtonLabels;
	private int defaultButtonIndex;
	public Button [] extraButtons;
	private SelectionListener actionListener;
	private String globalSettingKey = "Is_Global_Setting_Enabled";
	private String preferenceKey;
	private int customButtonType;
	private IResource project;
	
	public CustomMessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, 
							int dialogImageType, String[] dialogButtonLabels,int defaultIndex, String[] extraButtonLabels,
							SelectionListener actionListener, String preferenceKey) 
	{
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
		this.buttonLabels = dialogButtonLabels;
		if(extraButtonLabels!= null)
			this.extraButtonLabels = extraButtonLabels;
		this.actionListener = actionListener;
		this.preferenceKey = preferenceKey;
		this.customButtonType = DEFAULT_BUTTON_TYPE;
	}
	public CustomMessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, 
							int dialogImageType, String[] dialogButtonLabels,int defaultIndex, String[] extraButtonLabels,
							SelectionListener actionListener, String preferenceKey, IResource resource) 
	{
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
		this.buttonLabels = dialogButtonLabels;
		if(extraButtonLabels!= null)
			this.extraButtonLabels = extraButtonLabels;
		this.actionListener = actionListener;
		this.preferenceKey = preferenceKey;
		this.customButtonType = DEFAULT_BUTTON_TYPE;
		this.project = resource;
	}
	public CustomMessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, 
							int dialogImageType, String[] dialogButtonLabels,int buttonType,
							int defaultIndex, String[] extraButtonLabels, SelectionListener actionListener, String preferenceKey) 
	{
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
		this.buttonLabels = dialogButtonLabels;
		if(extraButtonLabels!= null)
			this.extraButtonLabels = extraButtonLabels;
		this.actionListener = actionListener;
		this.preferenceKey="";
		this.customButtonType = buttonType;
	}
	
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);	

		// create a layout with spacing and margins appropriate for the font size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData(
			GridData.HORIZONTAL_ALIGN_END |
			GridData.VERTICAL_ALIGN_CENTER);
		composite.setLayoutData(data);		

		composite.setFont(parent.getFont());
		// create composite for the don't show button
		Composite leftPane = new Composite(composite,SWT.NONE);
		
		// create a layout with spacing and margins appropriate for the font size.
		GridLayout leftPaneLayout = new GridLayout();
		leftPaneLayout.numColumns = 1;
		leftPaneLayout.makeColumnsEqualWidth = true;
		leftPaneLayout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		leftPaneLayout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		leftPaneLayout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		leftPaneLayout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);

		leftPane.setLayout(leftPaneLayout);

		GridData leftPaneData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING
		|GridData.HORIZONTAL_ALIGN_CENTER);
		leftPane.setLayoutData(leftPaneData);		

		// Add a check box if needed
		if(extraButtonLabels!= null )
			createExtraButtonsForButtonBar(leftPane);
		// create composite to hold the butons
		Composite rightPane = new Composite(composite,SWT.NONE);
		
		// create a layout with spacing and margins appropriate for the font size.
		GridLayout rightPaneLayout = new GridLayout();
		rightPaneLayout.numColumns = 0; // this is incremented by createButton
		rightPaneLayout.makeColumnsEqualWidth = true;
		rightPaneLayout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		rightPaneLayout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		rightPaneLayout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		rightPaneLayout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);

		rightPane.setLayout(rightPaneLayout);

		GridData rightPanedata = new GridData(
			GridData.HORIZONTAL_ALIGN_END |
			GridData.VERTICAL_ALIGN_CENTER);
		rightPane.setLayoutData(rightPanedata);		
		// Add the buttons to the button bar.
		createButtonsForButtonBar(rightPane);
		return composite;
	}
	protected void createButtonsForButtonBar(Composite parent) {
		
		buttons = new Button[buttonLabels.length];
		for (int i = 0; i < buttonLabels.length; i++) {
			String label = buttonLabels[i];
			Button button = createButton(parent, i, label, defaultButtonIndex == i);
			buttons[i] = button;
		}
	}
		protected void createExtraButtonsForButtonBar(Composite parent) {
		
		extraButtons = new Button[extraButtonLabels.length];
		for (int i = 0; i < extraButtonLabels.length; i++) {
			String label = extraButtonLabels[i];
			Button button = createExtraButton(parent,i,label,customButtonType);
			extraButtons[i] = button;
		}
	}
	
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout)parent.getLayout()).numColumns++;
		Button button;
		button = new Button(parent, SWT.PUSH);

		button.setText(label);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		
		button.setData(new Integer(id));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(((Integer) event.widget.getData()).intValue());
			}
			});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		button.setFont(parent.getFont());
		return button;
	}
	protected Button createExtraButton(Composite parent, int id, String label, int type) {
		Button button;
		button = new Button(parent, type);

		button.setText(label);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		
		
		if(type == 1<<5)
			button.setData(new Integer(id));
		else
			button.setData(new Integer(id+100));
		
		
		button.setSelection(false);
		button.addSelectionListener(actionListener);
		button.setFont(parent.getFont());
		return button;
	}
	public int open()
    {
		if(showDialog(preferenceKey,project))
			return super.open();
		return -1;

    }
    private boolean showDialog(String preferenceKey, IResource project)
    {
    	ArrayList preferenceList = new ArrayList();
    	ArrayList propertyList = new ArrayList();
    	ArrayList global = new ArrayList();
    	
    	// check wethere to use preference settings- global - or to use the properties settings
    	if(project!=null)
    	{
    		global = CppPlugin.readProperty(project,globalSettingKey);
    		if(!global.isEmpty())
    		{
    		
    			if(global.get(0).equals("Yes"))
    			{
    				preferenceList = CppPlugin.readProperty(preferenceKey);
    				if(!preferenceList.isEmpty())
    				{
    					String preference = (String)preferenceList.get(0);
						if (preference.equals("Yes"))
							return true;
						else
							return false;
    				}
    				else
    				{
    					return true;
    				}
    			}
    			else
    			{
    				propertyList = CppPlugin.readProperty(project, preferenceKey);
    				String preference = (String)propertyList.get(0);
					if (preference.equals("Yes"))
						return true;
					else
						return false;
    			}
    		}
    		else
    			return true;
    	}
    	else
    	{
    		preferenceList = CppPlugin.readProperty(preferenceKey);
    		if(preferenceList.isEmpty())
    			return true;
    		else
    		{
    			String preference = (String)preferenceList.get(0);
				if (preference.equals("Yes"))
					return true;
				else
					return false;
    		}
    	}
    }
}