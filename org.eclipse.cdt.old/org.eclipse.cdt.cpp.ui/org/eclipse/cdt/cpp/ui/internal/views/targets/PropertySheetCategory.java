package org.eclipse.cdt.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.eclipse.ui.views.properties.IPropertySheetEntryListener;

import java.util.ArrayList;
import java.util.List;
/**
 * A category in a PropertySheet used to group <code>IPropertySheetEntry</code>
 * entries so they are displayed together.
 */
/*package*/ class PropertySheetCategory {
	private String categoryName;
	private List entries = new ArrayList();
/**
 * Create a PropertySheet category with name.
 */
public PropertySheetCategory(String name) {
	categoryName = name;
}
/**
 * Add an <code>IPropertySheetEntry</code> to the list
 * of entries in this category. 
 */
public void addEntry(IPropertySheetEntry entry) {
	entries.add(entry);
}
/**
 * Return the category name.
 */
public String getCategoryName() {
	return categoryName;
}
/**
 * Returns the entries in this category.
 *
 * @return the entries in this category
 */
public IPropertySheetEntry[] getChildEntries() {
	return (IPropertySheetEntry[])entries.toArray(new IPropertySheetEntry[entries.size()]);
}
/**
 * Removes all of the entries in this category.
 * Doing so allows us to reuse this category entry.
 */
public void removeAllEntries() {
	entries = new ArrayList();
}
}
