/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import com.ibm.debug.connection.*;
import com.ibm.debug.epdc.*;      // EPDC classes
import com.ibm.debug.util.*;

/**
 * Java Debug Engine (JDE) main class.
 *
 * Note: All back end messages printed to the console should be taken from the
 * getConsoleResourceString(String key) method.  All messages sent to the
 * front end should be taken from the getResourceString(String key) method.
 * The locale setting for the front end may be different from that of the
 * back end but the back end will honor the front end's locale (given to us
 * via InitializeDE request)
 *
 * @see CmdInitializeDE
 */
public class Gdb
{

  /**
   * Start a java debugger.
   */
   public static void main(String args[])
   {
      startDebugger(args);
   }

  static void initDebugOutput()
  {
    // Allow debug output to be turned on with -DJT_EVT=n
    String dbgLevel = System.getProperty("JT_EVT");
    if (dbgLevel != null && Integer.parseInt(dbgLevel) > 0)
       showDebugOutput = true;

    // Initialize the TraceLogger
    if (traceLogger == null)
       traceLogger = new TraceLogger("JDE-GDB");
  }

  /*
   * This method should be used for debugger informational messages only.
   * This type of output will be supressed by the -qquiet
   * options.
   */
  static void msgOutput(String s)
  {
     if (!hideInformationalOutput)
     {
        traceLogger.msg(1,s);
     }
  }

  /*
   * This method should be used for end-user debugger error/warning messages only.
   */
  static void errorOutput(String s)
  {
//     if (traceLogger.ERR)
        traceLogger.err(1,s);
  }

  /**
   * This method should be used for internal debug messages only.  This type
   * of output is turned on with -dbg.  This
   * method should be replaced in the future with the appropriate
   * traceLogger.msg/err/evt calls directly.
   */
  public static void debugOutput(String s)
  {
    if (showDebugOutput)
    { if(s.startsWith("EPDC_CMD> "))
      {
          if(traceLogger.getEventLevel()>=1)
             traceLogger.msg(1,"");
          traceLogger.evt(1,">>>>>>>>>>>>>>>> "+s);
      }
      else
          traceLogger.evt(3,s);
    }
    if (_dbgLog != null) {
      _dbgLog.println("JDE-GDB> " + s);
    }
  }

  /*
   * This method should be used for exceptions that we want to see only when
   * debug output is requested.
   */
  public static void debugExceptionOutput(Exception e)
  {
     debugOutput("JDE-GDB> Handled Exception: " + e.toString() + " msg=" +
                  e.getMessage());
     StringWriter sw = new StringWriter();
     e.printStackTrace(new PrintWriter(sw));
     debugOutput(sw.toString());
  }

  /**
   * Called when an unexpected exception occurs.
   */
  public synchronized static void handleException(Exception e)
  {
     if (e instanceof SocketException)
     {
        // We've either lost the connection to the remote jvm or
        // the connection to our client.  In either case, we're toasted.
        // Exit gracefully.
        if (traceLogger.ERR)
            traceLogger.err(1,getConsoleResourceString("CONNECTION_LOST_MSG") );
     }
     else if (e instanceof InterruptedException)
     {
        if (traceLogger.ERR)
            traceLogger.err(2,"WARNING: InterruptedException intercepted and ignored");
     }
     else
     {
        if (traceLogger.ERR)
            traceLogger.err(1,getConsoleResourceString("UNHANDLED_EXCEPTION")+" ("+e.toString()+" msg="+e.getMessage()+")"  );

        Thread.currentThread().dumpStack();
//        StringWriter sw = new StringWriter();
//        e.printStackTrace(new PrintWriter(sw));
//        if (traceLogger.ERR)
//            traceLogger.err(1,getConsoleResourceString("UNHANDLED_EXCEPTION_TRACE")+sw.toString() );
     }
  }

  /**
   * Get a resource string that is to be printed to the console the back end
   * is running on.  The locale will match the system's default locale
   * setting (or user specific setting if -lang was used to override)
   */
   synchronized static String getConsoleResourceString(String key) {
      String msg;
      if (_backEndMessages != null)
      {
         try
         {
            msg = _backEndMessages.getString(key);
         }
         catch (MissingResourceException e)
         {
            msg = getResourceString("MISSING_RESOURCE_STRING")+key;
            if(Gdb.traceLogger.ERR)
                Gdb.traceLogger.err(2,getResourceString("MISSING_RESOURCE_STRING")+key );
            Gdb.debugOutput(msg);
         }
      }
      else
      {
         msg = "MISSING_RESOURCE_BUNDLE_FOR_KEY: "+key;
         if(Gdb.traceLogger.ERR)
             Gdb.traceLogger.err(1,"MISSING_RESOURCE_BUNDLE_FOR_KEY: "+key );
      }
      return msg;
   }

  /**
   * Set the locale for the console messages.
   */
   synchronized static boolean setConsoleLocale(Locale locale)
   {
      try
      {
         // Note: We need the fully qualified name here or else we may not be
         // able to find the resource bundle
         _backEndMessages =
            ResourceBundle.getBundle("org.eclipse.cdt.debug.gdbPicl.GdbMessages", locale);
      }
      catch (MissingResourceException e)
      {
         return false;
      }

      if (_backEndMessages == null)
         return false;
      else
         return true;
   }

  /**
   * Returns a new Locale from a local string (eg. "en_US")
   */
   public synchronized static Locale localeFromString(String lang)
   {
      StringTokenizer tok = new StringTokenizer(lang,"_");
      String language = tok.nextToken();
      String country = "";
      String variant = "";
      if (tok.hasMoreTokens())
         country = tok.nextToken();
      if (tok.hasMoreTokens())
         variant = tok.nextToken();

      return new Locale(language,country,variant);
   }

  /**
   * Start a java debugger with command line arguments
   */
   static void startDebugger(String args[])
   {
   	
// ***************************************************************************
//    Setup default values
// ***************************************************************************
      String  uidHost           = "";
      String  uidTitle          = "";
      String  uidPort           = "8001";
      String  uidPid            = "0";
      String  lang              = "";
      String  jsrcpath          = "";
      String  qsourcepath       = null;
      String  filterFile        = "module.lst";
      String  startUpKey        = "";
      String  gdbPath           = null;
      String  launcher          = "";
      String  projName          = "";
      String[] debuggeeArgs     = new String[0];
      int     serverPort        = 8000;
      boolean allowMultiConnect = false;
      boolean userSpecifiedPort = false;
      boolean autoStart         = false;
      boolean objectLevelTrace  = false;
      DebugEngine debugEngine   = null;

      initDebugOutput();

// ***************************************************************************
//    Try to use the system's default locale, if we fail we will use en_US
//    (note: the user may switch this with -lang option)
// ***************************************************************************
      if (!setConsoleLocale(Locale.getDefault()))
      {
         // The engine does not support the specified locale, we will try
         // using "en_US" instead
         if (setConsoleLocale(new Locale("en","US","")))
         {
            errorOutput(getConsoleResourceString("DEFAULT_LOCALE_NOT_SUPPORTED_MSG"));
         }
         else
         {
            errorOutput("MessagesBundle_en.properties resource file missing.");
            System.exit(-1);
         }
      }

// ***************************************************************************
//    Process commandline arguments
// ***************************************************************************
      int i=0;
      try
      {
         while (i < args.length)
         {
            if (args[i].startsWith("-"))
            {
               int equals = args[i].indexOf("=");
               if (equals >0)
               {
                  String option = args[i].substring(1,equals);
                  String optionArg = args[i].substring(equals+1);

                  if (option.startsWith("qpor"))
                  {
                     int port = Integer.parseInt(optionArg);
                     if (port > 0)
                     {
                         serverPort = port;
                         userSpecifiedPort = true;
                     }
                  }
                  else if (option.startsWith("qhos"))
                  {
                     uidHost = optionArg;
                  }
                  else if (option.startsWith("quip"))
                  {
                     if (Integer.parseInt(optionArg) > 0)
                         uidPort = optionArg;
                  }
                  else if (option.startsWith("qpid"))
                  {
                     uidPid = optionArg;
                  }
                  else if (option.startsWith("qtit"))
                  {
                     uidTitle = optionArg;
                  }
                  else if (option.startsWith("dbg"))
                  {
                     try
                     {
                        _dbgLog = new PrintWriter(new FileOutputStream(optionArg), true);
                     }
                     catch(Exception e) 
                     {
                         System.out.println("***ERROR: Could not open " + optionArg);
                        _dbgLog = null;
                     }
                  }
                  else if (option.startsWith("lang"))
                  {
                     lang = optionArg;
                  }
                  else if (option.startsWith("qsourcepath"))
                  {
                     qsourcepath = optionArg;
                  }
                  else if (option.startsWith("qfilter"))
                  {
                     filterFile = optionArg;
                  }
                  else if (option.startsWith("startupKey"))
                  {
                     // internal option, should not expose to user
                  	 startUpKey = args[i];
                  }
                  else if (option.startsWith("gdbPath"))
                  {
                     // internal option, should not expose to user
                     // locate where gdb script is on linux
                  	 gdbPath = optionArg;
                  }
                  else if (option.startsWith("launcher"))
                  {
                  	 launcher = args[i];
                  }
                  else if (option.startsWith("project"))
                  {
                  	 projName = args[i];
                  }
                  else if (option.startsWith("jsrcpath"))
                  { ; }
                  else if (option.startsWith("juserhome")) 
                  { ; }
                  else
                  {
                     errorOutput(getConsoleResourceString("UNKNOWN_OPTION_MSG")+
                        "'-" + option + "'");
                     usage();
                  }
               }
               else if (args[i].equalsIgnoreCase ("-dbg"))
               {
                  showDebugOutput = true;
               }
               else if (args[i].equalsIgnoreCase ("-help"))
               {
                  usage();
               }
               else if (args[i].equalsIgnoreCase ("-multi"))
               {
                  allowMultiConnect = true;
               }
               else if (args[i].equalsIgnoreCase ("-s"))
               {
                  autoStart = true;
               }
               else if (args[i].equalsIgnoreCase ("-qquiet"))
               {
                  hideInformationalOutput = true;
               }
               else
               {
                  errorOutput(getConsoleResourceString("UNKNOWN_OPTION_MSG")+
                     "'" + args [i] + "'");
                  usage();
               }
            }
            else
            {
               // Extract the remaining strings into debuggeeArgs
               int numArgs = args.length - i;
               debuggeeArgs = new String[numArgs];

               int arg = 0;
               while (arg < numArgs)
               {
                  debuggeeArgs[arg] = args[i];
                  i++; arg++;
               }
            }
            i++;
         }
      }
      catch (ArrayIndexOutOfBoundsException e) {
         errorOutput(getConsoleResourceString("OPTION_REQUIRES_ARG_MSG"));
         usage();
      }
      catch (NumberFormatException e)
      {
         errorOutput(getConsoleResourceString("INVALID_PORT_MSG") + "'" + args[i]
            + "'");
         usage();
      }

// ***************************************************************************
//    The user excplicitely set a different locale, switch over to it now
// ***************************************************************************
      if (!lang.equals(""))
      {
         if (!setConsoleLocale(localeFromString(lang)))
         {
            errorOutput(getConsoleResourceString("SPECIFIED_LOCALE_NOT_SUPPORTED_MSG"));
            System.exit(-1);
         }
      }

// ***************************************************************************
//    Perform a sanity checks on arguments
// ***************************************************************************

      if (!uidHost.equals(""))
      {
         // We will let the OS chose a port for us.  Warn the user that
         // the port they specified is meaningless with this option
         serverPort = 0;
         if (userSpecifiedPort)
         {
            errorOutput(getConsoleResourceString("PORT_IGNORED_MSG"));
         }
      }

      // If the user specified a program when it doesn't make sense to do so,
      // issue a warning.
      if (debuggeeArgs.length > 0)
      {
         if (allowMultiConnect || userSpecifiedPort)
         {
            errorOutput(getConsoleResourceString("CLASS_IGNORED_MSG"));
            debuggeeArgs = new String[0];
         }
      }

// ***************************************************************************
//    Copyright
// ***************************************************************************
      Gdb.msgOutput("");
      // ** TEMPORARILY REMOVED UNTIL CONSOLE TRANSLATION ISSUES RESOLVED **
      // Gdb.msgOutput(getConsoleResourceString("PRODUCT_NAME_MSG"));
      // ** TEMPORARILY REMOVED UNTIL CONSOLE TRANSLATION ISSUES RESOLVED **

      String versionText = "GdbPicl";

      MessageFormat form = new MessageFormat(versionText);

      // ** TEMPORARILY REMOVED UNTIL CONSOLE TRANSLATION ISSUES RESOLVED **
      // Gdb.msgOutput(getConsoleResourceString("COPYRIGHT_MSG"));
      // ** TEMPORARILY REMOVED UNTIL CONSOLE TRANSLATION ISSUES RESOLVED **
      String buildDate = (new java.text.SimpleDateFormat("yy/MM/dd").format(new Date())).toString();
      
      Gdb.msgOutput("GdbPicl (" + buildDate + ")");
      Gdb.msgOutput("Copyright (c) 1995, 2002 International Business Machines Corporation.");
      Gdb.msgOutput("All rights reserved.");
      Gdb.msgOutput("This program and the accompanying materials are made available ");
      Gdb.msgOutput("under the terms of the Common Public License which accompanies");
      Gdb.msgOutput("this distribution.");
      Gdb.msgOutput("");

      Gdb.debugOutput("engine build date :" + (new Date()).toString());

// ***************************************************************************
//    Create server socket and wait for a connection.  serverPort may have
//    been set to 0 via -uidhost.  We will get the real port we are
//    listening on so serverPort is valid from here on.
// ***************************************************************************
      ServerSocket server = null;
      try
      {
         server     = new ServerSocket(serverPort);
         serverPort = server.getLocalPort();
      }
      catch (IOException e)
      {
         errorOutput(getConsoleResourceString("PORT_IN_USE_MSG"));
         System.exit(-1);
      }

      if (server == null)
      {
         errorOutput(getConsoleResourceString("PORT_IN_USE_MSG"));
         System.exit(-1);
      }


      boolean exitGDB;

      if (allowMultiConnect)
          exitGDB = false;
      else
          exitGDB = true;

      do
      {
         String ipAddr = null;
         try
         {
            Object portArg[] = new Object[1];
            portArg[0] = Integer.toString(serverPort);
            String msg = "GDB "+Gdb.getConsoleResourceString("WAITING_FOR_CONNECTION_ON_PORT_MSG");
            form = new MessageFormat(msg);
            msg = form.format(portArg);
            Gdb.msgOutput(msg);
            // Start a UI as requested by user
            // !!! We are counting on a delay between the time the UI attempts
            // to connect and the time we start to listen.  This should not be
            // a problem.  You must run past the accept when debugging this code.
            if (!uidHost.equals(""))
            {
               InetAddress addr1 = InetAddress.getLocalHost();
               ipAddr = addr1.getHostAddress();
               String originatingHost = null;
               originatingHost = addr1.getByName(ipAddr).getHostName();

               /* some machine network configuration may still not give
                  fully-qualified name if DSN suffix is not filled out. In this
                  case we would use the numeric IP address until a better
                  solution can be found since an IPaddr may not get through
                  firewalls.
               */
               if (originatingHost.indexOf(".") == -1) // use ipAddr if not fully-qualified
                  originatingHost = ipAddr;
               UIStarter uiStarter =
                   new UIStarter(uidHost, uidPort, uidPid, uidTitle,
                                 originatingHost, String.valueOf(serverPort),
                                 startUpKey, launcher, projName, debuggeeArgs);

               uiStarter.setAutoStart(autoStart);

               if (!uiStarter.startUI())
               {
                  errorOutput(getConsoleResourceString("CANT_START_UI_MSG"));
                  System.exit(-1);
               }

               // Reset uidHost so we don't spawn multiple UI's forever if the
               // user requested multiple connections
               uidHost = "";

               // Do not wait forever for the UI to call back.
               server.setSoTimeout(UI_ATTACH_TIMEOUT);
            }

            Socket sock = server.accept();
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"########>>>>>>>> Gdb serverSocket.accept, will new DebugEngine then DebugEngine.start\n" );

            // Remove socket timeout (inherited from the ServerSocket
            // "server" and set in the UI Daemon case)
            sock.setSoTimeout(0);
            server.setSoTimeout(0);

            // EPDC connection.  Enable epdc dump when EPDCDUMP prop set.
            SocketConnection socketConnection = new SocketConnection(sock);
            socketConnection.startDumping();

            debugEngine = new GdbDebugEngine(socketConnection, gdbPath);

            debugEngine.setDebugOutput(showDebugOutput);
            debugEngine.setSrcPath(jsrcpath);

            // Make the value of the internal -qsourcepath option
            // available for debug session processors, if required.
            debugEngine.setQsourcepath(qsourcepath);

            debugEngine.filterFile  = filterFile;
            debugEngine.start();
         }
         catch (InterruptedIOException e)
         {
            errorOutput(getConsoleResourceString("UI_START_TIMEOUT_MSG"));
            System.exit(-1);
         }
         catch (UnknownHostException excp)
         {
           errorOutput(getConsoleResourceString("UNKNOWN_HOSTNAME_MSG") +excp.getMessage());
           System.exit(-1);
         }
         catch (IOException e)
         {
            errorOutput(getConsoleResourceString("CONNECTION_LOST_MSG"));
            System.exit(-1);
         }

         Gdb.msgOutput(getConsoleResourceString("CONNECTION_ESTABLISHED_MSG"));
      } while (!exitGDB);

      // Wait for the Debug Engine to exit
      try {
         debugEngine.join();
      }
      catch (Exception excp) {
         Gdb.handleException(excp);
      }

      // All done
      System.exit(0);
   }

   private static void usage()
   {
      msgOutput(getConsoleResourceString("USAGE_MSG"));
      System.exit(0);
   }

 
  // ***************************************************************************
  // Internationalization Initialization
  // ***************************************************************************
  private void init_Initialization()
  {
      // ***************************************************************************
      // Try to use the system's default locale, if we fail we will use en_US
      // ***************************************************************************
      if (!setMessageLocale(Locale.getDefault()))
      {
         // ***************************************************************************
         // We do not support the specified locale, we will try "en_US" instead
         // ***************************************************************************
         if (setMessageLocale(new Locale("en","US","")))
         {
            errorOutput(getResourceString("DEFAULT_LOCALE_NOT_SUPPORTED_MSG"));
         }
         else
         {
            errorOutput("GdbMessages_en.properties resource file missing.");
         }
      }
  }

   // ***************************************************************************
   // Attempt to set the locale for messages.
   // ***************************************************************************
   private boolean setMessageLocale(Locale locale)
   {
      try
      {
         _messagesBundle =  ResourceBundle.getBundle("org.eclipse.cdt.debug.gdbPicl.GdbMessages", locale);
      }
      catch (MissingResourceException e)
      {
         return false;
      }

      if (_messagesBundle == null)
         return false;
      else
         return true;
   }

  /**
   * Get a resource string from the Messages ResourceBundle object
   */
   public static String getResourceString(String key) 
   {
      if(_messagesBundle==null)
      {
          return key;
      }
      try
      {  
         return _messagesBundle.getString(key);
      }
      catch (MissingResourceException e)
      {  
         if(traceLogger.ERR)
             traceLogger.err(2,getResourceString("MISSING_RESOURCE_STRING")+key );
         return key+" ";
      }
   }

 
// ***************************************************************************
//  Type Indices
// ***************************************************************************
    public static final int TYPEINDEX_CHARACTER = 0;
    public static final int TYPEINDEX_FLOAT     = 1;
    public static final int TYPEINDEX_DOUBLE    = 2;
    public static final int TYPEINDEX_INTEGER   = 3;

// ***************************************************************************
//  Representation Indices
// ***************************************************************************
    public static final short REPINDEX_DECIMAL_0       = 0;
    public static final short REPINDEX_HEXADECIMAL_0   = 1;
    public static final short REPINDEX_BOOLEAN_0       = 2;
    public static final short REPINDEX_ASCII_0         = 3;
    public static final short REPINDEX_UNICODE_0       = 4;
    public static final short REPINDEX_STRING_0        = 5;
    public static final short REPINDEX_FLOATINGPOINT_0 = 6;
    public static final short REPINDEX_ARRAY_0         = 7;
    public static final short REPINDEX_IEEEFLOAT_0     = 8;

// ***************************************************************************
//  Representation Indices
// ***************************************************************************
    public static final short REPINDEX_DECIMAL       = 1;
    public static final short REPINDEX_HEXADECIMAL   = 2;
    public static final short REPINDEX_BOOLEAN       = 3;
    public static final short REPINDEX_ASCII         = 4;
    public static final short REPINDEX_UNICODE       = 5;
    public static final short REPINDEX_STRING        = 6;
    public static final short REPINDEX_FLOATINGPOINT = 7;
    public static final short REPINDEX_ARRAY         = 8;
    public static final short REPINDEX_IEEEFLOAT     = 9;

// ***************************************************************************
// Miscellaneous Constants
// ***************************************************************************

   // Allow this many milliseconds for a ui started from a ui daemon to connect
   private static final int UI_ATTACH_TIMEOUT = 120000;

   private static ResourceBundle _backEndMessages;
   private static ResourceBundle _messagesBundle;

   public static TraceLogger traceLogger  = new TraceLogger("GdbPicl");;
   public static boolean supportDeferredBreakpoint = true;
   private static boolean showDebugOutput         = false;
   private static boolean hideInformationalOutput = false;
   protected static PrintWriter _dbgLog = null;

}
