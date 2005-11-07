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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.pdom.core.PDOMDatabase;
import org.eclipse.cdt.internal.pdom.db.Database;
import org.eclipse.cdt.pdom.core.PDOMCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMBinding implements IBinding {

	protected final PDOMDatabase pdom;
	protected int record;
	
	private static final int STRING_REC_OFFSET = 0 * Database.INT_SIZE;
	private static final int FIRST_DECL_OFFSET = 1 * Database.INT_SIZE;
	private static final int FIRST_DEF_OFFSET = 2 * Database.INT_SIZE;
	private static final int FIRST_REF_OFFSET = 3 * Database.INT_SIZE;
	
	public static final int KEY_OFFSET = STRING_REC_OFFSET;
	
	public PDOMBinding(PDOMDatabase pdom, IASTName name, IBinding binding) throws CoreException {
		try {
			this.pdom = pdom;
			
			String namestr = new String(name.toCharArray());
			
			int stringRecord = 0;
			PDOMString string = PDOMString.find(pdom, namestr);
			if (string != null) {
				stringRecord = string.getRecord();
				record = pdom.getBindingIndex().find(stringRecord);
			}
			
			if (record == 0) {
				Database db = pdom.getDB();
				record = db.malloc(getRecordSize());

				if (stringRecord == 0)
					stringRecord = PDOMString.insert(pdom, namestr).getRecord();
				
				db.putInt(record + STRING_REC_OFFSET, stringRecord);
				pdom.getBindingIndex().insert(record);
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to allocate binding", e));
		}
	}

	public PDOMBinding(PDOMDatabase pdom, int bindingRecord) {
		this.pdom = pdom;
		this.record = bindingRecord;
	}
	
	protected int getRecordSize() {
		return 4 * Database.INT_SIZE;
	}

	public int getRecord() {
		return record;
	}

	public void addDeclaration(PDOMName name) throws IOException {
		PDOMName firstDeclaration = getFirstDeclaration();
		if (firstDeclaration != null) {
			firstDeclaration.setPrevInBinding(name);
			name.setNextInBinding(firstDeclaration);
		}
		pdom.getDB().putInt(record + FIRST_DECL_OFFSET, name.getRecord());
	}
	
	public PDOMName getFirstDeclaration() throws IOException {
		int firstDeclRec = pdom.getDB().getInt(record + FIRST_DECL_OFFSET);
		return firstDeclRec != 0 ? new PDOMName(pdom, firstDeclRec) : null;
	}
	
	public String getName() {
		throw new PDOMNotImplementedError();
	}

	public char[] getNameCharArray() {
		try {
			return new PDOMString(pdom, pdom.getDB().getInt(record + STRING_REC_OFFSET)).getString();
		} catch (IOException e) {
			PDOMCorePlugin.log(new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to allocate binding", e)));
			return new char[0];
		}
	}

	public IScope getScope() throws DOMException {
		// TODO implement this
		return null;
	}

}
