/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Doug Schaefer (WRS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.core.model;

/**
 * 
 */
public abstract class Option {

	/**
	 * @return the id of the Option.
	 */
	public String getId() {
		return null;
	}
	
	/**
	 * Serialize the object for storing in the project settings.
	 * 
	 * @param value
	 * @return
	 */
	public abstract String serialize(Object value);

	/**
	 * Recreate the object from the project settings.
	 * 
	 * @param value
	 * @return
	 */
	public abstract Object deserialize(String value);
	
}
