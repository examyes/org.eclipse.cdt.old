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
	
	private void handleNewLine() {
		// TODO handle the line :)
		
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
			while (n < len)
				if (b[n++] == '\n')
					break;
		
			char[] chars = new char[n];
			for (int i = 0; i < n; ++i)
				chars[i] = (char)b[p++];
			
			currentLine.append(chars);
			if (n < len)
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
