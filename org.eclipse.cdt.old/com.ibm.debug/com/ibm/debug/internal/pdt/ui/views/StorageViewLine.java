package com.ibm.debug.internal.pdt.ui.views;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/views/StorageViewLine.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:00:54)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.internal.picl.PICLStorage;


public class StorageViewLine extends Object {
	private PICLStorage fStorage;
	private String fAddress;
	private String f1stByte;
	private String f2ndByte;
	private String f3rdByte;
	private String f4thByte;
	private String fTranslated;

	public static final String P_ADDRESS = "address";
	public static final String P_1STBYTE = "1stByte";
	public static final String P_2NDBYTE = "2ndByte";
	public static final String P_3RDBYTE = "3rdByte";
	public static final String P_4THBYTE = "4thByte";
	public static final String P_TRANSLATED ="translated";
	public static final String[] PROPERTIES = new String[]{P_ADDRESS,P_1STBYTE, P_2NDBYTE, P_3RDBYTE, P_4THBYTE,P_TRANSLATED};


	public StorageViewLine(PICLStorage storage, String address, String firstByte, String secondByte, String thirdByte, String fourthByte, String translated) {
		fStorage = storage;
		fAddress = address;
		f1stByte = firstByte;
		f2ndByte = secondByte;
		f3rdByte = thirdByte;
		f4thByte = fourthByte;
		fTranslated = translated;
	}


	public PICLStorage getStorage() {
		return fStorage;
	}
	public String getAddress() {
		return fAddress;
	}
	public String get1stByte() {
		return f1stByte;
	}
	public String get2ndByte() {
		return f2ndByte;
	}
	public String get3rdByte() {
		return f3rdByte;
	}
	public String get4thByte() {
		return f4thByte;
	}
	public String getTranslated() {
		return fTranslated;
	}
	public void setAddress(String address) {
		fAddress = address;
	}
	public void set1stByte(String firstByte) {
		f1stByte = firstByte;
	}
	public void set2ndByte(String secondByte) {
		f2ndByte = secondByte;
	}
	public void set3rdByte(String thirdByte) {
		f3rdByte = thirdByte;
	}
	public void set4thByte(String fourthByte) {
		f4thByte = fourthByte;
	}
	public void setTranslated(String translated) {
		fTranslated = translated;
	}
}

