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
package org.eclipse.cdt.internal.build.ui;

import java.io.OutputStream;

import org.eclipse.cdt.build.core.IBuildConsole;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Build Console that displays to the Console View 
 */
public class BuildConsole implements IBuildConsole {

	private MessageConsole console;
	
	@Override
	public boolean isAvailable() {
		// We are only available when the workbench is running
		return PlatformUI.isWorkbenchRunning();
	}
	
	@Override
	public OutputStream getOutputStream() {
		return getConsole().newMessageStream();
	}
	
	@Override
	public OutputStream getErrorStream() {
		final MessageConsoleStream err = getConsole().newMessageStream();
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				err.setColor(new Color(Display.getDefault(), 240, 0, 0));
			}
		});
		return err;
	}

	public MessageConsole getConsole() {
		if (console == null) {
			IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
			for (IConsole c : consoleManager.getConsoles()) {
				if (getClass().getName().equals(c.getType())) {
					console = (MessageConsole)c;
					return console;
				}
			}
			
			console = new MessageConsole("CDT Build Console", getClass().getName(), null, true);
			consoleManager.addConsoles(new IConsole[] { console });
		}
		return console;
	}
	
	@Override
	public void activate() {
		getConsole().activate();
	}
	
}
