package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/PICLLaunchWizardSelectionPage.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 16:00:18)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.internal.picl.PICLModelPresentation;
import com.ibm.debug.internal.picl.PICLUtils;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;

public class PICLLaunchWizardSelectionPage extends WizardSelectionPage {

	private static final String PREFIX= "PICLLaunchWizardSelectionPage.";
	private static final String LAUNCHER= PREFIX + "launcher";
	private static final String UNKNOWN= PREFIX + "unknown";
	private static final String DEFAULT_LAUNCHER= PREFIX + "defaultLauncher";
	private static final String SELECT_ERROR_LAUNCHER= PREFIX + "selectErrorLauncher";
	/**
	 * Viewer for the launchers
	 */
	protected TableViewer fLaunchersList;

	/**
	 * List of launchers
	 */
	protected Object[] fLaunchers;

	/**
	 * Check box for setting default launcher
	 */
	protected Button fSetAsDefaultLauncher;

	/**
	 * A content provider for the elements list
	 */
	class ElementsContentProvider implements IStructuredContentProvider {

		/**
		 * @see IContentProvider#inputChanged
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return fLaunchers;
		}
	}

	class SimpleSorter extends ViewerSorter {
		/**
		 * @seeViewerSorter#isSorterProperty(Object, Object)
		 */
		public boolean isSorterProperty(Object element, Object property) {
			return true;
		}
	}

	public PICLLaunchWizardSelectionPage(Object[] allLaunchers) {
		super(PICLUtils.getResourceString(PREFIX + "title"));
		fLaunchers= allLaunchers;
	}
	public void createControl(Composite ancestor) {
		Composite root= new Composite(ancestor, SWT.NONE);
		GridLayout l= new GridLayout();
		l.numColumns= 1;
		root.setLayout(l);
		createLaunchersGroup(root);

		setTitle(PICLUtils.getResourceString(PREFIX + "title"));
		setImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_WIZBAN_DEBUG));
		initializeSettings();
		setControl(root);
/*
		WorkbenchHelp.setHelp(
			ancestor,
			new Object[] { IDebugHelpContextIds.LAUNCHER_SELECTION_WIZARD_PAGE });
*/
	}
	public void createLaunchersGroup(Composite root) {

		Label launchersLabel= new Label(root, SWT.NONE);
		launchersLabel.setText(PICLUtils.getResourceString(LAUNCHER));

		fLaunchersList= new TableViewer(new Table(root, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER)) {
			protected void handleDoubleSelect(SelectionEvent event) {
				updateSelection(getSelection());
				((PICLLaunchWizard)getWizard()).updateDefaultLauncher();
				getContainer().showPage(getNextPage());
			}
		};

		Table list= fLaunchersList.getTable();

		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		gd.heightHint= 200;
		gd.grabExcessVerticalSpace= true;
		gd.grabExcessHorizontalSpace= true;
		list.setLayoutData(gd);

		fLaunchersList.setContentProvider(new ElementsContentProvider());
		fLaunchersList.setLabelProvider(new PICLModelPresentation());
		fLaunchersList.setSorter(new SimpleSorter());
		fLaunchersList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				if (e.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection ss= (IStructuredSelection) e.getSelection();
					if (!ss.isEmpty()) {
						launcherSelected((ILauncher)ss.getFirstElement());
					}
				}
			}
		});
		fLaunchersList.setInput(fLaunchersList);
		fSetAsDefaultLauncher= new Button(root, SWT.CHECK);
		updateDefaultProject();
	}
	/**
	 * Implemented here to provide package access
	 */
	protected IWizardContainer getContainer() {
		return super.getContainer();
	}
	protected ILauncher getLauncher() {
		IStructuredSelection selection= (IStructuredSelection)fLaunchersList.getSelection();
		if (selection.isEmpty()) {
			return null;
		}
		return (ILauncher) selection.getFirstElement();
	}
	/**
	* Initialize the settings:<ul>
	* <li>If there is only one launcher, select it
	* </ul>
	*/
	protected void initializeSettings() {
		Runnable runnable= new Runnable() {
			public void run() {
				if (getControl().isDisposed()) {
					return;
				}
				if (fLaunchers.length == 0) {
					setErrorMessage(PICLUtils.getResourceString(SELECT_ERROR_LAUNCHER));
				} else
					fLaunchersList.setSelection(new StructuredSelection(fLaunchers[0]));
				}
		};
		Display.getCurrent().asyncExec(runnable);
	}
	/**
	 * Updates the elements list for the given launcher
	 */
	protected void launcherSelected(ILauncher launcher) {
		PICLLaunchWizardNode node= new PICLLaunchWizardNode(this, launcher);
		setSelectedNode(node);
		setMessage(null);
		updateDefaultLauncherButton(launcher);
		setDescription(node.getDescription());
	}
	/**
	 * Convenience method to set the error line
	 */
	public void setErrorMessage(String message) {
		super.setMessage(null);
		super.setErrorMessage(message);
	}
	/**
	 * Convenience method to set the message line
	 */
	public void setMessage(String message) {
		super.setErrorMessage(null);
		super.setMessage(message);
	}
	protected void updateDefaultLauncherButton(ILauncher launcher) {
		IProject project= ((PICLLaunchWizard)getWizard()).getProject();
		if (project == null) {
			// disable the control, we cannot set it without a project context
			fSetAsDefaultLauncher.setSelection(false);
			fSetAsDefaultLauncher.setEnabled(false);
		} else {
			// if the launcher is not the default launcher, enable the control
			try {
				ILauncher defaultLauncher= DebugPlugin.getDefault().getLaunchManager().getDefaultLauncher(project);
				if (defaultLauncher != null && defaultLauncher.equals(launcher)) {
					// disable the setting, but show that it is set
					fSetAsDefaultLauncher.setSelection(true);
					fSetAsDefaultLauncher.setEnabled(false);
				} else {
					// allow to set as default - in fact, set as default
					fSetAsDefaultLauncher.setSelection(true);
					fSetAsDefaultLauncher.setEnabled(true);
				}
			} catch (CoreException e) {
				// disable default launcher
				fSetAsDefaultLauncher.setSelection(false);
				fSetAsDefaultLauncher.setEnabled(false);
			}
		}
	}
	protected void updateDefaultProject() {
		IProject project= ((PICLLaunchWizard)getWizard()).getProject();
		String projectName= "";
		if (project != null) {
			projectName= project.getName();
		} else {
			projectName= PICLUtils.getResourceString(UNKNOWN);
		}
		fSetAsDefaultLauncher.setText(MessageFormat.format(PICLUtils.getResourceString(DEFAULT_LAUNCHER), new String[] {projectName}));
	}
}
