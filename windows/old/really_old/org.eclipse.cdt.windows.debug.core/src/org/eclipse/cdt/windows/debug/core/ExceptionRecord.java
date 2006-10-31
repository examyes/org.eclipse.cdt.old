/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.windows.debug.core;

/**
 * @author Doug Schaefer
 *
 */
public class ExceptionRecord {
	
	private int exceptionCode;
    private int exceptionFlags;
    private long exceptionRecord;
    private long exceptionAddress;
    private int numberParameters;
    private long[] exceptionInformation;

    @SuppressWarnings("unused")
    private ExceptionRecord(
    		int exceptionCode,
    		int exceptionFlags,
    		long exceptionRecord,
    		long exceptionAddress,
    		int numberParameters,
    		long[] exceptionInformation) {
    	this.exceptionCode = exceptionCode;
    	this.exceptionFlags = exceptionFlags;
    	this.exceptionRecord = exceptionRecord; 
    	this.exceptionAddress = exceptionAddress;
    	this.numberParameters = numberParameters;
    	this.exceptionInformation = exceptionInformation;
    }

	public int getExceptionCode() {
		return exceptionCode;
	}

	public int getExceptionFlags() {
		return exceptionFlags;
	}

	public long getExceptionRecord() {
		return exceptionRecord;
	}

	public long getExceptionAddress() {
		return exceptionAddress;
	}

	public int getNumberParameters() {
		return numberParameters;
	}

	public long[] getExceptionInformation() {
		return exceptionInformation;
	}

}
