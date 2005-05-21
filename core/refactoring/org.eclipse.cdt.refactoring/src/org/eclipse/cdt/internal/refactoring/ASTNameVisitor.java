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

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

/**
 * Visitor to prefer simple ASTNames over the qualified ones. This is different
 * to the strategy used within the dom-package. 
 */
abstract public class ASTNameVisitor extends ASTVisitor {
    private int fOffset= -1;
    private String fFileName;

    public ASTNameVisitor(String fileName) {
        this(fileName, -1);
    }
    
    public ASTNameVisitor(String fileName, int offset) {
        fFileName= fileName;
        fOffset= offset;
        shouldVisitNames=true;
    }
    
    abstract protected int visitName(IASTName name);
    
    final public int visit(IASTName name) {
        if (name instanceof ICPPASTQualifiedName) {
            ICPPASTQualifiedName qn= (ICPPASTQualifiedName) name;
            IASTName[] names= qn.getNames();
            boolean visited= false;
            for (int i = 0; i < names.length; i++) {
                if (checkLocation(names[i])) {
                    if (visitName(names[i]) == PROCESS_ABORT) {
                        return PROCESS_ABORT;
                    }
                    visited= true;
                }
            }
            if (!visited && names.length>0) {
                if (checkLocation(name)) {
                    return visitName(names[names.length-1]);
                }
            }
        }
        else if (checkLocation(name)) {
            return visitName(name);
        }
        return PROCESS_CONTINUE;
    }
        
    private boolean checkLocation(IASTNode node) {
        if (fFileName==null) {
            return true;
        }
        if (!fFileName.equals(node.getContainingFilename())) {
            return false;
        }
        IASTNodeLocation[] locs= node.getNodeLocations();
        if (locs==null || locs.length!=1) {
            return false;
        }
        if (fOffset==-1) {
            return true;
        }
        IASTFileLocation floc= locs[0].asFileLocation();
        int off= floc.getNodeOffset();
        int len = floc.getNodeLength();
        if (locs[0] instanceof IASTMacroExpansion && node instanceof IASTName) {
            IASTName name= (IASTName) node;
            off= 
                ASTManager.backrelateNameToMacroCallArgument(name, 
                        (IASTMacroExpansion) locs[0]);
            len= name.toCharArray().length;
        }
        return off <= fOffset && fOffset < off+len;
    }
}
