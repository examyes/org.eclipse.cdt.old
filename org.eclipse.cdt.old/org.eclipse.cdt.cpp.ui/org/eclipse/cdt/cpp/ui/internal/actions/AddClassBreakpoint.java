package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
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


public class AddClassBreakpoint extends AddFunctionBreakpoint
{ 
    public AddClassBreakpoint(DataElement subject, String label, DataElement command, DataStore dataStore)
    {	
        super(subject, label, command, dataStore);
    }

    public void run()
      {
	  for (int i = 0; i < _subject.getNestedSize(); i++)
	      {
		  DataElement member = _subject.get(i);
		  if (member.getType().equals("function") || member.getType().equals("constructor"))
		      {
			  createFunctionBreakpoint(member);
		      }
	      }
      }
}


