/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.cdt.oprofile.core.IOpcontrolProvider;
import org.eclipse.cdt.oprofile.core.OpcontrolException;
import org.eclipse.cdt.oprofile.core.OprofileCorePlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;


/**
 * A class which encapsulates running opcontrol.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class LinuxOpcontrolProvider implements IOpcontrolProvider {
	// Location of opcontrol security wrapper
	private static final String _OPCONTROL_PROGRAM = _findOpcontrol();

	// Initialize the Oprofile kernel module and oprofilefs
	private static final String _OPD_INIT_MODULE = "--init"; //$NON-NLS-1$
	
	// Setup daemon collection arguments
	private static final String _OPD_SETUP = "--setup"; //$NON-NLS-1$
	
	// Start the daemon process without starting data collection
	private static final String _OPD_START_DAEMON = "--start-daemon"; //$NON-NLS-1$
	
	// Start collecting profiling data
	private static final String _OPD_START_COLLECTION = "--start"; //$NON-NLS-1$
	
	// Flush the collected profiling data to disk
	private static final String _OPD_DUMP = "--dump"; //$NON-NLS-1$
	
	// Stop data collection
	private static final String _OPD_STOP_COLLECTION = "--stop"; //$NON-NLS-1$
	
	// Stop data collection and stop daemon
	private static final String _OPD_SHUTDOWN = "--shutdown"; //$NON-NLS_1$
	
	// Clear out data from current session
	private static final String _OPD_RESET = "--reset"; //$NON-NLS-1$
	
	// Save data from the current session
	private static final String _OPD_SAVE_SESSION = "--save="; //$NON-NLS-1$
	
	// Unload the oprofile kernel module and oprofilefs
	private static final String _OPD_DEINIT_MODULE = "--deinit";
	
	/**
	 * Unload the kernel module and oprofilefs
	 * @throws OpcontrolException
	 */
	public void deinitModule() throws OpcontrolException {
		_runOpcontrol(_OPD_DEINIT_MODULE, true);
	}
	
	/**
	 * Dump collected profiling data
	 * @throws OpcontrolException
	 */
	public void dumpSamples() throws OpcontrolException {
		_runOpcontrol(_OPD_DUMP, true);
	}
	
	/**
	 * Loads the kernel module and oprofilefs
	 * @throws OpcontrolException
	 */
	public void initModule() throws OpcontrolException {
		_runOpcontrol(_OPD_INIT_MODULE, true);
	}
	
	/**
	 * Clears out data from current session
	 * @throws OpcontrolException
	 */
	public void reset() throws OpcontrolException {
		_runOpcontrol(_OPD_RESET, true);
	}
	
	/**
	 * Saves the current ("default") session
	 * @param name	the name to which to save the session
	 * @throws OpcontrolException
	 */
	public void saveSession(String name) throws OpcontrolException {
		ArrayList cmd = new ArrayList();
		cmd.add(_OPD_SAVE_SESSION + name);
		_runOpcontrol(cmd, true);
	}
	
	/**
	 * Give setup aruments
	 * @param args	list of parameters for daemon
	 * @throws OpcontrolException
	 */
	public void setupDaemon(ArrayList args) throws OpcontrolException {
		_runOpcontrol(args, true);
	}
	
	/**
	 * Stop data collection and remove daemon
	 * @throws OpcontrolException
	 */
	public void shutdownDaemon() throws OpcontrolException {
		_runOpcontrol(_OPD_SHUTDOWN, true);
	}
	
	/**
	 * Start data collection (will start daemon if necessary)
	 * @throws OpcontrolException
	 */
	public void startCollection() throws OpcontrolException {
		_runOpcontrol(_OPD_START_COLLECTION, true);
	}
	
	/**
	 * Start daemon without starting profiling
	 * @throws OpcontrolException
	 */
	public void startDaemon() throws OpcontrolException {
		_runOpcontrol(_OPD_START_DAEMON, true);
	}
	
	/**
	 * Stop data collection
	 * @throws OpcontrolException
	 */
	public void stopCollection() throws OpcontrolException {
		_runOpcontrol(_OPD_STOP_COLLECTION, true);
	}
	
	// Convenience function
	private void _runOpcontrol(String cmd, boolean drainOutput) throws OpcontrolException {
		ArrayList list = new ArrayList();
		list.add(cmd);
		_runOpcontrol(list, drainOutput);
	}
	
	// Will add opcontrol program to beginning of args
	// args: list of opcontrol arguments (not including opcontrol program itself)
	private void _runOpcontrol(ArrayList args, boolean drainOutput) throws OpcontrolException {
		args.add(0, _OPCONTROL_PROGRAM);
		String[] cmdArray = new String[args.size()];
		args.toArray(cmdArray);
		
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(cmdArray);
		} catch (IOException ioe) {
			if (p != null) {
				p.destroy();
				p = null;
			}
			
			// Throw an exception
			Status status = new Status(Status.ERROR,
									   OprofileCorePlugin.getId(),
									   0 /* code */,
									   "error message",
									   ioe);
			throw new OpcontrolException(status);
		}
		
		if (p != null && drainOutput) {
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			try {
				String s;
				while ((s = stdout.readLine()) != null) {
					// drain
				}
			} catch (IOException ioe) {
				// We don't care if there were errors draining the output
			}
		}
	}
	
	private static String _findOpcontrol() {
		URL url = OprofileCorePlugin.getDefault().find(new Path("opcontrol")); //$NON-NLS-1$
		if (url != null) {
			try {
				return Platform.asLocalURL(url).getPath();
			} catch (IOException e) {
			}
		}
		
		// TODO: display error in unlikely event opcontrol not found
		// (which could only happen in case of corrupt installation)
		return null;
	}
}
