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

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;
import org.eclipse.cdt.refactoring.CRefactory;
import org.eclipse.cdt.refactoring.ICRefactoringArgument;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Represents the input to a refactoring. Important is file and offset the rest
 * can be calculated from the AST.
 */
public class CRefactoringArgument implements ICRefactoringArgument {
    private int fOffset=0;
    private String fText= ""; //$NON-NLS-1$
    private int fKind= CRefactory.ARGUMENT_UNKNOWN;
    private IFile fFile;
    
    private IBinding fBinding;
    private IScope fScope;
    private IASTTranslationUnit fTranslationUnit;

    public CRefactoringArgument(ITextEditor editor, ITextSelection selection) {
        fKind= CRefactory.ARGUMENT_UNKNOWN;
        fText= selection.getText();
        fOffset= selection.getOffset();
        if (editor != null) {
            IEditorInput ei= editor.getEditorInput();
            if (ei instanceof IFileEditorInput) {
                IFileEditorInput fei= (IFileEditorInput) ei;
                fFile= fei.getFile();
            }
            IDocumentProvider dp= editor.getDocumentProvider();
            if (dp != null) {
                IDocument doc= dp.getDocument(ei);
                if (doc != null) {
                    setToWord(doc, selection);
                }
            }
        }
    }
    
    public CRefactoringArgument(IFile file, int offset, String text) {
        fKind= CRefactory.ARGUMENT_UNKNOWN;
        fText= text;
        fOffset= offset;
        fFile= file;
    }
        

    private void setToWord(IDocument document, ITextSelection sel) {
        int offset = sel.getOffset();
        int length = sel.getLength();
        
        // in case the length is zero we have to decide whether to go
        // left or right.
        if (length==0) {
            // try right
            char chr= 0;
            char chl= 0;
            try {               
                chr= document.getChar(offset);
            }
            catch (BadLocationException e2) {
            }
            try {               
                chl= document.getChar(offset-1);
            }
            catch (BadLocationException e2) {
            }
            
            if (isPartOfIdentifier(chr)) {
                length=1;
            }
            else if (isPartOfIdentifier(chl)) {
                offset--;
                length=1;
            }
            else {
                return;
            }
        }
                
        int a= offset+length-1;
        int z= a;

        // move z one behind last character.
        try {               
            char ch = document.getChar(z);
            while(isPartOfIdentifier(ch)) {
                ch = document.getChar(++z);
            }
        }
        catch (BadLocationException e2) {
        }
        // move a one before the first character
        try {               
            char ch = document.getChar(a);
            while(isPartOfIdentifier(ch)) {
                ch = document.getChar(--a);
            }
        }
        catch (BadLocationException e2) {
        }
        
        if (a==z) {
            offset= a;
            length= 0;
        }
        else {
            offset= a+1;
            length= z-a-1;
        }
        try {
            fText= document.get(offset, length);
            fOffset= offset;
        } catch (BadLocationException e) {
        }
    }

    private boolean isPartOfIdentifier(char chr) {
        if (chr>='a' && chr<='z') {
            return true;
        }
        if (chr>='A' && chr<='Z') {
            return true;
        }
        switch(chr) {
        case '_':
                return true;
        }
        return false;
    }

    // overrider
    public String getName() {
        return fText;
    }

    // overrider
    public IFile getSourceFile() {
        return fFile;
    }

    // overrider
    public int getArgumentKind() {
        return fKind;
    }

    // overrider
    public int getOffset() {
        return fOffset;
    }

    public void setName(IASTName name) {
        fText= name.toString();
    }
    
    public void setOffset(int offset) {
        fOffset= offset;
    }
    
    public void setBinding(IASTTranslationUnit tu, IBinding binding, IScope scope) {
        fTranslationUnit= tu;
        fBinding= binding;
        fScope= scope;
        if (binding instanceof IVariable) {
            IVariable var= (IVariable) binding;
            if (binding instanceof IField) {
                fKind= CRefactory.ARGUMENT_FIELD;
            }
            else if (binding instanceof IParameter) {
                fKind= CRefactory.ARGUMENT_PARAMETER;
            }
            else {
                if (ASTManager.isLocalVariable(var, scope)) {
                    fKind= CRefactory.ARGUMENT_LOCAL_VAR;
                }
                else {
                    boolean isStatic= false;
                    try {
                        isStatic= var.isStatic();
                    } catch (DOMException e) {
                    }
                    if (isStatic) {
                        fKind= CRefactory.ARGUMENT_FILE_LOCAL_VAR;
                    }
                    else {
                        fKind= CRefactory.ARGUMENT_GLOBAL_VAR;
                    }
                }
            }
        }
        else if (binding instanceof IEnumerator) {
            fKind= CRefactory.ARGUMENT_ENUMERATOR;
        }
        else if (binding instanceof IFunction) {
            fKind= CRefactory.ARGUMENT_NON_VIRTUAL_METHOD;
            IFunction func= (IFunction) binding;
            if (binding instanceof CPPMethod) {
                CPPMethod method= (CPPMethod) binding;
                int isVirtual= ASTManager.UNKNOWN;
                try {
                    isVirtual = ASTManager.isVirtualMethod(method);
                } catch (DOMException e) {
                }
                if (isVirtual == ASTManager.TRUE) {
                    fKind= CRefactory.ARGUMENT_VIRTUAL_METHOD;
                }
            }
            else {
                boolean isStatic= false;
                try {
                    isStatic= func.isStatic();
                } catch (DOMException e) {
                }
                if (isStatic) {
                    fKind= CRefactory.ARGUMENT_FILE_LOCAL_FUNCTION;
                }
                else {
                    fKind= CRefactory.ARGUMENT_GLOBAL_FUNCTION;
                }
            }
        }
        else if (binding instanceof ICompositeType) {
            fKind= CRefactory.ARGUMENT_CLASS_TYPE;
        }
        else if (binding instanceof IEnumeration || binding instanceof ITypedef) {
            fKind= CRefactory.ARGUMENT_TYPE;
        }
        else if (binding instanceof ICPPNamespace) {
            fKind= CRefactory.ARGUMENT_NAMESPACE;
        }
        else if (binding instanceof IMacroBinding) {
            fKind= CRefactory.ARGUMENT_MACRO;
        }
    }

    public IScope getScope() {
        return fScope;
    }

    public IBinding getBinding() {
        return fBinding;
    }

    public IASTTranslationUnit getTranslationUnit() {
        return fTranslationUnit;
    }
}
