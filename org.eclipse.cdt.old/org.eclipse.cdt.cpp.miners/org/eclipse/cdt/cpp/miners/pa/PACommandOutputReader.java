package org.eclipse.cdt.cpp.miners.pa;


import java.util.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;


public class PACommandOutputReader implements ITraceReader {

 private DataElement _status;
 private DataStore _dataStore;
 private String _commandValue;
 private ArrayList _outputList;
 private int _index;
 
 // Constructor
 public PACommandOutputReader(DataElement status) {
  _status = status;
  _dataStore = status.getDataStore();
  _commandValue = _status.getParent().getAttribute(DE.A_VALUE);
  _outputList = _status.getAssociated("contents");
  _index = 0;
 }
 
 public String readLine() throws PAException {
 
  if (_index < _outputList.size()) {
   DataElement element = ((DataElement)_outputList.get(_index)).dereference();
   _index++;
   
   if (element != null)
    return element.getAttribute(DE.A_VALUE);
   else
    return null;
    
  }
  else
   return null;
 }
 
 public String getCommandValue() {
  return _commandValue;
 }
 
 public void close() {
 
 }
 
}