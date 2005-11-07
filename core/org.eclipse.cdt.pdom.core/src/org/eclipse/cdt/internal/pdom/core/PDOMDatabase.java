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

import java.io.IOException;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.pdom.db.Database;
import org.eclipse.cdt.internal.pdom.db.StringBTree;
import org.eclipse.cdt.internal.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.pdom.dom.PDOMString;
import org.eclipse.cdt.pdom.core.PDOMCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;


/**
 * The PDOM Database.
 * 
 * @author Doug Schaefer
 */
public class PDOMDatabase implements IPDOM {

	private final IPath dbPath;
	private final Database db;
	private int nameCount;
	
	private static final int VERSION = 0;
	
	public static final int STRING_INDEX = Database.DATA_AREA;
	private StringBTree stringIndex;
	
	public static final int FILE_INDEX = Database.DATA_AREA + Database.INT_SIZE;
	private StringBTree fileIndex;

	private static final QualifiedName dbNameProperty
		= new QualifiedName(PDOMCorePlugin.ID, "dbName"); //$NON-NLS-1$

	public PDOMDatabase(IProject project, PDOMManager manager) throws CoreException {
		String dbName = project.getPersistentProperty(dbNameProperty);
		if (dbName == null) {
			dbName = project.getName() + "_"
					+ System.currentTimeMillis() + ".pdom";
			project.setPersistentProperty(dbNameProperty, dbName);
		}
		
		dbPath = PDOMCorePlugin.getDefault().getStateLocation().append(dbName);
		
		try {
			db = new Database(dbPath.toOSString(), VERSION);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PDOMCorePlugin.ID, 0, "Failed to create database", e));
		}
	}

	public Database getDB() {
		return db;
	}

	public StringBTree getStringIndex() {
		if (stringIndex == null)
			stringIndex = new StringBTree(db, STRING_INDEX, PDOMString.STRING_OFFSET);
		return stringIndex;
	}
	
	public StringBTree getFileIndex() {
		if (fileIndex == null)
			fileIndex = new StringBTree(db, FILE_INDEX, PDOMFile.FILE_NAME_OFFSET);
		return fileIndex;
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
					if (name.toCharArray().length > 0)
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
					if (name.toCharArray().length > 0)
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
			new PDOMName(this, name);
			++nameCount;
		} catch (CoreException e) {
			PDOMCorePlugin.log(e);
		}
	}
	
	public void removeSymbols(IASTTranslationUnit ast) {
		
	}
	
	public int getNameCount() {
		return nameCount;
	}
	
	public void delete() throws CoreException {
		// TODO Auto-generated method stub
		
	}

	public ICodeReaderFactory getCodeReaderFactory() {
		return new PDOMCodeReaderFactory(this);
	}

	public ICodeReaderFactory getCodeReaderFactory(IWorkingCopy root) {
		return new PDOMCodeReaderFactory(this, root);
	}

	public IASTName[] getDeclarations(IBinding binding) {
		return new IASTName[0];
	}

	public IBinding resolveBinding(IASTName name) {
		return null;
	}

	public IBinding[] resolvePrefix(IASTName name) {
		return null;
	}
	
	
}
