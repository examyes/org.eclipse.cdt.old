package org.eclipse.cdt.cpp.miners.pa;

import org.eclipse.cdt.dstore.core.model.*;
import java.util.*;
import java.io.*;


public class PAMinerParseErrorThread extends Thread {

  private PAMiner		 _miner;
  private DataElement	 _traceElement;
  private DataElement	 _status;
  private InputStream    _stderr;
  private BufferedReader _reader;
  private PAMinerParseOutputThread _outputThread;
  private String		 _line;
  private boolean		 _keepRunning;

  public PAMinerParseErrorThread(PAMiner miner, DataElement traceElement, DataElement status, InputStream stderr) {
   
   _miner = miner;
   _traceElement = traceElement;
   _status = status;
   _stderr = stderr;
   _reader = new BufferedReader(new InputStreamReader(_stderr));
   _keepRunning = true;
  }
  
  
  public synchronized void finish() {
   _keepRunning = false;
  }
  
  public void setOutputThread(PAMinerParseOutputThread outputThread) {
   _outputThread = outputThread;
  }
 
  /**
   * Return the localized string from a given key
   */
  public String getLocalizedString(String key) {
    return _miner.getLocalizedString(key);
  }

  /**
  * Return an error code from the error stream
  */
  public String getErrorCode(String line) {
 
   
     if (line.indexOf("gmon.out:") >= 0 || line.indexOf("cannot open profile data file 'functioncheck.fc'") >= 0)
      return getLocalizedString("pa.NoTraceData");
     
     else if (line.indexOf("ommand not found") >= 0)
      return getLocalizedString("pa.NoCommand");
     
     else if (line.indexOf("o such file or directory") >= 0 ||
     		  line.indexOf("does not exist") >= 0)
      return getLocalizedString("pa.NoFile");
     
     else if (line.indexOf("he specified flag is not valid") >= 0 ||
     		  line.indexOf("unrecognized option") >= 0)
      return getLocalizedString("pa.NoOption");
     
     else
      return null;
      
  }
 
  public void run()
  {
  
    try
    {
      while (_keepRunning)
      {
        if (_reader.ready())
        {
          _line = _reader.readLine();
         
          if (_line == null)
          {
            finish();
          }
          else 
          {
            if (_line.trim().length() > 0)
            {
              // System.out.println("error line: " + _line);
              String errorCode = getErrorCode(_line);
              if (errorCode != null)
              {
                // System.out.println("error code: " + errorCode);
                DataStore dataStore = _miner.getDataStore();
    			dataStore.createObject(_traceElement, "error code", errorCode);
    			_status.setAttribute(DE.A_NAME, "done");
    			_status.setAttribute(DE.A_VALUE, "error");
                dataStore.refresh(_status, false);
                
                if (_outputThread.isAlive())
                 _outputThread.finish();
                 
                finish();
              }
              else {
                finish();
              }
            }
          }
         
        }
        Thread.sleep(50);
     }
     
    _reader.close();
  }
  catch (Exception e)
  {
    System.out.println(e);
  }
  
  // System.out.println("Return from error thread");
 }
 
}
