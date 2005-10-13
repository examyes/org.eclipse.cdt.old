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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.pdom.dom.SQLPDOMBinding;
import org.eclipse.cdt.internal.pdom.dom.SQLPDOMCVariable;
import org.eclipse.cdt.internal.pdom.dom.SQLPDOMFileLocation;
import org.eclipse.cdt.internal.pdom.dom.SQLPDOMName;
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

	private static final QualifiedName dbNameProperty = new QualifiedName(
			PDOMCorePlugin.ID, "dbName"); //$NON-NLS-1$

	private final String baseURL;

	private Connection connection;

	public SQLPDOM(IProject project) throws CoreException {
		// Load up Derby
		// TODO allow for other SQL drivers
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		} catch (ClassNotFoundException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to load Derby", e));
		}

		String dbName = project.getPersistentProperty(dbNameProperty);
		if (dbName == null) {
			dbName = "DB_" + project.getName() + "_"
					+ System.currentTimeMillis();
			project.setPersistentProperty(dbNameProperty, dbName);
		}

		baseURL = "jdbc:derby:"
				+ PDOMCorePlugin.getDefault().getStateLocation().toString()
				+ "/" + dbName;

		try {
			connection = DriverManager.getConnection(baseURL);
		} catch (SQLException e) {
			// try to create it
			try {
				setupDatabase(dbName);
				connection = DriverManager.getConnection(baseURL);
			} catch (SQLException e2) {
				// nope
				throw new CoreException(new Status(IStatus.ERROR,
						PDOMCorePlugin.ID, 0, "Failed to load database", e2));
			}
		}
	}

	private void setupDatabase(String dbName) throws CoreException {
		try {
			IPath targetPath = PDOMCorePlugin.getDefault().getStateLocation()
					.append(dbName);
			targetPath.toFile().mkdir();

			URL zipURL = Platform.asLocalURL(Platform.find(PDOMCorePlugin
					.getDefault().getBundle(), new Path("db.zip")));
			IPath zipPath = new Path(zipURL.getPath());
			ZipFile zipFile = new ZipFile(zipPath.toFile());
			Enumeration entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				String entryTarget = targetPath.append(entry.getName())
						.toOSString();
				if (entry.isDirectory()) {
					new File(entryTarget).mkdir();
				} else {
					InputStream in = zipFile.getInputStream(entry);
					OutputStream out = new BufferedOutputStream(
							new FileOutputStream(entryTarget));
					byte[] buffer = new byte[1024];
					int len;

					while ((len = in.read(buffer)) >= 0)
						out.write(buffer, 0, len);

					in.close();
					out.close();
				}
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to unzip database template",
					e));
		}
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
					shouldVisitDeclarations = true;
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
					shouldVisitDeclarations = true;
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
		try {
			// Figure out the binding id for the name
			SQLPDOMBinding pdomBinding = SQLPDOMBinding.create(this, name);
			if (pdomBinding == null)
				// Not a persistable binding, skip it
				return;

			new SQLPDOMName(this, name, pdomBinding);
		} catch (CoreException e) {
			// Need to log this eventually
			System.err.println(e.getMessage());
		}
	}

	private PreparedStatement getFileFromNameStmt;
	private PreparedStatement insertFileStmt;

	public synchronized int getFileId(String fileName) throws CoreException {
		try {
			if (getFileFromNameStmt == null) {
				getFileFromNameStmt = connection
						.prepareStatement("select id from Files where name = ?");
			}

			getFileFromNameStmt.setString(1, fileName);
			ResultSet rs = getFileFromNameStmt.executeQuery();

			// if record exists, setup from there
			int fileId;
			if (rs.next()) {
				fileId = rs.getInt(1);
			} else {
				// else create the record
				if (insertFileStmt == null) {
					insertFileStmt = connection.prepareStatement(
							"insert into Files(name) values (?)",
							Statement.RETURN_GENERATED_KEYS);
				}

				insertFileStmt.setString(1, fileName);
				insertFileStmt.executeUpdate();
				rs = insertFileStmt.getGeneratedKeys();

				if (rs.next()) {
					fileId = rs.getInt(1);
				} else {
					throw new CoreException(new Status(IStatus.ERROR,
							PDOMCorePlugin.ID, 0, "Failed to get fileId", null));
				}
			}
			rs.close();
			return fileId;
		} catch (SQLException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to get fileId", e));
		}
	}

	private PreparedStatement getFileNameStmt;
	
	public String getFileName(int fileId) throws CoreException {
		try {
			if (getFileNameStmt == null) {
				getFileNameStmt = connection.prepareStatement(
						"select name from Files where id = ?");
			}
			
			getFileNameStmt.setInt(1, fileId);
			ResultSet rs = getFileNameStmt.executeQuery();
			if (rs.next())
				return rs.getString(1);
			else
				return null;
		} catch (SQLException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to get fileName", e));
		}
	}
	
	private PreparedStatement getStringStmt;
	private PreparedStatement insertStringStmt;

	public synchronized int getStringId(String string, boolean add)
			throws CoreException {
		try {
			if (getStringStmt == null) {
				getStringStmt = connection
						.prepareStatement("select id from Strings where str = ?");
			}

			getStringStmt.setString(1, string);
			ResultSet rs = getStringStmt.executeQuery();

			// if record exists, setup from there
			int stringId;
			if (rs.next()) {
				stringId = rs.getInt(1);
			} else if (add) {
				// else create the record
				if (insertStringStmt == null) {
					insertStringStmt = connection.prepareStatement(
							"insert into Strings(str) values (?)",
							Statement.RETURN_GENERATED_KEYS);
				}

				insertStringStmt.setString(1, string);
				insertStringStmt.executeUpdate();
				rs = insertStringStmt.getGeneratedKeys();

				if (rs.next()) {
					stringId = rs.getInt(1);
				} else {
					throw new CoreException(new Status(IStatus.ERROR,
							PDOMCorePlugin.ID, 0, "Failed to get stringId",
							null));
				}
			} else {
				stringId = 0;
			}
			rs.close();
			return stringId;
		} catch (SQLException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to get stringId", e));
		}
	}

	private PreparedStatement getBindingIdStmt;

	private PreparedStatement insertBindingStmt;

	public void addBinding(SQLPDOMBinding binding) throws CoreException {
		int nameId = binding.getNameId();
		int type = binding.getBindingType();

		try {
			if (getBindingIdStmt == null) {
				getBindingIdStmt = connection
						.prepareStatement("select id from Bindings where nameId = ? and type = ?");
			}

			getBindingIdStmt.setInt(1, nameId);
			getBindingIdStmt.setInt(2, type);
			ResultSet rs = getBindingIdStmt.executeQuery();

			// if record exists, setup from there
			int bindingId;
			if (rs.next()) {
				bindingId = rs.getInt(1);
			} else {
				// else create the record
				if (insertBindingStmt == null) {
					insertBindingStmt = connection.prepareStatement(
							"insert into Bindings(nameId, type) values (?, ?)",
							Statement.RETURN_GENERATED_KEYS);
				}

				insertBindingStmt.setInt(1, nameId);
				insertBindingStmt.setInt(2, type);
				insertBindingStmt.executeUpdate();
				rs = insertBindingStmt.getGeneratedKeys();

				if (rs.next()) {
					bindingId = rs.getInt(1);
				} else {
					throw new CoreException(new Status(IStatus.ERROR,
							PDOMCorePlugin.ID, 0, "Failed to get bindingId",
							null));
				}
			}
			rs.close();
			binding.setId(bindingId);
		} catch (SQLException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to get bindingId", e));
		}
	}

	private PreparedStatement insertNameStmt;

public void addName(SQLPDOMName name) throws CoreException {
		try {
			if (insertNameStmt == null) {
				insertNameStmt
					= connection.prepareStatement("insert into Names values (?, ?, ?, ?, ?, ?, ?, ?)");
			}
			
			SQLPDOMFileLocation fileloc = (SQLPDOMFileLocation)name.getFileLocation();
			
			insertNameStmt.setInt(1, name.getNameId());
			insertNameStmt.setInt(2, fileloc.getFileId());
			insertNameStmt.setInt(3, fileloc.getNodeOffset());
			insertNameStmt.setInt(4, fileloc.getNodeLength());
			insertNameStmt.setInt(5, name.isDeclaration() ? 1 : 0);
			insertNameStmt.setInt(6, name.isReference() ? 1 : 0);
			insertNameStmt.setInt(7, name.isDefinition() ? 1 : 0);
			insertNameStmt.setInt(8, name.getBindingId());
			insertNameStmt.executeUpdate();
		} catch (SQLException e) {
			throw new CoreException(new Status(IStatus.ERROR, PDOMCorePlugin.ID, 0, "Failed to add name", e));
		}
	}	private PreparedStatement getBindingStmt;

	public IBinding resolveBinding(IASTName name) throws CoreException {
		// resolve from pdom
		try {
			int nameId = getStringId(new String(name.toCharArray()), false);
			if (nameId == 0)
				return null;

			if (getBindingStmt == null) {
				getBindingStmt = connection
						.prepareStatement("select id, type from Bindings where nameId = ?");
			}

			getBindingStmt.setInt(1, nameId);
			ResultSet rs = getBindingStmt.executeQuery();
			if (rs.next()) {
				// if there is more than on in the result set, we need
				// to check the context of the name to make sure the type
				// matches
				int bindingId = rs.getInt(1);
				int type = rs.getInt(2);

				switch (type) {
				case SQLPDOMBinding.B_CVARIABLE:
					return new SQLPDOMCVariable(bindingId, nameId, name.toCharArray());
				case SQLPDOMBinding.B_UNKNOWN:
					return new SQLPDOMBinding(bindingId, nameId, name.toCharArray());
				}
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to resolve binding", e));
		}
		return null;
	}

	private PreparedStatement getDeclarationsStmt;

	public IASTName[] getDeclarations(IBinding binding) throws CoreException {
		if (!(binding instanceof SQLPDOMBinding))
			// Not a pdom binding, so skip it
			return null;

		SQLPDOMBinding pdomBinding = (SQLPDOMBinding)binding;
		
		try {
			if (getDeclarationsStmt == null) {
				getDeclarationsStmt
					= connection.prepareStatement("select * from Names where isDecl = 1 and isDef = ? and bindingId = ?");
			}
			
			ArrayList names = new ArrayList();
			
			// first try defs
			getDeclarationsStmt.setInt(1, 1);
			getDeclarationsStmt.setInt(2, pdomBinding.getId());
			ResultSet rs = getDeclarationsStmt.executeQuery();
			while (rs.next()) {
				int fileId = rs.getInt(2);
				names.add(new SQLPDOMName(
					fileId,
					getFileName(fileId),
					rs.getInt(3),
					rs.getInt(4),
					rs.getInt(5) == 1,
					rs.getInt(6) == 1,
					rs.getInt(7) == 1,
					pdomBinding));
			}
			
			if (names.isEmpty()) {
				// none, let's try decls 
				getDeclarationsStmt.setInt(1, 0);
				getDeclarationsStmt.setInt(2, pdomBinding.getId());
				rs = getDeclarationsStmt.executeQuery();
				while (rs.next()) {
					int fileId = rs.getInt(2);
					names.add(new SQLPDOMName(
						fileId,
						getFileName(fileId),
						rs.getInt(3),
						rs.getInt(4),
						rs.getInt(5) == 1,
						rs.getInt(6) == 1,
						rs.getInt(7) == 1,
						pdomBinding));
				}
			}
			
			return (IASTName[])names.toArray(new IASTName[names.size()]);
		} catch (SQLException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to get declarations", e));
		}
	}

}
