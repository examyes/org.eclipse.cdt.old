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

import java.text.MessageFormat;
import java.util.*;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.c.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.refactoring.CRefactoringMatch;
import org.eclipse.cdt.refactoring.CRefactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Used per refactoring to cache the IASTTranslationUnits. Collects methods operating
 * on ASTNodes.
 */
public class ASTManager {
    public final static int TRUE= 1;
    public final static int FALSE= 0;
    public final static int UNKNOWN= -1;
    
    private CRefactory fRefactory;
    private Map fTranslationUnits= new HashMap();
    private HashSet fProblemUnits= new HashSet();

    public static int getOffset(IASTNode node) {
        IASTFileLocation fl= getFileLocation(node);
        if (fl != null) {
            return fl.getNodeOffset();
        }
        return -1;
    }

    public static IASTName getSimpleName(IASTName name) {
        if (name instanceof ICPPASTQualifiedName) {
            IASTName names[]= ((ICPPASTQualifiedName) name).getNames();
            if (names.length > 0) {
                name= names[names.length-1];
            }
        }
        return name;
    }

    public static IASTFileLocation getFileLocation(IASTNode node) {
        IASTNodeLocation[] locs = node.getNodeLocations();
        if (locs != null && locs.length == 1) {
            IASTNodeLocation loc= locs[0];
            if (loc instanceof IASTFileLocation) {
                return (IASTFileLocation) loc;
            }
        }
        return node.getTranslationUnit().flattenLocationsToFile(locs);
    }
    
    /**
     * Returns TRUE, FALSE or UNKNOWN.
     * @throws DOMException 
     */
    public int isSameBinding(IBinding b1, IBinding b2) throws DOMException {
        if (b1==null || b2==null) {
            return UNKNOWN;
        }
        if (b1.equals(b2)) {
            return TRUE;
        }
        String n1= b1.getName();
        String n2= b2.getName();
        if (!n1.equals(n2)) {
            return FALSE;
        }
        if (b1 instanceof ICompositeType) {
            if (!(b2 instanceof ICompositeType)) {
                return FALSE;
            }
            ICompositeType c1= (ICompositeType) b1;
            ICompositeType c2= (ICompositeType) b2;
            if (c1.getKey() != c2.getKey()) {
                return FALSE;
            }
            IScope s1= c1.getCompositeScope();
            IScope s2= c2.getCompositeScope();
            return isSameScope(s1, s2, false);
        }

        if (b1 instanceof IFunction) {
            if (!(b2 instanceof IFunction)) {
                return FALSE;
            }
            IFunction c1= (IFunction) b1;
            IFunction c2= (IFunction) b2;
            boolean fileStatic= false;
            if (b1 instanceof ICPPMethod) {
                if (!(b2 instanceof ICPPMethod)) {
                    return FALSE;
                }
            }
            else {
                fileStatic= c1.isStatic() || c2.isStatic();
            }
            int r1= isSameScope(b1.getScope(), b2.getScope(), fileStatic);
            if (r1 == FALSE) {
                return FALSE;
            }
            
            int r2= hasSameSignature(c1, c2);
            if (r2 == FALSE) {
                return FALSE;
            }
            if (r1!=r2) {
                return UNKNOWN;
            }
            return r1;
        }

        if (b1 instanceof IVariable) {
            IVariable c1= (IVariable) b1;
            IVariable c2= (IVariable) b2;
            boolean fileStatic= false;
            if (!(b2 instanceof IVariable)) {
                return FALSE;
            }
            if (b1 instanceof IField) {
                if (!(b2 instanceof IField)) {
                    return FALSE;
                }
            }
            else if (b1 instanceof IParameter) {
                if (!(b2 instanceof IParameter)) {
                    return FALSE;
                }
            }
            else {
                if (b2 instanceof IField || b2 instanceof IParameter) {
                    return FALSE;
                }
                fileStatic= c1.isStatic() || c2.isStatic();
            }
            int result= isSameScope(c1.getScope(), c2.getScope(), fileStatic);
            return result == UNKNOWN ? TRUE : result;
        }

        if (!b1.getClass().equals(b2.getClass())) {
            return FALSE;
        }
        return isSameScope(b1.getScope(), b2.getScope(), false);
    }
    
    public static int isSameScope(IScope s1, IScope s2, boolean fileStatic) throws DOMException {
        if (s1==s2) {
            return TRUE;
        }
        if (s1==null || s2==null) {
            return UNKNOWN;
        }
        
        if (s1.equals(s2)) {
            return TRUE;
        }
        
        String name1= getName(s1);
        String name2= getName(s2);
        
        if (s1 instanceof ICPPBlockScope) {
            if (s2 instanceof ICPPBlockScope) {
                ICPPBlockScope b1= (ICPPBlockScope) s1;
                ICPPBlockScope b2= (ICPPBlockScope) s2;
                return hasSameLocation(b1, b2, fileStatic);
            }
            return FALSE;
        }
        if (s1 instanceof ICPPNamespaceScope) {
            if (s2 instanceof ICPPNamespaceScope) {
                ICPPNamespaceScope n1= (ICPPNamespaceScope) s1;
                ICPPNamespaceScope n2= (ICPPNamespaceScope) s2;
                int r1= hasSameLocation(n1, n2, fileStatic);
                if (r1 == TRUE) {
                    return r1;
                }
                if (!name1.equals(name2)) {
                    return FALSE;
                }
                return isSameScope(n1.getParent(), n2.getParent(), fileStatic);
            }
            return FALSE;
        }

        if (!name1.equals(name2)) {
            return FALSE;
        }

        // classes
        if (s1 instanceof ICPPClassScope) {
            if (s2 instanceof ICPPClassScope) {
                return isSameScope(s1.getParent(), s2.getParent(), fileStatic);
            }
            return FALSE;
        }
        if (s1 instanceof ICCompositeTypeScope) {
            if (s2 instanceof ICCompositeTypeScope) {
                return isSameScope(s1.getParent(), s2.getParent(), fileStatic);
            }
            return FALSE;
        }
        if (s1 instanceof ICPPFunctionScope) {
            if (s2 instanceof ICPPFunctionScope) {
                return hasSameLocation(s1, s2, true);
            }
            return FALSE;
        }
        if (s1 instanceof ICFunctionScope || s1 instanceof ICFunctionPrototypeScope) {
            if (s2 instanceof ICFunctionScope || s2 instanceof ICFunctionPrototypeScope) {
                return hasSameLocation(s1, s2, true);
            }
            return FALSE;
        }
        
        return isSameScope(s1.getParent(), s2.getParent(), fileStatic);
    }

    public static String getName(IScope s1) {
        String name= null;
        try {
            name= getNameOrNull(s1.getPhysicalNode());
        }
        catch (DOMException e) {
        }
        return name == null ? s1.toString() : name;
    }

    public static String getFirstEnclosingName(IBinding binding) {
        IScope s= null;
        try {
            s = binding.getScope();
        } catch (DOMException e) {
        }
        return s != null ? getFirstEnclosingName(s) : binding.toString();
    }

    public static String getFirstEnclosingName(IScope scope) {
        IScope s= scope;
        String name= null;
        while (name== null && s != null) {
            try {
                name= getNameOrNull(s.getPhysicalNode());
            } catch (DOMException e) {
            }
            try {
                s= s.getParent();
            } catch (DOMException e) {
                s= null;
            }
        }
        return name==null ? s.toString() : name;
    }

    public static int hasSameSignature(IFunction b1, IFunction b2) throws DOMException {
        int r1= isSameParameterList(b1.getParameters(), b2.getParameters());
        if (r1 == FALSE) {
            return FALSE;
        }
        if (b1 instanceof ICPPMethod) {
            if (b2 instanceof ICPPMethod) {                
//                ICPPMethod m1= (ICPPMethod) b1;
//                ICPPMethod m2= (ICPPMethod) b2;
                // mstodo check for const, restrict
                return r1;
            }
            else {
                return FALSE;
            }
        }
        return r1;
    }

    public static int hasSameSignature(IFunctionType b1, IFunctionType b2) throws DOMException {
        return isSameParameterList(b1.getParameterTypes(), b2.getParameterTypes());
    }        

    private static int isSameParameterList(IType[] p1, 
            IType[] p2) throws DOMException {
        if (p1==p2) {
            return TRUE;
        }
        if (p1 == null || p2 == null) {
            return UNKNOWN;
        }
        if (p1.length != p2.length) {
            return FALSE;
        }
        int retval= TRUE;
        for (int i = 0; i < p2.length; i++) {
            switch (isSameType(p1[i], p2[i])) {
                case FALSE:
                    return FALSE;
                case UNKNOWN:
                    retval= UNKNOWN;
                    break;
            }
        }
        
        return retval;
    }

    private static int isSameParameterList(IParameter[] p1, 
            IParameter[] p2) throws DOMException {
        if (p1==p2) {
            return TRUE;
        }
        if (p1 == null || p2 == null) {
            return UNKNOWN;
        }
        if (p1.length != p2.length) {
            return FALSE;
        }
        int retval= TRUE;
        for (int i = 0; i < p2.length; i++) {
            switch (isSameType(p1[i].getType(), p2[i].getType())) {
                case FALSE:
                    return FALSE;
                case UNKNOWN:
                    retval= UNKNOWN;
                    break;
            }
        }
        
        return retval;
    }


    private static int isSameType(IType t1, IType t2) throws DOMException {
        t1= getRealType(t1);
        t2= getRealType(t2);
        if (t1==t2) {
            return TRUE;
        }
        if (t1 == null || t2 == null || t1 instanceof IProblemBinding ||
                t2 instanceof IProblemBinding) {
            return UNKNOWN;
        }
        
        if (t1 instanceof IArrayType) {
            if (t2 instanceof IArrayType) {
                IArrayType a1= (IArrayType) t1;
                IArrayType a2= (IArrayType) t2;
                return isSameType(a1.getType(), a2.getType());
            }
            return FALSE;
        }
        
        if (t1 instanceof IBasicType) {
            if (t2 instanceof IBasicType) {
                IBasicType a1= (IBasicType) t1;
                IBasicType a2= (IBasicType) t2;
                if (getBasicType(a1.getType()) != getBasicType(a2.getType())) {
                    return FALSE;
                }
                if (getSigned(a1) != getSigned(a2) || a1.isUnsigned() != a2.isUnsigned()) {
                    return FALSE;
                }
                if (a1.isLong() != a2.isLong() || a1.isShort() != a2.isShort()) {
                    return FALSE;
                }
                return TRUE;
            }
            return FALSE;
        }

        if (t1 instanceof ICompositeType) {
            if (t2 instanceof ICompositeType) {
                ICompositeType a1= (ICompositeType) t1;
                ICompositeType a2= (ICompositeType) t2;
                if (a1.getKey() != a2.getKey()) {
                    return FALSE;
                }
                return isSameScope(a1.getCompositeScope(), a2.getCompositeScope(), false);
            }
            return FALSE;
        }

        if (t1 instanceof ICPPReferenceType) {
            if (t2 instanceof ICPPReferenceType) {
                ICPPReferenceType a1= (ICPPReferenceType) t1;
                ICPPReferenceType a2= (ICPPReferenceType) t2;
                return isSameType(a1.getType(), a2.getType());
            }
            return FALSE;
        }

        if (t1 instanceof ICPPTemplateTypeParameter) {
            if (t2 instanceof ICPPTemplateTypeParameter) {
                return TRUE;
            }
            return FALSE;
        }

        if (t1 instanceof IEnumeration) {
            if (t2 instanceof IEnumeration) {
                IEnumeration a1= (IEnumeration) t1;
                IEnumeration a2= (IEnumeration) t2;
                
                return isSameScope(a1.getScope(), a2.getScope(), false);
            }
            return FALSE;
        }

        if (t1 instanceof IFunctionType) {
            if (t2 instanceof IFunctionType) {
                IFunctionType a1= (IFunctionType) t1;
                IFunctionType a2= (IFunctionType) t2;
                return hasSameSignature(a1, a2);
            }
            return FALSE;
        }

        if (t1 instanceof IPointerType) {
            if (t2 instanceof IPointerType) {
                IPointerType a1= (IPointerType) t1;
                IPointerType a2= (IPointerType) t2;
                if (a1.isConst() != a2.isConst() || a1.isVolatile() != a2.isVolatile()) {
                    return FALSE;
                }
                return isSameType(a1.getType(), a2.getType());
            }
            return FALSE;
        }

        if (t1 instanceof IQualifierType) {
            if (t2 instanceof IQualifierType) {
                IQualifierType a1= (IQualifierType) t1;
                IQualifierType a2= (IQualifierType) t2;
                if (a1.isConst() != a2.isConst() || a1.isVolatile() != a2.isVolatile()) {
                    return FALSE;
                }
                return isSameType(a1.getType(), a2.getType());
            }
            return FALSE;
        }
        
        return UNKNOWN;
    }

    private static boolean getSigned(IBasicType a2) throws DOMException {
        if (a2.isSigned()) {
            return true;
        }
        if (a2.isUnsigned()) {
            return false;
        }
        switch(a2.getType()) {
        case IBasicType.t_int:
        case IBasicType.t_unspecified:
            return true;
        }
        return false;
    }

    private static int getBasicType(int bc) {
        if (bc== IBasicType.t_unspecified) {
            bc= IBasicType.t_int;
        }
        return bc;
    }

    private static IType getRealType(IType t) {
        while(t instanceof ITypedef) {
            try {
                t= ((ITypedef) t).getType();
            } catch (DOMException e) {
            }
        }
        return t;
    }

    private static String getNameOrNull(IASTNode node) {
        if (node instanceof IASTDeclarator) {
            return getSimpleName(((IASTDeclarator) node).getName()).toString();
        }        
        if (node instanceof IASTNamedTypeSpecifier) {
            return getSimpleName(((IASTNamedTypeSpecifier) node).getName()).toString();
        }
        if (node instanceof IASTCompositeTypeSpecifier) {
            return getSimpleName(((IASTCompositeTypeSpecifier) node).getName()).toString();
        }
        if (node instanceof IASTTranslationUnit) {
            return ((IASTTranslationUnit) node).getFilePath();
        }
        return null;
    }

    private static int hasSameLocation(IScope s1, IScope s2, boolean fileStatic) throws DOMException {
        IASTNode node1= s1.getPhysicalNode();
        IASTNode node2= s2.getPhysicalNode();

        if (node1 == null || node2 == null) {
            return UNKNOWN;
        }
        if (!fileStatic && node1 instanceof IASTTranslationUnit &&
                node2 instanceof IASTTranslationUnit) {
            return TRUE;
        }
        
        IASTFileLocation l1= getFileLocation(node1);
        IASTFileLocation l2= getFileLocation(node2);
        if (l1==null || l2==null) {
            return UNKNOWN;
        }
        if (!l1.getFileName().equals(l2.getFileName())) {
            return FALSE;
        }
        if (l1.getNodeOffset() != l2.getNodeOffset()) {
            return FALSE;
        }
        if (l1.getNodeLength() != l2.getNodeLength()) {
            return FALSE;
        }
        return TRUE;
    }

    public ASTManager(CRefactory refactoringManager) {
        fRefactory= refactoringManager;
    }

    void analyzeArgument(CRefactoringArgument argument, IProgressMonitor pm, RefactoringStatus status) {
        if (argument == null) {
            return;
        }
         
        if (argument.getArgumentKind() != CRefactory.ARGUMENT_UNKNOWN) {
            return;
        }
        
        pm.beginTask(Messages.getString("ASTManager.task.analyze"), 2); //$NON-NLS-1$
        IASTTranslationUnit tu= getTranslationUnit(argument.getSourceFile(), status);
        pm.worked(1);
        analyzeArgument(argument, tu, pm, status);
        pm.worked(1);
        pm.done();
    }

    private void analyzeArgument(CRefactoringArgument argument, IASTTranslationUnit tu, IProgressMonitor pm, 
            RefactoringStatus status) {
        if (tu == null) {
            return;
        }
        
        IASTName name= findNameAtLocation(tu, argument.getOffset());
        if (name == null) {
            return;
        }
        argument.setName(name);
        IBinding binding= name.resolveBinding();
        if (binding != null) {
            IScope scope= null;
            try {
                scope = binding.getScope();
            } catch (DOMException e) {
                handleDOMException(tu, e, status);
            }
            argument.setBinding(name.getTranslationUnit(), binding, scope);
        }
    }

    private IASTTranslationUnit getTranslationUnit(IFile sourceFile, RefactoringStatus status) {
        IASTTranslationUnit tu=  (IASTTranslationUnit) fTranslationUnits.get(sourceFile);
        if (tu == null) {
            tu= fRefactory.getTranslationUnit(sourceFile, status);
            if (tu != null) {
                fTranslationUnits.put(sourceFile, tu);
            }
        }
        return tu;
    }

    public int[] findRangeOfScope(CRefactoringArgument argument, RefactoringStatus status) {
        int[] result= new int[] {0, Integer.MAX_VALUE};
        IScope scope= argument.getScope();
        if (argument.getBinding() instanceof IParameter) {
            try {
                scope= scope.getParent();
            } catch (DOMException e) {
                handleDOMException(argument.getTranslationUnit(), e, status);
            }
        }
        IASTNode node= null;
        try {
            node = scope.getPhysicalNode();
        } catch (DOMException e) {
            handleDOMException(argument.getTranslationUnit(), e, status);
        }
        if (node != null) {
            IASTFileLocation loc= getFileLocation(node);
            if (loc != null) {
                result[0]= loc.getNodeOffset();
                result[1]= result[0] + loc.getNodeLength();
            }
        }
        return result;
    }

    public void analyzeMatches(ArrayList matches, String name, IBinding[] bindings, 
            String replacementText, IProgressMonitor monitor, 
            Set equalBindings, Collection conflictingBindings, RefactoringStatus status) {
        int fileCount= countFiles(matches);
        monitor.beginTask(Messages.getString("ASTManager.task.generateAst"), 2*fileCount); //$NON-NLS-1$
        int i= 0;
        while (i<matches.size()) {
            CRefactoringMatch match = (CRefactoringMatch) matches.get(i);
            IFile file= match.getFile();
            IASTTranslationUnit tu= getTranslationUnit(file, status);
            monitor.worked(1);
            int iEnd= i+1;
            for (; iEnd<matches.size(); iEnd++) {
                if (!((CRefactoringMatch) matches.get(iEnd)).getFile().equals(file)) {
                    break;
                }
            }
            List relevantMatches=  matches.subList(i, iEnd);
            analyzeMatches(tu, name, bindings, relevantMatches, replacementText, 
                    equalBindings, conflictingBindings, status);
            i= i+ relevantMatches.size();
            monitor.worked(1);
        }
        
        monitor.done();
    }

    private void analyzeMatches(IASTTranslationUnit tu, final String lookfor,
            final IBinding[] bindings, final List relevantMatches, 
            final String replacementText, final Set equalBindings,
            final Collection conflictingBindings,
            final RefactoringStatus status) {
        final int lookforLen= lookfor.length();
        ASTNameVisitor nv = 
            new ASTNameVisitor() {
            protected int visitName(IASTName name) {
                String nameStr= name.toString();
                int len= nameStr.length();
                if (len == lookforLen) {
                    if (nameStr.equals(lookfor)) {
                        analyzeMatch(name, bindings, relevantMatches, replacementText, 
                                false, equalBindings, conflictingBindings, status);
                    }
                }
                else if (len == lookforLen+1) {
                    if (nameStr.charAt(0) == '~' && nameStr.endsWith(lookfor)) {
                        analyzeMatch(name, bindings, relevantMatches, replacementText, 
                                true, equalBindings, conflictingBindings, status);
                    }
                }
//                else if (len > lookforLen) {
//                    if (nameStr.charAt(lookforLen) == '<' && nameStr.startsWith(lookfor)) {
//                        analyzeMatch(name, bindings, relevantMatches, replacementText, false, 
//                                conflictingBindings);
//                    }
//                }                        
                return ASTVisitor.PROCESS_CONTINUE;
            }
        };
        nv.applyTo(tu);
    }

    protected void analyzeMatch(IASTName name, IBinding[] bindings, 
            List relevantMatches, String replacementText, boolean isDestructor, 
            Set equalBindings, Collection conflictingBindings, RefactoringStatus status) {
        IASTFileLocation loc= getFileLocation(name);
        if (loc != null) {
            int offset= loc.getNodeOffset();
            if (isDestructor) {
                offset++;
            }
            for (Iterator iter = relevantMatches.iterator(); iter.hasNext();) {
                CRefactoringMatch match = (CRefactoringMatch) iter.next();
                if (match.getOffset() == offset) {
                    IBinding binding= name.resolveBinding();
                    int cmp= FALSE;
                    if (equalBindings.contains(binding)) {
                        cmp= TRUE;
                    }
                    else if (binding instanceof IProblemBinding) {
                        cmp= UNKNOWN;
                        handleProblemBinding(name.getTranslationUnit(), (IProblemBinding) binding, status);
                    }
                    else {
                        for (int i = 0; i < bindings.length; i++) {
                            IBinding renameBinding = bindings[i];
                            try {
                                int cmp0= isSameBinding(binding, renameBinding);
                                if (cmp0 != FALSE) {
                                    cmp= cmp0;
                                }
                                if (cmp0 == TRUE) {
                                    equalBindings.add(renameBinding);
                                    break;
                                }
                            }
                            catch (DOMException e) {
                                handleDOMException(name.getTranslationUnit(), e, status);
                                cmp= UNKNOWN;
                            }
                        }
                    }
                    switch(cmp) {
                    case TRUE:
                        match.setASTInformation(CRefactoringMatch.AST_REFERENCE);
                        if (replacementText != null) {
                            IScope scope= workaround_getContainingScope(name);
                            if (scope != null) {
                                IBinding[] conflicting= null;
                                try {
                                    conflicting= workaround_find(scope, replacementText);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (conflicting != null && conflicting.length > 0) {
                                    conflictingBindings.addAll(Arrays.asList(conflicting));
                                }
                            }
                        }
                        break;
                    case FALSE:
                        iter.remove();
                        break;
                    }
                    break; // break the loop
                }
            }
        }
    }

    private int countFiles(ArrayList matches) {
        IFile file= null;
        int count= 0;
        for (Iterator iter = matches.iterator(); iter.hasNext();) {
            CRefactoringMatch match = (CRefactoringMatch) iter.next();
            if (!match.getFile().equals(file)) {
                count++;
                file= match.getFile();
            }
        }
        return count;
    }

    private IASTName findNameAtLocation(IASTTranslationUnit tu, int offset) {
        final IASTName[] result= new IASTName[] {null};
        ASTNameVisitor nv = new ASTNameVisitor(offset) {
            protected int visitName(IASTName name) {
                result[0]= name;
                return ASTVisitor.PROCESS_ABORT;
            }
        };
        nv.applyTo(tu);
        return result[0];
    }

    private IScope workaround_getContainingScope(IASTName name) {
        IASTTranslationUnit tu= name.getTranslationUnit();
        if (tu == null) {
            return null;
        }
        if (tu instanceof ICPPASTTranslationUnit) {
            return CPPVisitor.getContainingScope(name);
        }
        return CVisitor.getContainingScope(name);
    }
    
    public void handleDOMException(IASTTranslationUnit tu, DOMException e, RefactoringStatus status) {
        if (tu != null) {
            if (fProblemUnits.add(tu)) {
                status.addWarning(MessageFormat.format(
                        Messages.getString("ASTManager.warning.parsingErrors"), //$NON-NLS-1$
                        new Object[] {tu.getFilePath()}));
            }
        }
    }

    public void handleProblemBinding(IASTTranslationUnit tu, IProblemBinding pb, RefactoringStatus status) {
        if (tu != null) {
            if (fProblemUnits.add(tu)) {
                status.addWarning(MessageFormat.format(
                        Messages.getString("ASTManager.warning.parsingErrors"), //$NON-NLS-1$
                        new Object[] {tu.getFilePath()}));
            }
        }
    }

    public static int isVirtualMethod(CPPMethod method) throws DOMException {
        IASTDeclaration decl= method.getPrimaryDeclaration();
        IASTDeclSpecifier spec= null;
        if (decl instanceof IASTFunctionDefinition) {
            IASTFunctionDefinition def = (IASTFunctionDefinition) decl;
            spec= def.getDeclSpecifier();
        }
        else if (decl instanceof IASTSimpleDeclaration) {
            IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) decl;
            spec= sdecl.getDeclSpecifier();
        }
        if (spec instanceof ICPPASTDeclSpecifier) {
            ICPPASTDeclSpecifier cppSpec = (ICPPASTDeclSpecifier) spec;
            if (cppSpec.isVirtual()) {
                return TRUE;
            }
        }
            
        IScope scope= method.getScope();
        if (scope instanceof ICPPClassScope) {
            ICPPClassScope classScope = (ICPPClassScope) scope;
            ICPPClassType classType= classScope.getClassType();
            ICPPBase[] bases= classType.getBases();
            for (int i = 0; i < bases.length; i++) {
                ICPPBase base = bases[i];
                ICPPClassType baseType= base.getBaseClass();
                if (baseType != null) {
                    IScope baseScope= baseType.getCompositeScope();
                    if (baseScope != null) {
                        IBinding[] alternates= baseScope.find(method.getName());
                        for (int j = 0; j < alternates.length; j++) {
                            IBinding binding = alternates[j];
                            if (binding instanceof CPPMethod) {
                                CPPMethod alternateMethod = (CPPMethod) binding;
                                if (hasSameSignature(method, alternateMethod)!=FALSE) {
                                    if (isVirtualMethod(alternateMethod)==TRUE) {
                                        return TRUE;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return FALSE;
    }

    public static boolean isLocalVariable(IVariable v) {
        if (v instanceof IParameter) {
            return false;
        }
        IScope scope;
        try {
            scope = v.getScope();
        } catch (DOMException e) {
            return false;
        }
        if (scope instanceof ICPPFunctionScope ||
                scope instanceof ICPPBlockScope ||
                scope instanceof ICFunctionScope) {
            return true;
        }
        return false;
    }

    public static IBinding[] workaround_find(IScope scope, String name) throws DOMException {
        IBinding[] result= null;
        while (scope != null && (result==null || result.length==0)) {
            result = scope.find(name);
            scope= scope.getParent();
        }
        return result;
    }
}
