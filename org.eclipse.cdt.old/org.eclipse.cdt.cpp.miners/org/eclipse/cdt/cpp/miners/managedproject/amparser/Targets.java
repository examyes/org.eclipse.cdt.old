package com.ibm.cpp.miners.managedproject.amparser;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import java.util.*;

public class Targets
{
 private HashMap _theTargets;
 
 public Targets()
 {
  reset();
 }
 
 private void reset()
 {
  _theTargets = new HashMap();
 }

 public Target addTarget(String name, int type)
 {
  if ( (type < Am.TARGETTYPE_START) || (type > Am.TARGETTYPE_END))
   type = Am.PROGRAMS; //Default to PROGRAMS if no type was specified
  
  Target theTarget = new Target(name, type);
  _theTargets.put(name, theTarget);
  return theTarget;
 }
 
 public void addTargetAttribute(String targetName, int attType, String attValue)
 {
  Target theTarget = (Target)_theTargets.get(targetName);
  if (theTarget == null)
   theTarget = addTarget(targetName, -1);
  theTarget.addAttribute(attType,attValue);
 }

 public ArrayList getTargets()
 {
  return new ArrayList (_theTargets.values());
 }
}

