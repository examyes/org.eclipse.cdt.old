package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;

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
import org.eclipse.debug.core.model.IBreakpoint;

import com.ibm.debug.breakpoints.*;
import com.ibm.debug.internal.picl.PICLUtils;

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
			PICLLineBreakpoint breakpoint = new PICLLineBreakpoint();  // R2 - create our own CDTLineBreakpoint?
			IMarker breakpointMarker = file.createMarker("com.ibm.debug.internal.picl.PICLLineBreakpoint");
			
			IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
			
			breakpointMarker.setAttributes(new String[] {IBreakpoint.ID, IBreakpoint.ENABLED, IMarker.LINE_NUMBER, IMarker.CHAR_START, IMarker.CHAR_END},
						       new Object[] {new String(PICLUtils.getModelIdentifier()), new Boolean(true), lineLocation, new Integer(-1), new Integer(-1)});
			breakpoint.setMarker(breakpointMarker);
			
			Map map= breakpointMarker.getAttributes();
			map.put(IMarker.MESSAGE, "cpp");
			breakpointMarker.setAttributes(map);
			
			try
			    {
				breakpointManager.addBreakpoint(breakpoint);
			    }
			catch (DebugException de)
			    {
				System.out.println("BreakpointRulerAction: de" +de);
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


