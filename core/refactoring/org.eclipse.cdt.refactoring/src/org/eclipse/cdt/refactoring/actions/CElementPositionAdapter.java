/*
 * CElementPositionAdapter.java
 * Created on 09.06.2005
 *
 * Copyright 2005 Wind River Systems Inc. All rights reserved.
 */
package org.eclipse.cdt.refactoring.actions;

import org.eclipse.cdt.core.model.*;
import org.eclipse.cdt.refactoring.IPositionProvider;
import org.eclipse.cdt.refactoring.IPositionConsumer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

public class CElementPositionAdapter implements IPositionProvider {
    public boolean providePosition(Object o, IPositionConsumer consumer) {
        if (o instanceof ITranslationUnit) {
            return false;
        }
        if (o instanceof ISourceReference) {
            ISourceReference sref= (ISourceReference) o;
            ISourceRange range= null;
            IFile file= null;
            String text= null;

            try {
                range= sref.getSourceRange();
            } catch (CModelException e) {
            }
            if (o instanceof ICElement) {
                ICElement e= (ICElement) o;
                IResource r= e.getUnderlyingResource();
                text= e.getElementName();
                if (r instanceof IFile) {
                    file= (IFile) r;
                }
                if (range != null && file != null && text != null) {
                    int offset= range.getIdStartPos();
                    int useLength= 0;
                    int idx= text.length()-1;
                    while (idx >= 0) {
                        char c= text.charAt(idx);
                        if (!Character.isLetterOrDigit(c) &&  c!='_') {
                            text= text.substring(idx+1);
                            break;
                        }
                        useLength++;
                        idx--;
                    }
                    offset= range.getIdStartPos()+range.getIdLength()-useLength;
                    consumer.setPosition(file, offset, text);
                    return true;
                }
            }
        }
        return false;
    }
}
