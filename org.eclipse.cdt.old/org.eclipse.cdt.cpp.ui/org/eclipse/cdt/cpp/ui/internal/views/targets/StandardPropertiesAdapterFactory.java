package org.eclipse.cdt.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.ui.views.properties.ResourcePropertySource;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.IPropertySource;
/**
 * Dispenses an <code>IPropertySource</code> adapter for the core resource objects.
 */
/* package */ class StandardPropertiesAdapterFactory implements IAdapterFactory {
/* (non-Javadoc)
 * Method declared on IAdapterFactory.
 */
public Object getAdapter(Object o, Class adapterType) {
	if (adapterType.isInstance(o)) {
		return o;
	}
	if (adapterType == IPropertySource.class) {
		if (o instanceof IResource)
			return new ResourcePropertySource((IResource)o);
	}
	return null;
}
/* (non-Javadoc)
 * Method declared on IAdapterFactory.
 */
public Class[] getAdapterList() {
	return new Class[] {
		IPropertySource.class
	};
}
}
