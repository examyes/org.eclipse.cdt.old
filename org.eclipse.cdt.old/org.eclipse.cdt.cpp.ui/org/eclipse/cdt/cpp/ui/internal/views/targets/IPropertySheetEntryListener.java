package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
/**
 * Listener for changes in objects of type 
 * <code>IPropertySheetEntry</code>.
 * <p>
 * This interface is public since it appears in the api
 * of <code>IPropertySheetEntry</code>. It in not intended
 * to be implemented outside of this package
 * <p>
 */
public interface IPropertySheetEntryListener {
/**
 * A node's children have changed (children added or removed) 
 *
 * @param node the node whose's children have changed
 */
void childEntriesChanged(IPropertySheetEntry node);
/**
 * A entry's error message has changed
 *
 * @param entry the entry whose's error message has changed
 */
void errorMessageChanged(IPropertySheetEntry entry);
/**
 * A entry's value has changed 
 *
 * @param entry the entry whose's value has changed
 */
void valueChanged(IPropertySheetEntry entry);
}
