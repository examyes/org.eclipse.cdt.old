/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
package org.eclipse.cdt.debug.gdbPicl;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.Frame;

/**
 * This class implements the JT (JavaTrace) TraceLogger.  By using the various
 * 'if(jt.XXX)xxx(level,"text")' "macros" instead of System.out.println or System.err.println
 * all the messages can be redirected to a TCPcatcher and/or to a log file
 * and/or suppressed (setting the JT_XXX=0).  Typically, the usual/significant
 * messages have level=1, but more detailed/minor messages use level=2 or 3.
 * macro & level guidelines are given at the end of this section.
 * <ul>
 * <li>The RemoteDebugger Engine and/or RemoteDebugger UI instantiates this
 * TraceLogger class when they are created (<code>System.exit()</code> for fatal errors).
 * Typical invocation is "<code><b>TraceLogger jt = new TraceLogger("AppName");</b> (default="UNKNOWN_APP")</code>
 * <li>The TraceLogger constructor calles TraceLogger.initialize() to:
 *     <ul>
 *     <li>perform International_Initialization
 *     <li>read the cmdLine Property <B>JT_ERR=n, JT_EVT=n, JT_DBG=n</B> logging detail levels
 *     <li>read the cmdLine Property <B>JT_LOG</B> to see if System.out/err are also to be copied into a log file
 *         <br>If JT_LOG is set, log file has name "appName.txt" (uses TraceLogger("appName") appName)
 *     <li>read the cmdLine Property <B>JT_HOST="xxx"</B> to see if System.out/err redirect to a TCP host</li>
 *        <ul>
 *        <li>read the cmdLine Property <B>JT_PORT="8xxx"</B> (default=8800)</li>
 *        <li>creates a PrintStream on the socket
 *        <li>redefines jt.DBG/jt.ERR/jt.EVT outputs to the socket PrintStream</li>
 *        <li>checks cmdLine Property <B>JT_SHOWOUT</B> is undefined before redefining System.out to socket
 *        <li>checks cmdLine Property <B>JT_SHOWERR</B> is undefined before redefining System.err to socket
 *        <li>checks cmdLine Property <B>JT_THREAD</B> to see if ThreadName is to be added into App/Thrd/Msg
 *        <li>checks cmdLine Property <B>JT_XML</B> to see if XML trace logs are to be produced
 *        </ul>
 *     </ul>
 * <li>The RemoteDebugger Applications generate redirected System.out/err using:
 *     <B><ul>
 *     <li>if(jt.DBG)jt.dbg(int level, String msg);
 *     <li>if(jt.EVT)jt.evt(int level, String msg);
 *     <li>if(jt.ERR)jt.err(int level, String msg);
 *     <li>if(jt.ERR)jt.err(int level, String msg, Frame frm);</li>
 *     <li>if(jt.MSG)jt.msg(int level, String msg);
 *     <li>if(jt.OUT)jt.out(int level, String msg); //used by debugger engine for captured System.out
 *     </ul></B>
 * <li>The RemoteDebugger Applications should call TraceLogger<B>.flush()</B> before terminating.</li>
 * <li>Usage Guidelines: (note that Level-1 is usually enabled, so minimize usage)
 *     <ul>
 *     <li><B>"err"</B> function should be used for "this should not happen" type errors.
 *         <ol>
 *         <li>Level-1 is for critical errors, continuing is suspect.
 *         <li>Level-2 is for serious errors, but recovery/continuation is expected
 *         <li>Level-3 is for likely errors/exceptions handled during normal operations
 *         </ol>
 *     <li><B>"evt"</B> function should be used for significant operational events (not errors)
 *               caused by factors/actions/events outside this application subsystem
 *         <ol>
 *         <li>Level-1 is for major events (add a monitor, open a source, etc)
 *         <li>Level-2 is for significant user/system events (??)
 *         <li>Level-3 is for detailed ongoing events (monitor value changed, etc)
 *         </ol>
 *     <li><B>"dbg"</B> function should be used for application/subsystem specific debugging
 *         <ol>
 *         <li>Level-1 is for items which might affect other subsystems (helping flag cause/effects)
 *         <li>Level-2 is for typical "System.out" info useful to application/subsystem developer
 *         <li>Level-3 is for detailed info/dumps (normally only used for brief specific tests)
 *         </ol>
 *     <li><B>"msg"</B> function should be used for messages to the end-user (copyrights, etc)
 *         <ol>
 *         <li>Level-1 is for normal user messages (copyrights, etc)
 *         </ol>
 *     <li><B>"out"</B> function is used by debugger engine to output captured System.out messages
 *         <ol>
 *         <li>Level-1 is for all debuggee application System.out messages
 *         </ol>
 *     </ul>
 * </ul>
 */
public final class TraceLogger extends Object
{

  static public TraceLogger TRACE = new TraceLogger("Utl");
  // ***************************************************************************
  // Trace functions ('Macros') for DEBUG, EVENT, ERROR
  // ***************************************************************************
   /** used to always output user messages, level=1 (normal)
   * @param level is the int level of detail (Default=1)
   * @param msg is the String message text to be displayed
   */
   public final void msg(int level, String msg)
   {  if(level <=messageLevel)
      {  if(active)
             osOut.println(msg);
         else
             System.out.println(msg);
      }
      if(osLog!=null)   osLog.println(msg);
   }

   /*
    * Prepend the number of milliseconds since the last trace statement.
    * This represents the amount it took to get to this trace statement.
    *
    * Example o/p
    *
    *      60 evt[1]: UI: CmdLineSocketClient.run() port=8002
    *    2684 dbg[1]: UI: IbmVaHlp.getProductLocations
    *     130 evt[1]: UI: IbmVaHlp.getProductLocations
    *      10 evt[1]: UI: IbmVaHlp.verifyProductContext
    *
    * It looks like something took a long time between
    *         CmdLineSocketClient.run
    *         IbmVaHlp.getProductLocations
    * In actual fact the UI was waiting for the port to timeout
    * before it continued other processing.
    *
    * To time some code more precisely add a trace statement before
    * and after the code to be chacked.
    *         Debugger.TRACE.dbg(1, "Before my new code");
    *         myNewCode();
    *         Debugger.TRACE.dbg(1, "After  my new code");
    *
    * Would yield:
    *      10 dbg[1]: UI: Before my new code
    *    4001 dbg[1]: UI: After  my new code
    * This trace indicates that the new code toke 4.001 seconds to
    * complete execution.
    */
   final private void minorTimerElapseTime()
   {
      if (timings)
      {
         String msg = System.currentTimeMillis() - minorTimer + " ";
         switch (msg.length())
         {
            case 2: msg = "    " + msg; break;
            case 3: msg = "   "  + msg; break;
            case 4: msg = "  "   + msg; break;
            case 5: msg = " "    + msg; break;
         }
         if (active)      osOut.print(msg);
         if (osLog!=null) osLog.print(msg);
      }
   }

   final private void minorTimerReset()
   {
      if (timings)
         minorTimer = System.currentTimeMillis();
   }

   /*
   * This methods can be called time major events or milstones such as
   * the time required for initial startup to the Load Program dialog
   * appearing.
   * @param desc is string describing the major event or milestone
   */
   final public void milestoneTimerElapseTime(String desc)
   {
      if (timings && milestoneTimer != milestoneTimerStopped)
      {
         long curTime = System.currentTimeMillis();
         String msg = curTime - milestoneTimer + " ";
         switch (msg.length())
         {
            case 2: msg = "    " + msg; break;
            case 3: msg = "   "  + msg; break;
            case 4: msg = "  "   + msg; break;
            case 5: msg = " "    + msg; break;
         }

         if (JT_THREAD)
            msg += " *** Milestone " + clientName + "["+Thread.currentThread().getName()+"]: " + desc + " *** " + curTime;
         else
            msg += " *** Milestone " + clientName + ": " + desc + " *** " + curTime;

         if (active)      osOut.println(msg);
         if (osLog!=null) osLog.println(msg);
      }
   }

   final public void milestoneTimerElapseTimeAndStop(String desc)
   {
      milestoneTimerElapseTime(desc);
      milestoneTimerStop();
   }

   final public void milestoneTimerStart()
   {
      if (timings)
         milestoneTimer = System.currentTimeMillis();
   }

   final public void milestoneTimerStart(String desc)
   {
      if (timings)
      {
         milestoneTimer = System.currentTimeMillis();
         milestoneTimerElapseTime(desc);
      }
   }

   final public void milestoneTimerStop()
   {
      if (timings)
         milestoneTimer = milestoneTimerStopped;
   }

   /** used to output debug messages, level=1 (normal), level=2 (more), etc
   * @param level is the int level of detail (Default=1, 2=more, 3=everything)
   * @param msg is the String message text to be displayed
   */
   public final void dbg(int level, String msg)
   {  if(level <= debugLevel && JT_ENABLE)
      {  if(DBG)
            minorTimerElapseTime();
         String str = formatMsg(clientName, "dbg", level, msg);
         if (active) osOut.println(str);
         if (osLog!=null) osLog.println(str);
         if(DBG)
            minorTimerReset();
      }
   }
   /** used to output significant messages, level=1 (normal), level=2 (more), etc
   * @param level is the int level of detail (Default=1, 2=more, 3=everything)
   * @param msg is the String message text to be displayed
   */
   public final void evt(int level, String msg)
   {  if(level <=eventLevel && JT_ENABLE)
      {  if(DBG)
            minorTimerElapseTime();
         String str = formatMsg(clientName, "evt", level, msg);
         if(active) osOut.println(str);
         if(osLog!=null) osLog.println(str);
         if(DBG)
            minorTimerReset();
      }
   }
   private final String formatMsg(String component, String type, int level, String msg)
   {   String str = type+"["+level+"]: "+component;
       if(JT_THREAD)
         str = str + "["+Thread.currentThread().getName()+"]";
       str = str +": "+msg;
       if(xmlLog!=null) formatXml(component, type, level, msg);
       return str;
   }
   private final void formatXml(String component, String type, int level, String msg)
   {
       int i =msg.indexOf('<');
       if(i>=0)
          if(i>0) msg = msg.substring(0,i-1)+"&lt;";
          else    msg = msg.substring(1)+"&lt;";
       i=msg.indexOf('>');
       if(i>=0)
          msg = msg.substring(i)+"&gt;";

       xmlLog.println("    <logrec processid=\""+component+"\" threadid=\""
                      +Thread.currentThread().getName()
                      +"\" category=\""+type+"\" severity=\""+level
                      +"\" extendedmessage=\""+msg+"\" >  </logrec> ");
   }
   private final void initXml()
   {
       String userName = System.getProperty("user.name");
       String hostName = "unknownHostName";
       try { InetAddress host = InetAddress.getLocalHost();
             hostName = host.getHostName();        }
       catch (UnknownHostException e )            { }
       Calendar calendar = new GregorianCalendar();
       String date = calendar.get(Calendar.YEAR)+"-"+calendar.get(Calendar.MONTH)+"-"+calendar.get(Calendar.DAY_OF_MONTH);
       String time = calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+"."+calendar.get(Calendar.MILLISECOND);

       xmlLog.println("<?xml version=\"1.0\"?> ");
       xmlLog.println("<!DOCTYPE logfile SYSTEM \"dbglog.dtd\"> ");
       xmlLog.println("<logfile manufacturer=\"IBM\" product=\"Distributed Debugger\" version=\"7.0\" ");
       xmlLog.println("         clientuserid=\"" +userName +"\"  clienthostname=\"searle\" ");
       xmlLog.println("         date=\""+date+"\" time=\""+time+"\" xmlfilename=\""+clientName+".xml\"  > ");
       xmlLog.println("  <logrecv> ");
   }
   private final void closeXml()
   {
       xmlLog.println(" </logrecv> ");
       xmlLog.println("</logfile> ");
       xmlLog.flush();
       xmlLog.close();

   }
   /** used to output error messages, level=1 (normal), level=2 (more), etc
   * @param level is the int level of detail (Default=1, 2=more, 3=everything)
   * @param msg is the String message text to be displayed
   */
   public final void err(int level, String msg)
   {  err(level, msg, null);  }
   /** used to output error messages, ALSO sets msg into Frame-Title (will become POPUP)
   * @param level is the int level of detail (Default=1, 2=more, 3=everything)
   * @param msg is the String message text to be displayed
   * @param frm is the Frame (currently has title set, will be parent of POPUP)
   */
   public final void err(int level, String msg, Frame frm)
   {  String str = formatMsg(clientName, "ERR", level, msg);
      if(level <=errorLevel)
      {  if(active) osErr.println(str);
         if(active) osErr.flush();
         if(frm != null)   displayPopup(frm, str);
      }
      if(osLog!=null)   osLog.println(str);
   }
   private static final void displayPopup(Frame parent, String message)
   {  //JctMsgBox msgBox = new JctMsgBox(parent, message, "jt.ERR", JctMsgBox.ICON_STOP, JctMsgBox.BUTTONS_OK, true);
      //msgBox.setSize(200,200);  msgBox.show();
      //******* workaround: set title in parent frame instead of popup ******
      parent.show();  parent.setTitle(message);
      //******* remove sleep if modal popups are used **************
      try { java.lang.Thread.sleep(5000L);  }
      catch (InterruptedException e)     {  }
   }
   /** used by Debugger Engine to output captured System.out messages
   * @param level is the int level of detail (Default=1)
   * @param msg is the String message text to be displayed
   */
   public final void out(int level, String msg)
   {  if(level <=outLevel)
      {  String str = msg;
         if(active)
             osOut.print(str);
         else
             System.out.print(str);
      }
      if(osLog!=null)   osLog.print(msg);
   }


  // ***************************************************************************
  // Constructors and finalize()  & flush()
  // ***************************************************************************
  /** fully qualified TraceCatcher constructor (appname).
   *
   *
   * @param appName is the String name of app using the TraceLogger (DEFAULT="UNKNOWN_APP")
   */
   public TraceLogger(String name)
   {  if (name!=null && !name.equals("")) clientName = name;
      active=initialize();  // disables system.out if JT_HOST but socket failed
   }

   /** default constructor  (DEFAULT="UNKNOWN_APP") */
   public TraceLogger()
   { this("");
   }

   /** Needed because 'finalize()' isnt called until after System.out is gone */
   public final void flush()
   {  if(xmlLog!=null)
         closeXml();
      if(active)
      {  if(DBG) if(debugLevel>=2)
            showMessage("ABOUT_TO_FLUSH_SOCKET",null);
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
      if(active)
         if(DBG) if(debugLevel>=2)
            showMessage("SYSTEM_OUT_SOCKET_CLOSED", null);
   }



  // ***************************************************************************
  // Initialize the Trace Logger
  // ***************************************************************************
   private final boolean initialize()
   {
      init_Initialization(language);

      init_Debug_Event_Error_Levels();

  // ***************************************************************************
  // JT_HOST causes socket to be initialized for System.out
  // ***************************************************************************

      if(JT_LOG)
      {   String fileName = clientName+".txt";
          try
          {   osLog = new PrintStream(new FileOutputStream(fileName)) ;
          }
          catch (Exception e)
          {   showError("CANNOT_OPEN_LOG_FILE", fileName );
          }
      }

      if(JT_XML)
      {   String fileName = clientName+".xml";
          try
          {   xmlLog = new PrintStream(new FileOutputStream(fileName)) ;
              initXml();
          }
          catch (Exception e)
          {   showError("CANNOT_OPEN_XML_FILE", fileName );
          }
      }

      if(traceCatcher==false)
      {
         //if(DBG)
         //{  if( debugLevel >0 )
         //       if(DBG) if(debugLevel>=2) showMessage("NO_JT_HOST", null);
         //}
      }
      else
      {  if ( tracePort<8000 || tracePort>8999 )
         {   showError("INVALID_PORT",Integer.toString(tracePort));
         }
         try
         {  socket = new Socket(hostName, (tracePort) );
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
         //if(DBG) if(debugLevel>=2) showMessage("CONNECTION_ESTABLISHED_MSG", Integer.toString(tracePort));

         try
         {  PrintStream os=new PrintStream(new BufferedOutputStream(socket.getOutputStream()),true);
            osOut = os;
            osErr = os;
            String s = System.getProperty("JT_SHOWOUT");
            if(s==null)
               System.setOut(osOut);
            s = System.getProperty("JT_SHOWERR");
            if(s==null)
               System.setErr(osErr);
            if(DBG) if(debugLevel>=2)
               showMessage("SYSTEM_OUT_NOW_SOCKET",clientName);
         }
         catch (IOException e)
         {  showError("UNABLE_TO_ASSIGN_SYSTEM_OUT_TO_SOCKET", null);
            return false;
         }

      }
      return true;
   }

  // ***************************************************************************
  // Initialize the Debug/Error/Event levels and Log/Catcher
  // ***************************************************************************
  private final void init_Debug_Event_Error_Levels()
  {  String s;
     s = System.getProperty("JT_HOST");
     if(s!=null)
     {  if( !s.equals("") )
            hostName=s;
        traceCatcher=true;
     }
     s = System.getProperty("JT_PORT");
     if(s!=null)
     {  if( !s.equals("") )
        {  try {  tracePort = Integer.parseInt (s);   }
           catch (NumberFormatException e)
           {  showError("INVALID_JT_PORT",s);
           }
        }
     }
     s = System.getProperty("JT_LOG");
     if(s!=null)
        JT_LOG = true;
     s = System.getProperty("JT_XML");
     if(s!=null)
        JT_XML = true;
     s = System.getProperty("JT_THREAD");
     if(s!=null)
        JT_THREAD = true;
     s = System.getProperty("JT_MSG");
     if(s!=null)
     {  try { messageLevel = Integer.parseInt(s); }
        catch (NumberFormatException e)
        {  showError("INVALID_MESSG_LEVEL", s);
           System.exit(-1);
        }
     }
     s = System.getProperty("JT_DBG");
     if(s!=null)
     {  try { debugLevel = Integer.parseInt(s); }
        catch (NumberFormatException e)
        {  showError("INVALID_DEBUG_LEVEL", s);
           System.exit(-1);
        }
        DBG = debugLevel >= 0;
     }
     s = System.getProperty("JT_EVT");
     if(s!=null)
     {  try { eventLevel = Integer.parseInt(s); }
        catch (NumberFormatException e)
        {  showError("INVALID_EVENT_LEVEL", s);
           System.exit(-1);
        }
        EVT = eventLevel >= 0;
     }
     s = System.getProperty("JT_ERR");
     if(s!=null)
     {  try { errorLevel = Integer.parseInt(s); }
        catch (NumberFormatException e)
        {  showError("INVALID_ERROR_LEVEL", s);
           System.exit(-1);
        }
     }
     s = System.getProperty("JT_OUT");
     if(s!=null)
     {  try { outLevel = Integer.parseInt(s); }
        catch (NumberFormatException e)
        {  showError("INVALID_OUT_LEVEL", s);
           System.exit(-1);
        }
     }
     s = System.getProperty("JT_TIME");
     if(s!=null)
     {
        timings = true;             // elapse times
     }
     else
     {
        s = System.getProperty("JT_NOTIME");
        if(s!=null)
           timings = false;         // no elapse times
        else
           timings = (debugLevel > 0); // elapse times if JT_DEBUG > 0
     }
     s = System.getProperty("JT_ENABLE");
     if(s!=null)
     {  if( s.equals("") || s.equalsIgnoreCase("all") || s.equalsIgnoreCase(clientName) )
           JT_ENABLE = true;
        else
           JT_ENABLE = false;
     }
  }

  // ***************************************************************************
  // Display Messages/Errors and handle unexpected Exceptions
  // ***************************************************************************
  /**
   * called to display operations messages.
   */
   private final void showMessage(String m, String extraText)
   {  String s = getResourceString("JT_LOGGER") + getResourceString(m);
      if(extraText!=null)
         s = s + " " + extraText;
      osOut.println(s);
   }

  /**
   * called to display messages about unexpected errors.
   */
   private final void showError(String m, String extraText)
   {  String s = getResourceString("JT_LOGGER") + getResourceString(m);
      if(extraText!=null)
         s = s + " " + extraText;
      osErr.println(s);
   }

  private final void handleException(Exception e)
  {  osErr.println(e.toString());
     osErr.println(e.getMessage());
     e.printStackTrace(osErr);
  }

  // ***************************************************************************
  // Internationalization Initialization
  // ***************************************************************************
  private static final void init_Initialization(String l)
  {
      // The messages for the trace stuff are not translated so we won't
      // try to get them based on the user's locale - we'll only try and get
      // the english messages. See defect 9984:

      try
      {  _messagesBundle=ResourceBundle.getBundle("com.ibm.debug.util.MessagesBundle",
                                                  new Locale("en", "US"));
      }
      catch (MissingResourceException e)
      {
         System.out.println("No Resource file for language=\""+
             language+"\" country=\""+country+"\" variant=\""+variant+"\"");
         System.exit(-1);
      }
  }

  /**
   * Get a resource string from the messages ResourceBundle object
   */
   private static String getResourceString(String key) {
      try
      {  return _messagesBundle.getString(key);
      }
      catch (MissingResourceException e)
      {  System.out.println ("Error: Resource "+key+" not found");
         return "Error: Resource "+key+" not found";
      }
   }
   public final int getDebugLevel()
   {  return debugLevel;   }
   public final int getEventLevel()
   {  return eventLevel;   }
   public final int getErrorLevel()
   {  return errorLevel;   }
   public final int getMessageLevel()
   {  return messageLevel;   }

   public final void setDebugLevel(int i)
   {  debugLevel = i;   }
   public final void setEventLevel(int i)
   {  eventLevel = i;   }
   public final void setErrorLevel(int i)
   {  errorLevel = i;   }
   public final void setMessageLevel(int i)
   {  //if(i<=0)         // allow msg=0 to supress msg output
      //   i=1;
      messageLevel = i;
   }
   public final void setTimings(boolean b)
   {  timings = b;      }
   public final void setThread(boolean b)
   {  JT_THREAD = b;      }
   public void setModelTrace(boolean b)
   {  java.util.Properties props = System.getProperties();
      String name = "JT_MODEL";
      String old = (String)props.get(name);
      if(b)  old = (String)props.put(name,"1");
      else   props.remove(name);
      System.setProperties(props);
      String value = (String)props.get(name);
   }

// ***************************************************************************
//  Data members
// ***************************************************************************
    private static ResourceBundle _messagesBundle;
    private static String  language     = "";
    private static String  country      = "";
    private static String  variant      = "";
    private static boolean errorPopup   = true;
    private static boolean JT_LOG       = false;
    private        boolean JT_ENABLE    = true;
    private static boolean JT_XML       = false;
    private static boolean JT_THREAD    = false;
    private static boolean traceCatcher = false;
    private static boolean active       =true;
  /** The client app name (DEFAULT="UNKNOWN_APP") */
    private String  clientName   = "UNKNOWN_APP";
  /** The remote catcher TCP hostName (DEFAULT="localhost") */
    private static String hostName    = "localhost";
  /** The JT_LOG log file name (DEFAULT="AppName.txt") */
    protected String traceLogName= clientName+".txt";
  /** The default TCP Logger/Catcher port (DEFAULT=8800) */
    public static final int JT_PORT   = 8800;
  /** The logging/catching TCP port (DEFAULT=8800) */
    public static int tracePort               = JT_PORT;
    private PrintStream osLog                  = null;
    private PrintStream xmlLog                 = null;
    private Socket socket                      = null;
    private PrintStream osOut                  = System.out;
    private PrintStream osErr                  = System.err;
    private final  long milestoneTimerStopped  = -1;
    private static long milestoneTimer         = System.currentTimeMillis(); // major step timer
    private static long minorTimer             = System.currentTimeMillis(); // minor step or message interval timer

//  static final Data members used to eliminate "macro" code in user programs
//###############################################################################
    private static int   messageLevel   = 1;    // 1 always
    private static int       outLevel   = 1;    // 1 always
    private static int     errorLevel   = 1;    // 1 for production
    private static int     eventLevel   = -1;    // 0 for production
    private static int     debugLevel   = -1;    // 0 for production (not used)
    private static boolean    timings   = false;
  /** compile time static to enable/exclude 'msg' user message function/code */
    public static boolean MSG            =true;   // true always
  /** compile time static to enable/exclude 'out' PICL System.out message function/code */
    public static boolean OUT            =true;   // true always
  /** compile time static to enable/exclude 'err' error message function/code */
    public static boolean ERR            =true;   // true for production
  /** compile time static to enable/exclude 'evt' event message function/code */
    public static boolean EVT            =false;   // true for production ??
  /** compile time static to enable/exclude 'dbg' debug message function/code */
    public static boolean DBG            =false;   // false for production
//###############################################################################

}
