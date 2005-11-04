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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.pdom.core.PDOMCorePlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class SQLPDOMCodeReaderFactory implements ICodeReaderFactory {

	SQLPDOM pdom;
	List workingCopies;
	
	public SQLPDOMCodeReaderFactory(SQLPDOM pdom) {
		this.pdom = pdom;
	}

	public SQLPDOMCodeReaderFactory(SQLPDOM pdom, IWorkingCopy workingCopy) {
		this.pdom = pdom;
		if (workingCopies == null)
			workingCopies = new ArrayList(1);
		workingCopies.add(workingCopy);
	}
	
	public int getUniqueIdentifier() {
		return 0;
	}

	public CodeReader createCodeReaderForTranslationUnit(String path) {
		return ParserUtil.createReader(path,
				workingCopies == null ? null : workingCopies.iterator());
	}

	public CodeReader createCodeReaderForInclusion(String path) {
		try {
			try {
				path = new File(path).getCanonicalPath();
			} catch (IOException e) {
				// ignore and use the path we were passed in
			}
			if (pdom.getFileId(path, false) != 0)
				return null;
		} catch (CoreException e) {
			PDOMCorePlugin.log(e);
		}
		return ParserUtil.createReader(path, null);
	}

	public ICodeReaderCache getCodeReaderCache() {
		// No cache
		return null;
	}

}
