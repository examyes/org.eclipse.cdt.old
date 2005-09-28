/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.indexer;

import java.io.IOException;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.core.index.IIndexStorage;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.impl.IndexDelta;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;

/**
 * @author Doug Schaefer
 *
 */
public class SQLPDOMIndexer extends AbstractCExtension implements ICDTIndexer {

	public void addRequest(IProject project, IResourceDelta delta, int kind) {
		switch (kind) {
		case ICDTIndexer.PROJECT :
			// TODO index the whole project
			break;
        
		case ICDTIndexer.FOLDER : 
			// TODO source folder change? full index everything in it?
			break;
		
		case ICDTIndexer.COMPILATION_UNIT:
			// if the element has changed check to see if file is header,
			// if it is don't schedule for index - update dependencies will
			// take care of it.
			// otherwise just schedule element for index
			// TODO this is what the DOM indexer does, what do we need to do?
			boolean shouldAddFile=false;
			IFile file = (IFile) delta.getResource();
	
			if (delta.getKind()==IResourceDelta.CHANGED){
				if (CoreModel.isValidSourceUnitName(project, file.getName()))
					shouldAddFile=true;
			} else {
				shouldAddFile = true;
			}
			if (shouldAddFile){
				// TODO index the file
			}
			break;						
		}
	}

	public void addResource(IProject project, IResource resource) {
		// TODO Auto-generated method stub
	}

	public void addResourceByPath(IProject project, IPath path, int resourceType) {
		// TODO Auto-generated method stub
	}

	public IIndex getIndex(IPath path, boolean reuseExistingFile, boolean createIfMissing) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getIndexerFeatures() {
		// TODO Auto-generated method stub
		return 0;
	}

	public IIndexStorage getIndexStorage() {
		// Do nothing
		return null;
	}

	public ReadWriteMonitor getMonitorFor(IIndex index) {
		// TODO Auto-generated method stub
		return null;
	}

	public void indexerRemoved(IProject project) {
		// TODO Auto-generated method stub
		// We have been removed from this project, close up the database
	}

	public void indexJobFinishedNotification(IIndexJob job) {
		// TODO Auto-generated method stub
	}

	public boolean isIndexEnabled(IProject project) {
		// TODO Auto-generated method stub
		// Type cache asking for trouble :)
		return false;
	}

	public void notifyIdle(long idlingTime) {
		// TODO Auto-generated method stub
		// We could do some database clean up if idle is long enough...
	}

	public void notifyIndexerChange(IProject project) {
		// TODO Auto-generated method stub
		// We've been added to this project, fire up a full index on it
	}

	public void notifyListeners(IndexDelta indexDelta) {
		// TODO Auto-generated method stub
	}

	public void removeRequest(IProject project, IResourceDelta delta, int kind) {
		// TODO Auto-generated method stub
		// Resource removed
	}

	public void removeResource(IProject project, IResource resource) {
		// TODO Auto-generated method stub
	}

	public void saveIndex(IIndex index) throws IOException {
		// TODO Auto-generated method stub
	}

	public void setIndexerProject(IProject project) {
		// TODO Auto-generated method stub
	}

	public void shutdown() {
		// TODO Auto-generated method stub
		// Shutdown time, shutdown all indices?
	}
	
}
