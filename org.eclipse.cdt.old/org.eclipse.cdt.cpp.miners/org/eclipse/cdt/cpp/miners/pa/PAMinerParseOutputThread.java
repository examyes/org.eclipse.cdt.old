package org.eclipse.cdt.cpp.miners.pa;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import java.io.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.miners.pa.engine.*;

public class PAMinerParseOutputThread extends Thread {

  private PAMiner		 _miner;
  private DataStore		 _dataStore;
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
   _dataStore = _miner.getDataStore();
   
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
	_miner.getDataStore().refresh(_status, false);
    return;
  }

  if (_parseStatus.isAllDone())
  {
    // Reset the type of the trace element after the parsing
    _traceElement.setAttribute(DE.A_TYPE, PADataStoreAdaptor.getTraceProgramFormat(_traceFile));
    DataElement traceFunctionsRoot =  _dataStore.find(_traceElement, DE.A_VALUE, _miner.getLocalizedString("pa.TraceFuncRoot"), 1);
    traceFunctionsRoot.setAttribute(DE.A_TYPE, _traceElement.getType());

    PADataStoreAdaptor adaptor = new PADataStoreAdaptor(_traceElement);
    adaptor.populateDataStore(_traceElement, _traceFile);
    _status.setAttribute(DE.A_NAME, "done");
    _dataStore.refresh(_status, false);
  }
  
  // System.out.println("Return from output thread");
 }
 
}