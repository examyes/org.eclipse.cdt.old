package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/CopyTableViewToClipboardAction.java, eclipse, eclipse-dev, 20011128
// Version 1.2 (last modified 11/28/01 16:01:24)
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
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

public class CopyTableViewToClipboardAction extends Action {
	protected static final String PREFIX= "CopyViewToClipboardAction.";
	protected TableViewer fViewer;


	public CopyTableViewToClipboardAction(TableViewer viewer) {
		super(PICLUtils.getResourceString(PREFIX+"label"));
		setToolTipText(PICLUtils.getResourceString(PREFIX+"tooltip"));
		fViewer= viewer;

		WorkbenchHelp.setHelp(this, new Object[] { PICLUtils.getHelpResourceString("CopyViewToClipboardAction") });
	}
	
	
	private String concatenateTableAsString(TableItem[] itemList) {
		if (itemList.length == 0) return null;

		String tableContents = new String();
		int numColumns = ((Table)fViewer.getControl()).getColumnCount();
		ITableLabelProvider labelProvider = (ITableLabelProvider)fViewer.getLabelProvider();		
		TableColumn columns[] = ((Table)fViewer.getControl()).getColumns();

		//get the column headers
		for (int k=0; k < numColumns; k++) {
			tableContents += columns[k].getText();
		}
		
		tableContents += System.getProperty("line.separator");
			
		//get the column contents from all the rows
		for (int i=0; i < itemList.length; i++) {
			for (int j=0; j < numColumns; j++) {
				tableContents += "  " + labelProvider.getColumnText(itemList[i].getData(), j);
			}
			tableContents += System.getProperty("line.separator");
		}
		return tableContents;
	}
	
	/**
	 * @see Action
	 */
	public void run() {
		
		Clipboard clip = new Clipboard(fViewer.getControl().getDisplay());
		TableItem[] tableItems = ((Table)fViewer.getControl()).getItems();
		String tableAsString = new String();
		tableAsString = concatenateTableAsString(tableItems);
		if (!tableAsString.equals("")) {
			TextTransfer plainTextTransfer = TextTransfer.getInstance();
			clip.setContents(new Object[] {tableAsString}, new Transfer[] {plainTextTransfer});
		}
		
		setChecked(false);
	}
	/**
	 * @see Action
	 */
	public void setChecked(boolean value) {
		super.setChecked(value);
	}
}
