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
package org.eclipse.cdt.internal.pdom.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.pdom.core.PDOMCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class SQLPDOM implements IPDOM {
	
	private static final QualifiedName dbNameProperty
		= new QualifiedName(PDOMCorePlugin.ID, "dbName"); //$NON-NLS-1$

	private final String baseURL;
	
	private Connection conn;
	
	public SQLPDOM(IProject project) throws CoreException {
		// Load up Derby
		// TODO allow for other SQL drivers
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		} catch (ClassNotFoundException e) {
			throw new CoreException(new Status(IStatus.ERROR, PDOMCorePlugin.ID, 0, "Failed to load Derby", e));
		}
		
		String dbName = project.getPersistentProperty(dbNameProperty);
		if (dbName == null) {
			dbName = "DB_" + project.getName() + "_" + System.currentTimeMillis();
			project.setPersistentProperty(dbNameProperty, dbName);
		}
		
		baseURL = "jdbc:derby:" + PDOMCorePlugin.getDefault().getStateLocation().toString()	+ "/" + dbName;
		
		try {
			conn = DriverManager.getConnection(baseURL);
		} catch (SQLException e) {
			// try to create it
			try {
				conn = DriverManager.getConnection(baseURL + ";create=true");
			} catch (SQLException e2) {
				// nope
				throw new CoreException(new Status(IStatus.ERROR, PDOMCorePlugin.ID, 0, "Failed to load database", e2));
			}
		}
	}
	
}
