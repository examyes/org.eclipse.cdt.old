package org.eclipse.cdt.cpp.miners.pa.engine.functioncheck;


import java.util.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;

/**
 * FunctionCheckCallerToken represents a token in FunctionCheck's callee
 * list. A token has a number and a name.
 */
public class FunctionCheckCalleeToken {

  private String _number;
  private String _name;
  
  // Constructors
  public FunctionCheckCalleeToken() {
    _name = null;
    _number = null;
  }
  
  public FunctionCheckCalleeToken(String number, String name) {
    _number = number;
    _name   = name;
  }
  
  // getter methods
  public String getNumber() {
    return _number;
  }
  
  public String getName() {
    return _name;
  }
  
  // setter methods
  public void setNumber(String number) {
    _number = number;
  }
  
  public void setName(String name) {
    _name = name;
  }
  
  public String toString() {
    return _number + ": " + _name;
  }
  
}