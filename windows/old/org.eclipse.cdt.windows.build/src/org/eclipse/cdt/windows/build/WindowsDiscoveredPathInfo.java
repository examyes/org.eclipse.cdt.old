/**********************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.windows.build;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredScannerInfoSerializable;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author Doug Schaefer
 *
 */
public class WindowsDiscoveredPathInfo implements IDiscoveredPathInfo {

	private final IPath[] paths;
	
	public WindowsDiscoveredPathInfo() {
		WindowsRegistry reg = WindowsRegistry.getRegistry();
		
		// Include paths
		String sdkDir = reg.getLocalMachineValue("SOFTWARE\\Microsoft\\Microsoft SDKs\\Windows\\v6.0", "InstallationFolder");
		paths = new IPath[] {
			new Path(sdkDir.concat("\\VC\\Include")),
			new Path(sdkDir.concat("\\VC\\Include\\Sys")),
			new Path(sdkDir.concat("\\Include")),
			new Path(sdkDir.concat("\\Include\\gl"))
		};
	}
	
	public IPath[] getIncludePaths() {
		return paths;
	}

	public IProject getProject() {
		return null;
	}

	public IDiscoveredScannerInfoSerializable getSerializable() {
		return null;
	}

	public Map getSymbols() {
		return new HashMap();
	}

}
