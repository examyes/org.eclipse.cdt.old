package com.ibm.cpp.ui.internal.editor;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

public interface IConstants {
	
	public static final String PLUGIN_ID ="com.ibm.cpp.ui.editor";
	public static final String PREFIX = PLUGIN_ID+".";

	/*
	 * content outliner property
	 */
	public static final String P_CONTENT_OUTLINE=PREFIX+"content_outline";

	/**
	 * file extension - also declared in the plugin.xml
	 */
	public static final String P_RESOURCE_EXTENSION_LABEL = "xyz";	
	
	/**
	 * Content Outliner properties
	 */
	public static final String CO_LABEL = "co_label_not_diplayed";
	public static final String CO_ELEMENT_EMPTY_LABEL= "** empty line **";
	
	/**
	 * Property page constants of ContentOutlinerElement
	 */
	public static final String PROPERTY_PAGE_PROP1 = "property1";
	public static final String PROPERTY_PAGE_PROP1_LABEL = "This is a Property";

	/**
	 * Images
	 */
	 public static final String EDITOR_ACTION1_IMAGE = "getstarted_action_icon";
	 public static final String CO_ELEMENT_ENABLE_IMAGE = "co_element_enable";
	 public static final String CO_ELEMENT_DISABLE_IMAGE = "co_element_disable";
}
