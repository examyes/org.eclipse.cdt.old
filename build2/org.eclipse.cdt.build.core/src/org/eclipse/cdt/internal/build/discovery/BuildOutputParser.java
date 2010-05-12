/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.build.discovery;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.build.model.DiscoveredCommand;
import org.eclipse.cdt.build.model.IBuildService;
import org.eclipse.cdt.build.model.IConfiguration;
import org.eclipse.cdt.build.model.IToolChain;
import org.eclipse.cdt.internal.build.core.Activator;
import org.eclipse.core.runtime.CoreException;

/**
 * Parses build output and finds common commands for the tool chains.
 * 
 * @author Doug Schaefer
 */
public class BuildOutputParser extends OutputStream {

	private final OutputStream passThru;
	private final IConfiguration buildConfig;
	
	private static class PatternMap {
		public Pattern pattern;
		public IToolChain toolChain;
		
		public PatternMap(Pattern pattern, IToolChain toolChain) {
			this.pattern = pattern;
			this.toolChain = toolChain;
		}
	}
	
	List<PatternMap> patterns = new ArrayList<PatternMap>();
	private StringBuffer currentLine = new StringBuffer();
	
	public BuildOutputParser(OutputStream passThru, IConfiguration buildConfig) throws CoreException {
		this.passThru = passThru;
		this.buildConfig = buildConfig;
		
		// Collect the patterns
		IToolChain[] toolChains = Activator.getService(IBuildService.class).getToolChains(buildConfig.getToolChainIds());
		for (IToolChain toolChain : toolChains) {
			Pattern[] tcPatterns = toolChain.getDiscoveryPatterns();
			for (Pattern pattern : tcPatterns)
				patterns.add(new PatternMap(pattern, toolChain));
		}
	}
	
	private static enum State { inWS, inToken, singleQuoted, doubleQuoted, inBackSlash };
	
	private static String[] tokenize(StringBuffer line) {
		char [] chars = new char[line.length()];
		line.getChars(0, line.length(), chars, 0);
		StringBuffer token = new StringBuffer();
		List<String> tokens = new ArrayList<String>();
		
		// find white space separated tokens taking into account quoted arguments
		State state = State.inWS;
		
		for (int i = 0; i < chars.length; ++i) {
			char c = chars[i];
			
			if (c == '\\' && state != State.inBackSlash) {
				state = State.inBackSlash;
				continue;
			}
			
			switch (state) {
			case inBackSlash:
				token.append(c);
				state = State.inToken;
				break;
				
			case inWS:
				if (c == '"') {
					state = State.doubleQuoted;
				} else if (c == '\'') {
					state = State.singleQuoted;
				} else if (c != ' ' && c != '\t') {
					token.append(c);
					state = State.inToken;
				}
				break;
				
			case inToken:
				if (c == '"') {
					state = State.doubleQuoted;
				} else if (c == '\'') {
					state = State.singleQuoted;
				} else if (c == ' ' || c == '\t') {
					tokens.add(token.toString());
					token.delete(0, token.length());
					state = State.inWS;
				} else {
					token.append(c);
				}
				break;
				
			case doubleQuoted:
				if (c == '"')
					state = State.inToken;
				else
					token.append(c);
				break;
				
			case singleQuoted:
				if (c == '\'')
					state = State.inToken;
				else
					token.append(c);
				break;
			}
		}
		
		if (state != State.inWS)
			tokens.add(token.toString());
		
		return tokens.toArray(new String[tokens.size()]);
	}
	
	private void handleNewLine() {
		String[] tokens = null;
		for (PatternMap pattern : patterns) {
			if (pattern.pattern.matcher(currentLine).find()) {
				if (tokens == null)
					tokens = tokenize(currentLine);
				DiscoveredCommand command = pattern.toolChain.getDiscoveredCommand(tokens);
				// TODO check if this is a new command and add to db if it is
			}
		}
		
		// Clear the line
		currentLine.delete(0, currentLine.length());
	}
	
	@Override
	public void write(int b) throws IOException {
		passThru.write(b);
		
		if (b == '\n')
			handleNewLine();
		else
			currentLine.append((char)b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		passThru.write(b, off, len);
		
		// Search for newline
		int p = off;
		int n = 0;
		while (n < len && p < off + len) {
			boolean newline = false;
			int s = 0;
			while (n < len) {
				if (b[n++] == '\n') {
					newline = true;
					break;
				}
				else
					++s;
			}

			char[] chars = new char[s];
			for (int i = 0; i < s; ++i)
				chars[i] = (char)b[p++];
			
			currentLine.append(chars);
			if (newline)
				handleNewLine();
		}		
	}
	
	@Override
	public void flush() throws IOException {
		passThru.flush();
	}
	
	@Override
	public void close() throws IOException {
		passThru.close();
	}
	
}
