package org.eclipse.cdt.cpp.miners.pa.engine;

/**
 * ITraceReader provides an interface to read the trace output.
 */
public interface ITraceReader {

  /**
   * Read and return the next line in the trace output
   */
  public String readLine() throws PAException;
  
  /**
   * Close the trace reader
   */
  public void close();
}