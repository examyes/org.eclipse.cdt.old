package org.eclipse.cdt.cpp.miners.pa.engine;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;

/**
 * PACallCycle represents a cycle in the call graph. 
 * A cycle contains information about a group of cyclic or recursive functions which
 * call each other.
 */
public class PACallCycle {
 
 private ArrayList _callers;
 private ArrayList _cycleMembers;
 private int _cycleNumber;
 
 /**
  * Constructor
  */
 public PACallCycle(int cycleNumber) {
  _cycleNumber = cycleNumber;
  _cycleMembers = new ArrayList();
  _callers = new ArrayList();
 }
 
 public void addCaller(PACallArc caller) {
  _callers.add(caller);
 }
 
 public void addCycleMember(PACallArc cycleMember) {
  _cycleMembers.add(cycleMember);
 }
 
 public int numberOfCallers() {
  return _callers.size();
 }
 
 public int numberOfCycleMembers() {
  return _cycleMembers.size();
 }
 
 /**
  * Return a cycle function for a given index
  */
 public PATraceFunction getCycleMember(int index) {
 
   if (index >= 0 && index < _cycleMembers.size()) {
    PACallArc callArc = (PACallArc)_cycleMembers.get(index);
    return callArc.getCallee();
   }
   else
    return null;
 }
 
 /**
  * Return the number of cyclic functions in this cycle
  */
 public int numberOfCyclicFunctions() {
 
   int cyclics = 0; 
   for (int i= 0; i < numberOfCycleMembers(); i++) {
    if (getCycleMember(i).isCyclic())
     cyclics++;
   }
   return cyclics;
 }
 
}