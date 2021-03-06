/**
 * 
 */
package org.eclipse.cdt.csharp.core;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.runtime.CoreException;

/**
 * @author DSchaefer
 *
 */
public class CSharpLanguage extends AbstractLanguage {

	private static final String ID = Activator.PLUGIN_ID + ".cSharp";
	
	public String getName() {
		return "C#";
	}
	
	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu) {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTCompletionNode getCompletionNode(CodeReader reader, IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index, IParserLogService log, int offset) throws CoreException {
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
		return super.getAdapter(adapter);
	}

	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index, IParserLogService log) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
