package com.ibm.cpp.ui.internal.editor.contributor;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.editor.*;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.*;


/**
 * Implements a Dummy action
 */
public class CppEditorAction extends Action {
/**
 * The constructor
 */
   public CppEditorAction(String label, String tooltip, String fileName)
   {
   	super(label);

 	   this.setToolTipText(tooltip);

      CppPlugin plugin = CppPlugin.getPlugin();
      ImageDescriptor imageDescriptor = plugin.getImageDescriptor(fileName);
      this.setImageDescriptor(imageDescriptor);	
   }
/**
 * Executes the action code
 *
 * Called from the platform.
 */

   public void run()
   {
	  Trace.me("CppLpexEditorAction","actionPerformed(Window)","Action executed");
   }
}
