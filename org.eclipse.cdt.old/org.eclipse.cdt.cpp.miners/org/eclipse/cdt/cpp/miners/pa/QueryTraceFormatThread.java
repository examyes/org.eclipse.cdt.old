package org.eclipse.cdt.cpp.miners.pa;

import java.util.*;
import org.eclipse.cdt.dstore.core.model.*;


public class QueryTraceFormatThread extends Thread
{ 
  private DataElement _cmdStatus;
  private DataElement _status;

  public QueryTraceFormatThread() {
  
  }
    
  public void init(DataElement cmdStatus, DataElement status) {
    _cmdStatus = cmdStatus;
    _status    = status;
  
  }
  
  public void run() {
  
    while (!_cmdStatus.getName().equals("done")) {
    
      try {
       Thread.sleep(30);
      }
      catch (InterruptedException e) {
       break;
      }
            
    }
    
    String format = "invalid trace program";
    ArrayList results = _cmdStatus.getAssociated("contents");
    for (int i=0; i < results.size(); i++) {
      
      // System.out.println(i+ ": " + ((DataElement)results.get(i)).getName());
      DataElement element = (DataElement)results.get(i);
      String line = element.getName();
      
      if (!line.startsWith("> ") && line.trim().length() > 0) {
       
        if (line.indexOf("mcount") >= 0) {
         format = "gprof";
         break;
        }
        else if (line.indexOf("cyg_profile_func_enter") >= 0) {
         format = "functioncheck";
         break;
        }
      }
      
    }
    
    _status.setAttribute(DE.A_NAME, "done");
    _status.setAttribute(DE.A_VALUE, format);
    _status.getDataStore().refresh(_status, false);
  }
  
}