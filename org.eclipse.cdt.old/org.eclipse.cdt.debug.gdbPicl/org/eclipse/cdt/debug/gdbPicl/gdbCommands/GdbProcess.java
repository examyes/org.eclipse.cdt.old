//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.gdbCommands;
import  com.ibm.debug.gdbPicl.*;
import  com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.util.LineInputStreamReader;
import com.ibm.debug.util.Platform;
import java.io.*;
import java.util.Vector;

/** Launches a gdb process. 
  * <BR>Example:<code> GdbProcess gdb </code>
  * <pre>
  * f0 -> "GdbProcess"
  * f1 -> ( fileName() ( parameters() )? )
  * </pre>
  *
  * @see GdbProcess
 **/

public class GdbProcess 
{
  public  static final int MAXSECONDS = 20;
  private static int MAXLINES = GdbDebugSession.MAX_GDB_LINES+GdbDebugSession.MAX_GDB_LINES;
  private static char PMPT[] =  { (char)26, (char)26 };
  public  static final String MARKER = new String(PMPT);
  private static String PROMPT = MARKER +"prompt";   // set annotate 2
  private boolean _annotated = false;
  public  boolean getAnnotated()          { return _annotated; }
  public  void    setAnnotated(boolean b) 
  { _annotated=b; 
    if(b) writeLine("set annotate 2");
    else  writeLine("set annotate 0");
  }

  private Process proc = null;
  private BufferedWriter wtr = null;
  private InputStream is = null;
  private LineInputStreamReader lisr = null;
  private InputStream isError = null;
  private LineInputStreamReader lisrError = null;
  private String _lastCommand = "";

  public GdbProcess (String command)  
  {
    if(command==null || command.equals("") )
    {  if (Gdb.traceLogger.ERR) 
           Gdb.traceLogger.err(2,"GdbProcess received command=null");    
       return;
    }
    try
    {  
       _lastCommand = command;
       if (Gdb.traceLogger.EVT) 
           Gdb.traceLogger.evt(1,"================ GdbProcess execCommand=" +command);
       proc = Runtime.getRuntime().exec(command);

       wtr= new BufferedWriter (new OutputStreamWriter(proc.getOutputStream()));
       is = proc.getInputStream();
       lisr = new LineInputStreamReader(is);
       isError = proc.getErrorStream();
       lisrError = new LineInputStreamReader(isError);
       setAnnotated(true);
    }
    catch(SecurityException excp)
    {  if (Gdb.traceLogger.ERR) 
           Gdb.traceLogger.err(2,"GdbProcess SecurityException="+excp);    
    }
    catch(java.io.IOException excp)
    {  if (Gdb.traceLogger.ERR) 
           Gdb.traceLogger.err(2,"GdbProcess IOException="+excp);    
    }

    String cmd = "set annotate 2";
    boolean ok = writeLine(cmd);
    if(ok) { cmd = "set height 0"; ok = writeLine(cmd); }
    if(ok) { cmd = "set width 0";  ok = writeLine(cmd); }
    if(!ok)
    {   if (Gdb.traceLogger.ERR) 
            Gdb.traceLogger.err(2,"GdbProcess failed to writeLine="+cmd );
        return;
    }
  }

  public boolean writeLine(String cmd)
  {  
     if(proc!=null)
        if(!isProcessRunning())
           killRunningProcess();
     boolean ok = false;
     if (proc==null)
     {  if (Gdb.traceLogger.ERR) 
            Gdb.traceLogger.err(2,"GdbProcess.writeLine proc==null" );    
        return ok;
     }
     if (Gdb.traceLogger.DBG) 
         Gdb.traceLogger.dbg(3,"GdbProcess writeLine to stdIn="+cmd );
     try
     {  
        wtr.write( cmd +"\n" ); 
        wtr.flush();
        ok = true;
        _lastCommand = cmd;
     }
     catch(java.io.IOException excp)
     {  if (Gdb.traceLogger.ERR) 
            Gdb.traceLogger.err(2,"GdbProcess.writeLine IOException="+excp);    
     }
     return ok;
  }
  public String[] readAllLines()
  {  if (proc==null)
     {  if (Gdb.traceLogger.ERR) 
            Gdb.traceLogger.err(2,"GdbProcess.readAllLines proc==null" );    
        return null;
     }
     Vector allLines = new Vector();
     String txt = null;
     for(int i=0; i<MAXLINES; i++) // up to MAXLINES read **OR** until PROMPT
     {   
         txt = readLine();
         if(txt!=null)
         {    if (Gdb.traceLogger.DBG) 
                  Gdb.traceLogger.dbg(3,"GdbProcess readAllLines=" +txt);
              allLines.addElement(txt);
              if(txt.equals(PROMPT) && !lisr.ready() )  // PROMPT '->->prompt', so ALL DOME
                 break;
         }else 
            break;
     }
     if(allLines.size()>=1)
     {
        String str = (String)allLines.elementAt(0);
        if (Gdb.traceLogger.DBG) 
            Gdb.traceLogger.dbg(3,"GdbProcess readAllLines str[0]="+str+" cmd="+_lastCommand+"<<<<<<" );
        if(str!=null && str.equals(_lastCommand) )
        {    allLines.removeElementAt(0);
             if (Gdb.traceLogger.DBG) 
                 Gdb.traceLogger.dbg(1,"GdbProcess readAllLines removing cmd==str[0]=="+str );
        }
     }
     String[] lines = new String[allLines.size()];
     for(int i=0; i<allLines.size(); i++)
        lines[i] = (String)allLines.elementAt(i);
     return lines;
  }

  public String readLine()
  {  if(proc!=null)
        if(!isProcessRunning())
           killRunningProcess();
     if (proc==null)
     {  if (Gdb.traceLogger.DBG) 
            Gdb.traceLogger.dbg(1,"GdbProcess.readLine proc==null" );    
        return null;
     }
     String  txt = null;

     if(!lisrError.ready() && !lisr.ready())
     {    try{Thread.sleep(200);} catch(InterruptedException e){}
     }
     for(int i=0; i<MAXSECONDS; i++)
     {   
        if(lisrError.ready() )
        {  
            txt = lisrError.readLine();  
            break;
        }
        else if(lisr.ready() )
        {   
            txt = lisr.readLine();  
            break;
        }
        else
        {
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"**************** GdbProcess.readLine rdr NOT ready, will **SLEEP** then retry");
            try{Thread.sleep(1000);} catch(InterruptedException e){}
        }
        if (proc==null)
        {  if (Gdb.traceLogger.ERR) 
               Gdb.traceLogger.err(1,"GdbProcess.readAllLines proc==null" );    
           return txt;
        }
     }
     if (Gdb.traceLogger.EVT) 
         Gdb.traceLogger.evt(4,"GdbProcess.readLine=" +txt);
     return txt;
  }

  public boolean isReady()
  {  if(lisr==null || proc==null)
         return false;
     return lisr.ready();
  }

  public boolean isProcessRunning()
  {  
     boolean isRunning = false;
     if (proc!=null)
     {   try 
        {   int rc = proc.exitValue(); 
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"GdbProcess PROC is FINISHED rc="+rc +"\n" );
            killRunningProcess();
        }
        catch(IllegalThreadStateException exc)
        {   isRunning = true;
        }
     }
     return isRunning;
  }

  public void killRunningProcess()
  {
     if(proc!=null)
        proc.destroy();
     proc = null;
     if(lisr!=null)
     {  try { lisr.close(); }
        catch(java.io.IOException excp) {}
        lisr = null;
     }
     if(wtr!=null)
     {  try { wtr.close(); }
        catch(java.io.IOException excp) {}
        wtr = null;
     }
  }
}


