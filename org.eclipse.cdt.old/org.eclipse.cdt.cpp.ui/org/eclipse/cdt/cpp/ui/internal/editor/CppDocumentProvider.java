package com.ibm.cpp.ui.internal.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.Iterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.util.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;



public class CppDocumentProvider extends FileDocumentProvider {
	
		protected class CppMarkerAnnotationModel extends ResourceMarkerAnnotationModel {
			
			public CppMarkerAnnotationModel(IResource resource) {
				super(resource);
			}
			
			protected MarkerAnnotation createMarkerAnnotation(IMarker marker) {
				return new CppMarkerAnnotation(marker);
			}
		};
		
	/**
	 * Constructor
	 */
	public CppDocumentProvider() {
   
	}
	/**
	 * @see IWorkingCopyManager#connect(IEditorInput)
	 */
	public void connect(IEditorInput input) throws CoreException {
		super.connect(input);
   
	}

/*	protected ICompilationUnit createCompilationUnit(IFile file) {
		Object element= JavaCore.create(file);
		if (element instanceof ICompilationUnit)
			return (ICompilationUnit) element;
		return null;
	}
*/
	/**
	 * Replaces createAnnotationModel of the super class
	 */
//	protected IAnnotationModel createCppAnnotationModel(Object element) throws CoreException {
	protected IAnnotationModel createCppAnnotationModel(Object element) {
//		if ( !(element instanceof IFileEditorInput))
//			throw new CoreException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_RESOURCE_TYPE));
		
		IFileEditorInput input= (IFileEditorInput) element;
   
		return new CppMarkerAnnotationModel(input.getFile());
	}
	/**
	 * @see AbstractDocumentProvider#createElementInfo(Object)
	 */
   protected ElementInfo createElementInfo(Object element) throws CoreException
   {
//		if ( !(element instanceof IFileEditorInput))
//			throw new CoreException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_RESOURCE_TYPE));
   
			
		ElementInfo info = null;
		IFileEditorInput input = (IFileEditorInput) element;
		IFile original = input.getFile();
		if (original != null)
      {
				
         
	      try
         {
				try
            {
					input.getFile().refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException ce) {
               System.out.println("CppDocumentProvider:createElementInfo Core exception - " +ce);
				}
				
				IDocument document = createDocument(element);

				IAnnotationModel model = createCppAnnotationModel(element);

            FileInfo elementInfo= new FileInfo(document, model, null);
				elementInfo.fModificationStamp = computeModificationStamp(original);
            return elementInfo;
				
			} catch (CoreException ce) {
            System.out.println("CppDocumentProvider:createElementInfo Core exception - " +ce);
			}
		} else {		
        
			info = super.createElementInfo(element);
		}
      return info;
	}
	/**
	 * @see IWorkingCopyManager#disconnect(IEditorInput)
	 */
	public void disconnect(IEditorInput input) {
		super.disconnect(input);
	}
	/**
	 * @see AbstractDocumentProvider#disposeElementInfo(Object, ElementInfo)
	 */
/*		
	protected void disposeElementInfo(Object element, ElementInfo info) {
		if (info instanceof CompilationUnitInfo) {
			CompilationUnitInfo cuInfo= (CompilationUnitInfo) info;
			
			if (cuInfo.fBufferSynchronizer != null)
				cuInfo.fBufferSynchronizer.uninstall();
			
			cuInfo.fCopy.destroy();
		}
		
		super.disposeElementInfo(element, info);
	}
*/
	/**
	 * @see AbstractDocumentProvider#doSaveDocument(IProgressMonitor, Object, IDocument, boolean)
	 */
/*
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {
				
		ElementInfo elementInfo= getElementInfo(element);		
		if (elementInfo instanceof CompilationUnitInfo) {
			CompilationUnitInfo info= (CompilationUnitInfo) elementInfo;
			
			try {					
				// update structure, assumes lock on info.fCopy
				info.fCopy.reconcile();
				
				ICompilationUnit original= (ICompilationUnit) info.fCopy.getOriginalElement();
				IResource resource= original.getUnderlyingResource();
				
				if (resource != null && !overwrite)
					checkSynchronizationState(info.fModificationStamp, resource);
				
				// commit working copy
				info.fCopy.commit(overwrite, monitor);
				
				AbstractMarkerAnnotationModel model= (AbstractMarkerAnnotationModel) info.fModel;
				model.updateMarkers(info.fDocument);
				
				if (resource != null)
					info.setModificationStamp(computeModificationStamp(resource));
										
			} catch (JavaModelException x) {
				throw new CoreException(x.getStatus());
			}
			
		} else {
			super.doSaveDocument(monitor, element, document, overwrite);
		}		
	}
*/
	/**
	 * @see IWorkingCopyManager#getWorkingCopy(Object)
	 */
/*
	public ICompilationUnit getWorkingCopy(IEditorInput element) {
		
		ElementInfo elementInfo= getElementInfo(element);		
		if (elementInfo instanceof CompilationUnitInfo) {
			CompilationUnitInfo info= (CompilationUnitInfo) elementInfo;
			return info.fCopy;
		}
		
		return null;
	}
*/
	/**
	 * @see AbstractDocumentProvider#resetDocument(Object)
	 */
/*
	public void resetDocument(Object element) throws CoreException {
		if (element == null)
			return;
			
		ElementInfo elementInfo= getElementInfo(element);		
		if (elementInfo instanceof CompilationUnitInfo) {
			CompilationUnitInfo info= (CompilationUnitInfo) elementInfo;
			if (info.fCanBeSaved) {
				try {
					
					ICompilationUnit original= (ICompilationUnit) info.fCopy.getOriginalElement();
					
					fireElementContentAboutToBeReplaced(element);
					
					removeUnchangedElementListeners(element, info);
					info.fDocument.set(original.getSource());
					info.fCanBeSaved= false;
					addUnchangedElementListeners(element, info);
					
					fireElementContentReplaced(element);
					
				} catch (JavaModelException x) {
					throw new CoreException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_RESOURCE, x));
				}
			}
		} else {
			super.resetDocument(element);
		}
	}
*/
	/**
	 * @see IWorkingCopyManager#shutdown
	 */
	public void shutdown() {
		Iterator e= getConnectedElements();
		while (e.hasNext())
			disconnect(e.next());
	}
}
