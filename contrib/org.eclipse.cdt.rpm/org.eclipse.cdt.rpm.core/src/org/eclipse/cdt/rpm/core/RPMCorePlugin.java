/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.cdt.rpm.core;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jface.preference.IPreferenceStore;

import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class RPMCorePlugin extends AbstractUIPlugin {
	//The shared instance.
	private static RPMCorePlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	static final String file_sep = System.getProperty("file.separator"); //$NON-NLS-1$
	
	/**
	 * The constructor.
	 */
	public RPMCorePlugin() {
		//super();
		plugin = this;
		try {
			resourceBundle= ResourceBundle.getBundle("org.eclipse.cdt.rpm.core.RPMPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static RPMCorePlugin getDefault() {
		return plugin;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
	}
	
	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}
	
	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return RPMCorePlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle= RPMCorePlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	protected void initializeDefaultPreferences(IPreferenceStore store)
		 {
		  String user_name = System.getProperty("user.name");
		  store.setDefault("IRpmConstants.RPM_WORK_AREA","/var/tmp");
		  store.setDefault("IRpmConstants.USER_WORK_AREA",file_sep + "rpm_workarea");
		  store.setDefault("IRpmConstants.RPM_DISPLAYED_LOG_NAME",".logfilename_"
		  		+ user_name);
		  store.setDefault("IRpmConstants.SPEC_FILE_PREFIX","eclipse_");
		  store.setDefault("IRpmConstants.SRPM_INFO_FILE",file_sep+".srpminfo");
		  store.setDefault("IRpmConstants.RPM_SHELL_SCRIPT","rpmshell.sh");
		  store.setDefault("IRpmConstants.RPM_LOG_NAME","rpmbuild.log");
		  store.setDefault("IRpmConstants.RPM_RESOURCE_FILE",".rpmrc");
		  store.setDefault("IRpmConstants.RPM_MACROS_FILE",".rpm_macros");
		  store.setDefault("IRpmConstants.AUTHOR_NAME",user_name); //$NON-NLS-1$ //$NON-NLS-2$
		  store.setDefault("IRpmConstants.AUTHOR_EMAIL",user_name +"@" + getHostName()); //$NON-NLS-1$ //$NON-NLS-2$
	
		  store.setDefault("IRpmConstants.MAKE_CMD", "/usr/bin/make"); //$NON-NLS-1$ //$NON-NLS-2$
		  store.setDefault("IRpmConstants.RPM_CMD", "/bin/rpm"); //$NON-NLS-1$ //$NON-NLS-2$
		  store.setDefault("IRpmConstants.RPMBUILD_CMD", "/usr/bin/rpmbuild"); //$NON-NLS-1$ //$NON-NLS-2$
		  store.setDefault("IRpmConstants.CHMOD_CMD", "/bin/chmod"); //$NON-NLS-1$ //$NON-NLS-2$
		  store.setDefault("IRpmConstants.CP_CMD", "/bin/cp"); //$NON-NLS-1$ //$NON-NLS-2$
		  store.setDefault("IRpmConstants.DIFF_CMD", "/usr/bin/diff"); //$NON-NLS-1$ //$NON-NLS-2$
		  store.setDefault("IRpmConstants.TAR_CMD", "/bin/tar"); //$NON-NLS-1$ //$NON-NLS-2$
		 }
	/** 
	* Method getHostName gets the name of the host to use as part of the
	* e-mail address for the changelog entry in the spec file.
	* @return String containing the name of the host, "" if error
	*/
   public static String getHostName()
	{
	   String hostname;
		 try {
			 hostname = java.net.InetAddress.getLocalHost().getHostName();
		 } catch (UnknownHostException e) {
			 return "";
		 }
		 // Trim off superflous stuff from the hostname
		 int firstdot = hostname.indexOf("."); //$NON-NLS-1$
		 int lastdot = hostname.lastIndexOf("."); //$NON-NLS-1$
		 // If the two are equal, no need to trim name
		 if (firstdot == lastdot) {
		   return hostname;
		 }
		 String hosttemp = ""; //$NON-NLS-1$
		 String hosttemp2 = hostname;
		 while (firstdot != lastdot) {
		   hosttemp = hosttemp2.substring(lastdot) + hosttemp;
		   hosttemp2 = hostname.substring(0,lastdot);
		   lastdot = hosttemp2.lastIndexOf("."); //$NON-NLS-1$
		 }
		 return hosttemp.substring(1);
	}
}
