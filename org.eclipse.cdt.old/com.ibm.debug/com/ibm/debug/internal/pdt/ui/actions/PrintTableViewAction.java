package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/PrintTableViewAction.java, eclipse, eclipse-dev, 20011128
// Version 1.2 (last modified 11/28/01 16:01:26)
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
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;

public class PrintTableViewAction extends Action {
	protected static final String PREFIX= "PrintViewAction.";
	protected TableViewer fViewer;
	protected String printJobTitle;
	
	
	public PrintTableViewAction(TableViewer viewer, String pJobTitle) {
		super(PICLUtils.getResourceString(PREFIX+"label"));
		setToolTipText(PICLUtils.getResourceString(PREFIX+"tooltip"));
		fViewer= viewer;
		printJobTitle = pJobTitle;
		WorkbenchHelp.setHelp(this, new Object[] { PICLUtils.getHelpResourceString("PrintViewAction") });
	}
	
	/*
	 * draws a Table to a GC which has been initialized with a Printer.
	 * startJob() and startPage() must be called before printTable(...),
	 * and endPage() and endJob() must be called after printTable(...).
	 */
	private void printTable(TableItem[] itemList, GC printGC, Printer printer) {

		String tableContents = new String();
		int numColumns = ((Table)fViewer.getControl()).getColumnCount();
		ITableLabelProvider labelProvider = (ITableLabelProvider)fViewer.getLabelProvider();		
		TableColumn columns[] = ((Table)fViewer.getControl()).getColumns();
		int lineNum = 1;
		
		//get the column headers
		for (int k=0; k < numColumns; k++) {
			tableContents += "  " + columns[k].getText();
		}
		printGC.drawString(tableContents, 10, 10+(lineNum*printGC.getFontMetrics().getHeight()));

		//for all items in the table
		for (int i=0; i < itemList.length; i++) {
			tableContents = "";
			//print all columns for this row
			for (int j=0; j < numColumns; j++) {
				tableContents += "  " + labelProvider.getColumnText(itemList[i].getData(), j);
			}
			printGC.drawString(tableContents, 10, 10+(lineNum*printGC.getFontMetrics().getHeight()));
			lineNum++;

			// if we've run over the end of a page, start a new one
			if (20+lineNum*printGC.getFontMetrics().getHeight() > printer.getClientArea().height) {
				lineNum=0;
				printer.endPage();
				printer.startPage();
				//print column headers again
				for (int k=0; k < numColumns; k++) {
					tableContents += columns[k].getText();
				}
				printGC.drawString(tableContents, 10, 10+(lineNum*printGC.getFontMetrics().getHeight()));
			}
		}
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
		TableItem[] tableItems = ((Table)fViewer.getControl()).getItems();

		printer.startJob(printJobTitle);	// start the print job and assign it a title
		printer.startPage();					// start the first page
		printTable(tableItems, gc, printer);// print all rows of the table
		printer.endPage();					// end the last page
		printer.endJob();						// end the print job
		gc.dispose();
		printer.dispose();
		setChecked(false);	}
}
