package org.eclipse.cdt.cpp.ui.internal.editor.codeassist;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.cdt.cpp.ui.internal.editor.*;

import org.eclipse.core.resources.*;
import org.eclipse.swt.SWT;
import com.ibm.lpex.alef.*;
import com.ibm.lpex.alef.contentassist.*;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.*;

import com.ibm.lpex.core.LpexCommonParser;

public class CppSourceViewerConfiguration extends LpexSourceViewerConfiguration
{
  private CppEditor _input;

  public CppSourceViewerConfiguration(CppEditor input)
      {
        super();
        _input = input;
      }

  public IContentAssistant getLpexContentAssistant(ISourceViewer viewer)
      {		
        IEditorInput editorInput = _input.getEditorInput();
        if (editorInput instanceof IFileEditorInput)
        {
           IFile file = ((IFileEditorInput) editorInput).getFile();
           if (file != null)
           {
             ContentAssistant assistant = new ContentAssistant();
             assistant.setContentAssistProcessor(new CppCompletionProcessor(file), IDocument.DEFAULT_CONTENT_TYPE);
             assistant.setProposalPopupOrientation(assistant.PROPOSAL_OVERLAY);
             return assistant;
           }
        }
        return null;
      }
}
