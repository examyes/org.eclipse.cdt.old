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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.internal.pdom.core.SQLPDOM;

/**
 * @author Doug Schaefer
 *
 */
public class SQLPDOMFileLocation implements IASTFileLocation {

	private int offset;
	private int length;
	
	private String fileName;
	private int fileId;
	
	private static PreparedStatement getFileFromNameStmt;
	private static PreparedStatement insertFileStmt;
	
	/**
	 * Create the PDOM version of the file location.
	 *  
	 * @param pdom
	 * @param location
	 */
	public SQLPDOMFileLocation(SQLPDOM pdom, IASTFileLocation location) throws SQLException {
		offset = location.getNodeOffset();
		length = location.getNodeLength();
		fileName = location.getFileName();

		// Get the File id
		Connection conn = pdom.getConnection();
		synchronized (conn) {
			if (getFileFromNameStmt == null) {
				getFileFromNameStmt
					= conn.prepareStatement("SELECT Id FROM File WHERE name = ?");
			}
		}
		
		ResultSet rs;
		synchronized (getFileFromNameStmt) {
			getFileFromNameStmt.setString(1, fileName);
			rs = getFileFromNameStmt.executeQuery();
		}
		
		// if record exists, setup from there
		if (rs.next()) {
			fileId = rs.getInt(1);
		} else {
			// else create the record
			synchronized (conn) {
				if (insertFileStmt == null) {
					insertFileStmt
						= conn.prepareStatement("INSERT INTO File(name) VALUES (?)",
								Statement.RETURN_GENERATED_KEYS);
				}
			}
			
			synchronized (insertFileStmt) {
				insertFileStmt.setString(1, fileName);
				rs = insertFileStmt.executeQuery();
			}
			
			if (rs.next()) {
				fileId = rs.getInt(1);
			} else {
				// TODO throw an exception or something...
			}
		}		
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
