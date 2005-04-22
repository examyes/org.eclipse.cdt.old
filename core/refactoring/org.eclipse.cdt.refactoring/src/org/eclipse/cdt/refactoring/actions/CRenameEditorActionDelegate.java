/*******************************************************************************
 * Copyright (c) 2004-2005 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 ******************************************************************************/ 

package org.eclipse.cdt.refactoring.actions;
        
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.*;

/**
 * Launches a rename refactoring.
 */          
public class CRenameEditorActionDelegate implements IEditorActionDelegate {
    CRenameAction fAction= new CRenameAction();
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        fAction.setEditor(targetEditor);
        action.setEnabled(fAction.isEnabled());
    }
    public void run(IAction action) {
        fAction.run();
    }
    public void selectionChanged(IAction action, ISelection selection) {
    }
}
