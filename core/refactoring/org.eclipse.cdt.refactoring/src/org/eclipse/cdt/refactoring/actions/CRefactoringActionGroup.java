/*******************************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * Copyright (c) 2005 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * Markus Schorn - ported to the needs of new refactoring implementation 
 ******************************************************************************/

package org.eclipse.cdt.refactoring.actions;

import org.eclipse.cdt.refactoring.IPositionConsumer;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action group that adds refactor actions (for example Rename..., Move..., etc)
 * to a context menu and the global menu bar.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
public class CRefactoringActionGroup extends ActionGroup implements IPositionConsumer {
    /**
     * Pop-up menu: id of the refactor sub menu (value <code>org.eclipse.cdt.ui.refactoring.menu</code>).
     * 
     * @since 2.1
     */
    public static final String MENU_ID = "org.eclipse.cdt.ui.refactoring.menu"; //$NON-NLS-1$

    /**
     * Pop-up menu: id of the reorg group of the refactor sub menu (value
     * <code>reorgGroup</code>).
     * 
     * @since 2.1
     */
    public static final String GROUP_REORG = "reorgGroup"; //$NON-NLS-1$

    /**
     * Pop-up menu: id of the type group of the refactor sub menu (value
     * <code>typeGroup</code>).
     * 
     * @since 2.1
     */
    public static final String GROUP_TYPE = "typeGroup"; //$NON-NLS-1$

    /**
     * Pop-up menu: id of the coding group of the refactor sub menu (value
     * <code>codingGroup</code>).
     * 
     * @since 2.1
     */
    public static final String GROUP_CODING = "codingGroup"; //$NON-NLS-1$

    /**
     * Pop-up menu: id of the undo/redo of the refactor sub menu (value
     * <code>undoRedoGroup</code>).
     * 
     * @since 2.1
     */
    private static final String GROUP_UNDO = "undoRedoGroup"; //$NON-NLS-1$

    private String fGroupName= IWorkbenchActionConstants.GROUP_REORGANIZE;
    private CRenameAction fRenameAction;
    private CUndoAction fUndoAction;
    private CRedoAction fRedoAction;

    
    /**
     * Creates a new <code>RefactorActionGroup</code>. 
     */
    public CRefactoringActionGroup(IWorkbenchWindow ww, String groupName) {
        if (groupName != null && groupName.length() > 0) {
            fGroupName= groupName;
        }
        fRenameAction = new CRenameAction();
        fUndoAction= new CUndoAction(ww); //$NON-NLS-1$
        fRedoAction= new CRedoAction(ww); //$NON-NLS-1$
    }
    
    public void init(IWorkbenchPartSite site) {
        fRenameAction.setWorkbenchPart(site.getPart());
    }
    
    public void setEditor(ITextEditor textEditor) {
        fRenameAction.setEditor(textEditor);
        ISelection sel= null;
        if (textEditor != null) {
            sel= textEditor.getSelectionProvider().getSelection();
        }
        fUndoAction.selectionChanged(sel);
        fRedoAction.selectionChanged(sel);
    }


    /* (non-Javadoc)
     * Method declared in ActionGroup
     */
    public void fillActionBars(IActionBars actionBars) {
        super.fillActionBars(actionBars);
        actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), fRenameAction);
    }

    /* (non-Javadoc)
     * Method declared in ActionGroup
     */
    public void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);
        IMenuManager refactorSubmenu = new MenuManager("Refactor", MENU_ID); //$NON-NLS-1$
        refactorSubmenu.add(new Separator(GROUP_REORG));
        refactorSubmenu.add(fRenameAction);
        refactorSubmenu.add(new Separator(GROUP_UNDO));
        refactorSubmenu.add(fUndoAction);
        refactorSubmenu.add(fRedoAction);
        
        menu.appendToGroup(fGroupName, refactorSubmenu);
    }

    public void updateActionBars() {
    }

    /*
     * @see ActionGroup#dispose()
     */
    public void dispose() {
        fUndoAction.dispose();
        super.dispose();
    }

    public void setPosition(IFile file, int startPos, String text) {
        fRenameAction.setPosition(file, startPos, text);
        fUndoAction.selectionChanged(null);
        fRedoAction.selectionChanged(null);
    }
}
