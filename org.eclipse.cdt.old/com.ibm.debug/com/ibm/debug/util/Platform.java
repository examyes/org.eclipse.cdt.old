package com.ibm.debug.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/util/Platform.java, java-util, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:32:52)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public final class Platform
{  //private final static String kCBIBMCopyright="(c) Copyright IBM Corporation 1998";


   // Return true when the operating system is some unix flavour
   public static boolean useSMD()
   {  return _useSMD;
   }

   // Return true when the operating system is any of the windows os's
   public static boolean isWindows()
   {
      // Add more tests in the future
      return _isWindows;
   }

   // Return true when the operating system is some unix flavour
   public static boolean isUnix()
   {
      // Add more tests in the future
      return _isUnix;
   }

   public static boolean isAIX()
   {
      return _isAIX;
   }

   public static boolean isHPUX()
   {
      return _isHPUX;
   }

   public static boolean isLinux()
   {
      return _isLinux;
   }

   public static boolean isOS390()
   {
      return _isOS390;
   }

   public static boolean isSolaris()
   {
      return _isSolaris;
   }

   public static boolean isSCO()
   {
      return _isSCO;
   }

   public static boolean isNT()
   {
      return _isNT;
   }

   public static boolean isWin2000()
   {
      return _isWin2000;
   }

   public static boolean is95()
   {
      return _is95;
   }

   public static boolean is98()
   {
      return _is98;
   }

   public static boolean isOS2()
   {
      return _isOS2;
   }

   public static boolean isIA64()
   {
      return _isIA64;
   }

   public static boolean hasVaHelp()
   {
      return _hasVaHelp;
   }

   // Return true when file names are case sensitive, i.e. two files with
   // same letters but in different case are considered different
   public static boolean isCaseSensitive()
   {
      return isUnix();
   }

   public static boolean filenamesMatch(String fn1, String fn2)
   {
      if(isCaseSensitive())
         return(fn1.equals(fn2));
      else
         return(fn1.equalsIgnoreCase(fn2));
   }

   public static void main(String[] args)
   {
      System.out.println("os.name="+System.getProperties().getProperty("os.name") );

      System.out.println("is Windows: "+ isWindows());
      System.out.println("is NT: "     + isNT());
      System.out.println("is Unix: "   + isUnix());
      System.out.println("is AIX: "    + isAIX());
      System.out.println("is SCO: "    + isSCO());
      System.out.println("is Solaris: "+ isSolaris());
      System.out.println("is Linux: "  + isLinux());
      System.out.println("is OS/390: " + isOS390());
   }
   private static final String  _os   =     System.getProperties().getProperty("os.name");

   private static final boolean _isNT =     _os.equals("Windows NT");
   private static final boolean _isWin2000 =_os.equals("Windows 2000");
   private static final boolean _isHPUX=    _os.equals("HP-UX");
   private static final boolean _is95 =     _os.equals("Windows 95");
   private static final boolean _is98 =     _os.equals("Windows 98");
   private static final boolean _isWindows= _os.startsWith("Windows");//_isNT || _is95 || _is98 || _isWin2000;

   private static final boolean _isAIX =    _os.equals("AIX");
   private static final boolean _isSolaris= _os.equals("Solaris") || _os.equals("SunOS");
   private static final boolean _isSCO =    _os.equals("UnixWare");
   private static final boolean _isLinux =  _os.equals("Linux");
   private static final boolean _isOS390 =  _os.equals("OS/390");
   private static final boolean _isUnix =   _isAIX || _isSolaris || _isLinux || _isSCO || _isHPUX;

   private static final boolean _isOS2 =    _os.equals("OS/2");

   private static final String  _arch =     System.getProperties().getProperty("os.arch");
   private static final boolean _isIA64 =   _arch.equals("IA64");

   private static final boolean _hasVaHelp=        _isWindows || _isAIX;
   private static final boolean _hasLocalJavaPicl= _isAIX || _isNT || _isWin2000 || _isSolaris || _isHPUX;
   private static final boolean _hasLocalCppPicl=  _isAIX || _isNT || _isWin2000 || _isSolaris;

   private static final boolean _useSMD = (_isUnix || System.getProperty("JT_SMD"  )!=null)
                                                  && (System.getProperty("JT_NOSMD")==null);

   private static Platform _dontLetTheClassBeFinalized=new Platform();
}
