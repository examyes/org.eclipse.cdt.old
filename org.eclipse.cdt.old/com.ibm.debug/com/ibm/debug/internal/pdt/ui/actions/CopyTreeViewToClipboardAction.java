package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/CopyTreeViewToClipboardAction.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:01:12)
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
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

public class CopyTreeViewToClipboardAction extends Action {
	protected static final String PREFIX= "CopyViewToClipboardAction.";
	protected StructuredViewer fViewer;


	public CopyTreeViewToClipboardAction(StructuredViewer viewer) {
		super(PICLUtils.getResourceString(PREFIX+"label"));
		setToolTipText(PICLUtils.getResourceString(PREFIX+"tooltip"));
		fViewer= viewer;

		WorkbenchHelp.setHelp(this, new Object[] { PICLUtils.getHelpResourceString("CopyViewToClipboardAction") });
	}


	private String concatenateTreeAsString(TreeItem[] itemList, int treeDepth) {
		if (itemList.length == 0) return "";
		String treeContents = new String();
		for (int i=0; i < itemList.length; i++) {
			if (itemList[i].getText().equals("")) {continue;}	// if the child is not expanded/visible, skip it
			for (int j=0; j<treeDepth; j++) {treeContents += "  ";}
			treeContents += itemList[i].getText() + System.getProperty("line.separator") + concatenateTreeAsString(itemList[i].getItems(), treeDepth+1);
		}
		return treeContents;
	}

	/**
	 * @see Action
	 */
	public void run() {

		Clipboard clip = new Clipboard(fViewer.getControl().getDisplay());
		TreeItem[] treeItems = ((Tree)fViewer.getControl()).getItems();
		String treeAsString = new String();
		treeAsString = concatenateTreeAsString(treeItems, 0);
		if (!treeAsString.equals("")) {
			TextTransfer plainTextTransfer = TextTransfer.getInstance();
			clip.setContents(new Object[] {treeAsString}, new Transfer[] {plainTextTransfer});
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
