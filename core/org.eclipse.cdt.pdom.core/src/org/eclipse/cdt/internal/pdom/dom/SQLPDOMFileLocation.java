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

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.internal.pdom.core.SQLPDOM;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class SQLPDOMFileLocation implements IASTFileLocation {

	private int offset;
	private int length;
	private String fileName;
	private int fileId;
	
	/**
	 * Create the PDOM version of the file location.
	 *  
	 * @param pdom
	 * @param location
	 */
	public SQLPDOMFileLocation(SQLPDOM pdom, IASTFileLocation location) throws CoreException {
		offset = location.getNodeOffset();
		length = location.getNodeLength();
		fileName = location.getFileName();
		fileId = pdom.getFileId(fileName);
	}

	public SQLPDOMFileLocation(int fileId, String fileName, int offset, int length) {
		this.fileId = fileId;
		this.fileName = fileName;
		this.offset = offset;
		this.length = length;
	}
	
	public String getFileName() {
		return fileName;
	}

	public int getFileId() {
		return fileId;
	}
	
	public int getStartingLineNumber() {
		throw new SQLPDOMNotImplementedError();
	}

	public int getEndingLineNumber() {
		throw new SQLPDOMNotImplementedError();
	}

	public int getNodeOffset() {
		return offset;
	}

	public int getNodeLength() {
		return length;
	}

	public IASTFileLocation asFileLocation() {
		return this;
	}

}
