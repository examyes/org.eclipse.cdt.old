package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerRulerAction;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugConstants;

public class AddStatementBreakpoint extends CustomAction
{ 
  public AddStatementBreakpoint(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
      }

    public void createBreakpointMarker(DataElement statement)
    {
	String fileName   = (String)(statement.getElementProperty(DE.P_SOURCE_NAME));
	Integer lineLocation = (Integer)(statement.getElementProperty(DE.P_SOURCE_LOCATION));
	int line = lineLocation.intValue();	
	
	ModelInterface api = ModelInterface.getInstance();
	IResource file =  api.findFile(fileName);
	if (file != null)
	    {
		try
		    {
			IMarker breakpoint = file.createMarker("com.ibm.debug.internal.picl.PICLLineBreakpoint");
			
			IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
			breakpointManager.configureLineBreakpoint(breakpoint, "com.ibm.debug.internal.picl" , true, line, 0, 1);
			
			Map map= breakpoint.getAttributes();
			map.put(IMarker.MESSAGE, "cpp");
			breakpoint.setAttributes(map);
			
			try
			    {
				breakpointManager.addBreakpoint(breakpoint);
			    }
			catch (DebugException de)
			    {
				System.out.println(de);
			    }
		    }
		catch (CoreException ce)
		    {
			System.out.println(ce);
		    }
	    }
      }

    public void run()
    {
	createBreakpointMarker(_subject);
    }

}


