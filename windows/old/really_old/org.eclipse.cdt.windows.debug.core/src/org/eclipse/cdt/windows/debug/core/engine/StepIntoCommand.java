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

package org.eclipse.cdt.windows.debug.core.engine;


/**
 * @author Doug Schaefer
 *
 */
public class StepIntoCommand extends DebugCommand {

	@Override
	protected String getName() {
		return "StepInto";
	}

	@Override
	public int run(DebugEngine engine) {
		return engine.stepInto();
	}
	
}
