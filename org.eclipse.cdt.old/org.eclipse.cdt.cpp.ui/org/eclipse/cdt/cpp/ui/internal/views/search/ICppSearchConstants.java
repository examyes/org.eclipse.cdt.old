package com.ibm.cpp.ui.internal.views.search;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

public interface ICppSearchConstants {

	int UNKNOWN= -1;

	/* nature of searched element */
	int TYPE= 0;
	//int METHOD= 1;
	//int PACKAGE= 2;
	//int CONSTRUCTOR= 3;
	//int FIELD= 4;
	//int CLASS= 5;
	//int INTERFACE= 6;

	/* nature of match */
	//int DECLARATIONS= 0;
	int IMPLEMENTORS= 1;
	int REFERENCES= 2;
}
