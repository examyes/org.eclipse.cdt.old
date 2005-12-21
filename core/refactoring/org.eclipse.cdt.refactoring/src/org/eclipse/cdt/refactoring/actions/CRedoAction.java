/*******************************************************************************
 * Copyright (c) 2005 Wind River Systems, Inc. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 ******************************************************************************/ 

package org.eclipse.cdt.refactoring.actions;

import org.eclipse.cdt.internal.refactoring.Messages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Redo an undone refactoring.
 * @deprecated This action is now longer needed. Undo is now performed via the
 *  global undo/redo stack provided by <code>org.eclipse.core.commands</code>. 
 */          
public class CRedoAction extends Action {
    private IWorkbenchWindowActionDelegate fDelegate;

    CRedoAction(IWorkbenchWindow ww) {
        super(Messages.getString("CRefactoringActionGroup.aciton.redo.label")); //$NON-NLS-1$
        fDelegate= new org.eclipse.ltk.ui.refactoring.RedoRefactoringAction();
        fDelegate.init(ww);
    }
    
    public void dispose() {
        if (fDelegate != null) {
            fDelegate.dispose();
            fDelegate= null;
        }
    }
    
    public void selectionChanged(ISelection sel) {
        fDelegate.selectionChanged(this, sel);
    }
    
    public void run() {
        fDelegate.run(this);
    }
    
}
