package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLUtils.java, eclipse, eclipse-dev, 20011129a
// Version 1.18 (last modified 11/29/01 16:51:17)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.ibm.debug.PICLDebugPlugin;


/**
 * This class serves as a location for utility methods for the PICL.
 */
public class PICLUtils {

	/**
	 * Debug setting..if <code>true</code> internal errors are logged to
	 * the standard out.
	 */
	protected static final boolean DEBUG= false;


	private static boolean fLogging = false;

	/**
	 * The resource bundle within which all PICL resource Strings are held
	 */
	private static ResourceBundle fgResourceBundle;
	/**
	 * The resource bundle within which all PICL help id Strings are held
	 */
	private static ResourceBundle helpResourceBundle;
	/**
	 * The image registry which holds <code>Image</code>s
	 */
	private static ImageRegistry imageRegistry;
	/**
	 * A table of all the <code>ImageDescriptor</code>s
	 */
	private static HashMap imageDescriptors;

	private final static String CVIEW = "cview16/";
	private final static String EVIEW = "eview16/";
	private final static String CTOOL = "ctool16/";
	private final static String CLCL = "clcl16/";
	private final static String DLCL = "dlcl16/";
	private final static String ELCL = "elcl16/";
	private final static String OBJECT = "obj16/";
	private final static String WIZBAN = "wizban/";


	private static URL ICON_BASE_URL = null;
	static {
		try {
                    if(Display.getCurrent() != null)
                    {
        			if (Display.getCurrent().getIconDepth() > 4) {
        				ICON_BASE_URL = new URL(PICLDebugPlugin.getInstance().getDescriptor().getInstallURL(), "icons/full/");
        			} else {
        				ICON_BASE_URL = new URL(PICLDebugPlugin.getInstance().getDescriptor().getInstallURL(), "icons/basic/");
        			}
                    }
		} catch (MalformedURLException me) {}
		try {
			if (System.getProperty("EVENTS") != null)
				fLogging = true;
		} catch(Exception e) {}
	}


	/**
	 * Retrieve the requested String resource.  Practice lazy retrieval on the resource bundle.
	 */
	public static String getResourceString(String key) {
		if (fgResourceBundle == null) {
			fgResourceBundle= getResourceBundle();
		}
		if (fgResourceBundle != null) {
			return fgResourceBundle.getString(key);
		} else {
			return "!" + key + "!";
		}
	}

	/**
	 * Retrieve the requested help id.  Practice lazy retrieval on the resource bundle.
	 */
	public static String getHelpResourceString(String key) {
		if (helpResourceBundle == null) {
			helpResourceBundle= getHelpResourceBundle();
		}
		if (helpResourceBundle != null) {
			return helpResourceBundle.getString(key);
		} else {
			return "!" + key + "!";
		}
	}

	/**
	 * Plug in the single argument to the resource String for the key to get a formatted resource String
	 */
	public static String getFormattedString(String key, String arg) {
		String string= getResourceString(key);
		return MessageFormat.format(string, new String[] { arg });
	}

	/**
	 * Plug in the arguments to the resource String for the key to get a formatted resource String
	 */
	public static String getFormattedString(String key, String[] args) {
		String string= getResourceString(key);
		return MessageFormat.format(string, args);
	}

	/**
	 * Returns the resource bundle used by all parts of the debug ui package.
	 */
	public static ResourceBundle getResourceBundle() {
		try {
			return ResourceBundle.getBundle("com.ibm.debug.internal.picl.PICLResources");
		} catch (MissingResourceException e) {
		}
		return null;
	}

	/**
	 * Returns the resource bundle used by all parts of the debug ui package.
	 */
	public static ResourceBundle getHelpResourceBundle() {
		try {
			return ResourceBundle.getBundle("com.ibm.debug.internal.pdt.ui.util.HelpResources");
		} catch (MissingResourceException e) {
		}
		return null;
	}

	/**
	 * Convenience method to log internal errors
	 */
	public static void logError(Exception e) {
		if (DEBUG) {
			System.out.println("Internal error logged from internal debug PICL: ");
			e.printStackTrace();
			System.out.println();
		}
	}

	public static void logText(String text) {
		if (fLogging)
			System.out.println(text);
	}


	/**
	 * Use to log events so that they are controlled by the property "EVENTS"
	 */
	public static void logEvent(String text, Object originator) {
		if (fLogging) {
			System.out.println("EVENT(" + getBaseName(originator) + ")-> " + text);
		}
	}

	/**
	 * Returns the base name of a fully qualified class name.
	 */
	public static String getBaseName(Object obj) {
		if (obj == null)
			return null;
		else {
			String qualifiedClassName = obj.getClass().getName();
			return qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".") + 1);
		}
	}

	/**
	 * Returns a string that is used to identify an object as being associated with the IBM debugger plugin
	 * NOTE: this returns the same value as IDebugElement.getModelIdentifier()
	 * @return String that represents the modelIdentifier
	 * @see PICLDebugElement
	 */

	public static String getModelIdentifier() {
		return "com.ibm.debug.internal.picl";
	}


	public static ImageRegistry getImageRegistry() {
		if (imageRegistry == null)
			imageRegistry = new ImageRegistry();
		return imageRegistry;
	}

	public static ImageRegistry initializeImageRegistry() {
		imageRegistry = new ImageRegistry();
		imageDescriptors = new HashMap(30);
		declareImages();
		return imageRegistry;
	}

	private final static void declareRegistryImage(String key, String path) {
		ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
		try {
			desc = ImageDescriptor.createFromURL(makeIconFileURL(path));
		} catch (MalformedURLException me) { }
		imageRegistry.put(key, desc);
		imageDescriptors.put(key, desc);
	}

	private static URL makeIconFileURL(String iconPath) throws MalformedURLException {
		if (ICON_BASE_URL == null) {
			throw new MalformedURLException();
		}
		return new URL(ICON_BASE_URL, iconPath);
	}

	public static ImageDescriptor getImageDescriptor(String key) {
		if (imageDescriptors == null) {
			initializeImageRegistry();
		}
		return (ImageDescriptor)imageDescriptors.get(key);
	}

	public static Image getImage(String key) {
		if (imageRegistry == null) {
			initializeImageRegistry();
		}
		return getImageRegistry().get(key);
	}

	private static void declareImages() {

		//Colored views
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CVIEW_EXCEPTION_DIALOG, CVIEW + "exception_misc.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CVIEW_GDB_VIEW, CVIEW + "gdbView.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CVIEW_MODULES_VIEW, CVIEW + "modules_view.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CVIEW_MONITOR_VIEW, CVIEW + "monitor_view.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CVIEW_REGISTER_VIEW, CVIEW + "register_view.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CVIEW_SET_PREFERRED_SOURCE_VIEW, CVIEW + "setpreferredsourceview_ps.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CVIEW_SOURCE_VIEW, CVIEW + "source_view.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CVIEW_STORAGE_VIEW, CVIEW + "storage_view.gif");

		//Enabled views
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_EVIEW_EXCEPTION_DIALOG, EVIEW + "exception_misc.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_EVIEW_MODULES_VIEW, EVIEW + "modules_view.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_EVIEW_MONITOR_VIEW, EVIEW + "monitor_view.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_EVIEW_REGISTER_VIEW, EVIEW + "register_view.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_EVIEW_SET_PREFERRED_SOURCE_VIEW, EVIEW + "setpreferredsourceview_ps.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_EVIEW_SOURCE_VIEW, EVIEW + "source_view.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_EVIEW_STORAGE_VIEW, EVIEW + "storage_view.gif");

		//Colored local toolbars (mouseover)
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_ADD_COMPILED_EXCEPTION, CLCL + "addcompiledexception_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_COLLAPSE_ALL, CLCL + "collapseall_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_COPY_VIEW_TO_CLIPBOARD, CLCL + "copyviewtoclipboard_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_DISABLE_MONITOR, CLCL + "disablemonitor_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_DISABLE_STORAGE, CLCL + "disablestorage_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_EXPAND_ALL, CLCL + "expandall_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_FILTER_MODULES, CLCL + "filtermodules_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_MAP_STORAGE, CLCL + "mapstorage_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_MONITOR_EXPRESSION, CLCL + "monitorexpression_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_PRINT_VIEW, CLCL + "printview_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_REMOVE_ALL_MONITORS, CLCL + "removeallmonitors_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_REMOVE_MONITOR, CLCL + "removemonitor_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_REMOVE_ALL_STORAGE, CLCL + "removeallstorage_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_REMOVE_STORAGE, CLCL + "removestorage_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_RETRY_EXCEPTION, CLCL + "retryexception_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_RUN_EXCEPTION, CLCL + "runexception_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_SHOW_DETAILS, CLCL + "showdetails_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_SHOW_STORAGE_STYLES, CLCL + "showstoragestyles_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_SORT_MODULES, CLCL + "sortmodules_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_STEP_DEBUG, CLCL + "stepdebug_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_STEP_EXCEPTION, CLCL + "stepexception_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_CLCL_STORAGE_RESET, CLCL + "storagereset_tsk.gif");

		//Disabled local toolbars
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_ADD_COMPILED_EXCEPTION, DLCL + "addcompiledexception_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_COLLAPSE_ALL, DLCL + "collapseall_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_COPY_VIEW_TO_CLIPBOARD, DLCL + "copyviewtoclipboard_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_DISABLE_MONITOR, DLCL + "disablemonitor_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_DISABLE_STORAGE, DLCL + "disablestorage_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_EXPAND_ALL, DLCL + "expandall_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_FILTER_MODULES, DLCL + "filtermodules_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_MAP_STORAGE, DLCL + "mapstorage_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_MONITOR_EXPRESSION, DLCL + "monitorexpression_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_PRINT_VIEW, DLCL + "printview_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_REMOVE_ALL_MONITORS, DLCL + "removeallmonitors_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_REMOVE_MONITOR, DLCL + "removemonitor_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_REMOVE_ALL_STORAGE, DLCL + "removeallstorage_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_REMOVE_STORAGE, DLCL + "removestorage_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_RETRY_EXCEPTION, DLCL + "retryexception_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_RUN_EXCEPTION, DLCL + "runexception_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_SHOW_DETAILS, DLCL + "showdetails_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_SHOW_STORAGE_STYLES, DLCL + "showstoragestyles_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_SORT_MODULES, DLCL + "sortmodules_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_STEP_DEBUG, DLCL + "stepdebug_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_STEP_EXCEPTION, DLCL + "stepexception_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_DLCL_STORAGE_RESET, DLCL + "storagereset_tsk.gif");

		//Enabled local toolbars
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_ADD_COMPILED_EXCEPTION, ELCL + "addcompiledexception_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_COLLAPSE_ALL, ELCL + "collapseall_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_COPY_VIEW_TO_CLIPBOARD, ELCL + "copyviewtoclipboard_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_DISABLE_MONITOR, ELCL + "disablemonitor_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_DISABLE_STORAGE, ELCL + "disablestorage_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_EXPAND_ALL, ELCL + "expandall_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_FILTER_MODULES, ELCL + "filtermodules_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_MAP_STORAGE, ELCL + "mapstorage_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_MONITOR_EXPRESSION, ELCL + "monitorexpression_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_PRINT_VIEW, ELCL + "printview_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_REMOVE_ALL_MONITORS, ELCL + "removeallmonitors_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_REMOVE_MONITOR, ELCL + "removemonitor_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_REMOVE_ALL_STORAGE, ELCL + "removeallstorage_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_REMOVE_STORAGE, ELCL + "removestorage_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_RETRY_EXCEPTION, ELCL + "retryexception_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_RUN_EXCEPTION, ELCL + "runexception_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_SHOW_DETAILS, ELCL + "showdetails_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_SHOW_STORAGE_STYLES, ELCL + "showstoragestyles_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_SORT_MODULES, ELCL + "sortmodules_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_STEP_DEBUG, ELCL + "stepdebug_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_STEP_EXCEPTION, ELCL + "stepexception_tsk.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ELCL_STORAGE_RESET, ELCL + "storagereset_tsk.gif");

		//Objects
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_ACTIVE_BREAKPOINT, OBJECT + "activebreakpoint_obj.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_FILE, OBJECT + "file_obj.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_FUNCTION, OBJECT + "function_obj.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_MODULE, OBJECT + "module_obj.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_NO_FILE, OBJECT + "nofile_obj.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_PART, OBJECT + "part_obj.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_REGISTER, OBJECT + "register_obj.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_REGISTER_CHANGED, OBJECT + "registerchanged_obj.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_REGISTER_GROUP, OBJECT + "registergroup_obj.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_SOURCE, OBJECT + "source_obj.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_VARIABLE, OBJECT + "variable_obj.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_VARIABLE_CHANGED, OBJECT + "variablechanged_obj.gif");
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_VARIABLE_DISABLED, OBJECT + "variabledisabled_obj.gif");

		//Wizard banners
		declareRegistryImage(IPICLDebugConstants.PICL_ICON_BREAKPOINT_WIZARD, WIZBAN + "breakpoint_wiz.gif");
	}


	/**
	 * Returns a String that is the current path to the this plugin's directory
	 * This is more accurate that asking the plugin directly since it seems to always give back "plugin"
	 * @return a string that contains the full path to the debugger's plugin directory
	 */
	public static String getPluginPath() {
		return ((PluginDescriptor)PICLDebugPlugin.getInstance().getDescriptor()).getInstallURLInternal().getPath();
	}
}
