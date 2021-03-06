/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.c99;

import java.util.ArrayList;
import java.util.List;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.parser.c99.C99ParseResult;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.C99Preprocessor;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.LocationResolver;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;
import org.eclipse.core.runtime.CoreException;


/**
 * Implementation of the ILanguage extension point, 
 * provides the ability to add LPG based languages to CDT.
 *
 * @author Mike Kucera
 */
public abstract class BaseExtensibleLanguage extends AbstractLanguage implements ILanguage, ICLanguageKeywords {
	
	// The parser is maintained as a singleton object because there
	// is a bad performance bottleneck in the constructor method.
	// This means this object isn't thread safe, so methods have been marked synchronized.
	private IParser parser = null;
	
	
	/**
	 * Returns a keyword map that will provide the information necessary
	 * to implement the methods in ICLanguageKeywords.
	 * 
	 * Can be overridden in subclasses to provide additional keywords
	 * for language extensions.
	 */
	protected abstract IKeywordMap getKeywordMap();
	
	
	/**
	 * Retrieve the token parser (runs after the preprocessor runs).
	 * 
	 * Can be overridden in subclasses to provide a different parser
	 * for a language extension.
	 */
	protected abstract IParser getParser();
	
	
	/**
	 * Returns a lexer factory that creates lexers for the language extension.
	 */
	protected abstract ILexerFactory getLexerFactory();
	
	
	/**
	 * Returns a token map for the language extension, this is used to 
	 * map token kinds that the language extension defines back to 
	 * token kinds that can be understood by the preprocessor.
	 */
	protected abstract IPPTokenComparator<IToken> getTokenComparator();
	
	
	/**
	 * Return any additional builtin macros and directives that are
	 * to be supported by the language variant.
	 */
	protected abstract IPreprocessorExtensionConfiguration getPreprocessorExtensionConfiguration();
	
	
	protected abstract IC99TokenCollector getTokenCollector();
	
	
	public Object getAdapter(Class adapter) {
		if (adapter == IPDOMLinkageFactory.class)
			return new PDOMCLinkageFactory();
		else
			return super.getAdapter(adapter);
	}
	
	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu) {
		return null;
	}
	
	public synchronized IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator, IIndex index, int options, IParserLogService log) throws CoreException {
		
		IParseResult parseResult = parse(reader, scanInfo, fileCreator, index, null, options);
		IASTTranslationUnit tu = parseResult.getTranslationUnit();
		return tu;
	}
	
	
	public synchronized IASTTranslationUnit getASTTranslationUnit(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log) throws CoreException {
		
		return getASTTranslationUnit(reader, scanInfo, fileCreator, index, 0, log);
	}

	
	public synchronized IASTCompletionNode getCompletionNode(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log, int offset) {
		
		IParseResult parseResult = completionParse(reader, scanInfo, fileCreator, index, offset);
		IASTCompletionNode node = parseResult.getCompletionNode();
		
		return node;
	}
	
	
	
	/**
	 * Preform the actual parse.
	 */
	public synchronized IParseResult parse(CodeReader reader, IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index, Integer contentAssistOffset) {
		return parse(reader, scanInfo, fileCreator, index, contentAssistOffset, 0);
	}
	
	/**
	 * Perform the actual parse.
	 */
	public synchronized IParseResult parse(CodeReader reader, IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index, Integer contentAssistOffset, int options) {

		boolean scanComments = (options & OPTION_ADD_COMMENTS) != 0;
		int preprocessorOptions = scanComments ? C99Preprocessor.OPTION_GENERATE_COMMENTS_FOR_ACTIVE_CODE : 0;
		
		ILexerFactory lexerFactory = getLexerFactory();
		IPPTokenComparator<IToken> comparator = getTokenComparator();
		
		C99Preprocessor<IToken> preprocessor = new C99Preprocessor<IToken>(lexerFactory, comparator, reader, scanInfo, fileCreator, preprocessorOptions);
		if(parser == null)
			parser = getParser();

		LocationResolver resolver = new LocationResolver();
		
		IPreprocessorExtensionConfiguration extConf = getPreprocessorExtensionConfiguration();
		IC99TokenCollector tokenCollector = getTokenCollector();
		tokenCollector.setParser(parser);
		
		// the preprocessor injects tokens into the parser
		int translationUnitSize;
		if (contentAssistOffset == null)
			translationUnitSize = preprocessor.preprocess(tokenCollector, resolver, extConf);
		else
			translationUnitSize = preprocessor.preprocess(tokenCollector, resolver, extConf, contentAssistOffset.intValue());

		if(preprocessor.encounteredError())
			return new C99ParseResult(null, null, true);

		tokenCollector.done(translationUnitSize);
		
		// bit of a CDT AST specific hack
		IParseResult result = parser.parse();
		IASTTranslationUnit ast = result.getTranslationUnit();
		if(ast instanceof CASTTranslationUnit) {
			CASTTranslationUnit tu = (CASTTranslationUnit) ast;
			tu.setIndex(index);
			tu.setLocationResolver(resolver);
		}
		
		return result;
	}

	

	
	/**
	 * Public so that these methods may be called directly from unit tests.
	 */
	public synchronized IParseResult completionParse(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, int offset) {

		return parse(reader, scanInfo, fileCreator, index, new Integer(offset));
	}
	
	
	
	
	public synchronized IASTName[] getSelectedNames(IASTTranslationUnit ast, int start, int length) {
		IASTNode selectedNode= ast.selectNodeForLocation(ast.getFilePath(), start, length);

		if (selectedNode == null)
			return new IASTName[0];

		if (selectedNode instanceof IASTName)
			return new IASTName[] { (IASTName) selectedNode };

		
		final List<IASTName> nameList= new ArrayList<IASTName>();
		
		CASTVisitor nameCollector = new CASTVisitor() {
			{ shouldVisitNames= true; }
			public int visit(IASTName name) {
				nameList.add(name);
				return PROCESS_CONTINUE;
			}
		};
		
		selectedNode.accept(nameCollector);
		return (IASTName[]) nameList.toArray(new IASTName[nameList.size()]);
	}


	
	public String[] getBuiltinTypes() {
		return getKeywordMap().getBuiltinTypes();
	}


	public String[] getKeywords() {
		return getKeywordMap().getKeywords();
	}


	public String[] getPreprocessorKeywords() {
		return getKeywordMap().getPreprocessorKeywords();
	}
}
