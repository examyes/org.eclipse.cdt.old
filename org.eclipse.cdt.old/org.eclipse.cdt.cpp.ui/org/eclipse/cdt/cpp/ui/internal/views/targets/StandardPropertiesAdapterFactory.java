package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
