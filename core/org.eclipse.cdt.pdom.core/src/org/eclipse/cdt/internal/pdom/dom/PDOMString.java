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
import org.eclipse.cdt.internal.pdom.db.BTree;
import org.eclipse.cdt.internal.pdom.db.Database;
import org.eclipse.cdt.internal.pdom.db.StringComparator;
import org.eclipse.cdt.internal.pdom.db.StringVisitor;
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

	private static final int REF_OFFSET = 0;
	private static final int STRING_OFFSET = Database.INT_SIZE;
	
	public static PDOMString insert(PDOMDatabase pdom, String string) throws CoreException {
		try {
			BTree index = pdom.getStringIndex();
			Database db = pdom.getDB();
			PDOMString pdomString = find(pdom, string);
			if (pdomString == null) {
				int record = db.malloc(STRING_OFFSET + (string.length() + 1) * Database.CHAR_SIZE);
				db.putInt(record + REF_OFFSET, 0);
				db.putString(record + STRING_OFFSET, string);
				index.insert(record, new StringComparator(db, STRING_OFFSET));
				pdomString = new PDOMString(pdom, record);
			}
			return pdomString;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to allocate string", e));
		}
	}

	public static abstract class Visitor extends StringVisitor {
		public Visitor(Database db, String key) {
			super(db, STRING_OFFSET, key);
		}
	}
	
	public static class FindVisitor extends Visitor {
		int record;
		
		public FindVisitor(Database db, String key) {
			super(db, key); 
		}
		
		public boolean visit(int record) throws IOException {
			// just capture the first record
			this.record = record;
			return false;
		}
		
		public int getRecord() {
			return record;
		}
	}
	
	public static PDOMString find(PDOMDatabase pdom, String string) throws CoreException {
		try {
			BTree index = pdom.getStringIndex();
			FindVisitor visitor = new FindVisitor(pdom.getDB(), string);
			index.visit(visitor);
			int record = visitor.getRecord();
			return (record != 0) ? new PDOMString(pdom, record) : null;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to find string", e));
		}
	}
	
	public PDOMString(PDOMDatabase pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}
	
	public int getRecord() {
		return record;
	}
	
	public char[] getString() throws IOException {
		return pdom.getDB().getString(record + STRING_OFFSET).toCharArray();
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
