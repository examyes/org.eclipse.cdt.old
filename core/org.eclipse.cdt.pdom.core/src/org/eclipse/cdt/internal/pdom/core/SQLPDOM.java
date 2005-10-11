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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.pdom.dom.SQLPDOMBinding;
import org.eclipse.cdt.pdom.core.PDOMCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
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
	
	private Connection connection;
	
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
			connection = DriverManager.getConnection(baseURL);
		} catch (SQLException e) {
			// try to create it
			try {
				setupDatabase(dbName);
				connection = DriverManager.getConnection(baseURL);
			} catch (SQLException e2) {
				// nope
				throw new CoreException(new Status(IStatus.ERROR, PDOMCorePlugin.ID, 0, "Failed to load database", e2));
			}
		}
	}
	
	private void setupDatabase(String dbName) throws CoreException {
		try {
			IPath targetPath = PDOMCorePlugin.getDefault().getStateLocation().append(dbName);
			targetPath.toFile().mkdir();

			URL zipURL = Platform.asLocalURL(Platform.find(PDOMCorePlugin.getDefault().getBundle(), new Path("db.zip")));
			IPath zipPath = new Path(zipURL.getPath());
			ZipFile zipFile = new ZipFile(zipPath.toFile());
			Enumeration entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry)entries.nextElement();
				String entryTarget = targetPath.append(entry.getName()).toOSString();
				if (entry.isDirectory()) {
					new File(entryTarget).mkdir();
				} else {
					InputStream in = zipFile.getInputStream(entry);
					OutputStream out = new BufferedOutputStream(new FileOutputStream(entryTarget)); 
					byte[] buffer = new byte[1024];
					int len;
					
					while ((len = in.read(buffer)) >= 0)
						out.write(buffer, 0, len);
					
					in.close();
					out.close();
				}
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, PDOMCorePlugin.ID, 0, "Failed to unzip database template", e));
		}
	}

	private void createTables() throws SQLException {
		if (connection == null)
			return;

		Statement stmt = connection.createStatement();

		// Table: Strings
		// Contains all the identifier strings used as names
		// Referenced by the Names table and the Bindings table
		stmt.executeUpdate("CREATE TABLE Strings ("
				+ "id int GENERATED ALWAYS AS IDENTITY PRIMARY KEY"
				+ "string VARCHAR NOT NULL)");
		stmt.executeUpdate("CREATE INDEX StringIx on String (string)");

		// Table: File
		stmt.executeUpdate("CREATE TABLE File ("
				+ "id int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
				+ "name VARCHAR NOT NULL)");
		stmt.executeUpdate("CREATE INDEX FileNameIx on File (name)");
		
		// Table: Name
		stmt.executeUpdate("CREATE TABLE Name ("
				+ "name VARCHAR NOT NULL,"
				+ "fileId INT NOT NULL,"
				+ "offset INT NOT NULL,"
				+ "length INT NOT NULL,"
				+ "bindingId INT)");
		
		// Table: Binding
		stmt.executeUpdate("CREATE TABLE Binding ("
				+ "bindingId int GENERATED ALWAYS AS IDENTIT PRIMARY KEY"
				+ "type int NOT NULL");
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public void removeSymbols(ITranslationUnit tu) {
		// remove all symbols located in the tu's file
	}

	public void addSymbols(IASTTranslationUnit ast) {
		ParserLanguage language = ast.getParserLanguage();
		ASTVisitor visitor;
		if (language == ParserLanguage.C)
			visitor = new CASTVisitor() {
				{
					shouldVisitNames = true;
				}
				
				public int visit(IASTName name) {
					addSymbol(name);
					return PROCESS_CONTINUE;
				};
			};
		else if (language == ParserLanguage.CPP)
			visitor = new CPPASTVisitor() {
				{
					shouldVisitNames = true;
				}
				
				public int visit(IASTName name) {
					addSymbol(name);
					return PROCESS_CONTINUE;
				};
			};
		else
			return;
		
		ast.accept(visitor);
	}

	public void addSymbol(IASTName name) {
		// Figure out the binding id for the name
		IBinding binding = name.resolveBinding();
		int bindingId;
		
		if (binding instanceof IProblemBinding) {
			// This would be a reference to something that isn't in the AST or in the PDOM
			// Not sure what to do with it so we just skip this symbol
			return;
		} else if (binding instanceof SQLPDOMBinding) {
			// Ah, the binding is already in the PDOM, we can simply get the binding id
			// directly from it
			bindingId = ((SQLPDOMBinding)binding).getId();
		} else {
			// It's a DOM binding, need to create the PDOM version of it
			if (binding instanceof IVariable) {
				if (binding instanceof ICPPVariable) {
					
				} else {
					
				}
			}
		}
		
	}
	
}
