package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.dialogs.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import java.io.*;
import java.util.*;

import org.eclipse.jface.viewers.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.ui.*;

/**
 * This class shows how IActionDelegate implementations
 * should be used for global action registration for menu
 * and tool bars. Action proxy object is created in the
 * desktop based on presentation information in the plugin.xml
 * file. Delegate is not loaded until the first time the user
 * presses the button or selects the menu. Based on the action
 * availability, it is possible that the button will disable
 * instead of executing.
 */
public class CppActionDelegateFolderBuild extends CppActionDelegate 
{
    public void run(IAction action) 
    {
        BuildDialog d= new BuildDialog(_currentResource, "Command History");
	d.open();
        if (d.getReturnCode() == d.OK)
	    {	  
		String invocation = d.getInvocation();
		ModelInterface api = CppPlugin.getModelInterface();	
		api.command(_currentResource, invocation, true);
	    }      
    }    

    protected void checkEnabledState(IAction action)
    {

    }
}
