package org.eclipse.cdt.cpp.miners.pa;

import java.util.*;
import java.io.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;

public class PAMinerParseOutputThread extends Thread {

  private PAMiner		 _miner;
  private DataElement	 _traceElement;
  private DataElement	 _status;
  private InputStream    _stdout;
  private BufferedReader _reader;
  private String		 _traceFormat;
  private String		 _line;
  private boolean		 _keepRunning;

  private PAMinerParseErrorThread _errorThread;
  private PATraceFile	 _traceFile;
  private PAParseStatus	 _parseStatus;

  public PAMinerParseOutputThread(PAMiner miner, DataElement traceElement, DataElement status, String traceFormat, InputStream stdout) {
   
   _miner = miner;
   _traceElement = traceElement;
   _status = status;
   _stdout = stdout;
   _traceFormat = traceFormat;
   
   _reader = new BufferedReader(new InputStreamReader(_stdout));
   
   try {
    _traceFile = PAAdaptor.createTraceFile(_reader, _traceFormat);
   }
   catch (Exception e) {
    System.out.println(e);
   }
   
   _keepRunning = true;
   _parseStatus = _traceFile.getParseStatus();
   
  }
  
  
  public synchronized void finish() {
   _keepRunning = false;
  }
  
  public void setErrorThread(PAMinerParseErrorThread errorThread) {
   _errorThread = errorThread;
  }
 
  /**
   * Return the localized string from a given key
   */
  public String getLocalizedString(String key) {
    return _miner.getLocalizedString(key);
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
         
          if (_line == null || _parseStatus.isAllDone())
          {              
            if (_parseStatus.isAllDone())
            {
              if (_errorThread.isAlive())
                _errorThread.finish();
            }
            
            finish();
          }
          else 
          {
            _traceFile.processLine(_line);
          }
         
        }
     }
     
    _reader.close();
  }
  catch (Exception e)
  {
    System.out.println(e);

    if (_errorThread.isAlive())
      _errorThread.finish();
    
    _status.setAttribute(DE.A_NAME, "done");
	_status.setAttribute(DE.A_VALUE, "error");
	_miner.getDataStore().refresh(_status, true);
    return;
  }

  if (_parseStatus.isAllDone())
  {
    PADataStoreAdaptor adaptor = new PADataStoreAdaptor(_traceElement);
    adaptor.populateDataStore(_traceElement, _traceFile);
    _status.setAttribute(DE.A_NAME, "done");
    _miner.getDataStore().refresh(_status, true);
  }
  
  // System.out.println("Return from output thread");
 }
 
}