package org.eclipse.cdt.internal.pdom.dom;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.pdom.core.SQLPDOM;
import org.eclipse.core.runtime.CoreException;

public class SQLPDOMName implements IASTName {

	private char[] name;
	private SQLPDOMFileLocation fileLocation;
	private SQLPDOMBinding binding;
	
	
	public static final int R_UNKNOWN = 0;
	public static final int R_DECLARATION = 1;
	public static final int R_REFERENCE = 2;
	public static final int R_DEFINITION = 4;
	private int role;
	
	public SQLPDOMName(SQLPDOM pdom, IASTName name, SQLPDOMBinding binding) throws CoreException {
		this.name = name.toCharArray();
		this.binding = binding;
		fileLocation = new SQLPDOMFileLocation(pdom, name.getFileLocation());
		
		if (name.isDefinition())
			role = R_DEFINITION;
		else if (name.isDeclaration())
			role = R_DECLARATION;
		else if (name.isReference())
			role = R_REFERENCE;

		pdom.addName(this);
	}

	public SQLPDOMName(int fileId,
					   String fileName,
					   int offset,
					   int length,
					   int role,
					   SQLPDOMBinding binding) {
		fileLocation = new SQLPDOMFileLocation(fileId, fileName, offset, length);
		this.role = role;
		this.binding = binding;
		name = binding.getNameCharArray();
	}
			
	public int getNameId() {
		return binding.getNameId(); 
	}
	
	public int getBindingId() {
		return binding.getId();
	}
	
	public IBinding resolveBinding() {
		return binding;
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

	public int getRole() {
		return role;
	}
	
	public boolean isDeclaration() {
		return role == R_DECLARATION;
	}

	public boolean isReference() {
		return role == R_REFERENCE;
	}

	public boolean isDefinition() {
		return role == R_DEFINITION;
	}

	public IASTTranslationUnit getTranslationUnit() {
		return new SQLPDOMTranslationUnit();
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
