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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.c.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.refactoring.CRefactoringMatch;
import org.eclipse.cdt.refactoring.CRefactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;

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
    private CRefactoringArgument fArgument;
    private IBinding[] fValidBindings;
    private String fRenameTo;
    private HashSet fEqualToValidBinding;
    private HashSet fConflictingBinding;

    public static IASTFileLocation getLocationInTranslationUnit(IASTNode node) {
        return node.getTranslationUnit().flattenLocationsToFile(
                node.getNodeLocations());
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
    
    /**
     * Returns TRUE, FALSE or UNKNOWN.
     * @throws DOMException 
     */
    public static int isSameBinding(IBinding b1, IBinding b2) throws DOMException {
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
        
        IASTFileLocation l1= node1.getNodeLocations()[0].asFileLocation();
        IASTFileLocation l2= node2.getNodeLocations()[0].asFileLocation();
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

    public static int backrelateNameToMacroCallArgument(IASTName name, IASTMacroExpansion me) {
        int expansionCount= 0;
        IASTMacroExpansion mloc= me;
        IASTNodeLocation[] locs= null;
        boolean done=false;
        while (!done) {
            IASTPreprocessorMacroDefinition mdef= mloc.getMacroDefinition();
            if (!(mdef instanceof IASTPreprocessorFunctionStyleMacroDefinition)) {
                return -1;
            }
            expansionCount++;
            
            locs= mloc.getExpansionLocations();
            if (locs==null || locs.length != 1) {
                return -1;
            }
            IASTNodeLocation aloc= locs[0];
            if (aloc instanceof IASTFileLocation) {
                done= true;
            }
            else if (aloc instanceof IASTMacroExpansion) {
                mloc= (IASTMacroExpansion) aloc;
            }
        }
        IASTMacroExpansion[] macroExpansions= 
            new IASTMacroExpansion[expansionCount];
        macroExpansions[expansionCount-1]= me;
        for (int i= expansionCount-2; i>=0; i--) {
            macroExpansions[i]= (IASTMacroExpansion) macroExpansions[i+1].getExpansionLocations()[0];
        }
        String orig= name.getTranslationUnit().getUnpreprocessedSignature(locs);
        
        int count= countOccurrencesOf(name.toString(), orig);
        if (count != 1) {
            return -1;
        }
        // because of bug#90956 we need to use a heuristics.
        int lidx= orig.indexOf(name.toString());
        if (lidx == -1) {
            return -1;
        }
        return locs[0].getNodeOffset()+lidx;
    }

    private static int countOccurrencesOf(String sf, String text) {
        Pattern p= Pattern.compile("\\b" +sf+"\\b");  //$NON-NLS-1$//$NON-NLS-2$
        Matcher m= p.matcher(text);
        int count= 0;
        int start= 0;
        while(m.find(start)) {
            count++;
            start= m.end();
        }
        return count;
    }

    private static IScope getContainingScope(IASTName name) {
        IASTTranslationUnit tu= name.getTranslationUnit();
        if (tu == null) {
            return null;
        }
        if (tu instanceof ICPPASTTranslationUnit) {
            return CPPVisitor.getContainingScope(name);
        }
        return CVisitor.getContainingScope(name);
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

    public static boolean isLocalVariable(IVariable v, IScope scope) {
        if (v instanceof IParameter) {
            return false;
        }
        while (scope != null) {
            if (scope instanceof ICPPFunctionScope ||
                    scope instanceof ICPPBlockScope ||
                    scope instanceof ICFunctionScope) {
                return true;
            }
            try {
                scope= scope.getParent();
            } catch (DOMException e) {
                scope= null;
            }
        }
        return false;
    }

    public static boolean isLocalVariable(IVariable v) {
        try {
            return isLocalVariable(v, v.getScope());
        } catch (DOMException e) {
            return false;
        }
    }

    public static IBinding[] findInScope(final IScope scope, String name,
            boolean removeGlobalsWhenClassScope) throws DOMException {
        IBinding[] result= null;
        result = scope.find(name);
        if (result == null || result.length==0) {
            return result;
        }
        
        // eliminate global bindings when looking up in a class type
        if (removeGlobalsWhenClassScope &&
                (scope instanceof ICPPClassScope || 
                        scope instanceof ICCompositeTypeScope)) {
            int count= 0;
            for (int i = 0; i < result.length; i++) {
                IBinding binding = result[i];
                IScope bscope= binding.getScope();
                if (! (bscope instanceof ICPPClassScope || bscope instanceof ICCompositeTypeScope)) {
                    result[i]= null;
                }
                else {
                    count++;
                }
            }
            if (count < result.length) {
                IBinding[] copy= new IBinding[count];
                int i=0;
                for (int j = 0; j < result.length; j++) {
                    IBinding b = result[j];
                    if (b != null) {
                        copy[i++]= b;
                    }
                }
                result= copy;
            }
        }        
        
        // try to find constructors
        if (scope instanceof ICPPBlockScope) {
            for (int i = 0; i < result.length; i++) {
                IBinding binding = result[i];
                if (binding instanceof ICPPClassType) {
                    ICPPClassType classType= (ICPPClassType) binding;
                    if (classType.getKey() == ICPPClassType.k_class) {
                        IBinding[] cons= classType.getConstructors();
                        if (cons.length > 0 && ! (cons[0] instanceof IProblemBinding)) {
                            result[i]= cons[0];
                        }
                    }
                }
            }
        }

        return result;
    }
    


    public ASTManager(CRefactory refactoringManager, CRefactoringArgument arg) {
        fRefactory= refactoringManager;
        fArgument= arg;
    }

    void analyzeArgument(IProgressMonitor pm, RefactoringStatus status) {
        if (fArgument == null) {
            return;
        }
         
        if (fArgument.getArgumentKind() != CRefactory.ARGUMENT_UNKNOWN) {
            return;
        }
        
        pm.beginTask(Messages.getString("ASTManager.task.analyze"), 2); //$NON-NLS-1$
        IASTTranslationUnit tu= getTranslationUnit(fArgument.getSourceFile(), true, status);
        pm.worked(1);
        if (tu != null) {
            IASTNode node= findNameAtLocation(tu, tu.getFilePath(), fArgument.getOffset());
            if (node instanceof IASTName) {
                IASTName name= (IASTName) node;
                fArgument.setName(name);
                IBinding binding= name.resolveBinding();
                if (binding != null) {
                    IScope scope= null;
                    try {
                        scope = binding.getScope();
                    } catch (DOMException e) {
                        handleDOMException(tu, e, status);
                    }
                    fArgument.setBinding(name.getTranslationUnit(), binding, scope);
                }
            }
        }
        pm.worked(1);
        pm.done();
    }

    private IASTName findNameAtLocation(IASTTranslationUnit tu, String fileName,
            int offset) {
        final IASTName[] result= new IASTName[] {null};
        ASTNameVisitor nv = new ASTNameVisitor(fileName, offset) {
            protected int visitName(IASTName name) {
                result[0]= name;
                return ASTVisitor.PROCESS_ABORT;
            }
        };
        tu.accept(nv);
        if (result[0]==null) {
            IASTPreprocessorMacroDefinition[] m= tu.getMacroDefinitions();
            for (int i = 0; i < m.length && result[0]==null; i++) {
                IASTPreprocessorMacroDefinition mdef = m[i];
                IASTName name= mdef.getName();
                nv.visit(name);
                if (result[0] == null) {
                    IASTName[] refs= tu.getReferences(name.resolveBinding());
                    for (int j = 0; j < refs.length && result[0] == null; j++) {
                        nv.visit(refs[j]);
                    }
                }
            }
        }                

        return result[0];
    }

    private IASTTranslationUnit getTranslationUnit(IFile sourceFile, 
            boolean cacheit, RefactoringStatus status) {
        IASTTranslationUnit tu=  (IASTTranslationUnit) fTranslationUnits.get(sourceFile);
        if (tu == null) {
            tu= fRefactory.getTranslationUnit(sourceFile, status);
            if (tu != null && cacheit) {
                fTranslationUnits.put(sourceFile, tu);
            }
        }
        return tu;
    }

    public void analyzeTextMatches(ArrayList matches, IProgressMonitor monitor, 
            RefactoringStatus status) {
        CRefactoringMatchStore store= new CRefactoringMatchStore();
        for (Iterator iter = matches.iterator(); iter.hasNext();) {
            CRefactoringMatch match = (CRefactoringMatch) iter.next();
            store.addMatch(match);
        }
        
        monitor.beginTask(Messages.getString("ASTManager.task.generateAst"), //$NON-NLS-1$ 
                2*store.getFileCount()); 

        List files= store.getFileList();
        for (Iterator iter = files.iterator(); iter.hasNext();) {
            IFile file = (IFile) iter.next();
            if (store.contains(file)) {
                IASTTranslationUnit tu= getTranslationUnit(file, false, status);
                monitor.worked(1);
                analyzeTextMatchesOfTranslationUnit(tu, store, status);
                if (status.hasFatalError()) {
                    return;
                }
                monitor.worked(1);
            }
            else {
                monitor.worked(2);
            }
        }
        monitor.done();
    }

    private void analyzeTextMatchesOfTranslationUnit(IASTTranslationUnit tu, 
            final CRefactoringMatchStore store, final RefactoringStatus status) {
        fEqualToValidBinding= new HashSet();
        fConflictingBinding= new HashSet();
        final Set paths= new HashSet();
        
        analyzeMacroMatches(tu, store, paths, status);
        if (status.hasFatalError()) {
            return;
        }
        if (fArgument.getArgumentKind() == CRefactory.ARGUMENT_MACRO) {
            analyzeRenameToMatches(tu, store, paths, status);
        }
        else {
            analyzeLanguageMatches(tu, store, paths, status);
        }

        for (Iterator iter = paths.iterator(); iter.hasNext();) {
            IPath path = (IPath) iter.next();
            if (path != null) {
                store.removePath(path);
            }
        }
        handleConflictingBindings(tu, status);
        fEqualToValidBinding= null;
        fConflictingBinding= null;
    }

    private void analyzeLanguageMatches(IASTTranslationUnit tu, 
            final CRefactoringMatchStore store, final Set paths,
            final RefactoringStatus status) {
        ASTNameVisitor nv = new ASTSpecificNameVisitor(fArgument.getName()) {
            protected int visitName(IASTName name, boolean isDestructor) {
                IPath path= analyzeAstMatch(name, store, isDestructor, status);
                paths.add(path);
                return ASTVisitor.PROCESS_CONTINUE;
            }
        };
        tu.accept(nv);
    }

    private void analyzeMacroMatches(IASTTranslationUnit tu, 
            final CRefactoringMatchStore store, final Set pathsVisited,
            final RefactoringStatus status) {
        String lookfor= fArgument.getName();
        IASTPreprocessorMacroDefinition[] mdefs= tu.getMacroDefinitions();
        for (int i = 0; i < mdefs.length; i++) {
            IASTPreprocessorMacroDefinition mdef = mdefs[i];
            IASTName macroName= mdef.getName();
            String macroNameStr= macroName.toString();
            if (fRenameTo.equals(macroNameStr)) {
                status.addFatalError(MessageFormat.format(
                        Messages.getString("ASTManager.error.macro.name.conflict"), //$NON-NLS-1$
                        new Object[] {fRenameTo}));
                return;
            }
            else if (lookfor.equals(macroNameStr)) {
                IPath path= analyzeAstMatch(macroName, store, false, status);
                pathsVisited.add(path);
    
                IASTName[] refs= tu.getReferences(macroName.getBinding());
                for (int j = 0; j < refs.length; j++) {
                    path= analyzeAstMatch(refs[j], store, false, status);
                    pathsVisited.add(path);
                }
            }
        }
    }

    private void analyzeRenameToMatches(IASTTranslationUnit tu, 
            CRefactoringMatchStore store, final Set paths, 
            final RefactoringStatus status) {
        ASTNameVisitor nv = new ASTSpecificNameVisitor(fRenameTo) {
            protected int visitName(IASTName name, boolean isDestructor) {
                IPath path= analyzeRenameToMatch(status, name);
                paths.add(path);
                return ASTVisitor.PROCESS_CONTINUE;
            }
        };
        tu.accept(nv);
    }

    protected IPath analyzeRenameToMatch(final RefactoringStatus status, IASTName name) {
        IASTNodeLocation[] locations= name.getNodeLocations();
        IPath path= null;
        if (locations != null && locations.length==1) {
            IASTNodeLocation loc= locations[0];
            IASTFileLocation floc= loc.asFileLocation();
            if (floc != null) {
                path= new Path(floc.getFileName());
                if (path != null) {
                    IBinding binding= name.resolveBinding();
                    if (binding instanceof IProblemBinding) {
                        handleProblemBinding(name.getTranslationUnit(), 
                                (IProblemBinding) binding, status);
                    }
                    else if (binding != null) {
                        fConflictingBinding.add(binding);
                    }
                }
            }
        }
        return path;
    }

    protected IPath analyzeAstMatch(IASTName name, CRefactoringMatchStore store, 
            boolean isDestructor, RefactoringStatus status) {
        IPath path= null;
        CRefactoringMatch match= null;
        
        IASTNodeLocation[] locations= name.getNodeLocations();
        if (locations != null && locations.length==1) {
            IASTNodeLocation loc= locations[0];
            IASTFileLocation floc= loc.asFileLocation();
            if (floc != null) {
                path= new Path(floc.getFileName());
                if (path != null) {
                    match= store.findMatch(path, floc.getNodeOffset() + (isDestructor ? 1 : 0));
                    // bug 90978 IASTMacroExpansions should be handled right away,
                    // as a workaround look with fileOffset first.
                    if (match==null && loc instanceof IASTMacroExpansion) {
                        IASTMacroExpansion me= (IASTMacroExpansion) loc;
                        int offset= backrelateNameToMacroCallArgument(name, me);
                        match= store.findMatch(path, offset + (isDestructor ? 1 : 0));
                    }
                }
            }
        }
        if (match != null) {
            analyzeAstTextMatchPair(match, name, status);
        }
        return path;
    }

    private void analyzeAstTextMatchPair(CRefactoringMatch match, IASTName name, 
            RefactoringStatus status) {
        
        IBinding binding= name.resolveBinding();
        int cmp= FALSE;
        if (fEqualToValidBinding.contains(binding)) {
            cmp= TRUE;
        }
        else if (binding instanceof IProblemBinding) {
            cmp= UNKNOWN;
            handleProblemBinding(name.getTranslationUnit(), (IProblemBinding) binding, status);
        }
        else {
            for (int i = 0; i < fValidBindings.length; i++) {
                IBinding renameBinding = fValidBindings[i];
                try {
                    int cmp0= isSameBinding(binding, renameBinding);
                    if (cmp0 != FALSE) {
                        cmp= cmp0;
                    }
                    if (cmp0 == TRUE) {
                        fEqualToValidBinding.add(renameBinding);
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
            if (fRenameTo != null) {
                IScope scope= getContainingScope(name);
                if (scope != null) {
                    IBinding[] conflicting= null;
                    try {
                        conflicting= findInScope(scope, fRenameTo, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (conflicting != null && conflicting.length > 0) {
                        fConflictingBinding.addAll(Arrays.asList(conflicting));
                    }
                }
            }
            break;
        case FALSE:
            match.setASTInformation(CRefactoringMatch.AST_REFERENCE_OTHER);
            break;
        }
    }

    public void handleDOMException(IASTTranslationUnit tu, final DOMException e, RefactoringStatus status) {
        handleProblemBinding(tu, e.getProblem(), status);
    }

    public void handleProblemBinding(IASTTranslationUnit tu, final IProblemBinding pb, RefactoringStatus status) {
        if (tu != null) {
            if (fProblemUnits.add(tu.getFilePath())) {
                status.addWarning(MessageFormat.format(
                        Messages.getString("ASTManager.warning.parsingErrors"), //$NON-NLS-1$
                        new Object[] {tu.getFilePath()}));
            }
        }
    }

    protected void handleConflictingBindings(IASTTranslationUnit tu, 
            RefactoringStatus status) {   
        if (fConflictingBinding.isEmpty()) {
            return;
        }
        
        int argKind= fArgument.getArgumentKind();
        boolean isVarParEnumerator= false;
        boolean isLocalVarPar= false;
        boolean isFunction= false;
        boolean isContainer = false;
        boolean isMacro= false;

        switch(argKind) {
        case CRefactory.ARGUMENT_LOCAL_VAR:  
        case CRefactory.ARGUMENT_PARAMETER:
            isLocalVarPar= true;
        case CRefactory.ARGUMENT_FILE_LOCAL_VAR:    
        case CRefactory.ARGUMENT_GLOBAL_VAR:
        case CRefactory.ARGUMENT_FIELD:     
        case CRefactory.ARGUMENT_ENUMERATOR:         
            isVarParEnumerator= true;
            break;
        case CRefactory.ARGUMENT_FILE_LOCAL_FUNCTION:
        case CRefactory.ARGUMENT_GLOBAL_FUNCTION:
        case CRefactory.ARGUMENT_VIRTUAL_METHOD:     
        case CRefactory.ARGUMENT_NON_VIRTUAL_METHOD:
            isFunction= true;
            break;
        case CRefactory.ARGUMENT_TYPE:
        case CRefactory.ARGUMENT_CLASS_TYPE:
        case CRefactory.ARGUMENT_NAMESPACE:
            isContainer = true;
            break;
        case CRefactory.ARGUMENT_MACRO:      
            isMacro= true;
            break;
        case CRefactory.ARGUMENT_INCLUDE_DIRECTIVE:  
            break;
        }
        
        Collection[] cflc= new Collection[] {new HashSet(), new ArrayList(), new ArrayList()};
        String[] errs= null;
        if (isMacro) {
            errs= new String[]{
                    Messages.getString("CRenameLocalProcessor.error.conflict") //$NON-NLS-1$
            };
            cflc[0]= fConflictingBinding;
        }
        else {
            errs= new String[]{
                    Messages.getString("CRenameLocalProcessor.error.shadow"),  //$NON-NLS-1$
                    Messages.getString("CRenameLocalProcessor.error.redeclare"),  //$NON-NLS-1$
                    Messages.getString("CRenameLocalProcessor.error.isShadowed"),  //$NON-NLS-1$
                    Messages.getString("CRenameLocalProcessor.error.overloads")};  //$NON-NLS-1$
            classifyConflictingBindings(tu, (Set) cflc[0], cflc[1], cflc[2], status);
        }
        
        for (int i = 0; i < 3; i++) {
            Collection coll= cflc[i];
            for (Iterator iter = coll.iterator(); iter.hasNext();) {
                boolean warn= false;
                String msg= errs[i];
                IBinding conflict = (IBinding) iter.next();
                String what= null;
                if (conflict instanceof IEnumerator) {
                    if (isVarParEnumerator || isFunction || isMacro) {
                        what= Messages.getString("CRenameLocalProcessor.enumerator"); //$NON-NLS-1$
                    }
                }
                else if (conflict instanceof ICPPField) {
                    if (isVarParEnumerator || isFunction || isMacro) {
                        what= Messages.getString("CRenameLocalProcessor.field"); //$NON-NLS-1$
                    }
                }
                else if (conflict instanceof IParameter) {
                    if (isVarParEnumerator || isFunction || isMacro) {
                        if (i==1 && argKind ==CRefactory.ARGUMENT_LOCAL_VAR) {
                            msg= errs[0];
                        }
                        what= Messages.getString("CRenameLocalProcessor.parameter"); //$NON-NLS-1$
                    }
                }                    
                else if (conflict instanceof IVariable) {
                    if (isVarParEnumerator || isFunction || isMacro) {
                        IVariable conflictingVar= (IVariable) conflict;
                        what= Messages.getString("CRenameLocalProcessor.globalVariable"); //$NON-NLS-1$
                        if (ASTManager.isLocalVariable(conflictingVar)) {
                            if (i==1 && argKind==CRefactory.ARGUMENT_PARAMETER) {
                                msg= errs[2];
                            }
                            what= Messages.getString("CRenameLocalProcessor.localVariable"); //$NON-NLS-1$
                        }
                        else {
                            try {
                                if (conflictingVar.isStatic()) {
                                    what= Messages.getString("CRenameTextProcessor.fileStaticVariable"); //$NON-NLS-1$
                                }
                            } catch (DOMException e) {
                            }
                        }
                    }
                }
                else if (conflict instanceof ICPPConstructor) {
                    if (isVarParEnumerator || isFunction || isMacro) {
                        what= Messages.getString("CRenameLocalProcessor.constructor"); //$NON-NLS-1$
                    }
                }
                else if (conflict instanceof ICPPMethod) {
                    if (isVarParEnumerator || isFunction || isMacro) {
                        if (i==1) {
                            IBinding r= fArgument.getBinding();
                            if (r instanceof ICPPMethod) {
                                try {
                                    if (ASTManager.hasSameSignature((ICPPMethod) r, 
                                            (ICPPMethod) conflict) == ASTManager.FALSE) {
                                        msg= errs[3];
                                        warn= true;
                                    }
                                } catch (DOMException e) {
                                }
                            }
                        }
                        what= Messages.getString("CRenameLocalProcessor.method"); //$NON-NLS-1$
                    }
                }
                else if (conflict instanceof IFunction) {
                    if (isVarParEnumerator || isFunction || isMacro) {
                        boolean ignore= false;
                        if (isLocalVarPar) {
                            IASTName[] refs= 
                                fArgument.getTranslationUnit().getReferences(conflict);
                            if (refs==null || refs.length==0) {
                                ignore= true;
                            }
                        }
                        if (!ignore) {
                            IFunction conflictingFunction= (IFunction) conflict;
                            if (i==1 && conflict instanceof ICPPFunction) {
                                IBinding r= fArgument.getBinding();
                                if (r instanceof ICPPFunction) {
                                    try {
                                        if (ASTManager.hasSameSignature((ICPPFunction) r, 
                                                conflictingFunction) == ASTManager.FALSE) {
                                            msg= errs[3];
                                            warn= true;
                                        }
                                    } catch (DOMException e) {
                                    }
                                }
                            }

                            boolean isStatic= false;
                            try {
                                isStatic= conflictingFunction.isStatic();
                            } catch (DOMException e) {
                            }
                            if (isStatic) {
                                what= Messages.getString("CRenameTextProcessor.fileStaticFunction"); //$NON-NLS-1$
                            }
                            else {
                                what= Messages.getString("CRenameTextProcessor.globalFunction"); //$NON-NLS-1$
                            }
                        }
                    }
                }
                else if (conflict instanceof ICompositeType ||
                        conflict instanceof IEnumeration ||
                        conflict instanceof ITypedef) {
                    if (isContainer || isMacro) {
                        what= Messages.getString("CRenameTextProcessor.type"); //$NON-NLS-1$
                    }
                }
                else if (conflict instanceof ICPPNamespace) {
                    if (isContainer || isMacro) {
                        what= Messages.getString("CRenameTextProcessor.namespace"); //$NON-NLS-1$
                        if (argKind==CRefactory.ARGUMENT_NAMESPACE) {
                            warn= true;
                        }
                    }
                }
                if (what != null) {
                    String formatted= MessageFormat.format(
                            Messages.getString("CRenameLocalProcessor.error.nameErrorWhat"), //$NON-NLS-1$
                            new Object[]{conflict.getName(), msg, what});
                    RefactoringStatusEntry[] entries= status.getEntries();
                    for (int j = 0; formatted != null && j<entries.length; j++) {
                        RefactoringStatusEntry entry = entries[j];
                        if (formatted.equals(entry.getMessage())) {
                            formatted= null;
                        }
                    }
                    if (formatted != null) {
                        if (warn) {
                            status.addWarning(formatted);
                        }
                        else {
                            status.addError(formatted);
                        }
                    }
                }
            }
        }
    }

    protected void classifyConflictingBindings(IASTTranslationUnit tu, 
            Set shadows, Collection redecl, Collection barriers, 
            RefactoringStatus status) {
        // collect bindings on higher or equal level
        String name= fArgument.getName();
        IBinding[] newBindingsAboverOrEqual= null;
        IScope oldBindingsScope= null;
        for (Iterator iter = fEqualToValidBinding.iterator(); 
                (newBindingsAboverOrEqual==null || newBindingsAboverOrEqual.length==0) && iter.hasNext();) {
            IBinding oldBinding = (IBinding) iter.next();
            if (oldBinding.getName().equals(name)) {
                try {
                    oldBindingsScope = oldBinding.getScope();
                    if (oldBindingsScope != null) {
                        newBindingsAboverOrEqual = ASTManager.findInScope(oldBindingsScope, fRenameTo, false);
                    }
                } catch (DOMException e) {
                    handleDOMException(tu, e, status);
                }
            }            
        }
        if (newBindingsAboverOrEqual == null) {
            newBindingsAboverOrEqual= new IBinding[0];
        }
        
        // check conflicting bindings for being from above or equal level.
        for (Iterator iter = fConflictingBinding.iterator(); iter.hasNext();) {
            IBinding conflictingBinding= (IBinding) iter.next();
            if (conflictingBinding != null) {
                boolean isAboveOrEqual= false;
                for (int i = 0; !isAboveOrEqual && i<newBindingsAboverOrEqual.length; i++) {
                    IBinding aboveBinding = newBindingsAboverOrEqual[i];
                    try {
                        if (isSameBinding(aboveBinding, conflictingBinding) == TRUE) {
                            isAboveOrEqual= true;
                        }
                    } catch (DOMException e) {
                        handleDOMException(tu, e, status);
                    }
                }
                if (!isAboveOrEqual) {
                    barriers.add(conflictingBinding);
                }
            }
        }

        // find bindings on same level
        for (int i = 0; i<newBindingsAboverOrEqual.length; i++) {
            IBinding aboveBinding = newBindingsAboverOrEqual[i];
            IScope aboveScope;
            try {
                aboveScope = aboveBinding.getScope();
                if (isSameScope(aboveScope, oldBindingsScope, false)==TRUE) {
                    redecl.add(aboveBinding);
                }
                else {
                    shadows.add(aboveBinding);
                }
            } catch (DOMException e) {
                handleDOMException(tu, e, status);
            }
        }
    }

    public void setValidBindings(IBinding[] validBindings) {
        fValidBindings= validBindings;
    }

    public void setRenameTo(String renameTo) {
        fRenameTo= renameTo;
    }

}
