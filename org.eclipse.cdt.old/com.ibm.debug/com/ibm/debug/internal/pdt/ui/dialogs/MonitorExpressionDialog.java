package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/MonitorExpressionDialog.java, eclipse, eclipse-dev, 20011128
// Version 1.9 (last modified 11/28/01 16:00:50)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.internal.ui.AbstractDebugView;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

import org.eclipse.ui.help.WorkbenchHelp;
import com.ibm.debug.WorkspaceSourceLocator;
import com.ibm.debug.internal.pdt.ui.views.MonitorView;
import com.ibm.debug.internal.pdt.ui.views.StorageView;
import com.ibm.debug.internal.picl.IPICLDebugConstants;
import com.ibm.debug.internal.picl.PICLException;
import com.ibm.debug.internal.picl.PICLThread;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.model.Location;
import com.ibm.debug.model.ViewInformation;


/**
 * Dialog to monitor expressions (program monitors show up in Monitors View, storage monitors show up in Storage View)
 */
public class MonitorExpressionDialog extends StatusDialog {
	protected final static String PREFIX= "MonitorExpressionDialog.";

	private PICLThread thread;
	private boolean defaultIsStorageMonitor;

	private Text expressionInput;
	private Button monitorTypeIsProgramButton;
	private Button monitorTypeIsStorageButton;
	private Group monitorTypeGroup;
	private Label fileNameLabel;
	private Label lineNumberLabel;
	private Label viewInfoLabel;
	private Label threadNameLabel;

	/**
	 * Constructor for MonitorExpressionDialog
	 */
	public MonitorExpressionDialog(Shell parentShell, PICLThread thread, boolean defaultIsStorageMonitor) {
		super(parentShell);
		setTitle(PICLUtils.getResourceString(PREFIX+"title"));
		setDefaultImage(PICLUtils.getImage(IPICLDebugConstants.PICL_ICON_CLCL_MONITOR_EXPRESSION));
		this.thread = thread;
		this.defaultIsStorageMonitor = defaultIsStorageMonitor;

		WorkbenchHelp.setHelp(parentShell, new Object[] { PICLUtils.getHelpResourceString("MonitorExpressionDialog") });
	}

	protected Control createDialogArea(Composite ancestor) {
		Composite parent = new Composite(ancestor, SWT.NULL);
		//FillLayout fill = new FillLayout();
		//fill.type = SWT.VERTICAL;
		//parent.setLayout(fill);
		//parent.setLayout(new RowLayout());
		parent.setLayout(new GridLayout());
		GridData spec2= new GridData();
		spec2.grabExcessVerticalSpace= true;
		spec2.grabExcessHorizontalSpace= true;
		spec2.horizontalAlignment= spec2.FILL;
		spec2.verticalAlignment= spec2.CENTER;
		parent.setLayoutData(spec2);


		expressionInput = new Text(parent, SWT.BORDER);
		GridData spec= new GridData();
		spec.grabExcessVerticalSpace= false;
		spec.grabExcessHorizontalSpace= true;
		spec.horizontalAlignment= spec.FILL;
		spec.verticalAlignment= spec.BEGINNING;
		expressionInput.setLayoutData(spec);

		monitorTypeGroup = new Group (parent, SWT.NONE);
		monitorTypeGroup.setLayout (new GridLayout());
		monitorTypeGroup.setLayoutData (new GridData (GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		monitorTypeGroup.setText(PICLUtils.getResourceString(PREFIX+"group"));

		monitorTypeIsProgramButton = new Button(monitorTypeGroup, SWT.RADIO);
		monitorTypeIsProgramButton.setText(PICLUtils.getResourceString(PREFIX+"programmonitor"));
		monitorTypeIsStorageButton = new Button(monitorTypeGroup, SWT.RADIO);
		monitorTypeIsStorageButton.setText(PICLUtils.getResourceString(PREFIX+"storagemonitor"));

		fileNameLabel = new Label(parent, SWT.NULL);
		fileNameLabel.setLayoutData(new GridData());

		lineNumberLabel = new Label(parent, SWT.NULL);
		lineNumberLabel.setLayoutData(new GridData());

		viewInfoLabel = new Label(parent, SWT.NULL);
		viewInfoLabel.setLayoutData(new GridData());

		threadNameLabel = new Label(parent, SWT.NULL);
		threadNameLabel.setLayoutData(new GridData());

		initStatusInfo();

		expressionInput.setFocus();
		return parent;
	}


	private void initStatusInfo() {
		try {
			ViewInformation viewInfo = thread.getViewInformation();
			Location location = thread.getLocation(viewInfo);

			if (defaultIsStorageMonitor) {
				monitorTypeIsStorageButton.setSelection(true);
			} else {
				monitorTypeIsProgramButton.setSelection(true);
			}
			fileNameLabel.setText(PICLUtils.getResourceString(PREFIX+"filename") + " " + location.file().name());
			lineNumberLabel.setText(PICLUtils.getResourceString(PREFIX+"linenumber") +  " " + Integer.toString(location.lineNumber()));
			viewInfoLabel.setText(PICLUtils.getResourceString(PREFIX+"viewtype") + " " + viewInfo.name());
			threadNameLabel.setText(PICLUtils.getResourceString(PREFIX+"threadname") + " " + thread.getName());
		} catch (Exception e) {
		}
	}


	protected void okPressed() {
		String expression = expressionInput.getText().trim();

		if (thread == null || !(thread instanceof PICLThread) || thread.isTerminated()) {
			return;
		}

		int lineNum = ((PICLThread)thread).getLocation(((PICLThread)thread).getViewInformation()).lineNumber();

		//get the resource (project) and viewinformation associated with the thread
		IResource inputResource = null;
		ViewInformation viewInfo = null;

		ISourceLocator sourceLocator = thread.getLaunch().getSourceLocator();
		if (sourceLocator instanceof WorkspaceSourceLocator) {
			WorkspaceSourceLocator wslocator = (WorkspaceSourceLocator) sourceLocator;
			inputResource  = wslocator.getHomeProject();
		}
		if (inputResource == null) {
			return;
		}

		// create a marker on the resource and set the line number and file name where the expression is to be evaluated
		IMarker monitorMarker = null;
		try {
			monitorMarker = inputResource.createMarker(IPICLDebugConstants.PICL_MONITORED_EXPRESSION);
			monitorMarker.setAttribute(IMarker.LINE_NUMBER, lineNum);
			monitorMarker.setAttribute(IPICLDebugConstants.SOURCE_FILE_NAME, thread.getLocation(thread.getViewInformation()).file().name());
		} catch (Exception e) {
			return;
		}

		//open a new view if necessary
		IWorkbenchPage p= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
		if (p == null) { return; }
		AbstractDebugView view = null;
		if (monitorTypeIsStorageButton.getSelection()) {
			view= (StorageView) p.findView("com.ibm.debug.pdt.ui.StorageView");
		} else {
			view= (MonitorView) p.findView("com.ibm.debug.pdt.ui.MonitorView");
		}
		if (view == null) {
			try {
				IWorkbenchPart activePart= p.getActivePart();
				if (monitorTypeIsStorageButton.getSelection()) {
					view= (StorageView) p.showView("com.ibm.debug.pdt.ui.StorageView");
				} else {
					view= (MonitorView) p.showView("com.ibm.debug.pdt.ui.MonitorView");
				}
				p.activate(activePart);
			} catch (PartInitException e) {
				//DebugUIUtils.logError(e);
				return;
			}
		}
		p.bringToTop(view);


		try {
			if (monitorTypeIsStorageButton.getSelection()) {
				((PICLThread)thread).monitorStorage(monitorMarker, expression, viewInfo);
			} else {
				((PICLThread)thread).monitorExpression(monitorMarker, expression, viewInfo);
			}
		} catch (PICLException pe) {
			int indexOfAmpersand = expression.indexOf("&", 0);
			while (indexOfAmpersand >= 0) {;
				expression = expression.substring(0, indexOfAmpersand) + "&" + expression.substring(indexOfAmpersand, expression.length());
				indexOfAmpersand += 2;
				indexOfAmpersand = expression.indexOf("&", indexOfAmpersand);
			}
			MessageDialog.openError(null, PICLUtils.getResourceString(PREFIX+"evaluationfailed"), PICLUtils.getResourceString(PREFIX+"expression") + " \"" + expression + "\" " + PICLUtils.getResourceString(PREFIX+"couldnotbeevaluated") + " \"" + ((PICLThread)thread).getLabel(true) + "\"");
			return;
		}

		super.okPressed();
	}


	private SelectionListener fListListener= new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
		}

		public void widgetDefaultSelected(SelectionEvent event) {
				okPressed();
		}
	};
}
