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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IParserConfiguration;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * @author Doug Schaefer
 * 
 * This class overrides the parser configuration. It combines the traditional
 * build information with the macro definitions captured by the PDOM.
 */
public class SQLPDOMParserConfiguration implements IParserConfiguration {

	private IProject project;
	private IFile file;
	
	private class ScannerInfo implements IScannerInfo {

		IScannerInfo baseInfo
			= CCorePlugin.getDefault().getScannerInfoProvider(project)
				.getScannerInformation(file);
				
		private HashMap symbols;

		public ScannerInfo(SQLPDOM pdom) {
			symbols = new HashMap();
			symbols.putAll(baseInfo.getDefinedSymbols());
			// Add in all the symbols from the pdom
		}
		
		public Map getDefinedSymbols() {
			return symbols;
		}
		
		public String[] getIncludePaths() {
			return baseInfo.getIncludePaths();
		}
	}
	
	private ScannerInfo scannerInfo;
	
	public SQLPDOMParserConfiguration(SQLPDOM pdom) {
		scannerInfo = new ScannerInfo(pdom);
	}
	
	public IScannerInfo getScannerInfo() {
		return scannerInfo;
	}

	public String getParserDialect() {
		// returning null is fine and results in the default behavior
		return null;
	}

}
