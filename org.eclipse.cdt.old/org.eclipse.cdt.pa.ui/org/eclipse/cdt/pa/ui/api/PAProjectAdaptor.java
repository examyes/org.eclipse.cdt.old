package org.eclipse.cdt.pa.ui.api;
 
/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;
import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;
import java.util.*;


public class PAProjectAdaptor implements ICppProjectListener {

  private static PAProjectAdaptor _instance;
  private ModelInterface 		  _cppApi;
  private PAModelInterface 		  _api;
  
  // constructor
  private PAProjectAdaptor() {
    _cppApi = ModelInterface.getInstance();
    _api    = PAModelInterface.getInstance();
  }
  
  /**
   * Return the single instance
   */
  public static PAProjectAdaptor getInstance() {
   
   if (_instance == null) {
    _instance = new PAProjectAdaptor();
   }
   
   return _instance;
  }
  
  
  public Shell getShell() {
   return _api.getShell();
  }
  
  
  public void projectChanged(CppProjectEvent event) {

    int type = event.getType();
  	IProject project = event.getProject();
  	switch (type)
  	{
	  case CppProjectEvent.CLOSE:
  	  case CppProjectEvent.DELETE:
  	  {
  		 // System.out.println("project closed: ");
  		 
  		 // If this is a remote project, then the connection is already closed
  		 // when we get here. We simply fire a project changed event so that the
  		 // views can update their input elements.
		 if (project instanceof Repository)
		 {
  		    PATraceEvent projectEvent = new PATraceEvent(PATraceEvent.PROJECT_CHANGED, null);
  		    _api.getTraceNotifier().fireTraceChanged(projectEvent);
		 }
		 // If it is a local project, we will dispatch corresponding PA events to 
		 // delete the trace file and trace project elements.
		 else
		 {
  		   DataElement traceProject = _api.findTraceProjectElement(project);
  		   if (traceProject != null)
  		   {
  		   
  		     ArrayList traceFiles = _api.getTraceFiles(traceProject);
  		     for (int i=0; i < traceFiles.size(); i++)
  		     {
  		       DataElement fileElement = (DataElement)traceFiles.get(i);
            
               PATraceEvent fileEvent = new PATraceEvent(PATraceEvent.FILE_DELETED, fileElement);
               _api.getTraceNotifier().fireTraceChanged(fileEvent);
             }
  		   
  		     PATraceEvent projectEvent = new PATraceEvent(PATraceEvent.PROJECT_DELETED, traceProject);
  		     _api.getTraceNotifier().fireTraceChanged(projectEvent);
  		   
  		     _api.getDataStore().deleteObject(_api.getLocalProjectsRoot(), traceProject);
  		   }
  		   
  		 }		 
  	  }
  	  break;
  				  		
  		
  	  default:
  		break;
  	}
  	 
  }

}
