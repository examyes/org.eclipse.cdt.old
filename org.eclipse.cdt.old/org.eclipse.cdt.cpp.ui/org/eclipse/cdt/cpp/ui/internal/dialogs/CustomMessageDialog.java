package org.eclipse.cdt.cpp.ui.internal.dialogs;

import org.eclipse.cdt.cpp.ui.internal.actions.ConfigureAction;
import org.eclipse.cdt.dstore.ui.actions.CustomAction;
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
	
	private Button[] buttons;
	private String[] buttonLabels;
	private String[] extraButtonLabels;
	private int defaultButtonIndex;
	public Button [] extraButtons;
	private SelectionListener actionListener;

	
	public CustomMessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, 
							int dialogImageType, String[] dialogButtonLabels,
							int defaultIndex, String[] extraButtonLabels, SelectionListener actionListener) 
	{
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
		this.buttonLabels = dialogButtonLabels;
		if(extraButtonLabels!= null)
			this.extraButtonLabels = extraButtonLabels;
		this.actionListener = actionListener;
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
		leftPaneLayout.numColumns = 1;// this is incremented by createExtraButton
		leftPaneLayout.makeColumnsEqualWidth = true;
		leftPaneLayout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		leftPaneLayout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		leftPaneLayout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		leftPaneLayout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);

		leftPane.setLayout(leftPaneLayout);

		GridData leftPanedata = new GridData(
			GridData.HORIZONTAL_ALIGN_END |
			GridData.VERTICAL_ALIGN_CENTER);
		leftPane.setLayoutData(leftPanedata);		

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
			Button button = createExtraButton(parent,i,label,SWT.CHECK);
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
		
		button.setData(new Integer(id));
		button.setSelection(false);
		button.addSelectionListener(actionListener);
		button.setFont(parent.getFont());
		return button;
	}
}