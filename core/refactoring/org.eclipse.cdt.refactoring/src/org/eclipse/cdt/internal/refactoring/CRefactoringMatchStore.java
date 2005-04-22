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

import java.util.*;

import org.eclipse.cdt.refactoring.CRefactoringMatch;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

public class CRefactoringMatchStore {
    private Map fFileToPathMap= new HashMap();
    private Map fPathToMatches= new HashMap();
    private Comparator fOffsetComparator;

    public CRefactoringMatchStore() {
        fOffsetComparator= new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((CRefactoringMatch) o1).getOffset() - 
                    ((CRefactoringMatch) o2).getOffset();
            }
        };
    }
    
    public void addMatch(CRefactoringMatch match) {
        IPath path= resolvePath(match.getFile());
        if (path != null) {
            Map matchesForPath= getMatchesForPath(path, true);
            matchesForPath.put(match, match);
        }
    }

    private Map getMatchesForPath(IPath path, boolean create) {
        Map map= (Map) fPathToMatches.get(path);
        if (map == null && create) {
            map= new TreeMap(fOffsetComparator);
            fPathToMatches.put(path, map);
        }
        return map;
    }

    private IPath resolvePath(IFile file) {
        IPath path= (IPath) fFileToPathMap.get(file);
        if (path == null) {
            path= file.getLocation();
            if (path == null) {
                path= file.getFullPath();
            }
            fFileToPathMap.put(file, path);
        }
        return path;
    }

    public int getFileCount() {
        return fFileToPathMap.size();
    }

    public List getFileList() {
        return new ArrayList(fFileToPathMap.keySet());
    }

    public boolean contains(IFile file) {
        return fFileToPathMap.containsKey(file);
    }

    public CRefactoringMatch findMatch(IPath path, int nodeOffset) {
        Map map= (Map) fPathToMatches.get(path);
        if (map != null) {
            return (CRefactoringMatch) map.get(new CRefactoringMatch(null, nodeOffset, 0, 0));
        }
        return null;
    }

    public void removePath(IPath path) {
        Map map= (Map) fPathToMatches.remove(path);
        if (map != null && !map.isEmpty()) {
            IFile file= ((CRefactoringMatch) map.values().iterator().next()).getFile();
            fFileToPathMap.remove(file);
        }
    }
}
