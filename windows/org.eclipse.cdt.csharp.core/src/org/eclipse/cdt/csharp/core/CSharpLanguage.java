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

	private static final String ID = Activator.PLUGIN_ID + ".cSharp";
	
	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu) {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTTranslationUnit getASTTranslationUnit(ITranslationUnit file,
			int style) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTTranslationUnit getASTTranslationUnit(ITranslationUnit file,
			ICodeReaderFactory codeReaderFactory, int style)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTCompletionNode getCompletionNode(IWorkingCopy workingCopy,
			int offset) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getId() {
		return ID;
	}

	public IASTName[] getSelectedNames(IASTTranslationUnit ast, int start,
			int length) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

}
