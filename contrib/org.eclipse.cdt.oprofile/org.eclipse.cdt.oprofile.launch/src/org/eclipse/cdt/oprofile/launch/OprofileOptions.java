/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.launch;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.oprofile.core.Oprofile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;


/**
 * This class represents global oprofile options.
 * @author keiths
 */
public class OprofileOptions
{
	private String _kernelImageFile;
	//private String _processIdFilter;
	//private String _processGroupFilter;
	private boolean _verboseLogging;
	private int _separateSamples;
	private int[][] _limits = null;

	public static final int SEPARATE_NONE = 0;
	public static final int SEPARATE_LIBRARY = 1;
	public static final int SEPARATE_KERNEL = 2;
	public static final int SEPARATE_ALL = 3;
	
	private final String ARG_KERNEL_IMAGE = "--vmlinux"; //$NON-NLS-1$
	private final String ARG_VERBOSE = "--verbose"; //$NON-NLS-1$
	private final String ARG_SEPARATE_SAMPLES = "--separate"; //$NON-NLS-1$
	private final String ARG_SEPARATE_ARGS[] =
	{
		"none", //$NON-NLS-1$
		"library", //$NON-NLS-1$
		"kernel", //$NON-NLS-1$
		"all" //$NON-NLS-1$
	};
	
	public OprofileOptions()
	{
		_kernelImageFile = Oprofile.getKernelImageFile();
		//_processIdFilter = "";
		//_processGroupFilter = "";
		_verboseLogging = false;
		_separateSamples = SEPARATE_NONE;	
	}
	
	/**
	 * Determines whether the global oprofile options represented by this
	 * object are valid
	 * @return whether the options are valid
	 */
	public boolean isValid()
	{
		return true;
	}		
	
	/**
	 * Saves the global options of this object into the specified launch
	 * configuration
	 * @param config	the launch configuration
	 */
	public void saveConfiguration(ILaunchConfigurationWorkingCopy config)
	{
		config.setAttribute(LaunchPlugin.ATTR_KERNEL_IMAGE_FILE, _kernelImageFile);
		config.setAttribute(LaunchPlugin.ATTR_VERBOSE_LOGGING, _verboseLogging);
		config.setAttribute(LaunchPlugin.ATTR_SEPARATE_SAMPLES, _separateSamples);
	}
	
	/**
	 * Loads this object with the global options in the given launch
	 * configuration
	 * @param config	the launch configuration
	 */
	public void loadConfiguration(ILaunchConfiguration config)
	{
		try
		{
			_kernelImageFile = config.getAttribute(LaunchPlugin.ATTR_KERNEL_IMAGE_FILE, Oprofile.getKernelImageFile());
			_verboseLogging = config.getAttribute(LaunchPlugin.ATTR_VERBOSE_LOGGING, false);
			_separateSamples = config.getAttribute(LaunchPlugin.ATTR_SEPARATE_SAMPLES, SEPARATE_NONE);
		}
		catch (CoreException e)
		{
		}
	}
	
	/**
	 * Method getKernelImageFile.
	 * @return the kernel image file
	 */
	public String getKernelImageFile()
	{
		return _kernelImageFile;
	}
	
	/**
	 * Method getProcessIdFilter().
	 * @return the process id filter in use
	 *
	public String getProcessIdFilter()
	{
		return _processIdFilter;
	}*/
	
	/**
	 * Method getProcessGroupFilter.
	 * @return the process group filter in use
	 *
	public String getProcessGroupFilter()
	{
		return _processGroupFilter;
	}*/
	

	/**
	 * Method getVerboseLogging.
	 * @return whether to be verbose in the daemon log
	 */
	public boolean getVerboseLogging()
	{
		return _verboseLogging;
	}
	
	/**
	 * Method getSeparateSamples.
	 * @return whether and how to separate samples for each distinct application
	 */
	public int getSeparateSamples()
	{
		return _separateSamples;
	}
	
	/**
	 * Sets the kernel image file
	 * @param image	the kernel image file
	 */
	public void setKernelImageFile(String image)
	{
		_kernelImageFile = image;
	}
		
	/**
	 * Sets whether to enable verbose logging in the daemon log
	 * @param b	whether to enable verbose logging
	 */
	public void setVerboseLogging(boolean b)
	{
		_verboseLogging = b;
	}
	
	/**
	 * Sets whether/how to collect separate samples for each distinct application
	 * @param how	one of SEPARATE_NONE, SEPARATE_LIBRARY, SEPARATE_KERNEL, SEPARATE_ALL
	 */
	public void setSeparateSamples(int how)
	{
		_separateSamples = how;
	}
	
	/**
	 * Converts this object into a collection command line arguments suitable
	 * for passing to op_start
	 * @return a Collection of command line arguments
	 */
	public Collection toArguments()
	{
		ArrayList args = new ArrayList();
		args.add(_argKernelImage());
		
		if (getVerboseLogging())
			args.add(_argVerbose());
		args.add(_argSeparateSamples());
		return args;
	}
	
	// returns the cli parameter specifying the kernel image
	private String _argKernelImage()
	{
		return new String(ARG_KERNEL_IMAGE + "=" + getKernelImageFile()); //$NON-NLS-1$
	}

	// returns the cli parameter to specify verbose logging
	private String _argVerbose()
	{
		return new String(ARG_VERBOSE);
	}
	
	// returns the cli parameter for specifying to separate samples for applications
	private String _argSeparateSamples()
	{
		return new String(ARG_SEPARATE_SAMPLES + "=" + ARG_SEPARATE_ARGS[getSeparateSamples()]);
	}
}
