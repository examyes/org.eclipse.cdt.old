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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * Allows for alternate search implementations.
 */
public interface ICRefactoringSearch {
    int SCOPE_FILE = 1;
    int SCOPE_WORKSPACE = 2;
    int SCOPE_RELATED_PROJECTS = 3;
    int SCOPE_SINGLE_PROJECT = 4;
    int SCOPE_WORKING_SET = 5;

    IStatus searchWord(int searchScope, IFile file, String selectedWorkingSet, 
            String[] filePatterns, String identifier, IProgressMonitor monitor, List matches);

}
