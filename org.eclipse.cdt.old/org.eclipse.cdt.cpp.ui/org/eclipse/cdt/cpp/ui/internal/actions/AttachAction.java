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
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerRulerAction;

import org.eclipse.swt.widgets.*;
import org.eclipse.jface.dialogs.*;

public class AttachAction extends CustomAction
{
    private String _invocation;

    public AttachAction(DataElement subject, String label, DataElement command, DataStore dataStore)
    {	
        super(subject, label, command, dataStore);
    }

    public AttachAction(java.util.List subjects, String label, DataElement command, DataStore dataStore)
    {	
        super(subjects, label, command, dataStore);
    }

    public void run()
    {	
   	//org.eclipse.debug.ui.actions.DebugAction dbg = new org.eclipse.debug.ui.actions.DebugAction();
   	//dbg.runWithEvent(null, null);
      IWorkbench workbench = CppPlugin.getDefault().getWorkbench();
      IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
      if (windows != null && windows.length > 0)
      {
   		final Shell shell= windows[0].getShell();

   		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
	      ILaunchConfigurationType configType = lm.getLaunchConfigurationType("org.eclipse.cdt.debugattach.launchconfig");		

         DebugUITools.openLaunchConfigurationDialog(shell, new StructuredSelection(configType), ILaunchManager.DEBUG_MODE);
         // RW - add some code in CppAttachInfoTab:setDefaults() to try and initialize the PID with the one available from this view.
      }
    }
}

