package org.eclipse.cdt.internal.pdom.dom;

import java.sql.SQLException;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.pdom.core.SQLPDOM;

public class SQLPDOMName implements IASTName {

	public char[] name;
	SQLPDOMFileLocation fileLocation;
	
	public SQLPDOMName(SQLPDOM pdom, IASTName name) throws SQLException {
		this.name = name.toCharArray();
		fileLocation = new SQLPDOMFileLocation(pdom, name.getFileLocation());
	}
	
	public IBinding resolveBinding() {
		throw new SQLPDOMNotImplementedError();
	}

	public IBinding getBinding() {
		throw new SQLPDOMNotImplementedError();
	}

	public void setBinding(IBinding binding) {
		throw new SQLPDOMNotImplementedError();
	}

	public IBinding[] resolvePrefix() {
		throw new SQLPDOMNotImplementedError();
	}

	public char[] toCharArray() {
		return name;
	}

	public boolean isDeclaration() {
		throw new SQLPDOMNotImplementedError();
	}

	public boolean isReference() {
		throw new SQLPDOMNotImplementedError();
	}

	public boolean isDefinition() {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTTranslationUnit getTranslationUnit() {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTNodeLocation[] getNodeLocations() {
		throw new SQLPDOMNotImplementedError();
	}

	public IASTFileLocation getFileLocation() {
		return fileLocation;
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
