package org.eclipse.cdt.cpp.miners.pa.engine;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


/**
 * A PAException is thrown when the PA engine encounters an error.
 */
public class PAException extends Exception
{

 /**
  * Constructor
  */
 public PAException (String msg) {
	super(msg);
 }
 
}