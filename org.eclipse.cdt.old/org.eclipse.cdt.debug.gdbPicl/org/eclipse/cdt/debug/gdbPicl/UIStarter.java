/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/UIStarter.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:42:02)   (based on Jde 12/29/98 1.7)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A Class used to start a UI remotely via a UI Daemon 
 */
class UIStarter
{
   UIStarter(String uidHost,
             String uidPort,
             String uidPid,
             String uidTitle,
             String originatingHost,
             String originatingPort,
             String startUpKey,
             String launcher,
             String projName,
             String[] debuggeeArgs)
   {
      this.uidHost         = uidHost;
      this.uidPort         = uidPort;
      this.uidPid          = uidPid;
      this.uidTitle        = uidTitle;
      this.originatingHost = originatingHost;
      this.originatingPort = originatingPort;
      this.startUpKey      = startUpKey;
      this.launcher        = launcher;
      this.projName        = projName;
      this.debuggeeArgs    = debuggeeArgs;
   }

   /** Tell this UIStarter whether the UI should request an attach or not */
   void setPerformAttach(boolean performAttach)
   {
      this.performAttach = performAttach;
   }

   /** Tell this UIStarter whether the UI should request an autostart or not */
   void setAutoStart(boolean autoStart)
   {
      this.autoStart = autoStart;
   }

   /**
    * Connect to a UI Daemon process and request a UI be initiated
    */
   boolean startUI()
   {
      Socket socket = null;

      try
      {
         socket = new Socket(uidHost, Integer.parseInt(uidPort));
      }
      catch (UnknownHostException e)
      {
         return false;
      }
      catch (NumberFormatException e)
      {
         return false;
      }
      catch (IOException e)
      {
         return false;
      }
      
      try
      {
         ByteArrayOutputStream tmpOut;
         OutputStream sockOut;
         int totalLength = 0;

         tmpOut  = new ByteArrayOutputStream();
         sockOut = socket.getOutputStream();

         // The +1 is because we'll always send -qengine :

         int argCount = debuggeeArgs.length + 1;

         if (performAttach)
             argCount++;

         if (autoStart)
             argCount++;
             
         if (startUpKey.length() > 0)
             argCount++;
         
         if (launcher.length() > 0)
             argCount++;
             
         if (projName.length() > 0)
             argCount++;

         // totalLength counts the total number of bytes in our strings
         totalLength += writeAsciiString(tmpOut,originatingHost + "\n");
         totalLength += writeAsciiString(tmpOut,originatingPort + "\n");
         totalLength += writeAsciiString(tmpOut,uidTitle + "\n");
         totalLength += writeAsciiString(tmpOut,argCount + "\n");
                
         if (performAttach)
             totalLength += writeAsciiString(tmpOut,"-a" + uidPid + "\n");

         if (autoStart)
             totalLength += writeAsciiString(tmpOut,"-s\n");

         totalLength += writeAsciiString(tmpOut,"-qengine=cpppicl\n");
         
         if (startUpKey.length() > 0)
			totalLength += writeAsciiString(tmpOut, startUpKey + "\n");
         
         if (launcher.length() > 0)
            totalLength += writeAsciiString(tmpOut, launcher + "\n");
             
         if (projName.length() > 0)
	         totalLength += writeAsciiString(tmpOut, projName + "\n");

         for (int j=0;j<debuggeeArgs.length;j++)
         {       	
            //below will take care of parameters with embedded space
            debuggeeArgs[j] = "\"" + debuggeeArgs[j] + "\"";
            totalLength += writeAsciiString(tmpOut,debuggeeArgs[j] + "\n");
         }
         
         // Reverse the byte order of our total length and spit it out
         byte[] lengthBytes = new byte[4];

         short low  = (short) totalLength;
         short high = (short) (totalLength >> 16);

         lengthBytes[3] = (byte) low;
         lengthBytes[2] = (byte) (low >> 8);
         lengthBytes[1] = (byte) high;
         lengthBytes[0] = (byte) (high >> 8);

         sockOut.write(lengthBytes,0,4);
         tmpOut.writeTo(sockOut);
         sockOut.flush();

         socket.close();
      }
      catch (IOException e)
      {
         return false;
      }

      return true;
   }

   private int writeAsciiString(ByteArrayOutputStream os, String str)
   {
      byte[] bytes;
      try
      {
         bytes = str.getBytes("8859_1");
      }
      catch (UnsupportedEncodingException e)
      {
         bytes = str.getBytes();
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(1,Gdb.getResourceString("ASCII_ENCODING_NOT_SUPPORTED") );
      }
      os.write(bytes,0,bytes.length);
      return bytes.length;
   }

   private String uidHost;
   private String uidPort;
   private String uidPid;
   private String uidTitle;
   private String originatingHost;
   private String originatingPort;
   private String startUpKey;
   private String launcher;
   private String projName;
   private String[] debuggeeArgs;
   private boolean performAttach;
   private boolean autoStart;
}
