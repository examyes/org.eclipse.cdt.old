package org.eclipse.cdt.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

/**
 * Bundle of all images used by the Cpp UI plugin.
 */
public class CppPluginImages {

	private static URL fgIconLocation;

	static 
	{
		try
		{
			fgIconLocation= new URL(org.eclipse.cdt.cpp.ui.internal.CppPlugin.getDefault().getDescriptor().getInstallURL(), "icons/");
		} catch (MalformedURLException ex) {}
	}

	// The plugin registry
	private final static ImageRegistry PLUGIN_REGISTRY= org.eclipse.cdt.cpp.ui.internal.CppPlugin.getDefault().getImageRegistry();
	private static final String T_WIZBAN= "full/wizban/";
	private static final String T_ELCL= "full/clcl16/";

	private static final String NAME_PREFIX= "org.eclipse.cdt.cpp.ui";
	private static final int    NAME_PREFIX_LENGTH= NAME_PREFIX.length();

	// Define image names
	public static final String IMG_ELCL_TRG_ADD= NAME_PREFIX + "add_exe.gif";
	public static final String IMG_ELCL_TRG_BUILD= NAME_PREFIX + "run_exe.gif";
	public static final String IMG_ELCL_TRG_REM= NAME_PREFIX + "remove_exe.gif";
	public static final String IMG_ELCL_TRG_REMALL= NAME_PREFIX + "removeall_exe.gif";
	public static final String IMG_WIZBAN_TRG_NEW= NAME_PREFIX + "newtarget_wiz.gif";
	
	
	// Define images
	public static final ImageDescriptor DESC_ELCL_TRG_ADD= createManaged(T_ELCL, IMG_ELCL_TRG_ADD);
	public static final ImageDescriptor DESC_ELCL_TRG_BUILD= createManaged(T_ELCL, IMG_ELCL_TRG_BUILD);
	public static final ImageDescriptor DESC_ELCL_TRG_REM= createManaged(T_ELCL, IMG_ELCL_TRG_REM);
	public static final ImageDescriptor DESC_ELCL_TRG_REMALL= createManaged(T_ELCL, IMG_ELCL_TRG_REMALL);
	public static final ImageDescriptor DESC_WIZBAN_NEWTARGET_WIZ= createManaged(T_WIZBAN, IMG_WIZBAN_TRG_NEW);

	private static ImageDescriptor create(String prefix, String name) {
		return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
	}
	private static ImageDescriptor createManaged(String prefix, String name) {
			ImageDescriptor result= ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
			PLUGIN_REGISTRY.put(name, result);
			return result;
	}
	public static Image get(String key) {
		return PLUGIN_REGISTRY.get(key);
	}
	private static URL makeIconFileURL(String prefix, String name) {
		StringBuffer buffer= new StringBuffer(prefix);
		buffer.append(name);
		try 
		{
			return new URL(fgIconLocation, buffer.toString());
		} catch (MalformedURLException ex) {
			return null;
		}
	}
}
