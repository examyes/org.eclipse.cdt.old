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

package org.eclipse.cdt.internal.refactoring;

import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;

/**
 * Refactoring implementation using a refactoring processor.
 */
public class CRenameRefactoring extends ProcessorBasedRefactoring {

    private CRenameProcessor fProcessor;

    public CRenameRefactoring(CRenameProcessor processor) {
        super(processor);
        fProcessor= processor;
    }
    // overrider
    public RefactoringProcessor getProcessor() {
        return fProcessor;
    }
}
