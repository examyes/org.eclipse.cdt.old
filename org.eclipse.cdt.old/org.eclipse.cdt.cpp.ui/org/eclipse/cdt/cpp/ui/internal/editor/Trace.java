package com.ibm.cpp.ui.internal.editor;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

/**
 * Traces the interaction between the desktop code and the example code.
 * If you want to see the trace make sure the desktop
 * is launched with a console window.
 */

public class Trace {

	/**
	 * Disable tracing by default
	 */
//	private static boolean TRACING = false;
	private static boolean TRACING = true; // *as* *as* *as* *as*
/**
 * The default constructor.
 */
public Trace() {
	super();
}
/**
 * Prints a String to the Standard Output Stream.
 *
 * @param aString String to print.
 * @return String - the printed String.
 */
public static void me(String aClass, String aMethod, String aComment) {
	if (TRACING)
		System.out.println("Tracing -> "+aClass+"#"+aMethod+":"+aComment);
	return;
}
/**
 * Sets the tracing boolean value based on the
 * value found in the XML file.
 * if the value is 'traceOn' then the
 * trace will be on, otherwise the trace
 * will be off
 *
 */
public static void setTrace(Object data) {
	Trace.me("Trace","setTrace","Trace was "+TRACING);	
	TRACING = ("traceOn".equals(data));
	Trace.me("Trace","setTrace","Trace is "+TRACING);
}
}
