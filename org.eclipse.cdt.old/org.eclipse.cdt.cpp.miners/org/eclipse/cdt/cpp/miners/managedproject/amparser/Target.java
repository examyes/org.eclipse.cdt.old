package com.ibm.cpp.miners.managedproject.amparser;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;

public class Target
{
 private String      _name;
 private int         _type;
 private ArrayList   _attributes; //Really an ArrayList of ArrayLists of Strings
 
 public Target(String name, int type)
 {
  _name = name;
  _type = type;
  _attributes = new ArrayList();
  for(int i=Am.ATTRIBUTE_START; i<=Am.ATTRIBUTE_END; i++)
   _attributes.add(new ArrayList());
 }

 public void addAttribute(int attType, String attValue)
 {
  ArrayList theAttribute = (ArrayList)_attributes.get(attType);
  theAttribute.add(attValue);
 }

 public ArrayList getAttribute(int attType)
 {
  ArrayList theAttribute = (ArrayList)_attributes.get(attType);
  return theAttribute;
 }

 public String getName()
 {
  return _name;
 }

 public int getType()
 {
  return _type;
 }
}
