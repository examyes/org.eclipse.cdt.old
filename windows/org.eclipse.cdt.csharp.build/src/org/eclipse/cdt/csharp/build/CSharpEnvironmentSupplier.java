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

package org.eclipse.cdt.csharp.build;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.utils.WindowsRegistry;

/**
 * @author Doug Schaefer
 *
 */
public class CSharpEnvironmentSupplier implements
		IConfigurationEnvironmentVariableSupplier {

	private static class CSharpBuildEnvironmentVariable implements IBuildEnvironmentVariable {
		
		private final String name;
		private final String value;
		private final int operation;
		
		public CSharpBuildEnvironmentVariable(String name, String value, int operation) {
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

	private IBuildEnvironmentVariable path;
	
	public CSharpEnvironmentSupplier() {
		WindowsRegistry reg = WindowsRegistry.getRegistry();

		// The .Net Framework Location
		String netRoot = reg.getLocalMachineValue("SOFTWARE\\Microsoft\\.NETFramework", "InstallRoot");
		String netVer = reg.getLocalMachineValueName("SOFTWARE\\Microsoft\\.NETFramework\\policy\\v2.0", 0);
		
		String bin = netRoot + "v2.0." + netVer + ';';
		path = new CSharpBuildEnvironmentVariable("PATH", bin, IBuildEnvironmentVariable.ENVVAR_PREPEND);
	}
	
	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		return path;
	}

	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		return new IBuildEnvironmentVariable[] { path };
	}

}
