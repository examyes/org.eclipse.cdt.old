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
package org.eclipse.cdt.internal.pdom.dom;

import java.io.IOException;

import org.eclipse.cdt.internal.pdom.core.PDOMDatabase;
import org.eclipse.cdt.internal.pdom.db.Database;
import org.eclipse.cdt.internal.pdom.db.StringBTree;
import org.eclipse.cdt.pdom.core.PDOMCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This is a reference counted string used to eliminate duplication
 * of names.
 * 
 * @author Doug Schaefer
 */
public class PDOMString {

	private PDOMDatabase pdom;
	private int record;

	public static final int REF_OFFSET = 0;
	public static final int STRING_OFFSET = Database.INT_SIZE;

	public static PDOMString insert(PDOMDatabase pdom, String string) throws CoreException {
		try {
			StringBTree index = pdom.getStringIndex();
			Database db = pdom.getDB();
			int record = index.find(string);
			if (record == 0) {
				record = db.malloc(STRING_OFFSET + (string.length() + 1) * Database.CHAR_SIZE);
				db.putInt(record + REF_OFFSET, 0);
				db.putString(record + STRING_OFFSET, string);
				index.insert(record);
			} else {
				// Increment reference count
				db.putInt(record + REF_OFFSET, db.getInt(record + REF_OFFSET) + 1);
			}
			return new PDOMString(pdom, record);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to allocate string", e));
		}
	}

	public static PDOMString find(PDOMDatabase pdom, String string) throws CoreException {
		try {
			StringBTree index = pdom.getStringIndex(); 
			int record = index.find(string);
			return (record != 0) ? new PDOMString(pdom, record) : null;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to find string", e));
		}
	}
	
	private PDOMString(PDOMDatabase pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}
	
	public int getRecord() {
		return record;
	}
	
	public void free() throws CoreException {
		try {
			Database db = pdom.getDB();
			int refCount = db.getInt(record + REF_OFFSET);
			if (refCount == 0)
				db.free(record);
			else
				db.putInt(record + REF_OFFSET, refCount - 1);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to free string", e));
		}
	}
	
}
