package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.*;
import com.ibm.dstore.core.model.*;

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
  return new ObjectWindow(parent, 0, _plugin.getCurrentDataStore(), _plugin.getImageRegistry(), this, true);
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
 }
    
 public void doSpecificInput(DataElement projectParseInformation)
 {
  //Find the Parsed Source Object under the projectParseInformation
  DataElement parsedSource = projectParseInformation.getDataStore().find(projectParseInformation, DE.A_NAME, "Parsed Files", 1);
  if (parsedSource == null)
   return;
  
  //Finally just set the input and the title
  if (_viewer.getInput() == parsedSource)
      {
	  _viewer.resetView();
      }
  else
      {
	  _viewer.setInput(parsedSource);	
	  _viewer.selectRelationship("contents");
	  setTitle(projectParseInformation.getName() + " Parsed-Files");   
      }
  
 } 	
}










