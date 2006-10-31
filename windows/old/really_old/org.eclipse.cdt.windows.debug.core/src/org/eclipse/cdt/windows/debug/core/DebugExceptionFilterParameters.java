/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.windows.debug.core;

/**
 * @author Doug Schaefer
 *
 */
public class DebugExceptionFilterParameters {

	private int executionOption;
	private int continueOption;
	private int textSize;
	private int commandSize;
	private int secondCommandSize;
	private int exceptionCode;

	@SuppressWarnings("unused")
	private DebugExceptionFilterParameters(
			int executionOption,
			int continueOption,
			int textSize,
			int commandSize,
			int secondCommandSize,
			int exceptionCode) {
		this.executionOption = executionOption;
		this.continueOption = continueOption;
		this.textSize = textSize;
		this.commandSize = commandSize;
		this.secondCommandSize = secondCommandSize;
		this.exceptionCode = exceptionCode;
	}
			
	public int getExecutionOption() {
		return executionOption;
	}

	public int getContinueOption() {
		return continueOption;
	}

	public int getTextSize() {
		return textSize;
	}

	public int getCommandSize() {
		return commandSize;
	}

	public int getSecondCommandSize() {
		return secondCommandSize;
	}

	public int getExceptionCode() {
		return exceptionCode;
	}

}
