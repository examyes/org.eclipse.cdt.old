package org.eclipse.cdt.cpp.miners.pa.engine;

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