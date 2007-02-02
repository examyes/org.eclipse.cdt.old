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

package org.eclipse.cdt.mingw.build;

import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.envvar.IProjectEnvironmentVariableSupplier;
import org.eclipse.core.runtime.Platform;

/**
 * @author Doug Schaefer
 */
public class MingwProjectEnvironmentVariableSupplier implements
		IProjectEnvironmentVariableSupplier {

	private static class MingwBuildEnvironmentVariable implements IBuildEnvironmentVariable {
		private final String name;
		private final String value;
		private final int operation;
		
		public MingwBuildEnvironmentVariable(String name, String value, int operation) {
			this.name = name;
			this.value = value;
			this.operation = operation;
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
		
		public String getDelimiter() {
			return ";";
		}
	}
	
	private final IBuildEnvironmentVariable path;
	
	public MingwProjectEnvironmentVariableSupplier() {
		String bin = Platform.getInstallLocation().getURL().getFile().substring(1)
			+ "mingw/bin";

		path = new MingwBuildEnvironmentVariable(
				"PATH",
				bin,
				IBuildEnvironmentVariable.ENVVAR_PREPEND);
	}
	
	public IBuildEnvironmentVariable getVariable(String variableName,
			IManagedProject project, IEnvironmentVariableProvider provider) {
		if (variableName.equals(path.getName()))
			return path;
		else
			return null;
	}

	public IBuildEnvironmentVariable[] getVariables(IManagedProject project,
			IEnvironmentVariableProvider provider) {
		return new IBuildEnvironmentVariable[] { path };
	}

}
