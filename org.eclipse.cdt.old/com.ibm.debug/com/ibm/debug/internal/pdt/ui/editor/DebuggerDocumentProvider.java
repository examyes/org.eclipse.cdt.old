package com.ibm.debug.internal.pdt.ui.editor;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/editor/DebuggerDocumentProvider.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:00:25)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.internal.picl.EngineSuppliedViewEditorInput;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class DebuggerDocumentProvider extends FileDocumentProvider {

	/**
	 * Constructor for DebuggerDocumentProvider
	 */
	public DebuggerDocumentProvider() {
		super();
	}

	/*
	 * @see AbstractDocumentProvider#createAnnotationModel(Object)
	 */
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input= (IFileEditorInput) element;
			return new DebuggerMarkerAnnotationModel(input.getFile());
		} else if (element instanceof EngineSuppliedViewEditorInput) {
			EngineSuppliedViewEditorInput input = (EngineSuppliedViewEditorInput) element;
			return new DebuggerMarkerAnnotationModel(input.getProject(), input.getName());
		}

		return super.createAnnotationModel(element);
	}

	/*
	 * @see AbstractDocumentProvider#createElementInfo(Object)
	 */
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		if (element instanceof EngineSuppliedViewEditorInput) {

			EngineSuppliedViewEditorInput input= (EngineSuppliedViewEditorInput) element;

			IDocument d= createDocument(element);
			IAnnotationModel m= createAnnotationModel(element);
			FileSynchronizer f= null;
		//	f.install();

			FileInfo info= new FileInfo(d, m, f);
			info.fModificationStamp= 0; //computeModificationStamp(input.getFile());

			return info;
		}

		return super.createElementInfo(element);
	}

}

