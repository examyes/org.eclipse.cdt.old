package org.eclipse.cdt.dstore.core.util;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.*;
import java.util.*;

public class ObjectSize implements Serializable
{
 private static ObjectSize _os = null;
 
 public static ObjectSize getInstance()
 {
  if (_os == null)
   _os = new ObjectSize();
  return _os;
 }
 
 private int getSize(Object theObject)
 {
  try
  {
   ByteArrayOutputStream ostream = new ByteArrayOutputStream();
   ObjectOutputStream p = new ObjectOutputStream(ostream);
   p.writeObject(theObject);
   p.flush();
   return ostream.toByteArray().length;
  }
  catch (Throwable e) {e.printStackTrace();}
  return -1;
 }
 
 public int testSingle(Object theObject)
 {
  return getSize(new Dummy(theObject, 1, true));
 }
 

 public int testCopies(Object theObject, int iterations)
 {
  return getSize(new Dummy(theObject, iterations, false));
 }

 public int testInstances(Object theObject, int iterations)
 {
  return getSize(new Dummy(theObject, iterations, true));
 }
 
 public class Dummy implements Serializable
 {
  ArrayList theList;
  public Dummy (Object theObject, int iterations, boolean newInstances)
  {
   theList = new ArrayList();
   try
   {
   for (int i=0; i<iterations; i++)
    if (newInstances)
     theList.add(theObject.getClass().newInstance());
    else
     theList.add(theObject);
   }
   catch (Throwable e)
   {}
  }
 }
 


 public static void main(String args[])
 {
  String obj = new String();
  System.out.println("Single Object    => " + ObjectSize.getInstance().testSingle(obj));
  System.out.println("100000 Copies    => " + ObjectSize.getInstance().testCopies(obj,100000));
  System.out.println("100000 Instances => " + ObjectSize.getInstance().testInstances(obj,100000));
 }
 


}


