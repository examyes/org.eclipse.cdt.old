/**
 * 
 */
package org.eclipse.cdt.csharp.core;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.CoreException;

/**
 * @author DSchaefer
 *
 */
public class CSharpLanguage implements ILanguage {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ILanguage#createModelBuilder(org.eclipse.cdt.core.model.ITranslationUnit)
	 */
	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ILanguage#getASTTranslationUnit(org.eclipse.cdt.core.model.ITranslationUnit, int)
	 */
	public IASTTranslationUnit getASTTranslationUnit(ITranslationUnit file,
			int style) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ILanguage#getASTTranslationUnit(org.eclipse.cdt.core.model.ITranslationUnit, org.eclipse.cdt.core.dom.ICodeReaderFactory, int)
	 */
	public IASTTranslationUnit getASTTranslationUnit(ITranslationUnit file,
			ICodeReaderFactory codeReaderFactory, int style)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ILanguage#getCompletionNode(org.eclipse.cdt.core.model.IWorkingCopy, int)
	 */
	public ASTCompletionNode getCompletionNode(IWorkingCopy workingCopy,
			int offset) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ILanguage#getId()
	 */
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ILanguage#getSelectedNames(org.eclipse.cdt.core.dom.ast.IASTTranslationUnit, int, int)
	 */
	public IASTName[] getSelectedNames(IASTTranslationUnit ast, int start,
			int length) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

}
