package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/IPICLDebugConstants.java, eclipse, eclipse-dev, 20011128
// Version 1.24 (last modified 11/28/01 15:58:08)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */


public interface IPICLDebugConstants {
	
	/**
	 * PICL Model identifier
	 */
	public static final String PICL_MODEL_IDENTIFIER = "com.ibm.debug.internal.picl";

    /**
	 * The identifier for markers representing line breakpoints for picl engines.
	 */
	public static final String PICL_LINE_BREAKPOINT = "com.ibm.debug.PICLLineBreakpoint";
	/**
	 * The identifier for markers representing function entry breakpoints for picl engines.
	 */
	public static final String PICL_ENTRY_BREAKPOINT = "com.ibm.debug.PICLEntryBreakpoint";

	/**
	 * The identifier for markers representing load occurance breakpoints for picl engines.
	 */
	public static final String PICL_LOAD_BREAKPOINT = "com.ibm.debug.PICLLoadBreakpoint";

	/**
	 * The identifier for markers representing address breakpoints for picl engines.
	 */
	public static final String PICL_ADDRESS_BREAKPOINT = "com.ibm.debug.PICLAddressBreakpoint";

	/**
	 * The identifier for markers representing watch (storage change) breakpoints for picl engines.
	 */
	public static final String PICL_WATCH_BREAKPOINT = "com.ibm.debug.PICLWatchBreakpoint";

	/**
	 * The identifier for markers required for setting monitored expressions.
	 */
	public static final String PICL_MONITORED_EXPRESSION = "com.ibm.debug.MonitoredExpression";
	
	/**
	 * The identifier for markers required for setting monitored expressions on registers.
	 */
	public static final String PICL_MONITORED_REGISTER = "com.ibm.debug.MonitoredRegister";
	
	/**
	 * The identifier for markers representing temporary locations, such as for Run To Location action.
	 */
	public static final String PICL_LOCATION_MARKER = "com.ibm.debug.PICLLocationMarker";

	//-----------------------------------------------------------------------------------
	// The following are attributes associated with PICL breakpoints.  Not all attributes
	// are available for every breakpoint type.   See the plugin.xml for more information
	//-----------------------------------------------------------------------------------

	/**
	 * The line number is part of the marker
	 * This constant is used when there is an error with the line number
	 */
	public static final String LINE_NUMBER = "lineNumber";

	/**
	 * The breakpoint attribute storing the install count
	 * This attribute is an <code>int</code>. 
	 * This represents the number of times this breakpoint has been installed.
	 * If == 0 then the breakpoint has not been installed.
	 * If => 1 then the breakpoint is installed
	 */
	public static final String INSTALL_COUNT = "installCount";
	
	/**
	 * The breakpoint attribute storing the editable value.
	 * This attribute is a <code>boolean</code>. Breakpoints
	 * that can be edited will have a value of true.
	 */
	public static final String EDITABLE = "editable";
	
	/**
	 * The breakpoint attribute that identifies the module name for this
	 * breakpoint.   This is used to set a breakpoint when not associated 
	 * with a project.
	 * This attribute is a <code>String</code>.
	 */
	public static final String MODULE_NAME = "moduleName";

	/**
	 * The breakpoint attribute that identifies the object name for this
	 * breakpoint.   This is used to set a breakpoint when not associated 
	 * with a project.
	 * This attribute is a <code>String</code>.
	 * Other platforms may refer to this as a compile unit
	 */
	public static final String OBJECT_NAME = "objectName";

	/**
	 * The breakpoint attribute that identifies the source file name for this
	 * breakpoint.   This is used to set a breakpoint when not associated 
	 * with a project.
	 * This attribute is a <code>String</code>.
	 */
	public static final String SOURCE_FILE_NAME = "sourceFileName";

	/**
	 * Breakpoint attribute that indicates the thread that this breakpoint is 
	 * to be set on.  
	 * This attribute is a <code>String</code>.
	 * On most platforms the thread is identified as a number
	 */
	public static final String THREAD = "thread";

	/**
	 * The following breakpoint attributes are collectively used to restrict a 
	 * breakpoint being activated.   This is especially useful in a loop.
	 * These attributes are <code>int</code>.
	 * Other platforms may refer to this as a compile unit
	 */
	public static final String EVERY_VALUE = "everyValue";
	public static final String TO_VALUE = "toValue";
	public static final String FROM_VALUE = "fromValue";

	/**
	 * The breakpoint attribute storing the expression value.
	 * This attribute is a <code>String</code>. When a breakpoint has
	 * an expression, the breakpoint does not suspend execution until
	 * the expression is satisfied. 
	 */
	public static final String CONDITIONAL_EXPRESSION = "conditionalExpression";

	/**
	 * The breakpoint attribute storing the deferred value.
	 * This attribute is a <code>boolean</code>. Breakpoints
	 * can be deferred if the compile unit to which it will belong
	 * has not been loaded yet.
	 */
	public static final String DEFERRED = "deferred";

	/**
	 * The breakpoint attribute storing the case sensitive value.
	 * This attribute is a <code>boolean</code>. 
	 * This value determines how engines look up names
	 */
	public static final String CASESENSITIVE = "caseSensitive";

	/**
	 * The breakpoint attribute storing the function name value.
	 * This is used by the Entry breakpoint
	 * This attribute is a <code>String</code>.
	 */
	public static final String FUNCTION_NAME = "functionName";

	/**
	 * The breakpoint attribute storing the value of the number
	 * of bytes (from starting address) to be monitored for storage changes.
	 * This attribute is an <code>int</code>.
	 */
	public static final String NUM_BYTES_MONITORED = "numBytesMonitored";

	/**
	 * The breakpoint attribute storing the address value.
	 * This attribute is either the address for an address breakpoint
	 * or it could also be the start address used in conjunction with
	 * the number of bytes monitored for a watch breakpoint.
	 * This attribute is a <code>String</code>.
	 */
	public static final String ADDRESS_EXPRESSION = "addressExpression";

	/**
	 * The breakpoint attribute that stores the result of setting the 
	 * breakpoint in the debuggee.  If this is not a deferred breakpoint then
	 * this field indicates the status of the last attempt to set the breakpoint.
	 * This attribute is a <code>boolean</code>. 
	 */
	public static final String ERROR = "error";

	/**
	 * The breakpoint attribute that stores the text of the error that resulted from setting the 
	 * breakpoint in the debuggee.  
	 * This is only valid if the "error" attribute is true.
	 * This attribute is a <code>String</code>. 
	 */
	public static final String ERROR_MSGTEXT = "errorMsgText";

	/**
	 * The breakpoint attribute that stores the attribute that was in error.
	 * If null then it is a general.   This field will contain the 
	 * attribute name.  This field is only valid if the "error" attribute is true.
	 * This attribute is a <code>String</code>. 
	 */
	public static final String ERROR_ATTRIBUTE = "errorAttribute";
	
	
	/**
	 * Internal flag used to indicate that this breakpoint must be updated on the engine
	 * This attribute is a <code>boolean</code>
	 */
	
	public static final String UPDATE_BREAKPOINT = "updateBreakpoint";

	/**
	 * The breakpoint attribute that stores a unique ID for the debug target.
	 * This is used by debug targets to determine which one should update the 
	 * error attributes.
	 * This attribute is a <code>String</code>. 
	 */
	public static final String ENGINE_ID_ATTRIBUTE = "engineID";
	
	//-----------------------------------------------------------------------------------
	// The following are attribute lists that can be used to get the attributes for 
	// specific breakpoint types
	//-----------------------------------------------------------------------------------
		
	public static final String[] PICL_LINE_BREAKPOINT_ATTRIBUTES = {EDITABLE,
																	MODULE_NAME,
																	OBJECT_NAME,
																	SOURCE_FILE_NAME,
																	THREAD,
																	EVERY_VALUE,
																	TO_VALUE,
																	FROM_VALUE,
																	CONDITIONAL_EXPRESSION,
																	DEFERRED,
																	CASESENSITIVE,
																	ERROR,
																	ERROR_MSGTEXT,
																	ERROR_ATTRIBUTE,
																	ENGINE_ID_ATTRIBUTE };

	public static final String[] PICL_ENTRY_BREAKPOINT_ATTRIBUTES = {EDITABLE,
																	MODULE_NAME,
																	OBJECT_NAME,
																	SOURCE_FILE_NAME,
																	THREAD,
																	EVERY_VALUE,
																	TO_VALUE,
																	FROM_VALUE,
																	CONDITIONAL_EXPRESSION,
																	DEFERRED,
																	CASESENSITIVE,
																	ERROR,
																	ERROR_MSGTEXT,
																	ERROR_ATTRIBUTE,
																	FUNCTION_NAME,
																	ENGINE_ID_ATTRIBUTE };
																	
	public static final String[] PICL_ADDRESS_BREAKPOINT_ATTRIBUTES = {EDITABLE,
																	MODULE_NAME,
																	OBJECT_NAME,
																	SOURCE_FILE_NAME,
																	THREAD,
																	EVERY_VALUE,
																	TO_VALUE,
																	FROM_VALUE,
																	CONDITIONAL_EXPRESSION,
																	DEFERRED,
																	CASESENSITIVE,
																	ERROR,
																	ERROR_MSGTEXT,
																	ERROR_ATTRIBUTE,
																	ADDRESS_EXPRESSION,
																	ENGINE_ID_ATTRIBUTE };
																	
	public static final String[] PICL_LOAD_BREAKPOINT_ATTRIBUTES = {EDITABLE,
																	ERROR,
																	ERROR_MSGTEXT,
																	ERROR_ATTRIBUTE,
																	MODULE_NAME,
																	THREAD,
																	ENGINE_ID_ATTRIBUTE };
																	
	public static final String[] PICL_WATCH_BREAKPOINT_ATTRIBUTES = {EDITABLE,
																	MODULE_NAME,
																	OBJECT_NAME,
																	SOURCE_FILE_NAME,
																	THREAD,
																	EVERY_VALUE,
																	TO_VALUE,
																	FROM_VALUE,
																	CONDITIONAL_EXPRESSION,
																	DEFERRED,
																	ERROR,
																	ERROR_MSGTEXT,
																	ERROR_ATTRIBUTE,
																	ADDRESS_EXPRESSION,
																	NUM_BYTES_MONITORED,
																	ENGINE_ID_ATTRIBUTE };




	//-----------------------------------------------------------------------------------
	// The following are directory constants
	//-----------------------------------------------------------------------------------

	//public static final String PICL_ICONS = "icons/";

	//-----------------------------------------------------------------------------------
	// Icon constants
	//-----------------------------------------------------------------------------------
	
	//colored views	
	public static final String PICL_ICON_CVIEW_EXCEPTION_DIALOG= "PICL_ICON_CVIEW_EXCEPTION_DIALOG";
	public static final String PICL_ICON_CVIEW_GDB_VIEW= "PICL_ICON_CVIEW_GDB_VIEW";
	public static final String PICL_ICON_CVIEW_MODULES_VIEW= "PICL_ICON_CVIEW_MODULES_VIEW";
	public static final String PICL_ICON_CVIEW_MONITOR_VIEW= "PICL_ICON_CVIEW_MONITOR_VIEW";
	public static final String PICL_ICON_CVIEW_REGISTER_VIEW= "PICL_ICON_CVIEW_REGISTER_VIEW";
	public static final String PICL_ICON_CVIEW_SET_PREFERRED_SOURCE_VIEW= "PICL_ICON_CVIEW_SET_PREFERRED_SOURCE_VIEW";
	public static final String PICL_ICON_CVIEW_SOURCE_VIEW= "PICL_ICON_CVIEW_SOURCE_VIEW";
	public static final String PICL_ICON_CVIEW_STORAGE_VIEW= "PICL_ICON_CVIEW_STORAGE_VIEW";

	//enabled views
	public static final String PICL_ICON_EVIEW_EXCEPTION_DIALOG= "PICL_ICON_EVIEW_EXCEPTION_DIALOG";
	public static final String PICL_ICON_EVIEW_MODULES_VIEW= "PICL_ICON_EVIEW_MODULES_VIEW";
	public static final String PICL_ICON_EVIEW_MONITOR_VIEW= "PICL_ICON_EVIEW_MONITOR_VIEW";
	public static final String PICL_ICON_EVIEW_REGISTER_VIEW= "PICL_ICON_EVIEW_REGISTER_VIEW";
	public static final String PICL_ICON_EVIEW_SET_PREFERRED_SOURCE_VIEW= "PICL_ICON_EVIEW_SET_PREFERRED_SOURCE_VIEW";
	public static final String PICL_ICON_EVIEW_SOURCE_VIEW= "PICL_ICON_EVIEW_SOURCE_VIEW";
	public static final String PICL_ICON_EVIEW_STORAGE_VIEW= "PICL_ICON_EVIEW_STORAGE_VIEW";

	//colored local toolbars (mouseover)
	public static final String PICL_ICON_CLCL_ADD_COMPILED_EXCEPTION= "PICL_ICON_CLCL_ADD_COMPILED_EXCEPTION";
	public static final String PICL_ICON_CLCL_COLLAPSE_ALL= "PICL_ICON_CLCL_COLLAPSE_ALL";
	public static final String PICL_ICON_CLCL_COPY_VIEW_TO_CLIPBOARD= "PICL_ICON_CLCL_COPY_VIEW_TO_CLIPBOARD";
	public static final String PICL_ICON_CLCL_DISABLE_MONITOR= "PICL_ICON_CLCL_DISABLE_MONITOR";
	public static final String PICL_ICON_CLCL_DISABLE_STORAGE= "PICL_ICON_CLCL_DISABLE_STORAGE";
	public static final String PICL_ICON_CLCL_EXPAND_ALL= "PICL_ICON_CLCL_EXPAND_ALL";
 	public static final String PICL_ICON_CLCL_FILTER_MODULES= "PICL_ICON_CLCL_FILTER_MODULES";
	public static final String PICL_ICON_CLCL_MAP_STORAGE= "PICL_ICON_CLCL_MAP_STORAGE";
	public static final String PICL_ICON_CLCL_MONITOR_EXPRESSION= "PICL_ICON_CLCL_MONITOR_EXPRESSION";
	public static final String PICL_ICON_CLCL_PRINT_VIEW= "PICL_ICON_CLCL_PRINT_VIEW";
	public static final String PICL_ICON_CLCL_REMOVE_ALL_MONITORS= "PICL_ICON_CLCL_REMOVE_ALL_MONITORS";
	public static final String PICL_ICON_CLCL_REMOVE_MONITOR= "PICL_ICON_CLCL_REMOVE_MONITOR";
	public static final String PICL_ICON_CLCL_REMOVE_ALL_STORAGE= "PICL_ICON_CLCL_REMOVE_ALL_STORAGE";
	public static final String PICL_ICON_CLCL_REMOVE_STORAGE= "PICL_ICON_CLCL_REMOVE_STORAGE";
	public static final String PICL_ICON_CLCL_RETRY_EXCEPTION= "PICL_ICON_CLCL_RETRY_EXCEPTION";
	public static final String PICL_ICON_CLCL_RUN_EXCEPTION= "PICL_ICON_CLCL_RUN_EXCEPTION";
	public static final String PICL_ICON_CLCL_SHOW_DETAILS= "PICL_ICON_CLCL_SHOW_DETAILS";
	public static final String PICL_ICON_CLCL_SHOW_STORAGE_STYLES= "PICL_ICON_CLCL_SHOW_STORAGE_STYLES";
	public static final String PICL_ICON_CLCL_SORT_MODULES= "PICL_ICON_CLCL_SORT_MODULES";
	public static final String PICL_ICON_CLCL_STEP_DEBUG= "PICL_ICON_CLCL_STEP_DEBUG";
	public static final String PICL_ICON_CLCL_STEP_EXCEPTION= "PICL_ICON_CLCL_STEP_EXCEPTION";
	public static final String PICL_ICON_CLCL_STORAGE_RESET= "PICL_ICON_CLCL_STORAGE_RESET";

	//disabled local toolbars
	public static final String PICL_ICON_DLCL_ADD_COMPILED_EXCEPTION= "PICL_ICON_DLCL_ADD_COMPILED_EXCEPTION";
	public static final String PICL_ICON_DLCL_COLLAPSE_ALL= "PICL_ICON_DLCL_COLLAPSE_ALL";
	public static final String PICL_ICON_DLCL_COPY_VIEW_TO_CLIPBOARD= "PICL_ICON_DLCL_COPY_VIEW_TO_CLIPBOARD";
	public static final String PICL_ICON_DLCL_DISABLE_MONITOR= "PICL_ICON_DLCL_DISABLE_MONITOR";
	public static final String PICL_ICON_DLCL_DISABLE_STORAGE= "PICL_ICON_DLCL_DISABLE_STORAGE";
	public static final String PICL_ICON_DLCL_EXPAND_ALL= "PICL_ICON_DLCL_EXPAND_ALL";
 	public static final String PICL_ICON_DLCL_FILTER_MODULES= "PICL_ICON_DLCL_FILTER_MODULES";
	public static final String PICL_ICON_DLCL_MAP_STORAGE= "PICL_ICON_DLCL_MAP_STORAGE";
	public static final String PICL_ICON_DLCL_MONITOR_EXPRESSION= "PICL_ICON_DLCL_MONITOR_EXPRESSION";
	public static final String PICL_ICON_DLCL_PRINT_VIEW= "PICL_ICON_DLCL_PRINT_VIEW";
	public static final String PICL_ICON_DLCL_REMOVE_ALL_MONITORS= "PICL_ICON_DLCL_REMOVE_ALL_MONITORS";
	public static final String PICL_ICON_DLCL_REMOVE_MONITOR= "PICL_ICON_DLCL_REMOVE_MONITOR";
	public static final String PICL_ICON_DLCL_REMOVE_ALL_STORAGE= "PICL_ICON_DLCL_REMOVE_ALL_STORAGE";
	public static final String PICL_ICON_DLCL_REMOVE_STORAGE= "PICL_ICON_DLCL_REMOVE_STORAGE";
	public static final String PICL_ICON_DLCL_RETRY_EXCEPTION= "PICL_ICON_DLCL_RETRY_EXCEPTION";
	public static final String PICL_ICON_DLCL_RUN_EXCEPTION= "PICL_ICON_DLCL_RUN_EXCEPTION";
	public static final String PICL_ICON_DLCL_SHOW_DETAILS= "PICL_ICON_DLCL_SHOW_DETAILS";
	public static final String PICL_ICON_DLCL_SHOW_STORAGE_STYLES= "PICL_ICON_DLCL_SHOW_STORAGE_STYLES";
	public static final String PICL_ICON_DLCL_SORT_MODULES= "PICL_ICON_DLCL_SORT_MODULES";
	public static final String PICL_ICON_DLCL_STEP_DEBUG= "PICL_ICON_DLCL_STEP_DEBUG";
	public static final String PICL_ICON_DLCL_STEP_EXCEPTION= "PICL_ICON_DLCL_STEP_EXCEPTION";
	public static final String PICL_ICON_DLCL_STORAGE_RESET= "PICL_ICON_DLCL_STORAGE_RESET";

	//enabled local toolbars
	public static final String PICL_ICON_ELCL_ADD_COMPILED_EXCEPTION= "PICL_ICON_ELCL_ADD_COMPILED_EXCEPTION";
	public static final String PICL_ICON_ELCL_COLLAPSE_ALL= "PICL_ICON_ELCL_COLLAPSE_ALL";
	public static final String PICL_ICON_ELCL_COPY_VIEW_TO_CLIPBOARD= "PICL_ICON_ELCL_COPY_VIEW_TO_CLIPBOARD";
	public static final String PICL_ICON_ELCL_DISABLE_MONITOR= "PICL_ICON_ELCL_DISABLE_MONITOR";
	public static final String PICL_ICON_ELCL_DISABLE_STORAGE= "PICL_ICON_ELCL_DISABLE_STORAGE";
	public static final String PICL_ICON_ELCL_EXPAND_ALL= "PICL_ICON_ELCL_EXPAND_ALL";
 	public static final String PICL_ICON_ELCL_FILTER_MODULES= "PICL_ICON_ELCL_FILTER_MODULES";
	public static final String PICL_ICON_ELCL_MAP_STORAGE= "PICL_ICON_ELCL_MAP_STORAGE";
	public static final String PICL_ICON_ELCL_MONITOR_EXPRESSION= "PICL_ICON_ELCL_MONITOR_EXPRESSION";
	public static final String PICL_ICON_ELCL_PRINT_VIEW= "PICL_ICON_ELCL_PRINT_VIEW";
	public static final String PICL_ICON_ELCL_REMOVE_ALL_MONITORS= "PICL_ICON_ELCL_REMOVE_ALL_MONITORS";
	public static final String PICL_ICON_ELCL_REMOVE_MONITOR= "PICL_ICON_ELCL_REMOVE_MONITOR";
	public static final String PICL_ICON_ELCL_REMOVE_ALL_STORAGE= "PICL_ICON_ELCL_REMOVE_ALL_STORAGE";
	public static final String PICL_ICON_ELCL_REMOVE_STORAGE= "PICL_ICON_ELCL_REMOVE_STORAGE";
	public static final String PICL_ICON_ELCL_RETRY_EXCEPTION= "PICL_ICON_ELCL_RETRY_EXCEPTION";
	public static final String PICL_ICON_ELCL_RUN_EXCEPTION= "PICL_ICON_ELCL_RUN_EXCEPTION";
	public static final String PICL_ICON_ELCL_SHOW_DETAILS= "PICL_ICON_ELCL_SHOW_DETAILS";
	public static final String PICL_ICON_ELCL_SHOW_STORAGE_STYLES= "PICL_ICON_ELCL_SHOW_STORAGE_STYLES";
	public static final String PICL_ICON_ELCL_SORT_MODULES= "PICL_ICON_ELCL_SORT_MODULES";
	public static final String PICL_ICON_ELCL_STEP_DEBUG= "PICL_ICON_ELCL_STEP_DEBUG";
	public static final String PICL_ICON_ELCL_STEP_EXCEPTION= "PICL_ICON_ELCL_STEP_EXCEPTION";
	public static final String PICL_ICON_ELCL_STORAGE_RESET= "PICL_ICON_ELCL_STORAGE_RESET";

	//objects
	public static final String PICL_ICON_ACTIVE_BREAKPOINT= "PICL_ICON_ACTIVE_BREAKPOINT";
	public static final String PICL_ICON_FILE= "PICL_ICON_FILE";
	public static final String PICL_ICON_FUNCTION= "PICL_ICON_FUNCTION";
	public static final String PICL_ICON_MODULE= "PICL_ICON_MODULE";
	public static final String PICL_ICON_NO_FILE= "PICL_ICON_NO_FILE";
	public static final String PICL_ICON_PART= "PICL_ICON_PART";
	public static final String PICL_ICON_REGISTER= "PICL_ICON_REGISTER";
	public static final String PICL_ICON_REGISTER_CHANGED= "PICL_ICON_REGISTER_CHANGED";
	public static final String PICL_ICON_REGISTER_GROUP= "PICL_ICON_REGISTER_GROUP";
	public static final String PICL_ICON_SOURCE= "PICL_ICON_SOURCE";
	public static final String PICL_ICON_VARIABLE= "PICL_ICON_VARIABLE";
	public static final String PICL_ICON_VARIABLE_CHANGED= "PICL_ICON_VARIABLE_CHANGED";
	public static final String PICL_ICON_VARIABLE_DISABLED= "PICL_ICON_VARIABLE_DISABLED";

	//wizard banners
	public static final String PICL_ICON_BREAKPOINT_WIZARD= "PICL_ICON_BREAKPOINT_WIZARD";		
	
	
	
	//--------------------------------------------------------------------
	// Register view resources
	//--------------------------------------------------------------------
	
	public static final String EMPTY_REGISTER_GROUP = "emptyRegisterGroup";
	public static final String REGISTER_GROUP = "registerGroup";


}
