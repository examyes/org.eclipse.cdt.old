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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.refactoring.CRefactory;
import org.eclipse.cdt.refactoring.ICRefactoringArgument;
import org.eclipse.cdt.refactoring.IPositionConsumer;

import org.eclipse.cdt.internal.refactoring.Messages;

/**
 * Launches a rename refactoring.
 */          
public class CRenameAction extends Action implements IPositionConsumer {
    private ITextEditor fEditor;
    private IFile fFile;
    private IWorkbenchSite fSite;
    private int fOffset;
    private String  fText;
    
    public CRenameAction() {
        super(Messages.getString("CRenameRefactoringAction.label")); //$NON-NLS-1$
        setActionDefinitionId("org.eclipse.cdt.ui.edit.text.c.rename.element"); //$NON-NLS-1$
    }
    
    public void setEditor(IEditorPart editor) {
        fEditor= null;
        fSite= null;
        if (editor instanceof ITextEditor) {
            fEditor= (ITextEditor) editor;
        }
        setEnabled(fEditor!=null);
    }

    /**
     * @deprecated use {@link CRenameAction#setSite(IWorkbenchSite).}
     */
    public void setWorkbenchPart(IWorkbenchPart part) {
    	setSite(part.getSite());
    }

	public void setSite(IWorkbenchSite site) {
        fEditor= null;
        fSite= site;
	}

    public void run() {
        if (fEditor != null) {
            ISelectionProvider provider= fEditor.getSelectionProvider();
            if (provider != null) {
                ISelection s= provider.getSelection();
                if (s instanceof ITextSelection) {
                    ICRefactoringArgument arg= CRefactory.createArgument(fEditor, (ITextSelection) s);
                    if (arg != null) {
                        CRefactory.getInstance().rename(fEditor.getSite().getShell(), arg);
                    }                        
                }
            }
        }
        else if (fSite != null) {
            ICRefactoringArgument arg= CRefactory.createArgument(fFile, fOffset, fText);
            if (arg != null) {
                CRefactory.getInstance().rename(fSite.getShell(), arg);
            }                        
        }            
    }

    public void setPosition(IFile file, int startPos, String text) {
        fFile= file;
        fOffset= startPos;
        fText= text;
        if (fFile != null && fText != null) {
            setEnabled(true);
        }
        else {
            setEnabled(false);
        }
    }
}
