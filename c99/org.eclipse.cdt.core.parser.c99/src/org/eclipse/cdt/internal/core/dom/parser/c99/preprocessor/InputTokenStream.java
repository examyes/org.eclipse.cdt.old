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

package org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor;

import java.io.File;
import java.util.Iterator;
import java.util.Stack;

import org.eclipse.cdt.core.dom.c99.IPreprocessorTokenCollector;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parsersym;

import lpg.lpgjavaruntime.IToken;

/**
 * A stack of token contexts, feeds tokens to the preprocessor.
 * 
 * This class does not support arbitrary lookahead.
 * 
 * This class allows for new sequences of tokens to be added to the
 * preprocessor input, for example when the source of an #include
 * needs to be processed or when a macro is expanded.
 * 
 * This class is also partly responsible for computing adjusted
 * token offsets (for example if a file is included then any tokens
 * after the #include will have their offsets adjusted by the size
 * of the included file).
 * 
 */
class InputTokenStream {
	
	public static final int NO_CONTENT_ASSIST_OFFSET = -1;
	
	private final Stack contextStack = new Stack(); // Stack<Context>
	private Context topContext = null;
	private boolean stuck = false;
	private int contentAssistOffset = NO_CONTENT_ASSIST_OFFSET;
	
	// Used to calculate the size of ther resulting translation unit
	private int translationUnitSize = 0;

	// for collecting comment tokens
	private IPreprocessorTokenCollector parser;
	private boolean collectCommentTokens = false;
	

	public InputTokenStream(IPreprocessorTokenCollector parser) {
		this.parser = parser;
	}
	
	/**
	 * Event that is fired when the content of an included file
	 * is fully consumed. This can be used to notify the preprocessor
	 * that an include has been exited.
	 */
	public interface IIncludeContextCallback {
		void contextClosed();
	}
	
	/**
	 * A frame on the contextStack.
	 * A Context can represent:
	 * - an included file
	 * - an argument to a macro, which should be processed in isolation from the rest of the tokens in the file
	 * - the contents of an include directive (the tokens between #include and the newline)
	 * - the result of a macro invocation
	 */
	private class Context {
		CodeReader reader;
		TokenList tokenList;
		boolean isolated = false; // a flag to indicate that processing should stop when the end of the context is reached
		IIncludeContextCallback callback;
		int adjustedGlobalOffset;
		
		/**
		 * @param startingOffset The adjusted offset of the start of the context. Adjusted means that the start offset
		 * of an included file will be adjusted according to the offset of the location where the file was included.
		 */
		Context(CodeReader reader, TokenList tokenList, boolean isolated, IIncludeContextCallback callback, int startingOffset) {
			this.reader = reader;
			this.tokenList = tokenList;
			this.isolated = isolated;
			this.callback = callback;
			this.adjustedGlobalOffset = startingOffset;
		}
	}
	
	
	/**
	 * Set the location of the content assist point in the
	 * working copy.
	 */ 
	public void setContentAssistOffset(int offset) {
		if(offset == NO_CONTENT_ASSIST_OFFSET) {
			this.contentAssistOffset = offset;
		}
		else if(offset < 0) {
			throw new IllegalArgumentException(Messages.getString("InputTokenStream.0") + offset); //$NON-NLS-1$
		}
		else {
			this.contentAssistOffset = offset;
		}
	}
	
	
	/**
	 * Returns true iff the file associated with the given code
	 * reader is already on the context stack.
	 */
	public boolean isCircularInclusion(CodeReader readerToTest) {
		for(Iterator iter = contextStack.iterator(); iter.hasNext();) {
			Context context = (Context) iter.next();
			if(CharArrayUtils.equals(context.reader.filename, readerToTest.filename))
				return true;
		}
		return false;
	}
	
	
	/**
	 * Returns the locaiton of the cursor in the working copy.
	 */
	public int getContentAssistOffset() {
		return contentAssistOffset;
	}
	
	
	public boolean isContentAssistMode() {
		return contentAssistOffset != NO_CONTENT_ASSIST_OFFSET;
	}
	
	/**
	 * Pushes the tokens that make up an included file onto the stack.
	 * 
	 * @param tokenList Tokens that comprise the included file
	 * @param reader The CodeReader that contains the source buffer of the included file
	 * @param inclusionLocaionOffset The global offset of the location of the #include directive
	 * @param callback A callback that will be fired when the included file is completely consumed
	 */
	public void pushIncludeContext(TokenList tokenList, CodeReader reader, int inclusionLocaionOffset, IIncludeContextCallback callback) {
		// adjust the offsets of the contexts that are already on the stack
		adjustGlobalOffsets(reader.buffer.length);
		topContext = (Context) contextStack.push(new Context(reader, tokenList, false, callback, inclusionLocaionOffset));
	}

	/**
	 * Used only when an include should be processed in isolation, in other words processing
	 * should stop when the end of the include is reached. I think this is only used
	 * for testing purposes.
	 */
	public void pushIsolatedIncludeContext(TokenList tokenList, CodeReader reader, int inclusionLocaionOffset, IIncludeContextCallback callback) {
		// adjust the offsets of the contexts that are already on the stack
		adjustGlobalOffsets(reader.buffer.length);
		topContext = (Context) contextStack.push(new Context(reader, tokenList, true, callback, inclusionLocaionOffset));
	}
	
	/**
	 * Pushes the tokens that are the result of a macro invocation.
	 * These tokens are made available for further processing.
	 */
	public void pushMacroExpansionContext(TokenList expansion, int expansionLocationOffset, IIncludeContextCallback callback) {
		if(expansion == null || expansion.isEmpty())
			return;
		
		int expansionSize = expansion.last().getEndOffset() - expansion.first().getStartOffset() + 1;
		adjustGlobalOffsets(expansionSize);

		Context context = new Context(topContext.reader, expansion, false, callback, expansionLocationOffset);
		topContext = (Context) contextStack.push(context);
	}
	
	
	private void adjustGlobalOffsets(int contextSize) {
		for(int i = 0; i < contextStack.size(); i++) {
			Context context = (Context) contextStack.get(i);
			context.adjustedGlobalOffset += contextSize;
		}
		translationUnitSize += contextSize;
	}
	
	/**
	 * An isolated context is used when processing should stop when the end of the given tokenList is reached,
	 * for example a macro argument or the tokens in an include directive.
	 * 
	 * Basically the preprocessor will push the tokens that make up a macro argument.
	 * The preprocessor will then be able to detect when those tokens are fully consumed.
	 */
	public void pushIsolatedContext(TokenList tokenList, IIncludeContextCallback callback) {
		// in this case the offset of the context does not really matter, so just set it to 0
		if(tokenList != null) {
			Context context = new Context(topContext == null ? null : topContext.reader, tokenList, true, callback, 0);
			topContext = (Context) contextStack.push(context);
		}
	}
	
	
	/**
	 * An unadjusted offset represents the original offset in the source file.
	 * An adjusted offset takes into account includes and macro expansions
	 * creating a meaningful global offset that can be used by the AST. 
	 */
	public int adjust(int localOffset) {
		return localOffset + topContext.adjustedGlobalOffset;
	}
	
	public int getCurrentOffset() {
		IToken token = peek();
		if(token == null)
			return getTranslationUnitSize();
		else
			return adjust(token.getStartOffset());
	}
	
	
	public String getCurrentFileName() {
		return new String(topContext.reader.filename);
	}
	
	public File getCurrentDirectory() {
		 File file = new File(getCurrentFileName());
         return file.getParentFile();
	}

	
	
	/**
	 * Returns true when the steam gets "stuck"
	 * meaning that he end of an isolated context
	 * is reached.
	 */
	public boolean isStuck() {
		return stuck;
	}
	
	
	/**
	 * Makes the stream "unstuck"
	 */
	public void resume() {
		stuck = false;
	}
	
	
	/**
	 * This will only return an accurate value when the preprocessor
	 * is at the very end of the input (when it is time to create an EOF token).
	 */
	public int getTranslationUnitSize() {
		return translationUnitSize;
	}
	
	
	/**
	 * Returns the current token.
	 * The offset of the return token are not yet adjusted.
	 * @return null if there are no tokens
	 */
	public IToken peek() {
		consumeCommentTokens();
		return nextToken(true);
	}
	
	
	/**
	 * Returns the current token and advances to the next token in sequence.
	 * The token's offsets will be adjusted to reflect includes and macro expansions
	 * if the adjust parameter is true.
	 * @return null if there are no more tokens
	 */
	public IToken next(boolean adjust) {
		consumeCommentTokens();
		IToken token = nextToken(false);
		if(adjust && topContext != null && token != null) {
			int offset = topContext.adjustedGlobalOffset;
			token.setStartOffset(token.getStartOffset() + offset);
			token.setEndOffset(token.getEndOffset() + offset);
		}
		return token;
	}
	
	
	/**
	 * Returns the current token and advances to the next token in sequence.
	 * The token's offsets will be adjusted to reflect includes and macro expansions.
	 * @return null if there are no more tokens
	 */
	public IToken next() {
		return next(true);
	}
	
	
	/**
	 * Sends comment tokens to the parser instead of to the preprocessor.
	 */
	private void consumeCommentTokens() {
		IToken token;
		while((token = nextToken(true)) != null) {
			int kind = token.getKind();
			if(kind != C99Parsersym.TK_SingleLineComment && kind != C99Parsersym.TK_MultiLineComment)
				break;
			
			if(collectCommentTokens && parser != null) {
				parser.addCommentToken(token);
			}
			nextToken(false); // throw away the comment token
		}
	}
	
	
	/**
	 * Returns the next token in sequence.
	 * If peek == false then the token is
	 * removed from the sequence.
	 */
	private IToken nextToken(boolean peek) {
		if(stuck || topContext == null)
			return null;
		
		TokenList topList = topContext.tokenList;
		
		if(topList.isEmpty()) {
			if(topContext.callback != null) 
				topContext.callback.contextClosed();
			if(topContext.isolated)
				stuck = true;
			popContext();
			return nextToken(peek);
		}
		else {
			return peek ?  topList.first() : topList.removeFirst();
		}
	}
	
	
	public boolean isContentAssistOffsetOnCurrentLine() {
		if(contentAssistOffset == NO_CONTENT_ASSIST_OFFSET)
			return false;
		
		// If we are past the last token of input then
		// we consider the offset to be reached.
		if(contextStack.isEmpty())
			return true;
		
		if(contextStack.size() != 1)
			return false;
		
		
		Iterator iter = topContext.tokenList.iterator();
		while(iter.hasNext()) {
			IToken token = (IToken)iter.next();
			if(token.getKind() == C99Parsersym.TK_NewLine) {
				return (token.getEndOffset() >= contentAssistOffset - 1);
			}
		}
		
		// Didn't encounter a newline.
		// The offset must be somewhere in the file so return true.
		// This assumes that this method would not be called if
		// the offset was encountered already somewhere in the file.
		return true;
	}
	
	
	
	/**
	 * Returns true iff the location of the content assist point
	 * (which would be the location of the cursor in the editor)
	 * has been reached.
	 */
	public boolean isContentAssistOffsetReached() {
		if(contentAssistOffset == NO_CONTENT_ASSIST_OFFSET)
			return false;
		
		// if we are past the last token of input then
		// we consider the offset to be reached
		peek(); // make sure that there isn't an empty context on the stack
		if(contextStack.isEmpty())
			return true;
		if(isStuck())
			return false;
		if(contextStack.size() != 1)
			return false;
		
		return contextStack.size() == 1 
		    && peek().getEndOffset() >= contentAssistOffset - 1;
	}
	
	/**
	 * Returns true iff there are no more tokens.
	 */
	public boolean isEmpty() {
		return peek() == null;
	}
	
	
	private void popContext() {
		contextStack.pop();
		topContext = (Context) (contextStack.isEmpty() ? null : contextStack.peek());
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("InputTokenStream { \n"); //$NON-NLS-1$
		for(Iterator iter = contextStack.iterator(); iter.hasNext();) {
			Context context = (Context) iter.next();
			sb.append("Context: "); //$NON-NLS-1$
			sb.append("(stop ").append(context.isolated).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(context.tokenList.toString()).append("\n"); //$NON-NLS-1$
		}
		return sb.append("\n}\n").toString(); //$NON-NLS-1$
	}


	public boolean isCollectCommentTokens() {
		return collectCommentTokens;
	}


	public void setCollectCommentTokens(boolean collectCommentTokens) {
		this.collectCommentTokens = collectCommentTokens;
	}
}