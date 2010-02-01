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
package org.eclipse.cdt.internal.build.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.cdt.build.core.IBuildConsole;
import org.eclipse.cdt.build.core.IBuildService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Launches a command sending the stdout and stderr downstream. Also handles the cancel
 * from a progress monitor.
 */
public class CommandLauncher {

	private class Pipe extends Thread {
		private final InputStream in;
		private final OutputStream out;
		private boolean done = false;
		
		public Pipe(String name, InputStream in, OutputStream out) {
			super(name);
			this.in = in;
			this.out = out;
		}
		
		public boolean isDone() {
			return done;
		}
		
		@Override
		public void run() {
			final int len = 256;
			byte[] buff = new byte[len];
			int pos = 0;
			while (true) {
				try {
					int c = in.read();
					if (c < 0) {
						// EOF
						out.write(buff, 0, pos);
						out.flush();
						synchronized (CommandLauncher.this) {
							done = true;
							CommandLauncher.this.notifyAll();
						}
						return;
					}
					
					buff[pos++] = (byte)c;
					if (c == '\n' || pos == len) {
						out.write(buff, 0, pos);
						out.flush();
						pos = 0;
					}
				} catch (IOException e) {
					Activator.logError(e);
				}
			}
		}
		
	}
	
	/**
	 * Launch the command and show the output on the build console 
	 * @param cmdarray
	 * @param envp
	 * @param dir
	 * @param buildConsole
	 * @param monitor
	 * @return exit value from the process
	 * @throws CoreException
	 */
	public int exec(String[] cmdarray, String[] envp, File dir, IProgressMonitor monitor) throws CoreException {
		try {
			IBuildConsole buildConsole = Activator.getService(IBuildService.class).getBuildConsole();
			buildConsole.activate();
			OutputStream out = buildConsole.getOutputStream();
			OutputStream err = buildConsole.getErrorStream();

			Process process = null;
			try {
				process = Runtime.getRuntime().exec(cmdarray, envp, dir);
			} catch (IOException e) {
				new PrintStream(err).println(e.getLocalizedMessage());
			}
			if (process == null)
				return 1;
			
			// close stdin since we don't use it
			process.getOutputStream().close();
			
			// print out the command
			StringBuffer cmdString = new StringBuffer("> ");
			for (String s : cmdarray) {
				cmdString.append(s);
				cmdString.append(' ');
			}
			new PrintStream(out).println(cmdString);
			
			// set up the pipe for stdout and stderr
			Pipe stdout = new Pipe(cmdarray[0] + " - stdout", process.getInputStream(), out);
			stdout.start();
			Pipe stderr = new Pipe(cmdarray[0] + " - stderr", process.getErrorStream(), err);
			stderr.start();
			
			synchronized (this) {
				while (!stdout.isDone() || !stderr.isDone()) {
					if (monitor.isCanceled()) {
						// kill the process
						process.destroy();
						break;
					}
					
					try {
						wait(500);
					} catch (InterruptedException e) {
						// just continue loop
					}
				}
			}
			
			try {
				return process.waitFor();
			} catch (InterruptedException e) {
				return process.exitValue();
			}
		} catch (IOException e) {
			throw new CoreException(Activator.getError(e));
		}
	}

}
