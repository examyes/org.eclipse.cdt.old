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

    public ASTNameVisitor() {
        this(-1);
    }
    
    public ASTNameVisitor(int offset) {
        fOffset= offset;
        shouldVisitDeclarations= shouldVisitStatements= shouldVisitNames= true;
    }
    
    abstract protected int visitName(IASTName name);

    public void applyTo(IASTTranslationUnit tu) {
        if (tu != null) {
            IASTFileLocation fl= ASTManager.getFileLocation(tu);
            if (fl != null) {
                fFileName= fl.getFileName();
                if (fFileName != null) {
                    tu.accept(this);
                }
            }
        }
    }
    
    final public int visit(IASTName name) {
        if (checkLocation(name)) {
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
                    return visitName(names[names.length-1]);
                }
                return PROCESS_SKIP;
            }
            return visitName(name);
        }
        return PROCESS_SKIP;
    }
    
    final public int visit(IASTDeclaration decl) {
        if (checkLocation(decl)) {
            return PROCESS_CONTINUE;
        }
        return PROCESS_SKIP;
    }
     
    final public int visit(IASTStatement statement) {
        if (checkLocation(statement)) {
            return PROCESS_CONTINUE;
        }
        return PROCESS_SKIP;
    }
    
    private boolean checkLocation(IASTNode node) {
        IASTFileLocation fl = ASTManager.getFileLocation(node);
        if (fl != null && fl.getFileName().equals(fFileName)) {
            if (fOffset < 0) {
                return true;
            }
            int off = fl.getNodeOffset();
            int len = fl.getNodeLength();
            return off <= fOffset && fOffset < off+len;
        }
        return false;
    }
}
