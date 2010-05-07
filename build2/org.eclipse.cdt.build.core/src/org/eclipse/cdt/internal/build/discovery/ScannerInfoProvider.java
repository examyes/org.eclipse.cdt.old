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

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IResource;

/**
 * @author Doug Schaefer
 *
 */
public class ScannerInfoProvider implements IScannerInfoProvider {

	public IScannerInfo getScannerInformation(IResource resource) {
		// TODO Auto-generated method stub
		return null;
	}

	// These don't seem to be used and they're not supported here
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
	}

	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
	}

}
