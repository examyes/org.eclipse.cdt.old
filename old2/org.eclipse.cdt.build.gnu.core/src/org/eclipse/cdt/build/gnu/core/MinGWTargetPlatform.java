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
package org.eclipse.cdt.build.gnu.core;

import org.eclipse.cdt.build.core.model.TargetPlatform;
import org.eclipse.core.runtime.Platform;

public class MinGWTargetPlatform extends TargetPlatform {

	@Override
	public boolean isAvailable() {
		// TODO It would be awesome if we could detect a cross compiler on Linux!
		return Platform.getOS().equals(Platform.OS_WIN32);
	}

}
