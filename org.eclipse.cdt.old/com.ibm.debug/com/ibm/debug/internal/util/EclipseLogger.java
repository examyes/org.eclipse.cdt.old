package com.ibm.debug.internal.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/util/EclipseLogger.java, eclipse-util, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 16:35:15)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class implements the EclipseLogger.  By using the various
 * 'if(LOG.XXX)xxx(level,"text")' "macros" instead of System.out.println or System.err.println
 * all the messages can be redirected to an EclipseLog and/or plugin file and/or a remote TraceCatcher.
 * Typically, the usual/significant messages have level=1,
 * but more detailed/minor messages use level=2 or 3.
 * macro type & level guidelines are given at the end of this section.
 * <ul>
 * <li>The IBMDebugger programs instantiate this EclipseLogger class when it they are created (<code>System.exit()</code> for fatal errors).
 * Typical invocation is "<code><b>EclipseLogger LOG = new EclipseLogger(plugin, "componentName");</b> (default="UNKNOWN_APP")</code>
 * <li>The EclipseLogger constructor calles EclipseLogger.initialize() to:
 *     <ul>
 *     <li>perform International_Initialization
 *     <li>see if EclipseLoggerSettings.properties exists in plugin directory, else uses .../internal/util/EclipseLoggerSettings.properties
 *     <li>reads EclipseLoggerSettings.properties <B>LOG_ERR=n, LOG_EVT=n, LOG_DBG=n</B> logging detail levels
 *     <li>reads EclipseLoggerSettings.properties <B>LOG_COMPONENT=name</B> to see this componentName is to be logged (DEFAULT=all)
 *     <li>reads EclipseLoggerSettings.properties <B>LOG_ECLIPSE</B> to see if messages are also to be sent to Eclipse Logs (DEFAULT=yes)
 *     <li>reads EclipseLoggerSettings.properties <B>LOG_CONSOLE</B> to see if messages are also to be sent to System.out (DEFAULT=no)
 *     <li>reads EclipseLoggerSettings.properties <B>LOG_FILE</B> to see if messages are also to be copied into a plugin log file (DEFAULT=no)
 *         <br>If LOG_FILE is set, log file has name "componentName.log" (uses EclipseLogger("componentName") componentName)
 *     <li>read the EclipseLoggerSettings.properties <B>LOG_THREAD</B> to see if ThreadName is to be added into Component/Thrd/Msg (DEFAULT=no)
 *     <li>read the EclipseLoggerSettings.properties <B>LOG_HOST="xxx"</B> to see if System.out/err redirect to a TCP host</li> (DEFAULT=no)
 *        <ul>
 *        <li>read the EclipseLoggerSettings.properties <B>LOG_PORT="8xxx"</B> (default=8800)</li>
 *        <li>creates a PrintStream on the socket
 *        </ul>
 *     </ul>
 * <li>The IBMDebugger programs generate messages using:
 *     <B><ul>
 *     <li>if(LOG.DBG)LOG.dbg(int level, String msg);
 *     <li>if(LOG.EVT)LOG.evt(int level, String msg);
 *     <li>if(LOG.ERR)LOG.err(int level, String msg);
 *     <li>if(LOG.OUT)LOG.out(int level, String msg); //used for non-log System.out, DONT NORMALLY USE
 *     <li>if(LOG.DBG)LOG.dbg(int level, String msg, Exception exc);</li>
 *     <li>if(LOG.EVT)LOG.evt(int level, String msg, Exception exc);</li>
 *     <li>if(LOG.ERR)LOG.err(int level, String msg, Exception exc);</li>
 *     </ul></B>
 * <li>The IBMDebugger programs can call EclipseLogger<B>.flush()</B> (flush console IO) before terminating.</li>
 * <li>Usage Guidelines: (note that Level-1 is usually enabled, so minimize usage)
 *     <ul>
 *     <li><B>"err"</B> function should be used for "this should not happen" type errors.
 *         <ol>
 *         <li>Level-1 is for critical errors, continuing is suspect.
 *         <li>Level-2 is for serious errors, but recovery/continuation is expected
 *         <li>Level-3 is for likely errors/exceptions handled during normal operations
 *         </ol>
 *     <li><B>"evt"</B> function should be used for significant operational events (not errors)
 *               indicating major application/subsystem changes and actions
 *         <ol>
 *         <li>Level-1 is for major events (add a monitor, open a source, etc)
 *         <li>Level-2 is for significant user/system events (??)
 *         <li>Level-3 is for detailed ongoing events (monitor value changed, etc)
 *         </ol>
 *     <li><B>"dbg"</B> function should be used for application/subsystem specific debugging
 *         <ol>
 *         <li>Level-1 is for information associated with events/actions (helping flag cause/effects)
 *         <li>Level-2 is for flagging various internal decision points, unusual but valid paths, etc
 *         <li>Level-3 is for detailed info/dumps (normally only used for brief specific tests)
 *         </ol>
 *     <li><B>"out"</B> function is used to output System.out messages -- NOT NORMALLY USED
 *         <ol>
 *         <li>Level-1 is for any non-logged System.out console messages
 *         </ol>
 *     </ul>
 * </ul>
 */
public final class EclipseLogger extends Object
{
  // ***************************************************************************
  // Constructors and finalize()  & flush()
  // ***************************************************************************
  /** fully qualified TraceCatcher constructor (appname).
   *
   *
   * @param componentName is the String componentName of the component using the EclipseLogger (DEFAULT="UNKNOWN_APP")
   */
   public EclipseLogger(Plugin plugin, String name)
   {  if (name!=null && !name.equals("")) componentName = name;
      if(EVT) if(STATIC!=null)
         evt(2,"EclipseLogger CTOR name="+name );

      if(!initialize(plugin))
         osOut = null;  // disables system.out if LOG_HOST but socket failed
      if(plugin==null)
      {
          osErr = System.err;
          showError("PLUGIN_IS_NULL",null);
          return;
      }
      pluginName = plugin.getDescriptor().toString();
      int underScore = pluginName.lastIndexOf("_");
      if(underScore>0)
         pluginName = pluginName.substring(0,underScore);
      eclipseLogger = plugin.getLog();
      if(eclipseLogger==null)
         eclipseLogger = Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog();
      if(eclipseLogger==null)
         LOG_ECLIPSE=false;
   }

   /** default constructor  (DEFAULT="UNKNOWN_APP") */
   public EclipseLogger()
   {
      this(Platform.getPlugin(PlatformUI.PLUGIN_ID), "");
   }

  // ***************************************************************************
  // Trace functions ('Macros') for DEBUG, EVENT, ERROR
  // ***************************************************************************

   /** used to output debug messages, level=1 (normal), level=2 (more), etc
   * @param level is the int level of detail (Default=1, 2=more, 3=everything)
   * @param msg is the String message text to be displayed
   * @param exc (optional) is an associated Exception
   */
   public final void dbg(int level, String msg)
   {  dbg(level, msg, null); }
   public final void dbg(int level, String msg, Exception exc)
   {  if(level <= debugLevel && LOG_COMPONENT)
      {  String str = formatMsgBody(componentName, msg);
         if(LOG_ECLIPSE)
         {
            str = formatMsgBody(componentName, msg);
            eclipseLogger.log( new Status(Status.OK ,pluginName, Status.OK, str, null) );
         }
         if(LOG_CONSOLE || osLog!=null)
         {
            str = formatMsgHdr("dbg", level) +str;
            if(LOG_CONSOLE)
            {  if (osOut!=null) osOut.println(str);
            }
            if (osLog!=null) osLog.println(str);
         }
      }
   }
   /** used to output significant messages, level=1 (normal), level=2 (more), etc
   * @param level is the int level of detail (Default=1, 2=more, 3=everything)
   * @param msg is the String message text to be displayed
   * @param exc (optional) is an associated Exception
   */
   public final void evt(int level, String msg)
   {  evt(level, msg, null); }
   public final void evt(int level, String msg, Exception exc)
   {  if(level <=eventLevel && LOG_COMPONENT)
      {  String str = formatMsgBody(componentName, msg);
         if(LOG_ECLIPSE)
         {
            eclipseLogger.log( new Status(Status.INFO ,pluginName, Status.OK, str, null) );
         }
         if(LOG_CONSOLE || osLog!=null)
         {
            str = formatMsgHdr("evt", level) +str;
            if(LOG_CONSOLE)
            {  if (osOut!=null) osOut.println(str);
            }
            if (osLog!=null) osLog.println(str);
         }
      }
   }
   /** used to output error messages, ALSO sets msg into Frame-Title (will become POPUP)
   * @param level is the int level of detail (Default=1, 2=more, 3=everything)
   * @param msg is the String message text to be displayed
   * @param exc (optional) is an associated Exception
   */
   public final void err(int level, String msg)
   {  err(level, msg, null); }
   public final void err(int level, String msg, Exception exc)
   {  if(level <=errorLevel)
      {  String str = formatMsgBody(componentName, msg);
         if(LOG_ECLIPSE)
         {
            if(level>=1)
               eclipseLogger.log( new Status(Status.ERROR ,pluginName, Status.OK, str, null) );
            else
               eclipseLogger.log( new Status(Status.WARNING ,pluginName, Status.OK, str, null) );
         }
         if(LOG_CONSOLE || osLog!=null)
         {
            str = formatMsgHdr("ERR", level) +str;
            if(LOG_CONSOLE)
            {  if(osErr!=null) osErr.println(str);
               if(osErr!=null) osErr.flush();
            }
            if (osLog!=null) osLog.println(str);
         }
      }
   }
   /** used by Debugger Engine to output System.out messages NOT NORMALLY USED
   * @param level is the int level of detail (Default=1)
   * @param msg is the String message text to be displayed
   * @param exc (optional) is an associated Exception
   */
   public final void out(int level, String msg)
   {  out(level, msg, null); }
   public final void out(int level, String msg, Exception exc)
   {  if(level <=outLevel)
      {  String str = msg;
         if(osOut!=null)
             osOut.print(str);
         else
             System.out.print(str);
      }
      if(osLog!=null)   osLog.print(msg);
   }

   private final String formatMsg(String component, String type, int level, String msg)
   {   String str = formatMsgHdr(type,level) +formatMsgBody(component,msg);
       return str;
   }
   private final String formatMsgHdr(String type, int level)
   {   String str = type+"["+level+"]: ";
       return str;
   }
   private final String formatMsgBody(String component, String msg)
   {   String str = component;
       if(LOG_THREAD)
         str = str + "["+Thread.currentThread().getName()+"]";
       str = str +": "+msg;
       return str;
   }

   /** Needed because 'finalize()' isnt called until after System.out is gone */
   public final void flush()
   {  if(osOut!=null)
      {  if(DBG) if(debugLevel>=2)
            showMessage("ABOUT_TO_FLUSH_OUTPUT",null);
         System.out.flush();
         try { java.lang.Thread.sleep(1000L);  }
         catch (InterruptedException e)     {  }
      }
   }

   /** not really necessary, ensures socket is closed. */
   protected final void finalize()
   {  flush();
      if (socket!=null)
      {  try   { socket.close();   }
         catch (IOException e)  {  }
         catch (Exception e)  { handleException(e); }
      }
      if(osOut!=null)
         if(DBG) if(debugLevel>=2)
            showMessage("OUTPUT_CLOSED", null);
      showMessage("PROGRAM_DONE_MSG", null);
   }



  // ***************************************************************************
  // Initialize the Trace Logger
  // ***************************************************************************
   private final boolean initialize(Plugin plugin)
   {
      init_Initialization(language);

      init_Debug_Event_Error_Levels(plugin);

  // ***************************************************************************
  // LOG_HOST causes socket to be initialized for System.out
  // ***************************************************************************

      if(LOG_FILE && STATIC!=null)
      {   String fileName = componentName+".log";
          IPath path = plugin.getStateLocation();
          path = path.addTrailingSeparator();
          path.append(fileName);
          File file = path.toFile();
          file = new File(file, fileName);
          try
          {   osLog = new PrintStream(new FileOutputStream(file)) ;
          }
          catch (Exception e)
          {   showError("CANNOT_OPEN_LOG_FILE", file.toString() );
          }
      }

      if(traceCatcher==false)
      {
         //if(DBG) if( debugLevel >0 ) showMessage("NO_LOG_HOST", null);
         //}
      }
      else
      {  if ( logPort<8000 || logPort>8999 )
         {   showError("INVALID_PORT",Integer.toString(logPort));
         }
         try
         {  socket = new Socket(hostName, (logPort) );
         }
         catch (UnknownHostException e)
         {  showError("UNKNOWN_HOSTNAME_MSG", hostName);
            return false;
         }
         catch (BindException e)
         {  showError("CONNECT_BUSY_MSG", hostName);
            return false;
         }
         catch (ConnectException e)
         {  showError("CONNECT_FAILED_MSG", null);
            return false;
         }
         catch (Exception e)
         {  handleException(e);
            return false;
         }
         if(EVT) if(eventLevel>=2) showMessage("CONNECTION_ESTABLISHED_MSG", Integer.toString(logPort));

      }
      return true;
   }

  // ***************************************************************************
  // Initialize the Debug/Error/Event levels and Log/Catcher
  // ***************************************************************************
  private final void init_Debug_Event_Error_Levels(Plugin plugin)
  {  String s;

     //###################### LOG_EVT #################################
     s = getSettingsString("LOG_EVT");
     if(s!=null)
     {  try { eventLevel = Integer.parseInt(s); }
        catch (NumberFormatException e)
        {  showError("INVALID_EVENT_LEVEL", s);
           System.exit(-1);
        }
     }
     if(EVT) if(STATIC!=null)
        evt(3,"EclipseLogger_init STATIC.EVT="+STATIC.EVT+" STATIC.evt="+STATIC.getEventLevel()+" current LOG_EVT="+s );

     //###################### LOG_ECLIPSE, LOG_CONSOLE, LOG_COMPONENT, LOG_FILE, LOG_THREAD #################################
     LOG_ECLIPSE = true;   //DEFAULT
     s = getSettingsString("LOG_ECLIPSE");
     if(s!=null && (s.equalsIgnoreCase("no")||s.equalsIgnoreCase("false")) )
        LOG_ECLIPSE = false;
     if(EVT)  if(STATIC!=null)
        evt(3,"EclipseLogger_init LOG_ECLIPSE="+s );

     LOG_CONSOLE = false;  //DEFAULT
     s = getSettingsString("LOG_CONSOLE");
     if(s!=null && (s.equalsIgnoreCase("yes")||s.equalsIgnoreCase("true")) )
        LOG_CONSOLE = true;
     if(EVT)  if(STATIC!=null)
        evt(3,"EclipseLogger_init LOG_CONSOLE="+s );

     LOG_COMPONENT = true;   //DEFAULT
     s = getSettingsString("LOG_COMPONENT");
     if(s!=null && !s.equals("") && !s.equalsIgnoreCase("all") && !s.equalsIgnoreCase(componentName) )
        LOG_COMPONENT = false;
     if(EVT) if(STATIC!=null)
        evt(3,"EclipseLogger_init LOG_COMPONENT="+s );

     LOG_FILE = false;  //DEFAULT
     s = getSettingsString("LOG_FILE");
     if(s!=null && (s.equalsIgnoreCase("yes")||s.equalsIgnoreCase("true")) )
        LOG_FILE = true;
     if(EVT)  if(STATIC!=null)
        evt(3,"EclipseLogger_init LOG_FILE="+s );

     LOG_THREAD = false; //DEFAULT
     s = getSettingsString("LOG_THREAD");
     if(s!=null && (s.equalsIgnoreCase("yes")||s.equalsIgnoreCase("true")) )
        LOG_THREAD = true;
     if(EVT)  if(STATIC!=null)
        evt(3,"EclipseLogger_init LOG_THREAD="+s );

     //###################### LOG_HOST, LOG_PORT #################################
     s = getSettingsString("LOG_HOST");
     if(s!=null)
     {  if( !s.equals("") )
            hostName=s;
        traceCatcher=true;
     }
     if(EVT)  if(STATIC!=null)
        evt(3,"EclipseLogger_init LOG_HOST="+s );
     s = getSettingsString("LOG_PORT");
     if(s!=null)
     {  if( !s.equals("") )
        {  try {  logPort = Integer.parseInt (s);   }
           catch (NumberFormatException e)
           {  showError("INVALID_LOG_PORT",s);
           }
        }
     }
     if(EVT)  if(STATIC!=null)
        evt(3,"EclipseLogger_init LOG_PORT="+s );

     //###################### LOG_ERR, LOG_DBG, LOG_OUT #################################
     s = getSettingsString("LOG_ERR");
     if(s!=null)
     {  try { errorLevel = Integer.parseInt(s); }
        catch (NumberFormatException e)
        {  showError("INVALID_ERROR_LEVEL", s);
           System.exit(-1);
        }
     }
     if(EVT)  if(STATIC!=null)
        evt(3,"EclipseLogger_init STATIC.ERR="+STATIC.ERR+" STATIC.err="+STATIC.getErrorLevel()+" current LOG_ERR="+s );

     s = getSettingsString("LOG_DBG");
     if(s!=null)
     {  try { debugLevel = Integer.parseInt(s); }
        catch (NumberFormatException e)
        {  showError("INVALID_DEBUG_LEVEL", s);
           System.exit(-1);
        }
     }
     if(EVT)  if(STATIC!=null)
        evt(3,"EclipseLogger_init STATIC.DBG="+STATIC.DBG+" STATIC.dbg="+STATIC.getDebugLevel()+" current LOG_DGB="+s );

     s = getSettingsString("LOG_OUT");
     if(s!=null)
     {  try { outLevel = Integer.parseInt(s); }
        catch (NumberFormatException e)
        {  showError("INVALID_OUT_LEVEL", s);
           System.exit(-1);
        }
     }
     if(EVT)  if(STATIC!=null)
        evt(3,"EclipseLogger_init STATIC.OUT="+STATIC.OUT+" STATIC.out="+STATIC.getOutLevel()+" current LOG_OUT="+s );

  }

  // ***************************************************************************
  // Display Messages/Errors and handle unexpected Exceptions
  // ***************************************************************************
  /**
   * called to display operations messages.
   */
   private final void showMessage(String msg, String extraText)
   {  String str = getResourceString("LOG_LOGGER") + getResourceString(msg);
      if(extraText!=null)
         str = str + " " + extraText;
      evt(1,str);
   }

  /**
   * called to display messages about unexpected errors.
   */
   private final void showError(String msg, String extraText)
   {  String str = getResourceString("LOG_LOGGER") + getResourceString(msg);
      if(extraText!=null)
         str = str + " " + extraText;
      err(1,str);
   }

   private final void handleException(Exception exc)
   {   String str = getResourceString("LOG_LOGGER") + getResourceString("CAUGHT_EXCEPTION");
       err(1, str, exc);
   }

  // ***************************************************************************
  // Internationalization Initialization
  // ***************************************************************************

  private final void init_Initialization(String l)
  {
      // ***************************************************************************
      // Try to use the system's default locale, if we fail we will use en_US
      // ***************************************************************************
      if (!setMessageLocale(Locale.getDefault()))
      {
         // We do not support the specified locale, we will try "en_US" instead
         if (setMessageLocale(new Locale("en","US","")))
         {
            if(ERR)
               if(STATIC!=null)
                  STATIC.err(1,getResourceString("DEFAULT_LOCALE_NOT_SUPPORTED_MSG") );
         }
         else
         {
            if(ERR)
               if(STATIC!=null)
                  STATIC.err(1,"EclipseLoggerMessages_en.properties resource file missing." );
         }
      }

      // ***************************************************************************
      // EclipseLoggerSettings are only ENGLISH keywords (not intended for customer use)
      // ***************************************************************************
      try
      {
         _defaultBundle=ResourceBundle.getBundle(PACKAGE+".EclipseLoggerSettings", new Locale("", ""));
      }
      catch (MissingResourceException e)
      {
         if(ERR)
            if(STATIC!=null)
               STATIC.err(1,getResourceString("MISSING_DEFAULT_LOG_SETTING") );
      }
      try
      {
         _settingsBundle=ResourceBundle.getBundle("EclipseLoggerSettings", new Locale("", ""));
      }
      catch (MissingResourceException e)
      {
         if(EVT) if(STATIC!=null)
            STATIC.evt(1,"("+componentName+") "+getResourceString("MISSING_PLUGIN_LOG_SETTING") );
         _settingsBundle = _defaultBundle;
      }
  }
  /**
   * Attempt to set the locale for messages.
   */
   private boolean setMessageLocale(Locale locale)
   {
      try
      {
         _messagesBundle =  ResourceBundle.getBundle(PACKAGE+".EclipseLoggerMessages", locale);
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
   private String getResourceString(String key)
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
         if(ERR) if(STATIC!=null)
             STATIC.err(2,"("+key+") "+getResourceString("MISSING_RESOURCE_STRING") );
         return key+" ";
      }
   }
  /**
   * Get a property string from the Settings ResourceBundle object
   */
   private String getSettingsString(String key)
   {
      if(_settingsBundle==null && _defaultBundle==null)
      {
           return null;
      }
      try
      {
         return _settingsBundle.getString(key);
      }
      catch (MissingResourceException e)
      {
         try
         {
            return _defaultBundle.getString(key);
         }
         catch (MissingResourceException e2)
         {
            return null;
         }
      }
   }



   public final int getDebugLevel()
   {  return debugLevel;   }
   public final int getEventLevel()
   {  return eventLevel;   }
   public final int getErrorLevel()
   {  return errorLevel;   }
   public final int getOutLevel()
   {  return outLevel;   }

   public final void setDebugLevel(int i)
   {  debugLevel = i;   }
   public final void setEventLevel(int i)
   {  eventLevel = i;   }
   public final void setErrorLevel(int i)
   {  errorLevel = i;   }
   public final void setOutLevel(int i)
   {  outLevel = i;     }
   public final void setThread(boolean b)
   {  LOG_THREAD = b;      }

// ***************************************************************************
//  Data members
// ***************************************************************************
    private String  PACKAGE      = "com.ibm.debug.internal.util";
    private ResourceBundle _messagesBundle;
    private ResourceBundle _settingsBundle;
    private ResourceBundle _defaultBundle;
    private String  language      = "";
    private String  country       = "";
    private String  variant       = "";
    private boolean LOG_COMPONENT = true;
    private boolean LOG_ECLIPSE   = true;
    private ILog    eclipseLogger = Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog();
    private String  pluginName    = Platform.getPlugin(PlatformUI.PLUGIN_ID).getDescriptor().toString();
    private boolean LOG_CONSOLE   = false;
    private boolean LOG_FILE      = false;
    private boolean LOG_THREAD    = false;
    private PrintStream osErr     = System.err;
    private PrintStream osOut     = System.out;
    private PrintStream osLog     = null;
  /** The client app name (DEFAULT="UNKNOWN_APP") */
    private String  componentName = "UNKNOWN_APP";

    private boolean traceCatcher  = false;
    private Socket      socket    = null;
  /** The remote Catcher TCP port (DEFAULT=8800) */
    public  final int LOG_PORT    = 8800;
  /** The Logger TCP port (DEFAULT=8800) */
    public  int logPort     = LOG_PORT;
  /** The remote Catcher TCP hostName (DEFAULT="localhost") */
    private String hostName       = "localhost";

//  static final Data members used to eliminate "macro" code in user programs
//###############################################################################
    private int       outLevel   = 1;    // 1 always   (NOT NORMALLY USED)
    private int     errorLevel   = 1;    // 1 for production
    private int     eventLevel   = 0;    // 0 for production
    private int     debugLevel   = 0;    // 0 for production (not used)


/** compile time static logger */
    public  static EclipseLogger STATIC = new EclipseLogger(Platform.getPlugin(PlatformUI.PLUGIN_ID),"Utl");
/** compile time static to enable/exclude 'out' System.out message function/code */
    public static final boolean OUT            =true;   // true always
  /** compile time static to enable/exclude 'err' error message function/code */
    public static final boolean ERR            =true;   // true for production
  /** compile time static to enable/exclude 'evt' event message function/code */
    public static final boolean EVT            =true;   // true for production ??
  /** compile time static to enable/exclude 'dbg' debug message function/code */
    public static final boolean DBG            =true;   // false for production
//###############################################################################
}
