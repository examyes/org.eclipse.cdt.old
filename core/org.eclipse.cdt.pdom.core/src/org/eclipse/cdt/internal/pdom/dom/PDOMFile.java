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
 * Represents a file containing names.
 * 
 * @author Doug Schaefer
 *
 */
public class PDOMFile {

	private PDOMDatabase pdom;
	private int record;
	
	private static final int FIRST_NAME_OFFSET = 0;
	private static final int FILE_NAME_OFFSET = Database.INT_SIZE;
	
	public static final int KEY_OFFSET = FILE_NAME_OFFSET;

	public static PDOMFile insert(PDOMDatabase pdom, String filename) throws IOException {
		StringBTree index = pdom.getFileIndex();
		Database db = pdom.getDB();
		int record = index.find(filename);
		if (record == 0) {
			record = db.malloc(FILE_NAME_OFFSET + (filename.length() + 1) * Database.CHAR_SIZE);
			db.putInt(record + FIRST_NAME_OFFSET, 0);
			db.putString(record + FILE_NAME_OFFSET, filename);
			index.insert(record);
		}
		return new PDOMFile(pdom, record);
	}

	public static PDOMFile find(PDOMDatabase pdom, String filename) throws IOException {
		StringBTree index = pdom.getFileIndex(); 
		int record = index.find(filename);
		return (record != 0) ? new PDOMFile(pdom, record) : null;
	}
	
	public PDOMFile(PDOMDatabase pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}
	
	public int getRecord() {
		return record;
	}
	
	public String getFileName() throws IOException {
		return pdom.getDB().getString(record + FILE_NAME_OFFSET);
	}
	
	public int getFirstName() throws IOException {
		return pdom.getDB().getInt(record + FIRST_NAME_OFFSET);
	}

	public void setFirstName(int firstName) throws IOException {
		pdom.getDB().putInt(record + FIRST_NAME_OFFSET, firstName);
	}
	
	public void free() throws CoreException {
		try {
			pdom.getDB().free(record);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to free string", e));
		}
	}
	
}
