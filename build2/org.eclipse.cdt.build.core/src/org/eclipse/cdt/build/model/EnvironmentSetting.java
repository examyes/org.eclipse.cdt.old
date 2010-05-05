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

package org.eclipse.cdt.build.model;

/**
 * This class represents an edit to the environment. It changes a variable using the
 * named operation with the given value.
 * 
 * @author Doug Schaefer
 */
public class EnvironmentSetting {

	private final String variable;
	private final String value;
	
	public enum Operation { replace, prepend, append };
	
	private final Operation operation;
	
	public EnvironmentSetting(String variable, String value) {
		this(variable, value, Operation.replace);
	}
	
	public EnvironmentSetting(String variable, String value, Operation operation) {
		this.variable = variable;
		this.value = value;
		this.operation = operation;
	}
	
	public String getVariable() {
		return variable;
	}
	
	public String getValue() {
		return value;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
}
