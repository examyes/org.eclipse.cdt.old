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
        

/**
 * Redo an undone refactoring.
 * @deprecated This action is now longer needed. Undo is now performed via the
 *  global undo/redo stack provided by <code>org.eclipse.core.commands</code>. 
 */          
public class CRedoWorkbenchActionDelegate extends org.eclipse.ltk.ui.refactoring.RedoRefactoringAction {
}
