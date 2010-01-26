/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.ui.wizards;

import org.eclipse.cdt.build.core.IBuildService;
import org.eclipse.cdt.build.core.model.TargetPlatform;
import org.eclipse.cdt.internal.build.ui.Activator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class TargetPlatformBlock {

	private static class LabelProvider implements ILabelProvider {
		@Override
		public void removeListener(ILabelProviderListener listener) {
		}
		
		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		
		@Override
		public void dispose() {
		}
		
		@Override
		public void addListener(ILabelProviderListener listener) {
		}
		
		@Override
		public String getText(Object element) {
			if (element instanceof TargetPlatform)
				return ((TargetPlatform)element).getName();
			return element.toString();
		}
		
		@Override
		public Image getImage(Object element) {
			return null;
		}
	}
	
	private static class ContentProvider implements IStructuredContentProvider {
		@Override
		public void dispose() {
		}
		
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof TargetPlatform[])
				return (TargetPlatform[])inputElement;
			return new Object[0];
		}
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private static class UnavailableFilter extends ViewerFilter {
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof TargetPlatform)
				return ((TargetPlatform)element).isAvailable();
			return false;
		}
	}
	
	public TargetPlatformBlock(Composite parent) throws CoreException {
		Group targetPlatformGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		targetPlatformGroup.setLayout(layout);
		targetPlatformGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		targetPlatformGroup.setText("Target Platform");

		final ListViewer targetPlatformList = new ListViewer(targetPlatformGroup, SWT.SINGLE | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		targetPlatformList.getList().setLayoutData(gridData);
		targetPlatformList.setLabelProvider(new LabelProvider());
		targetPlatformList.setContentProvider(new ContentProvider());
		
		TargetPlatform[] targetPlatforms = Activator.getService(IBuildService.class).getTargetPlatforms();
		targetPlatformList.setInput(targetPlatforms);
		
		final ViewerFilter[] filters = new ViewerFilter[] { new UnavailableFilter() };
		final Button showAll = new Button(parent, SWT.CHECK);
		showAll.setText("Show only available target platforms");
		showAll.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (showAll.getSelection())
					targetPlatformList.setFilters(filters);
				else
					targetPlatformList.resetFilters();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		showAll.setSelection(true);
		targetPlatformList.setFilters(filters);
	}
}
