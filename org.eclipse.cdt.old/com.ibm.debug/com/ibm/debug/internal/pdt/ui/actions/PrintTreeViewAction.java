package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/PrintTreeViewAction.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:01:13)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.internal.picl.PICLUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;

public class PrintTreeViewAction extends Action {
	protected static final String PREFIX= "PrintViewAction.";
	protected StructuredViewer fViewer;
	protected String printJobTitle;
	public PrintTreeViewAction(StructuredViewer viewer, String pJobTitle) {
		super(PICLUtils.getResourceString(PREFIX+"label"));
		setToolTipText(PICLUtils.getResourceString(PREFIX+"tooltip"));
		fViewer= viewer;
		printJobTitle = pJobTitle;

		WorkbenchHelp.setHelp(this, new Object[] { PICLUtils.getHelpResourceString("PrintViewAction") });
	}


	/*
	 * draws a Tree to a GC which has been initialized with a Printer.
	 * startJob() and startPage() must be called before printTree(...),
	 * and endPage() and endJob() must be called after printTree(...).
	 */
	private int printTree(TreeItem[] itemList, int treeDepth, int lineNum, GC printGC, Printer printer) {
		if (itemList.length == 0) return lineNum;
		for (int i=0; i < itemList.length; i++) {
			if (itemList[i].getText().equals("")) {continue;}// if the child is NOT expanded/visible, skip it
			lineNum++;
			// if we've run over the end of a page, start a new one
			if (20+lineNum*printGC.getFontMetrics().getHeight() > printer.getClientArea().height) {
				lineNum=0;
				printer.endPage();
				printer.startPage();
			}
			printGC.drawString(itemList[i].getText(),
								10+(2*treeDepth*printGC.getFontMetrics().getAverageCharWidth()),
								10+(lineNum*printGC.getFontMetrics().getHeight()));
			lineNum = printTree(itemList[i].getItems(), treeDepth+1, lineNum, printGC, printer);
		}
		return lineNum;
	}

	/**
	 * @see Action
	 */
	public void run() {
		PrintDialog printDialog = new PrintDialog(fViewer.getControl().getDisplay().getActiveShell());
		PrinterData printerData = printDialog.open();	// pop up a system print dialog
		if (printerData == null) {setChecked(false); return;}
		Printer printer = new Printer(printerData);
		GC gc = new GC(printer);
		TreeItem[] treeItems = ((Tree)fViewer.getControl()).getItems();

		printer.startJob(printJobTitle);	// start the print job and assign it a title
		printer.startPage();					// start the first page
		printTree(treeItems, 0, 0, gc, printer);// print all nodes of the tree
		printer.endPage();					// end the last page
		printer.endJob();						// end the print job
		gc.dispose();
		printer.dispose();

		//valueChanged(isChecked());
		setChecked(false);
	}

	/**
	 * @see Action
	 */
	public void setChecked(boolean value) {
		super.setChecked(value);
		//valueChanged(value);
	}
/*
	private void valueChanged(boolean on) {
		ILabelProvider labelProvider= (ILabelProvider)fViewer.getLabelProvider();
		if (labelProvider instanceof IDebugModelPresentation) {
			IDebugModelPresentation debugLabelProvider= (IDebugModelPresentation)labelProvider;
			debugLabelProvider.setAttribute(IDebugModelPresentation.DISPLAY_VARIABLE_TYPE_NAMES, (on ? Boolean.TRUE : Boolean.FALSE));
			BusyIndicator.showWhile(fViewer.getControl().getDisplay(), new Runnable() {
				public void run() {
					fViewer.refresh();
				}
			});
		}
		setToolTipText(on ? DebugUIUtils.getResourceString(HIDE) : DebugUIUtils.getResourceString(SHOW));
	}
*/
}
