package com.ibm.debug.connection;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/connection/DebugDaemon.java, java-connection, eclipse-dev, 20011128
// Version 1.22.1.2 (last modified 11/28/01 16:29:41)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.util.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * DebugDaemon is a thread waiting for a connection request from the debug
 * engines. The daemon will process the request and pass the information to
 * the UI for starting a debug process.
 */
abstract public class DebugDaemon extends Thread
{
  protected Connection connection = null;

  protected EngineParameters engineParameters = null;

  // Assuming there is only one listener, i.e. UI.
  protected EngineConnectingListener listener = null;

  protected boolean isListening = false;

  /**
   * EngineParameters collects and saves the engine parameters sent
   * by the debug engine.
   */
  public abstract class EngineParameters extends DebuggerOptions
  {
    protected ConnectionInfo connectionInfo;
    protected String title;
    protected String arguments;

    protected final String OPTION_LAUNCHER         = "-launcher";
    protected final String OPTION_PROJECT          = "-project";
    protected final String OPTION_SOCKETSTYLE      = "-socketStyle";
    protected final String OPTION_STARTUPKEY       = "-startupKey";

    /**
     * The caller should clone the returned ConnectionInfo object
     * if it is to be saved for later use
     */
    public ConnectionInfo getConnectionInfo()
    {
      return connectionInfo;
    }

    public String getTitle()
    {
      return title;
    }

    public String getArguments()
    {
      return arguments;
    }

    public abstract String getProgramName();
    public abstract String getProgramParms();
    public abstract String getLoadStartupBehaviour();
    public abstract String getAttachStartupBehaviour();
    public abstract String getProcessID();

    public String getProject()
    {
      return valueByName(OPTION_PROJECT);
    }

    public String getLauncher()
    {
      return valueByName(OPTION_LAUNCHER);
    }

    public String getSocketStyle()
    {
      return valueByName(OPTION_SOCKETSTYLE);
    }

    public String getStartupKey()
    {
      return valueByName(OPTION_STARTUPKEY);
    }

    public String getValue(String property)
    {
      return valueByName(property);
    }

    public Hashtable getPairs()
    {
      return null;
    }

    public abstract void setInfo(InputStream inputStream) throws IOException;

    protected void setOptionDescriptors()
    {
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_LAUNCHER,         OPTION_LAUNCHER.length(),         true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_PROJECT,          OPTION_PROJECT.length(),          true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_SOCKETSTYLE,      OPTION_SOCKETSTYLE.length(),      true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_STARTUPKEY,       OPTION_STARTUPKEY.length(),       true,  false));
    }
  }


  /**
   * This class handles the new style of engine parameter.
   */
  public class NewEngineParameters extends EngineParameters
  {
    protected final String OPTION_ATTACHSTARTUPBEHAVIOUR = "-attachStartupBehaviour";
    protected final String OPTION_HOST                   = "-host";
    protected final String OPTION_LOADSTARTUPBEHAVIOUR   = "-loadStartupBehaviour";
    protected final String OPTION_PORT                   = "-port";
    protected final String OPTION_PROCESSID              = "-processID";
    protected final String OPTION_PROGRAMNAME            = "-programName";
    protected final String OPTION_PROGRAMPARMS           = "-programParms";
    protected final String OPTION_TITLE                  = "-title";

    public NewEngineParameters()
    {
      setOptionDescriptors();
    }

    public String getProgramName()
    {
      return valueByName(OPTION_PROGRAMNAME);
    }

    public String getProgramParms()
    {
      return valueByName(OPTION_PROGRAMPARMS);
    }

    public String getAttachStartupBehaviour()
    {
      return valueByName(OPTION_ATTACHSTARTUPBEHAVIOUR);
    }

    public String getLoadStartupBehaviour()
    {
      return valueByName(OPTION_LOADSTARTUPBEHAVIOUR);
    }

    public String getProcessID()
    {
      return valueByName(OPTION_PROCESSID);
    }

    public Hashtable getPairs()
    {
      return getOptions();
    }

    public void setInfo(InputStream inputStream) throws IOException
    {
      // clear out current options
      rebuild(new String[0], false);

      int numPairs = (new DataInputStream(inputStream)).readInt();
      if(numPairs > 0)
      {
        String pairs = readPairs(inputStream);

        if(pairs == null)
          throw new IOException("Could not read property/value pairs");

        String[] pairArray = makeOptionArray(pairs);
        if(pairArray.length != numPairs)
          throw new IOException("Expected " + numPairs + " property/value pairs but received " + pairArray.length);

        rebuild(pairArray, false);
      }

      String host = valueByName(OPTION_HOST);
      if(host == null)
          host = "localhost";

      String port = valueByName(OPTION_PORT);
      if(port == null)
          port = "";

      connectionInfo = newEngineConnectionInfo(host, port);
      title = valueByName(OPTION_TITLE);
      arguments = null;
    }

    protected String readPairs(InputStream inputStream)
    {
      int len;
      String result = "";
      try
      {
        while((len = inputStream.available()) > 0)
        {
          byte buffer[] = new byte[len];
          int bytesRead = inputStream.read(buffer, 0, len);
          result += new String(buffer, 0, bytesRead, "UTF8");
        }
      }
      catch(Exception e)
      {
        return null;
      }

      if(result.equals(""))
        return null;

      return result;
    }

    protected void setOptionDescriptors()
    {
      super.setOptionDescriptors();

      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_ATTACHSTARTUPBEHAVIOUR, OPTION_ATTACHSTARTUPBEHAVIOUR.length(), true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_HOST,                   OPTION_HOST.length(),                   true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_LOADSTARTUPBEHAVIOUR,   OPTION_LOADSTARTUPBEHAVIOUR.length(),   true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_PORT,                   OPTION_PORT.length(),                   true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_PROCESSID,              OPTION_PROCESSID.length(),              true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_PROGRAMNAME,            OPTION_PROGRAMNAME.length(),            true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_PROGRAMPARMS,           OPTION_PROGRAMPARMS.length(),           true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_TITLE,                  OPTION_TITLE.length(),                  true,  false));
    }

  }

  /**
   * This class handles the old style of engine parameter.
   */
  public class OldEngineParameters extends EngineParameters
  {
    protected final String OPTION_A                = "-a";
    protected final String OPTION_HELP             = "-help";
    protected final String OPTION_I                = "-i";
    protected final String OPTION_QADDRESS         = "-qaddress";
    protected final String OPTION_QCONNECT         = "-qconnect";
    protected final String OPTION_QDAEMON          = "-qdaemon";
    protected final String OPTION_QDISPLAY         = "-qdisplay";
    protected final String OPTION_QHOST            = "-qhost";
    protected final String OPTION_QLANG            = "-qlang";
    protected final String OPTION_QPID             = "-qpid";
    protected final String OPTION_QPORT            = "-qport";
    protected final String OPTION_QPROTOCOL        = "-qprotocol";
    protected final String OPTION_QQUIET           = "-qquiet";
    protected final String OPTION_QSESSION         = "-qsession";
    protected final String OPTION_QTITLE           = "-qtitle";
    protected final String OPTION_QUIPORT          = "-quiport";
    protected final String OPTION_S                = "-s";

    public OldEngineParameters()
    {
      setOptionDescriptors();
    }

    public String getProgramName()
    {
      return debuggeeName();
    }

    public String getProgramParms()
    {
      return debuggeeArgs();
    }

    public String getLoadStartupBehaviour()
    {
      if(valueByName(OPTION_S) != null)
        return "runToBreakpoint";
      else if(valueByName(OPTION_I) != null)
        return "debugInitialization";
      else
        return "runToMain";
    }

    public String getAttachStartupBehaviour()
    {
      if(valueByName(OPTION_S) != null)
        return "run";
      else
        return "stop";
    }

    public String getProcessID()
    {
      return valueByName(OPTION_A);
    }

    public void setInfo(InputStream inputStream) throws IOException
    {
      // clear out current options
      rebuild(new String[0], false);

      int argCount = 0;
      boolean readNull;
      String buffer [] = new String [1];
      //
      // Read the host and conduit
      //
      readNull = readLineOrNull(inputStream, buffer);
      String host = buffer[0];

      if (Connection.TRACE.DBG)
          Connection.TRACE.dbg(1, "DebugDaemon Host=" + host);

      if (readNull) return;

      readNull = readLineOrNull(inputStream, buffer);
      String conduit = buffer[0];

      if (Connection.TRACE.DBG)
          Connection.TRACE.dbg(1, "DebugDaemon Conduit =" + conduit);

      connectionInfo = newEngineConnectionInfo(host, conduit);
      if (readNull) return;

      //
      // Read the title
      //
      readNull = readLineOrNull(inputStream, buffer);
      title = buffer[0];

      if (Connection.TRACE.DBG)
          Connection.TRACE.dbg(1, "DebugDaemon Title =" + title);

      if (readNull) return;

      //
      // Read the parms
      //
      readNull = readLineOrNull(inputStream, buffer);
      arguments = "";
      String argCountString = buffer[0];

      if (Connection.TRACE.DBG)
          Connection.TRACE.dbg(1, "DebugDaemon Argcount =" + argCountString);

      if (argCountString == null || readNull) return;

      if (argCountString.length() > 0)
      {
        try
        {
          argCount = Integer.parseInt(argCountString);
        }
        catch (NumberFormatException e)
        {
        }
      }

      while (argCount-- > 0)
      {
        arguments += " ";
        readNull = readLineOrNull(inputStream, buffer);
        arguments += buffer[0];
        if (readNull) break;
      }
      if (Connection.TRACE.DBG)
          Connection.TRACE.dbg(1, "DebugDaemon Arguments =" + arguments);

      rebuild(arguments);
    }

    public boolean readLineOrNull(InputStream inputStream, String [] answer)
    throws IOException
    {
       char buffer[]  = new char[1024];
       int i = 0;
       char currentByte = 0;
       try
       {
          for (i = 0, currentByte = (char)inputStream.read();
               currentByte != '\n' && currentByte != '\r' && currentByte != 0;
               currentByte = (char)inputStream.read(), i++)
          {
             buffer[i] = currentByte;
          }
       }
       catch (IOException e) {}

       if (i == 0)
       {
          answer[0] = null;
       }
       else
       {
          answer[0] = new String(buffer,0,i);
       }
       if (currentByte == 0) return true;
         return false;
    }

    protected void setOptionDescriptors()
    {
      super.setOptionDescriptors();

      // Note that OPTION_A and OPTION_QDAEMON may have optional parameters, and so appear twice.
      // The "with parm" version appears first, to allow options of the form -a 123
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_A,                OPTION_A.length(),                true,  true));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_A,                OPTION_A.length(),                false, false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_HELP,             OPTION_HELP.length(),             false, false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_I,                OPTION_I.length(),                false, false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_QADDRESS,         OPTION_QADDRESS.length(),         true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_QCONNECT,         OPTION_QCONNECT.length(),         true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_QDAEMON,          OPTION_QDAEMON.length(),          true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_QDAEMON,          OPTION_QDAEMON.length(),          false, false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_QDISPLAY,         OPTION_QDISPLAY.length(),         true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_QHOST,            OPTION_QHOST.length(),            true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_QLANG,            OPTION_QLANG.length(),            true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_QPID,             OPTION_QPID.length(),             true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_QPID,             OPTION_QPID.length(),             false, false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_QPORT,            OPTION_QPORT.length(),            true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_QPROTOCOL,        OPTION_QPROTOCOL.length(),        true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_QQUIET,           OPTION_QQUIET.length(),           false, false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_QTITLE,           OPTION_QTITLE.length(),           true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_QUIPORT,          OPTION_QUIPORT.length(),          true,  false));
      addOptionDescriptor(new DebuggerOptionDescriptor(OPTION_S,                OPTION_S.length(),                false, false));
    }
  }


  /**
   * EngineConnectingEvent is generated whenever the debug daemon received
   * the connection request from the debug engine.
   */
  static public class EngineConnectingEvent extends java.util.EventObject
  {
    private EngineParameters engineParameters;

    public EngineConnectingEvent(Object source,
                                 EngineParameters parms)
    {
      super(source);
      engineParameters = parms;
    }

    public EngineParameters getEngineParameters()
    {
      return engineParameters;
    }
  }

  static public interface EngineConnectingListener
    extends java.util.EventListener
  {
    public void engineConnecting(EngineConnectingEvent e);
  }

  static public class EngineConnectingAdapter
    implements EngineConnectingListener
  {
    public void engineConnecting(EngineConnectingEvent e) { return; }
  }

  public void addEngineConnectingListener(EngineConnectingListener l)
  {
    listener = l;
  }

  abstract public Connection createConnection() throws IOException;

  private void fireEngineConnectingEvent()
  {
    if (listener != null) {
      listener.engineConnecting(new EngineConnectingEvent(this,
                                                          engineParameters));
    }
  }

  public Connection getConnection()
  {
    return connection;
  }

  /**
   * Returns the conduit that the daemon will listen on by default. The
   * value returned depends on the exact type of the connectionInfo
   * arg. For example, if the connectionInfo arg is of type
   * TCPIPConnectionInfo, the default conduit/port# is "8001".
   */
  public static String getDefaultConduit(ConnectionInfo connectionInfo)
  {
    if (connectionInfo instanceof TCPIPConnectionInfo)
       return "8001";
    else
       // TODO: Is there a default conduit for other kinds of comm protocols
       // e.g. a default pipe name???

       return null;
  }

  abstract public ConnectionInfo newEngineConnectionInfo(String host,
                                                         String conduit);

  /**
   * Stop the daemon thread.
   * This is a safe method of stopping the daemon.  May be overridden.
   * Overrides must reset the isListening flag.
   */
  public void
  stopListening()
  {
    isListening = false;
    try
    {
      connection.close();
    }
    catch (NullPointerException excp)
    {
      // Ignore exceptions
    }
    catch (Exception excp)
    {
      // Ignore exceptions
    }
  }

  private void readAndProcessRequest() throws IOException
  {
    connection.connectToClient(); // wait for a connection
    if (!isListening)
      return;
    int version = connection.beginRead();

    if(version >= 0)
        engineParameters = new OldEngineParameters();
    else
        engineParameters = new NewEngineParameters();

    try
    {
        engineParameters.setInfo(connection.getInputStream());
        connection.endRead();
    }
    catch(IOException e)
    {
        if(version < 0)
        {
            connection.getOutputStreamBuffer().write(-1);
            connection.getOutputStreamBuffer().write(0);
        }
        throw e;
    }

    if(version < 0)
    {
        connection.getOutputStreamBuffer().write(0);
        connection.getOutputStreamBuffer().write(0);
    }

    String socketStyle = engineParameters.getSocketStyle();
    if(socketStyle != null &&
       socketStyle.equals("single") &&
       connection instanceof SocketConnection)
    {
       SocketConnection clone = ((SocketConnection)connection).cloneConnectionAndClear();
       engineParameters.getConnectionInfo().setConnection(clone);
    }
    else
    {
       connection.disconnectFromClient();
    }
  }

  public void removeEngineConnectingListener(EngineConnectingListener l)
  {
    listener = null;
  }

  public void run()
  {
    isListening = true;

    while (isListening)
    {
      try
      {
        readAndProcessRequest();
        if (isListening)
          fireEngineConnectingEvent();
      }
      catch (SocketException e)
      {
        // Ignore Socket exceptions.
        // If they are as a result of being told to stop, we will fall out
      }
      catch (IOException e)
      {
        if (Connection.TRACE.MSG)
          Connection.TRACE.msg(1, "DebugDaemon: " + e.getMessage());
        isListening = false;
      }
      catch (NullPointerException e)
      {
        if (Connection.TRACE.MSG)
          Connection.TRACE.msg(1, "DebugDaemon: Null Pointer");
        isListening = false;
      }
    }
  }

  public void setConnection(Connection connection)
  {
    this.connection = connection;
  }

  /**
   * Start the daemon listening
   */
  public void start()
  {
    startListening();
  }

  /**
   * Start the daemon listening
   */
  public void startListening()
  {
    if (connection == null)
    {
      try
      {
        connection = createConnection();
      }
      catch (IOException e)
      {
        if (Connection.TRACE.ERR)
            Connection.TRACE.err(1, "DebugDaemon: " + e.getMessage());
          return;
      }
    }
    isListening = true;
    super.start();
  }

  /**
   * Returns whether the daemon is actively listening.
   */
  public boolean isListening() {
    return isListening;
  }

}
