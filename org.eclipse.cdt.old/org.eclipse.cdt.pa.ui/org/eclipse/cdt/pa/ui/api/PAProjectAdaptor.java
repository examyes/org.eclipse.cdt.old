package org.eclipse.cdt.pa.ui.api;
 
/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
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
  	
	  case CppProjectEvent.OPEN:
      {		    
		 if (project instanceof Repository)
		 {
			Repository rep = (Repository)project;		      
			_api.extendSchema(rep.getDataStore().getDescriptorRoot());
		 }
	  }
	  break;
	  
	  case CppProjectEvent.CLOSE:
  	  case CppProjectEvent.DELETE:
  	  {
  		 // System.out.println("project closed: " + projectName);
  		 
  		 DataStore dataStore = _api.getDataStore();
		 if (project instanceof Repository)
		 {
			dataStore = ((Repository)project).getDataStore();
		 }

  		 DataElement traceProject = _api.findTraceProjectElement(project);
  		 if (traceProject != null) {
  		   
  		   ArrayList traceFiles = _api.getTraceFiles(traceProject);
  		   for (int i=0; i < traceFiles.size(); i++) {
  		    DataElement fileElement = (DataElement)traceFiles.get(i);
            
            PATraceEvent fileEvent = new PATraceEvent(PATraceEvent.FILE_DELETED, fileElement);
            _api.getTraceNotifier().fireTraceChanged(fileEvent);
            
           }
  		   
  		   PATraceEvent projectEvent = new PATraceEvent(PATraceEvent.PROJECT_DELETED, traceProject);
  		   _api.getTraceNotifier().fireTraceChanged(projectEvent);
  		   
  		   dataStore.deleteObject(_api.getProjectsRoot(dataStore), traceProject);
  		 }		 
  	  }
  	  break;
  				  		
  		
  	  default:
  		break;
  	}
  	 
  }

}