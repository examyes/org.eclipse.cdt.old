package org.eclipse.cdt.cpp.ui.internal.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*; 
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;

import java.util.*;


public class ParsedSourceViewPart extends ProjectViewPart
{
 public ParsedSourceViewPart()
 {
  super();
 }
    
 public ObjectWindow createViewer(Composite parent, IActionLoader loader)
 {
  return new ObjectWindow(parent, ObjectWindow.TABLE, _plugin.getCurrentDataStore(), _plugin.getImageRegistry(), loader);
 }
    
 protected String getF1HelpId()
 {
  return CppHelpContextIds.PARSED_SOURCE_VIEW;
 }
    
 public void doClear()
 {
     _viewer.setInput(null);	
     _viewer.clearView();
     setTitle("Parsed-Files");
     if (_browseHistory != null)
     	_browseHistory.clear();
     	
     updateActionStates();	
 }
 
    
 public DataElement doSpecificInput(DataElement projectParseInformation)
 {
  //Find the Parsed Source Object under the projectParseInformation
  DataElement parsedSource = projectParseInformation.getDataStore().find(projectParseInformation, 
									 DE.A_NAME, "Parsed Files", 1);
  if (parsedSource == null)
  {
  	doClear();
   	return null;
  }
  
  //Finally just set the input and the title 
  if (_viewer.getInput() == parsedSource)
      {
	  // this is too expensive
	  //_viewer.resetView();
      }
  else
      {
      	if (!_browseHistory.contains(parsedSource))
      	{
	  		_viewer.setInput(parsedSource);	
	  		_viewer.selectRelationship("contents");
	  		setTitle(projectParseInformation.getName() + " Parsed-Files");   
	  
	   	  	_browseHistory.clear();
     		_browseHistory.add(parsedSource);
     	    _browsePosition = 0;
     	    updateActionStates();
      	}
      }
      
   return parsedSource;   
 } 	
}










