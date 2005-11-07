/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.pdom.ui.popup.actions;

import java.io.IOException;

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.PDOM;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.pdom.core.PDOMDatabase;
import org.eclipse.cdt.internal.pdom.db.BTree;
import org.eclipse.cdt.internal.pdom.db.Database;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Doug Schaefer
 *
 */
public class DumpStats implements IObjectActionDelegate {

	private ISelection selection;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		try {
			if (!(selection instanceof IStructuredSelection))
				return;
			
			StringBuffer buff = new StringBuffer();
			Object[] objs = ((IStructuredSelection)selection).toArray();
			for (int i = 0; i < objs.length; ++i) {
				if (!(objs[i] instanceof ICProject))
					continue;
	
				IProject project = ((ICProject)objs[i]).getProject();
				buff.append("Stats for project: ");
				buff.append(project.getName());
				buff.append('\n');
				
				IPDOM pdom = PDOM.getPDOM(((ICProject)objs[i]).getProject());
				if (!(pdom instanceof PDOMDatabase)) {
					buff.append("    Not an instance of PDOMDatabase\n");
					continue;
				}
				
				PDOMDatabase mypdom = (PDOMDatabase)pdom;
				
				buff.append("    nameCount = ");
				buff.append(mypdom.getNameCount());
				buff.append('\n');
				
				Database db = mypdom.getDB();
				
				buff.append("    numChunks = ");
				buff.append(db.getNumChunks());
				buff.append('\n');
				
				BTree fileIndex = mypdom.getFileIndex();
				buff.append("    File Index\n");
				
				buff.append("        height = ");
				buff.append(fileIndex.getHeight());
				buff.append('\n');
				
				buff.append("        nodeCount = ");
				buff.append(fileIndex.getNodeCount());
				buff.append('\n');
				
				buff.append("        recordCount = ");
				buff.append(fileIndex.getRecordCount());
				buff.append('\n');
				
				BTree stringIndex = mypdom.getStringIndex();
				buff.append("    String Index\n");
				
				buff.append("        height = ");
				buff.append(stringIndex.getHeight());
				buff.append('\n');
				
				buff.append("        nodeCount = ");
				buff.append(stringIndex.getNodeCount());
				buff.append('\n');
				
				buff.append("        recordCount = ");
				buff.append(stringIndex.getRecordCount());
				buff.append('\n');
			}
			
			MessageDialog.openInformation(null, "PDOM Info", buff.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
