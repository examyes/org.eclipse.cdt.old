/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

/**
 * Class which stores method information
 */
public class MethodInfo
{
   public String _name;             // abbreviated method name
   public String _fullName;         // full method name
   public String _returnType;
   public int _lineNum;             // first executable line in the method
   public int _startLineNum;        // starting line number for the method
}

