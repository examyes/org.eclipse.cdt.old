package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.ModelInterface;
import org.eclipse.cdt.dstore.core.model.DE;
import org.eclipse.cdt.dstore.core.model.DataElement;
import org.eclipse.cdt.dstore.core.model.DataStore;
import org.eclipse.cdt.dstore.ui.actions.CustomAction;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;

import com.ibm.debug.pdt.breakpoints.PICLLineBreakpoint;

public class AddStatementBreakpoint extends CustomAction
{ 
    private IWorkspace _workspace = null;
    private DataElement _statement;
    
    public AddStatementBreakpoint(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
	_workspace = CppPlugin.getPluginWorkspace();
      } 

    public void createBreakpointMarker(DataElement statement) 
    {
	_statement = statement;
	String fileName   = (String)(statement.getElementProperty(DE.P_SOURCE_NAME));
	Integer lineLocation = (Integer)(statement.getElementProperty(DE.P_SOURCE_LOCATION));
	int line = lineLocation.intValue();	
		    
	ModelInterface api = ModelInterface.getInstance();
	IResource file =  api.findFile(fileName);
	if (file != null)
	{
		new PICLLineBreakpoint(file,line);
	}	
//	IWorkspaceRunnable body = new IWorkspaceRunnable()
//	    {
//		private DataElement statement = _statement;
//
//		public void run(IProgressMonitor monitor) throws CoreException
//		{
//			    try
//				{
//				    PICLLineBreakpoint breakpoint = new PICLLineBreakpoint();  // R2 - create our own CDTLineBreakpoint?
//				    IMarker breakpointMarker = file.createMarker(IPICLDebugConstants.PICL_LINE_BREAKPOINT);
//				    
//				    IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
//				    
//				    breakpointMarker.setAttributes(
//								   new String[] {    IBreakpoint.ID, 
//										     IBreakpoint.ENABLED, 
//										     IMarker.LINE_NUMBER, 
//										     IMarker.CHAR_START, 
//										     IMarker.CHAR_END
//											 },
//								   new Object[] {
//								       new String(   PICLUtils.getModelIdentifier()), 
//									   new Boolean(true), 
//									   lineLocation, 
//									   new Integer(-1), 
//									   new Integer(-1)});
//				    breakpoint.setMarker(breakpointMarker);
//				    
//				    Map map= breakpointMarker.getAttributes();
//				    map.put(IMarker.MESSAGE, "cpp");
//				    breakpointMarker.setAttributes(map);
//				    try
//					{
//					    breakpointManager.addBreakpoint(breakpoint);					    
//					}
//				    catch (DebugException de)
//					{
//					    System.out.println("BreakpointRulerAction: de" +de);
//					}
//				}
//			    catch (CoreException ce)
//				{
//				    System.out.println(ce);
//				}
//
//
//			}
//		}
//	    };
//	    
//
//	    try
//		{
//		    _workspace.run(body, null);
//		}
//	catch (CoreException ce)
//	    {
//		System.out.println(ce);
//	    }
    }
    
    public void run()
    {
	createBreakpointMarker(_subject);
    }
    
}


