package com.ibm.dstore.core.util;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import java.io.*;

public class ObjectSize
{
 public static int getSize(Object theObject)
 {
  try
  {
   ByteArrayOutputStream ostream = new ByteArrayOutputStream();
   ObjectOutputStream p = new ObjectOutputStream(ostream);
   p.writeObject(theObject);
   p.flush();
   return ostream.toByteArray().length;
  }
  catch (Throwable e) {}
  return -1;
 }
}
