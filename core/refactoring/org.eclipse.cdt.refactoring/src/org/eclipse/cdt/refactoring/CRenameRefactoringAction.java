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

package org.eclipse.cdt.refactoring;
        
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.*;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Rename action that can be contributed to the editor or the workbench-window.
 */          
public class CRenameRefactoringAction 
		implements IWorkbenchWindowActionDelegate, IEditorActionDelegate {

    private IWorkbenchWindow fWorkbenchWindow;
    private ICRefactoringArgument fArgument;
    private IEditorPart fEditorPart= null;

    // overrider IActionDelegate
    public void run(IAction action) {
        if (fWorkbenchWindow==null) {
            return;
        }
        // need to reeval selection
        if (fEditorPart != null) {
            setEditorPart(fEditorPart);
        }
        if (fArgument != null) {
            CRefactory.getInstance().rename(fWorkbenchWindow.getShell(), fArgument);
        }
    }

    // overrider IActionDelegate
    public void selectionChanged(IAction action, ISelection selection) {
        fArgument= null;
        fEditorPart= null;
        if (fWorkbenchWindow!=null) {
            IWorkbenchPage page= fWorkbenchWindow.getActivePage();
            IEditorPart editorPart= page.getActiveEditor();
            if (editorPart == page.getActivePart()) {
                setEditorPart(editorPart);
            }
        }
        action.setEnabled(fWorkbenchWindow!=null && fArgument!=null);
    }

    private void setEditorPart(IEditorPart editorPart) {
        if (editorPart instanceof ITextEditor) {
            ITextEditor textEditor= (ITextEditor) editorPart;
            ISelectionProvider provider= textEditor.getSelectionProvider();
            if (provider != null) {
                ISelection s= provider.getSelection();
                if (s instanceof ITextSelection) {
                    fArgument= CRefactory.createArgument(
                            textEditor, (ITextSelection) s);
                    fEditorPart= editorPart;
                }
            }
        }
    }

    // overrider IWorkbenchWindowActionDelegate
    public void dispose() {        
    }

    // overrider IWorkbenchWindowActionDelegate
    public void init(IWorkbenchWindow window) {
        fWorkbenchWindow= window;
    }

    // overrider IEditorActionDelegate
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        fArgument= null;
        fWorkbenchWindow= null;
        if (targetEditor != null) {
            fWorkbenchWindow= targetEditor.getSite().getWorkbenchWindow();
            setEditorPart(targetEditor);
        }
        action.setEnabled(fWorkbenchWindow!=null && fArgument!=null);
    }
}
