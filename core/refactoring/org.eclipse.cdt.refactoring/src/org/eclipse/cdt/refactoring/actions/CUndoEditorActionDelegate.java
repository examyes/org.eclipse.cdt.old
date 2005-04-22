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
import org.eclipse.ltk.ui.refactoring.UndoRefactoringAction;
import org.eclipse.ui.*;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Undoes a refactoring.
 */          
public class CUndoEditorActionDelegate implements IEditorActionDelegate {
    private IWorkbenchWindowActionDelegate fDelegate;
    
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        if (targetEditor == null) {
            if (fDelegate != null) {
                fDelegate.dispose();
                fDelegate= null;
            }
            action.setEnabled(false);
        }
        else {
            if (fDelegate==null) {
                fDelegate= new UndoRefactoringAction();
                fDelegate.init(targetEditor.getSite().getWorkbenchWindow());
            }
            if (targetEditor instanceof ITextEditor) {
                ITextEditor txtEditor= (ITextEditor) targetEditor;
                fDelegate.selectionChanged(action,
                        txtEditor.getSelectionProvider().getSelection());
            }
            else {
                action.setEnabled(false);
            }
        }
    }
    public void run(IAction action) {
        if (fDelegate != null) {
            fDelegate.run(action);
        }
    }
    public void selectionChanged(IAction action, ISelection selection) {
        if (fDelegate != null) {
            fDelegate.selectionChanged(action, selection);
        }
    }
}
