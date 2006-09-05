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
package org.eclipse.cdt.windows.build;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.envvar.IProjectEnvironmentVariableSupplier;
import org.eclipse.cdt.utils.WindowsRegistry;

/**
 * @author DSchaefer
 *
 */
public class WindowsProjectEnvironmentVariableSupplier
		implements
			IProjectEnvironmentVariableSupplier {
	
	private Map<String, IBuildEnvironmentVariable> envvars;
	
	private static class WindowsBuildEnvironmentVariable implements IBuildEnvironmentVariable {
		
		private final String name;
		private final String value;
		private final int operation;
		
		public WindowsBuildEnvironmentVariable(String name, String value, int operation) {
			this.name = name;
			this.value = value;
			this.operation = operation;
		}
		
		public String getDelimiter() {
			return ";";
		}
		
		public String getName() {
			return name;
		}
		
		public String getValue() {
			return value;
		}

		public int getOperation() {
			return operation;
		}

	}

	public IBuildEnvironmentVariable getVariable(String variableName,
			IManagedProject project, IEnvironmentVariableProvider provider) {
		if (envvars == null)
			initvars();
		return envvars.get(variableName);
	}

	public IBuildEnvironmentVariable[] getVariables(IManagedProject project,
			IEnvironmentVariableProvider provider) {
		if (envvars == null)
			initvars();
		return envvars.values().toArray(new IBuildEnvironmentVariable[envvars.size()]);
	}

	private void addvar(IBuildEnvironmentVariable var) {
		envvars.put(var.getName(), var);
	}
	
	private void initvars() {
		envvars = new HashMap<String, IBuildEnvironmentVariable>();
		
		StringBuffer buff;
		WindowsRegistry reg = WindowsRegistry.getRegistry();
		
		// The SDK Location
		String sdkDir = reg.getLocalMachineValue("SOFTWARE\\Microsoft\\Microsoft SDKs\\Windows\\v6.0", "InstallationFolder");
		
		// The .Net Framework Location
		String netRoot = reg.getLocalMachineValue("SOFTWARE\\Microsoft\\.NETFramework", "InstallRoot");
		String netVer = reg.getLocalMachineValueName("SOFTWARE\\Microsoft\\.NETFramework\\policy\\v2.0", 0);
		
		// INCLUDE
		buff = new StringBuffer();
		buff.append(sdkDir).append("VC\\Include;");
		buff.append(sdkDir).append("VC\\Include\\Sys;");
		buff.append(sdkDir).append("Include;");
		buff.append(sdkDir).append("Include\\gl;");
		addvar(new WindowsBuildEnvironmentVariable("INCLUDE", buff.toString(), IBuildEnvironmentVariable.ENVVAR_PREPEND));

		// LIB
		buff = new StringBuffer();
		buff.append(sdkDir).append("VC\\Lib;");
		buff.append(sdkDir).append("Lib;");
		addvar(new WindowsBuildEnvironmentVariable("LIB", buff.toString(), IBuildEnvironmentVariable.ENVVAR_PREPEND));
		
		// PATH
		buff = new StringBuffer();
		buff.append(sdkDir).append("VC\\Bin;");
		buff.append(sdkDir).append("Bin;");
		buff.append(netRoot).append("v2.0.").append(netVer).append(';');
		addvar(new WindowsBuildEnvironmentVariable("PATH", buff.toString(), IBuildEnvironmentVariable.ENVVAR_PREPEND));
	}

}
