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

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * @author Doug Schaefer
 * 
 * This is really a dummy translation unit that is necessary for names
 * to be valid.
 */
public class SQLPDOMTranslationUnit implements IASTTranslationUnit {

	public IASTDeclaration[] getDeclarations() {
		throw new SQLPDOMNotImplementedError();
	}

	public void addDeclaration(IASTDeclaration declaration) {
		throw new SQLPDOMNotImplementedError();
	}

	public IScope getScope() {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTName[] getDeclarations(IBinding binding) {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTName[] getDefinitions(IBinding binding) {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTName[] getReferences(IBinding binding) {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTNodeLocation[] getLocationInfo(int offset, int length) {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTNode selectNodeForLocation(String path, int offset, int length) {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTPreprocessorMacroDefinition[] getMacroDefinitions() {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTPreprocessorIncludeStatement[] getIncludeDirectives() {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTPreprocessorStatement[] getAllPreprocessorStatements() {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTProblem[] getPreprocessorProblems() {
		throw new SQLPDOMNotImplementedError();
	}

	public String getUnpreprocessedSignature(IASTNodeLocation[] locations) {
		throw new SQLPDOMNotImplementedError();
	}

	public String getFilePath() {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTFileLocation flattenLocationsToFile(
			IASTNodeLocation[] nodeLocations) {
		throw new SQLPDOMNotImplementedError();
	}

	public IDependencyTree getDependencyTree() {
		throw new SQLPDOMNotImplementedError();
	}

	public String getContainingFilename(int offset) {
		throw new SQLPDOMNotImplementedError();
	}

	public ParserLanguage getParserLanguage() {
		throw new SQLPDOMNotImplementedError();
	}

	public IPDOM getPDOM() {
		throw new SQLPDOMNotImplementedError();
	}

	public void setPDOM(IPDOM pdom) {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTTranslationUnit getTranslationUnit() {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTNodeLocation[] getNodeLocations() {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTFileLocation getFileLocation() {
		throw new SQLPDOMNotImplementedError();
	}

	public String getContainingFilename() {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTNode getParent() {
		throw new SQLPDOMNotImplementedError();
	}

	public void setParent(IASTNode node) {
		throw new SQLPDOMNotImplementedError();
	}

	public ASTNodeProperty getPropertyInParent() {
		throw new SQLPDOMNotImplementedError();
	}

	public void setPropertyInParent(ASTNodeProperty property) {
		throw new SQLPDOMNotImplementedError();
	}

	public boolean accept(ASTVisitor visitor) {
		throw new SQLPDOMNotImplementedError();
	}

	public String getRawSignature() {
		throw new SQLPDOMNotImplementedError();
	}

}
