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

package org.eclipse.cdt.internal.refactoring;

import org.eclipse.cdt.refactoring.CRefactory;


/**
 * Rename processor that sets up the input page for renaming a global entity.
 */
public class CRenameMacroProcessor extends CRenameGlobalProcessor {

    public CRenameMacroProcessor(CRenameProcessor processor, String name) {
        super(processor, name);
    }
    
    protected int getAvailableOptions() {
        return CRefactory.OPTION_ASK_SCOPE | 
            CRefactory.OPTION_IN_CODE |
            CRefactory.OPTION_IN_COMMENT | 
            CRefactory.OPTION_IN_MACRO_DEFINITION |
            CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE;
    }
}
