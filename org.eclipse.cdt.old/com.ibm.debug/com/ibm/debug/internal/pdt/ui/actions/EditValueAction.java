package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/EditValueAction.java, eclipse, eclipse-dev, 20011128
// Version 1.7 (last modified 11/28/01 16:00:22)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValueModification;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;

import com.ibm.debug.internal.picl.PICLUtils;

/**
 * Action for changing the value of primitives and <code>String</code> variables.
 */
public class EditValueAction extends SelectionProviderAction {

	// Fields for inline editing
	protected Composite fComposite;
	protected Tree fTree;
	protected Label fEditorLabel;
	protected Text fEditorText;
	protected TreeEditor fTreeEditor;
	protected IVariable fVariable;
	private boolean crEvent;  //flag if a crEvent was received so know if should ignore focusLost event

	private static final String PREFIX= "EditValueAction.";

	public EditValueAction(Viewer viewer) {
		super(viewer, PICLUtils.getResourceString(PREFIX+"menuItem"));
		//setDescription(PICLUtils.getResourceString(PREFIX + DESCRIPTION));
		fTree= ((TreeViewer)viewer).getTree();
		fTreeEditor= new TreeEditor(fTree);

		WorkbenchHelp.setHelp(this, new Object[] { PICLUtils.getHelpResourceString("EditValueAction") });
	}
	/**
	 * Tidy up the widgets that were used
	 */
	private void cleanup() {
		if (fEditorText != null) {
			fEditorText.dispose();
			fEditorText = null;
			fVariable = null;
			fTreeEditor.setEditor(null, null);
			fComposite.setVisible(false);
		}
	}
	/**
	 * Edit the variable value with an inline text editor.
	 */
	protected void doActionPerformed(final IVariable variable) {
		final Shell activeShell= DebugUIPlugin.getActiveWorkbenchWindow().getShell();

		// If a previous edit is still in progress, finish it
		if (fEditorText != null) {
			saveChangesAndCleanup(fVariable, activeShell);
		}
		fVariable = variable;

		// Use a Composite containing a Label and a Text.  This allows us to edit just
		// the value, while still showing the variable name.
		fComposite = new Composite(fTree, SWT.NONE);
		fComposite.setBackground(fTree.getBackground());
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		fComposite.setLayout(layout);

		fEditorLabel = new Label(fComposite, SWT.LEFT);
		fEditorLabel.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		fEditorText = new Text(fComposite, SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		fEditorText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		String valueString= "";
		try {
			valueString= fVariable.getValue().getValueString();
		} catch (DebugException de) {
			ErrorDialog.openError(activeShell, PICLUtils.getResourceString("ErrorDialog.title"), null, de.getStatus());
		}
		TreeItem[] selectedItems = fTree.getSelection();
		fTreeEditor.horizontalAlignment = SWT.LEFT;
		fTreeEditor.grabHorizontal = true;
		fTreeEditor.setEditor(fComposite, selectedItems[0]);

		// There is no API on model presentation to get just the
		// variable name, so we have to make do with just calling IVariable.getName()
		String varName = "";
		try {
			varName = fVariable.getName();
		} catch (DebugException de) {
		}
		fEditorLabel.setText(varName + "=");

		fEditorText.setText(valueString);
		fEditorText.selectAll();

		fComposite.layout(true);
		fComposite.setVisible(true);
		fEditorText.setFocus();

		// CR means commit the change, ESC means abort changing the value
		fEditorText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				if (event.character == SWT.CR) {
					crEvent = true;
					saveChangesAndCleanup(fVariable, activeShell);
				}
				if (event.character == SWT.ESC) {
					cleanup();
				}
			}
		});

		// If the focus is lost, then act as if user hit CR and commit change
		//must make sure we don't act twice on the same edit action though
		//so first check if a keyReleased was received first.
		fEditorText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent fe) {
				if(!crEvent)
					saveChangesAndCleanup(fVariable, activeShell);
				crEvent=false;
			}
		});
	}
	/**
	 * @see Action
	 */
	public void run() {
		Iterator iterator= getStructuredSelection().iterator();
		doActionPerformed((IVariable)iterator.next());
	}
	/**
	 * If the new value validates, save it, and dispose the text widget,
	 * otherwise sound the system bell and leave the user in the editor.
	 */
	protected void saveChangesAndCleanup(IVariable variable, Shell shell) {
		String newValue= fEditorText.getText();
		try {
				variable.setValue(newValue);
			} catch (DebugException de) {
				ErrorDialog.openError(shell, PICLUtils.getResourceString("ErrorDialog.title"), null, de.getStatus());
			}
		cleanup();
	}
	/**
	 * @see SelectionProviderAction
	 */
	public void selectionChanged(IStructuredSelection sel) {
		update(sel);
	}
	/**
	 * Updates the enabled state of this action based
	 * on the selection
	 */
	protected void update(IStructuredSelection sel) {
		Iterator iter= sel.iterator();
		if (iter.hasNext()) {
			Object object= iter.next();
			if (object instanceof IValueModification) {
				IValueModification varMod= (IValueModification)object;
				if (!varMod.supportsValueModification()) {
					setEnabled(false);
					return;
				}
				setEnabled(!iter.hasNext());
				return;
			}
		}
		setEnabled(false);
	}
}


