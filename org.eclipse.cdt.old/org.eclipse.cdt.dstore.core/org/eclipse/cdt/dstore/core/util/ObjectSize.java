package com.ibm.dstore.core.util;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
 
 public int getSize(Object theObject)
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

 public int test(Object theObject, int iterations)
 {
  return getSize(new Dummy(theObject, iterations));
 }

 public class Dummy implements Serializable
 {
  ArrayList theList;
  public Dummy (Object theObject, int iterations)
  {
   theList = new ArrayList();
   for (int i=0; i<iterations; i++)
    theList.add(theObject);
  }
 }
}
