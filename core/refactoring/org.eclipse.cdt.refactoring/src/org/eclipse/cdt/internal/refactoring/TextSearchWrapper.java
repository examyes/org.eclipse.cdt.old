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

import java.io.*;
import java.util.*;

import org.eclipse.cdt.internal.refactoring.scanner.Scanner;
import org.eclipse.cdt.internal.refactoring.scanner.Token;
import org.eclipse.cdt.refactoring.*;
import org.eclipse.cdt.refactoring.CRefactory;
import org.eclipse.cdt.refactoring.ICRefactoringSearch;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.search.internal.core.ISearchScope;
import org.eclipse.search.internal.core.text.*;
import org.eclipse.ui.*;

/**
 * Wraps the platform text search and uses a scanner to categorize the text-matches
 * by location (comments, string-literals, etc.).
 */
public class TextSearchWrapper implements ICRefactoringSearch {
    
    public TextSearchWrapper() {}
    
    private ISearchScope createSearchScope(IFile file, int scope, 
            String workingSetName, String[] patterns) {
        switch (scope) {
        	case SCOPE_WORKSPACE:
        	    return defineSearchScope(file.getWorkspace().getRoot(), patterns);
        	case SCOPE_SINGLE_PROJECT:
        	    return defineSearchScope(file.getProject(), patterns);
        	case SCOPE_FILE:
        	    return defineSearchScope(file, patterns);
        	case SCOPE_WORKING_SET: {
        	    ISearchScope result= defineWorkingSetAsSearchScope(workingSetName, patterns);
        	    if (result == null) {
        	        result= defineSearchScope(file.getWorkspace().getRoot(), patterns);
        	    }
        		return result;
        	}
        }
	    return defineRelatedProjectsAsSearchScope(file.getProject(), patterns);
    }
    
    private ISearchScope defineRelatedProjectsAsSearchScope(IProject project, String[] patterns) {
        HashSet projects= new HashSet();
        LinkedList workThrough= new LinkedList();
        workThrough.add(project);
        while (!workThrough.isEmpty()) {
            IProject prj= (IProject) workThrough.removeLast();
            if (projects.add(prj)) {
                try {
                    workThrough.addAll(Arrays.asList(prj.getReferencedProjects()));
                    workThrough.addAll(Arrays.asList(prj.getReferencingProjects()));
                } catch (CoreException e) {
                    // need to ignore
                }
            }
        }
        IResource[] resources= (IResource[]) projects.toArray(new IResource[projects.size()]);
        return defineSearchScope(resources, patterns);
    }

    private ISearchScope defineWorkingSetAsSearchScope(String wsName, String[] patterns) {
        if (wsName == null) {
            return null;
        }
		IWorkingSetManager wsManager= PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet ws= wsManager.getWorkingSet(wsName);
		if (ws == null) {
		    return null;
		}
		TextSearchScope result= 	
		    new TextSearchScope("c/cpp", new IWorkingSet[] {ws}); //$NON-NLS-1$
		applyFilePatterns(result, patterns);
		return result;
    }

    private void applyFilePatterns(TextSearchScope scope, String[] patterns) {
        for (int i = 0; i < patterns.length; i++) {
            String pattern = patterns[i];
            scope.addExtension(pattern);
        }
    }

    private ISearchScope defineSearchScope(IResource resource, String[] patterns) {
        TextSearchScope result= new TextSearchScope("c/cpp"); //$NON-NLS-1$
        result.add(resource);
        applyFilePatterns(result, patterns);
        return result;
    }
    
    private ISearchScope defineSearchScope(IResource[] resources, String[] patterns) {
        TextSearchScope result= new TextSearchScope("c/cpp", resources); //$NON-NLS-1$            
        applyFilePatterns(result, patterns);
        return result;
    }
    
    /**
     * @param monitor
     */
    public IStatus searchWord(int scope, IFile resource, String workingSet, String[] patterns,
            String word, IProgressMonitor monitor, final List target) {
        int startPos= target.size();
        TextSearchEngine engine= new TextSearchEngine();
        StringBuffer searchPattern= new StringBuffer(word.length()+ 8);
        searchPattern.append("\\b"); //$NON-NLS-1$
        searchPattern.append("\\Q"); //$NON-NLS-1$
        searchPattern.append(word);
        searchPattern.append("\\E"); //$NON-NLS-1$
        searchPattern.append("\\b"); //$NON-NLS-1$

        ISearchScope searchscope= createSearchScope(resource, scope, workingSet, patterns);
        MatchLocator locator= new MatchLocator(searchPattern.toString(), true, true);
        final IProgressMonitor subProgress= new SubProgressMonitor(monitor, 95);
        ITextSearchResultCollector collector= new ITextSearchResultCollector() {
            public IProgressMonitor getProgressMonitor() {
                return subProgress;
            }
            public void aboutToStart(){
            }
            public void accept(IResourceProxy proxy, String line, int start, 
                    int length, int lineNumber) {
                accept(proxy, start, length);
            }
            public void done() {
            }
            public void accept(IResourceProxy proxy, int start, int length) {
                IResource res= proxy.requestResource();
                if (res instanceof IFile) {
                    target.add(new CRefactoringMatch((IFile) res, start, length, 0));
                }                
            }
        };
        IStatus result= engine.search(searchscope, false, collector, locator, false);
        categorizeMatches(target.subList(startPos, target.size()), 
                new SubProgressMonitor(monitor, 5));

        return result;
    }
    
    public void categorizeMatches(List matches, IProgressMonitor monitor) {
        monitor.beginTask(Messages.getString("TextSearch.monitor.categorizeMatches"), matches.size()); //$NON-NLS-1$
        IFile file= null;
        ArrayList locations= null;
        for (Iterator iter = matches.iterator(); iter.hasNext();) {
            CRefactoringMatch match = (CRefactoringMatch) iter.next();
            IFile tfile= match.getFile();
            if (file == null || !file.equals(tfile)) {
                file= tfile;
                locations= new ArrayList(); 
                computeLocations(file, locations);                
            }
            match.setLocation(findLocation(match, locations));            
            monitor.worked(1);
        }
    }

    final static Comparator COMPARE_FIRST_INTEGER= new Comparator() {
        public int compare(Object o1, Object o2) {
            return ((int[])o1)[0]-((int[])o2)[0];
        }
    };
    private int findLocation(CRefactoringMatch match, ArrayList states) {
        int pos= Collections.binarySearch(states, new int[] {match.getOffset()}, 
                COMPARE_FIRST_INTEGER);
        if (pos<0) {
            pos= -pos-2;
            if (pos < 0) {
                pos=0;
            }
        }
        int endOffset= match.getOffset() + match.getLength();
        int location= 0;
        while (pos<states.size()) {
            int[] info= (int[]) states.get(pos);
            if (info[0] >= endOffset) {
                break;
            }
            location |= info[1];
            pos++;
        }
        return location;
    }

    private void computeLocations(IFile file, ArrayList locations) {
        Reader reader;
        Scanner scanner= new Scanner();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(file.getContents(), file.getCharset()));
        } catch (CoreException e) {
            return;
        } catch (UnsupportedEncodingException e) {
            return;
        }
        try {
            scanner.initialize(reader);
            scanner.setReuseToken(true);
            Token token;
            int lastState= 0;
            while((token= scanner.nextToken()) != null) {
                int state= CRefactory.OPTION_IN_CODE;
                switch(token.getType()) {
                	case Token.tLINECOMMENT:
                    case Token.tBLOCKCOMMENT:
                        state= CRefactory.OPTION_IN_COMMENT;
                		break;
                    case Token.tSTRING:
                    case Token.tLSTRING:
                    case Token.tCHAR:
                        state= CRefactory.OPTION_IN_STRING_LITERAL;
                    	break;
                    case Token.tPREPROCESSOR:
                        state= categorizePreprocessor(token.getText());
                    	break;
                }
                if (state != lastState) {
                    locations.add(new int[] {token.getOffset(), state});
                    lastState= state;
                }
            }
        }
        finally {
            try {
                reader.close();
            } catch (IOException e1) {
            }
        }
    }

    private int categorizePreprocessor(String text) {
        boolean skipHash= true;
        int i=0;
        for (; i < text.length(); i++) {
            char c= text.charAt(i);
            if (!Character.isWhitespace(c)) {
                if (!skipHash) {
                    break;
                }
                skipHash= false;
                if (c != '#') {
                    break;
                }
            }
        }
        String innerText= text.substring(i);
        if (innerText.startsWith("include")) { //$NON-NLS-1$
            return CRefactory.OPTION_IN_INCLUDE_DIRECTIVE;
        }
        if (innerText.startsWith("define") || innerText.startsWith("undef")) { //$NON-NLS-1$ //$NON-NLS-2$
            return CRefactory.OPTION_IN_MACRO_DEFINITION;
        }
        return CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE;
    }
}
