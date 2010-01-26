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
package org.eclipse.cdt.build.core.model.options;

import org.eclipse.cdt.build.core.model.Option;

/**
 * 
 */
public class StringOption extends Option {

	@Override
	public String serialize(Object value) {
		if (!(value instanceof String))
			throw new IllegalArgumentException();
		return (String)value;
	}

	@Override
	public Object deserialize(String value) {
		return value;
	}

}
