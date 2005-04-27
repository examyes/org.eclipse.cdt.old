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

/**
 * Collects some basic functionality.
 */
public class CRefactoringUtils {

    public static boolean isIdentifierChar(char c) {
        return isLeadingIdentifierChar(c) || ('0'<=c && c<='9');
    }

    public static boolean isLeadingIdentifierChar(char c) {
        return ('A'<=c && c<='Z') || ('a'<=c && c<='z') || c=='_';
    }

    public static boolean checkIdentifier(String id) {
        if (id.length() == 0) {
            return false;
        }
        if (!isLeadingIdentifierChar(id.charAt(0))) {
            return false;
        }
        for (int i= 1; i < id.length(); i++) {
            if (!isIdentifierChar(id.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}