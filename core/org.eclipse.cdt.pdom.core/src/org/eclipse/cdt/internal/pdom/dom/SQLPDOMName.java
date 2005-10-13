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
	private boolean isDeclaration;
	private boolean isReference;
	private boolean isDefinition;
	
	public SQLPDOMName(SQLPDOM pdom, IASTName name, SQLPDOMBinding binding) throws CoreException {
		this.name = name.toCharArray();
		this.binding = binding;
		fileLocation = new SQLPDOMFileLocation(pdom, name.getFileLocation());
		
		isDeclaration = name.isDeclaration();
		isReference = name.isReference();
		isDefinition = name.isDefinition();

		pdom.addName(this);
	}

	public SQLPDOMName(int fileId,
					   String fileName,
					   int offset,
					   int length,
					   boolean isDeclaration,
					   boolean isReference,
					   boolean isDefinition,
					   SQLPDOMBinding binding) {
		fileLocation = new SQLPDOMFileLocation(fileId, fileName, offset, length);
		this.isDeclaration = isDeclaration;
		this.isReference = isReference;
		this.isDefinition = isDefinition;
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

	public boolean isDeclaration() {
		return isDeclaration;
	}

	public boolean isReference() {
		return isReference;
	}

	public boolean isDefinition() {
		return isDefinition;
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
