/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.launch;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.oprofile.core.OpcontrolException;
import org.eclipse.cdt.oprofile.core.OprofileCorePlugin;
import org.eclipse.cdt.oprofile.core.OprofileProperties;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.ErrorDialog;


/**
 * This class represents an Oprofile launch. It is responsible for starting and configuring
 * the daemon given a launch configuration. [Note that this also means that it will
 * launch a CLaunchConfiguration if one was specified in the config.]
 * @author keiths
 */
public class OprofileSession
{
	OprofileCounter[] _counters;
	OprofileOptions _options;
	String _launchConfig;
	
	public OprofileSession(ILaunchConfiguration config)
	{
		_counters = OprofileCounter.getCounters(config);
		_options = new OprofileOptions();
		_options.loadConfiguration(config);
		try
		{
			_launchConfig = config.getAttribute(LaunchPlugin.ATTR_C_LAUNCH_CONFIG, (String) null);
		}
		catch (CoreException ce)
		{
			_launchConfig = null;
		}
	}
	
	public void run ()
	{
		// Setup and start the daemon
		ArrayList command = new ArrayList();
		command.addAll(_options.toArguments());
		for (int i = 0; i < _counters.length; i++) {
			command.addAll(_counters[i].toArguments());
		}
		
		try {
			// Shutdown any currently running oprofile session
			OprofileCorePlugin.getDefault().getOpcontrolProvider().shutdownDaemon();
			OprofileCorePlugin.getDefault().getOpcontrolProvider().setupDaemon(command);
			OprofileCorePlugin.getDefault().getOpcontrolProvider().startCollection();
		} catch (OpcontrolException oe) {
			String title = OprofileProperties.getString("opcontrolProvider.error.dialog.title"); //$NON-NLS-1$
			String msg = OprofileProperties.getString("opcontrolProvider.error.dialog.message"); //$NON-NLS-1$
			ErrorDialog.openError(null /* parent shell */, title, msg, oe.getStatus());
		}
		
		// Run c/c++ launch config, if defined
		if (_launchConfig != null)
		{
			ILaunchConfiguration claunch = null;
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			try
			{
				claunch = manager.getLaunchConfiguration(_launchConfig);
			}
			catch (CoreException ce)
			{
				claunch = null;
			}
			
			if (claunch != null)
			{
				try
				{
					// TODO: Progress monitor
					claunch.launch(ILaunchManager.RUN_MODE, null);
				}
				catch (CoreException ce)
				{
				}
			}
		}
	}

	private Process _runCommand(String[] cmdArray)
	{
		Process p = null;
		try
		{
			p = Runtime.getRuntime().exec(cmdArray);
		}
		catch (IOException ioe)
		{
			if (p != null)
				p.destroy();
			p = null;
		}
		
		return p;
	}
}
