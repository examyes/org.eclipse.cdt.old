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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
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
public class PDOMName implements IASTName, IASTFileLocation {

	private final PDOMDatabase pdom;
	private int record;
	
	private static final int FILE_REC_OFFSET = 0 * Database.INT_SIZE;
	private static final int FILE_PREV_OFFSET = 1 * Database.INT_SIZE;
	private static final int FILE_NEXT_OFFSET = 2 * Database.INT_SIZE;
	private static final int STRING_REC_OFFSET = 3 * Database.INT_SIZE;
	private static final int BINDING_REC_OFFET = 4 * Database.INT_SIZE;
	private static final int BINDING_PREV_OFFSET = 5 * Database.INT_SIZE;
	private static final int BINDING_NEXT_OFFSET = 6 * Database.INT_SIZE;
	
	private static final int RECORD_SIZE = 7 * Database.INT_SIZE;

	public PDOMName(PDOMDatabase pdom, IASTName name) throws CoreException {
		try {
			this.pdom = pdom;
			
			Database db = pdom.getDB();
			
			record = db.malloc(RECORD_SIZE);
			
			// Hook us up the the liked name list from file
			IASTFileLocation fileloc = name.getFileLocation();
			String filename = fileloc.getFileName();
			PDOMFile pdomFile = PDOMFile.insert(pdom, filename);
			db.putInt(record + FILE_REC_OFFSET, pdomFile.getRecord());
			int firstName = pdomFile.getFirstName();
			if (firstName != 0) {
				db.putInt(record + FILE_NEXT_OFFSET, firstName);
				db.putInt(firstName + FILE_PREV_OFFSET, record);
			}
			pdomFile.setFirstName(record);
			
			int stringRecord = PDOMString.insert(pdom, new String(name.toCharArray())).getRecord();
			db.putInt(record + STRING_REC_OFFSET, stringRecord);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to allocate name", e));
		}
	}
	
	public IBinding resolveBinding() {
		throw new PDOMNotImplementedError();
	}

	public IBinding getBinding() {
		throw new PDOMNotImplementedError();
	}

	public void setBinding(IBinding binding) {
		throw new PDOMNotImplementedError();
	}

	public IBinding[] resolvePrefix() {
		throw new PDOMNotImplementedError();
	}

	public char[] toCharArray() {
		throw new PDOMNotImplementedError();
	}

	public boolean isDeclaration() {
		throw new PDOMNotImplementedError();
	}

	public boolean isReference() {
		throw new PDOMNotImplementedError();
	}

	public boolean isDefinition() {
		throw new PDOMNotImplementedError();
	}

	public IASTTranslationUnit getTranslationUnit() {
		throw new PDOMNotImplementedError();
	}

	public IASTNodeLocation[] getNodeLocations() {
		throw new PDOMNotImplementedError();
	}

	public IASTFileLocation getFileLocation() {
		return this;
	}

	public String getContainingFilename() {
		throw new PDOMNotImplementedError();
	}

	public IASTNode getParent() {
		throw new PDOMNotImplementedError();
	}

	public void setParent(IASTNode node) {
		throw new PDOMNotImplementedError();
	}

	public ASTNodeProperty getPropertyInParent() {
		throw new PDOMNotImplementedError();
	}

	public void setPropertyInParent(ASTNodeProperty property) {
		throw new PDOMNotImplementedError();
	}

	public boolean accept(ASTVisitor visitor) {
		throw new PDOMNotImplementedError();
	}

	public String getRawSignature() {
		throw new PDOMNotImplementedError();
	}

	public int getEndingLineNumber() {
		throw new PDOMNotImplementedError();
	}

	public String getFileName() {
		throw new PDOMNotImplementedError();
	}

	public int getStartingLineNumber() {
		throw new PDOMNotImplementedError();
	}

	public IASTFileLocation asFileLocation() {
		throw new PDOMNotImplementedError();
	}

	public int getNodeLength() {
		throw new PDOMNotImplementedError();
	}

	public int getNodeOffset() {
		throw new PDOMNotImplementedError();
	}
	
}
