package org.eclipse.cdt.cpp.miners.pa.engine.gprof;

import java.util.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;

/**
 * GprofCallGraphEntry represents an entry in gprof's call graph.
 * Entries are separated by dash-lines. Each entry has one primary line
 * which has a leading number in brackets. An entry may also have 
 * caller and subroutine lines.
 */
public class GprofCallGraphEntry {

  private PATraceFunction _entryFunction;
  private PACallCycle _callCycle;
  private PATraceFile _traceFile;
  private int _entryIndex;
  private ArrayList _callers;
  
  // Constructor
  public GprofCallGraphEntry(PATraceFile traceFile) {
   _traceFile = traceFile;
   _entryFunction = null;
   _callCycle = null;
   _entryIndex = 0;
   _callers = new ArrayList();
  }
  
  /**
   * Add a caller line to the call graph entry
   */
  public void addCallerLine(String line) throws Exception {
  
   if (line.equals("<spontaneous>"))
    return;
    
   PACallArc anArc = createCallArcForEntry(line, true);
   _callers.add(anArc);     
           
  }
  
  /**
   * Add the primary line to the call graph entry
   */
  public void addPrimaryLine(String line) throws Exception {
  
   // Error detection
   if (_entryFunction != null || _callCycle != null) {
    throw new PAException("Primary line cannot be added twice.");
   }
   
   PATokenizer tokenizer = new PATokenizer(line, 6);
   
   if (!GprofUtility.isValidCallNumber(tokenizer.getToken(4))) {
    tokenizer = new PATokenizer(line, 5);
   }
   
   if (tokenizer.getTokenNumber() < 5) {
    throw new PAException("Invalid call graph primary line: " + line);
   }
   
   // Get the entry index
   String firstToken = tokenizer.getToken(0);
   if (firstToken.charAt(0) == '[' && firstToken.charAt(firstToken.length()-1) == ']') {
    _entryIndex = Integer.parseInt(firstToken.substring(1, firstToken.length()-1));
   }
   
   // Get the function name
   String lastToken = tokenizer.getLastToken();
   String entryFunctionName = GprofUtility.trimmedFunctionName(lastToken);
   
   // Detect whether this primary line is for a cycle or a regular function
   if (entryFunctionName.startsWith("<cycle")) {
    int asIndex = entryFunctionName.indexOf("as");
    int cycleIndex = Integer.parseInt(entryFunctionName.substring(7, asIndex).trim());
    _callCycle = new PACallCycle(cycleIndex);
    _traceFile.addCallCycle(_callCycle);
    
    Iterator it = _callers.iterator();
    while (it.hasNext()) {
     PACallArc anArc = (PACallArc)it.next();
     _callCycle.addCaller(anArc);
    }
   }
   else {
    _entryFunction = _traceFile.findOrCreateTraceFunction(entryFunctionName);
    
    if (GprofUtility.isCyclicFunction(lastToken)) {
     _entryFunction.setCyclic(true);
    }
    
    Iterator it = _callers.iterator();
    while (it.hasNext()) {
     PACallArc anArc = (PACallArc)it.next();
     anArc.setCallee(_entryFunction);
     
     if (!_entryFunction.isCalleeOf(anArc.getCaller())) {
      anArc.getCaller().addCallee(anArc);     
      _entryFunction.addCaller(anArc);
     }
     
    }
   }
   
  }
  
  /**
   * Add a subroutine line to the call graph entry
   */
  public void addSubroutineLine(String line) throws Exception {
     
   PACallArc anArc = createCallArcForEntry(line, false);
   
   PATraceFunction subroutine = anArc.getCallee();
   if (_callCycle != null) {
    _callCycle.addCycleMember(anArc);
   }
   else if (_entryFunction != null && !subroutine.isCalleeOf(_entryFunction)) {   
    _entryFunction.addCallee(anArc);
    subroutine.addCaller(anArc);
   }
   
  }
  
  
  /**
   * Create a call arc for a caller/subroutine line in the call graph
   */
  private PACallArc createCallArcForEntry(String line, boolean isCaller) throws Exception {
  
   PATokenizer tokenizer = new PATokenizer(line, 4);
   
   double selfTime = 0;
   double childrenTime = 0;
   int callNumber = 0;
   
   // First assume this line has entries for children time and self time.
   // If this is not true, we need to reparse the line.
   boolean needToReparse = false;
   if (tokenizer.getTokenNumber() == 4) {
   
    try {
     selfTime = tokenizer.getTokenAsDouble(0);
     childrenTime = tokenizer.getTokenAsDouble(1);
     callNumber = GprofUtility.getFirstCallNumber(tokenizer.getToken(2));
    }
    catch (Exception e) {
     needToReparse = true;
    }
    
   }
   else {
    needToReparse = true;
   }
   
   // If we see an error in the first parse, we will reparse the line
   // assuming that it only contains the call number and function name.
   if (needToReparse) {
   
    tokenizer = new PATokenizer(line, 2);
    if (tokenizer.getTokenNumber() < 2) {
     throw new PAException("Invalid call graph line: " + line);
    }
    else {     
     callNumber = GprofUtility.getFirstCallNumber(tokenizer.getToken(0));
    }
    
   } 
   
   // Find or create a PA trace function from the function name
   String lastToken = tokenizer.getLastToken();
   String functionName = GprofUtility.trimmedFunctionName(lastToken);
   PATraceFunction traceFunction = _traceFile.findOrCreateTraceFunction(functionName);
   
   if (GprofUtility.isCyclicFunction(lastToken)) {
    traceFunction.setCyclic(true);
   }

   // Create the call arc in different ways depending on whether this is a caller or 
   // subroutine line and the number of tokens.
   PACallArc callArc = null;
   if (isCaller) {
   
     if (tokenizer.getTokenNumber() == 4) {
      callArc = new PACallArc(traceFunction, _entryFunction, callNumber, selfTime, childrenTime);
     }
     else {
      callArc = new PACallArc(traceFunction, _entryFunction);
      callArc.setCallNumber(callNumber);
     }
   
   }
   else {

     if (tokenizer.getTokenNumber() == 4) {
      callArc = new PACallArc(_entryFunction, traceFunction, callNumber, selfTime, childrenTime);
     }
     else {
      callArc = new PACallArc(_entryFunction, traceFunction);
      callArc.setCallNumber(callNumber);
     }
   
   }
   
   return callArc;
   
  }
  
  /**
   * Return the entry index
   */
  public int getEntryIndex() {
   return _entryIndex;
  }
      
}