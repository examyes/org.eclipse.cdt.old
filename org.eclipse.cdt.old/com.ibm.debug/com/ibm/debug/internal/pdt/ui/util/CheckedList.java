package com.ibm.debug.internal.pdt.ui.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/util/CheckedList.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 16:00:57)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;



/**
 * A selection widget that consists of a list and a text entry field. The list
 * of elements presented are limited to the pattern entered into the text entry
 * field.
 */
public class CheckedList extends Composite {

	// State
	private Object[] fElements;
	private Object[] unsortedElements;
	private ILabelProvider fRenderer;
	private boolean fIgnoreCase;
	private Hashtable renderedStringToElement;
	private Hashtable elementToRenderedString;
	 
	 //used to restore checked state after filtering	
	private Hashtable renderedStringToCheckedState; 
	
	
	// Implementation details
	private String[] fRenderedStrings;
	private int[] fFilteredElements;	
	private String fRememberedMatchText;
	

	// SWT widgets
	private Table fList;
	private Text fText;
	
	
		
	/**
	 * Creates new instance of the widget.
	 */
	public CheckedList(Composite parent, int style, ILabelProvider renderer, boolean ignoreCase) {
		super(parent, SWT.NONE);
		fRenderer= renderer;
		fIgnoreCase= ignoreCase;
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0; layout.marginWidth= 0;
		//XXX: 1G9V58A: ITPUI:WIN2000 - Dialog.convert* methods should be static
		setLayout(layout);
		createText();
		createList(style);
		fList.addSelectionListener(fListListener);		
	}
	/**
	 * Adds a selection change listener to this widget.
	 */
	public void addSelectionListener(SelectionListener listener) {
		fList.addSelectionListener(listener);
	}
	private void createList(int style) {
		fList= new Table(this, style);
		fList.setLayoutData(new GridData(GridData.FILL_BOTH));
		fList.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				fRenderer.dispose();
				removeSelectionListener(fListListener);
			}
		});		
	}
	
		
	private void createText() {
		fText= new Text(this, SWT.BORDER);		
		GridData spec= new GridData();
		spec.grabExcessVerticalSpace= false;
		spec.grabExcessHorizontalSpace= true;
		spec.horizontalAlignment= spec.FILL;
		spec.verticalAlignment= spec.BEGINNING;
		fText.setLayoutData(spec);		
		Listener l= new Listener() {
			public void handleEvent(Event evt) {				
				filter(false);
			}
		};		
		fText.addListener(SWT.Modify, l);
		
	}
	/**
	 * Filters the list of elements according to the pattern entered
	 * into the text entry field.
	 */
	public void filter(boolean forceUpdate) {
		int k= 0;
		String text= fText.getText();
		if (!forceUpdate && text.equals(fRememberedMatchText))
			return;
		fRememberedMatchText= text;
		StringMatcher matcher= new StringMatcher(text+"*", fIgnoreCase, false);
		for (int i= 0; i < fElements.length; i++) {
			if (matcher.match(fRenderedStrings[i])) {
				fFilteredElements[k]= i;
				k++;
			}
		}
		fFilteredElements[k]= -1;
		updateListWidget(fFilteredElements, k);
	}
	/**
	 * Returns the currently used filter text.
	 */
	public String getFilter() {
		return fText.getText();
	}
	/**
	 * Returns a list of selected elements. Note that the type of the elements
	 * returned in the list are the same as the ones passed to the selection list
	 * via <code>setElements</code>. The list doesn't contain the rendered strings.
	 */
	public List getSelection() {
		if (fList == null || fList.isDisposed() || fList.getSelectionCount() == 0)
			return new ArrayList(0);
		int[] listSelection= fList.getSelectionIndices();
		List selected= new ArrayList(listSelection.length);
		for (int i= 0; i < listSelection.length; i++) {
			selected.add(fElements[fFilteredElements[listSelection[i]]]);
		}
		return selected;
	}
	/**
	 * Returns the selection indices.
	 */
	public int[] getSelectionIndices() {
		return fList.getSelectionIndices();
	}
	
	/**
	 * Returns a vector of checked elements. Note that the type of the elements
	 * returned in the list are the same as the ones passed to the selection list
	 * via <code>setElements</code>. May be empty.
	 * 
	 * See also getStateArray()
	 */
	public Vector getCheckedElements() {
		
		Vector selected = new Vector();
		
		Enumeration enum = renderedStringToCheckedState.keys();
		Object current;
		while(enum.hasMoreElements())
		{
			current = enum.nextElement();
			if( ((Boolean)renderedStringToCheckedState.get(current)).booleanValue() )
				selected.addElement(renderedStringToElement.get(current));
			
		}
		
		return selected;
	}
	
	/**
	 * Returns an array with the checked state of each element, in the order the 
	 * elements were passed in.
	 */
		
	public boolean[] getStateArray()
	{		
		boolean[] states = new boolean[unsortedElements.length];
		for(int i=0; i < unsortedElements.length; i++)
			states[i] = ((Boolean)renderedStringToCheckedState.get(elementToRenderedString.get(unsortedElements[i]))).booleanValue();
		return states;
	}
			
	/**
	 * Sets the checked table items.
	 * Requires an array of booleans that is the same size as
	 * the list of items added to the table.  
	 */
	public void initializeCheckedTable(boolean[] checked){
		TableItem[] allTableItems= fList.getItems();		
		for(int i=0; i< allTableItems.length; i++)			
			allTableItems[i].setChecked(checked[i]);	
		
	}
	
	
	/**
	 * Removes a selection change listener to this widget.
	 */
	public void removeSelectionListener(SelectionListener listener) {
		fList.removeSelectionListener(listener);
	}
	
	private String[] renderStrings(boolean[] checkedArray) {		
		renderedStringToCheckedState = new Hashtable(fElements.length);
		renderedStringToElement = new Hashtable(fElements.length);
		elementToRenderedString = new Hashtable(fElements.length);
		String[] strings= new String[fElements.length];
		for (int i= 0; i < strings.length; i++) {
			strings[i]= fRenderer.getText(fElements[i]);
			renderedStringToCheckedState.put(strings[i], new Boolean(checkedArray[i]));	
			renderedStringToElement.put(strings[i], fElements[i]);
			elementToRenderedString.put(fElements[i], strings[i]);						
		}
		TwoArrayQuickSort.sort(strings, fElements, fIgnoreCase);
		return strings;
	}
	/**
	 * Select the pattern text.
	 */
	public void selectFilterText() {
		fText.selectAll();
	}
	/**
	 * Sets the list of elements presented in the widget.
	 */
	public void setElements(List elements, boolean refilter, boolean[] checkedArray) {
		// We copy the list since we sort it.	
		if(unsortedElements==null)	
			unsortedElements= elements.toArray();	
		if (elements == null)
			fElements= new Object[0];		
		else 
		{
			fElements= elements.toArray();			
		}
		fFilteredElements= new int[fElements.length+1];		
		fRenderedStrings= renderStrings(checkedArray);		
				
		if (refilter)
			filter(true);		
	}
	
	/**
	 * Will reinitialize checkboxes to new values. The boolean values correspond to the
	 * initial element list provided to setElements.
	 */
	public void resetChecks(boolean[] checks)
	{
		renderedStringToCheckedState.clear();
		
		for (int i= 0; i < fElements.length; i++)				
		{
			renderedStringToCheckedState.put(elementToRenderedString.get(fElements[i]), 
					new Boolean(checks[i]));					
		}
		//cannot combine because all items might not be showing
		TableItem[] allTableItems= fList.getItems();		
		for(int i=0; i< allTableItems.length; i++)			
			allTableItems[i].setChecked( ((Boolean)renderedStringToCheckedState.get(allTableItems[i].getText())).booleanValue() );	
	}
	
	/* 
	 * Non Java-doc
	 */
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);
		fText.setEnabled(enable);
		fList.setEnabled(enable);
	}
	/**
	 * Sets the filter pattern. Current only prefix filter pattern are supported.
	 */
	public void setFilter(String pattern, boolean refilter) {
		fText.setText(pattern);
		if (refilter)
			filter(true);
	}
	/*
	 * Non Java-doc
	 */
	public boolean setFocus() {
		return fText.setFocus();
	}
	/*
	 * Non Java-doc
	 */
	public void setFont(Font font) {
		super.setFont(font);
		fText.setFont(font);
		fList.setFont(font);
	}
	/**
	 * Selects the elements in the list determined by the given
	 * selection indices.
	 */
	protected void setSelection(int[] selection) {
		fList.setSelection(selection);
	}
	private void updateListWidget(int[] indices, int size) {
		if (fList == null || fList.isDisposed())
			return;
		fList.setRedraw(false);
		int itemCount= fList.getItemCount();		
		int oldSelectionIndex= fList.getSelectionIndex();
		if (size < itemCount) {
			fList.remove(0, itemCount-size-1);
		}
		Display d= fList.getDisplay();
		TableItem[] items= fList.getItems();
		for (int i= 0; i < size; i++) {
			TableItem ti= null;
			if (i < itemCount) {
				ti= items[i];
			} else {
				ti= new TableItem(fList, i);											
			}
			ti.setText(fRenderedStrings[indices[i]]);	
			ti.setChecked(((Boolean)renderedStringToCheckedState.get(fRenderedStrings[indices[i]])).booleanValue());		
			Image img= fRenderer.getImage(fElements[indices[i]]);
			if (img != null)
				ti.setImage(img);
		}
		if (fList.getItemCount() > 0) {
			fList.setSelection(0);
		}
					
		fList.setRedraw(true);
		Event event= new Event();
		fList.notifyListeners(SWT.Selection, event);
	}
	
	/**
	 * Checks every checkbox in the list
	 */
	public void checkAll()
	{
		TableItem[] allTableItems= fList.getItems();
		for(int i=0; i < allTableItems.length; i++)		
			allTableItems[i].setChecked(true);
			
		//not all items may be showing therefore cannot just clear hashtable and start again
		for(int i=0; i < fElements.length; i++)	
		{	
			
			renderedStringToCheckedState.put(elementToRenderedString.get(fElements[i]),new Boolean(true));
		}
		
	}
	
		
	/**
	 * Unchecks every checkbox in the list
	 */
	public void uncheckAll()
	{
		TableItem[] allTableItems= fList.getItems();	
		for(int i=0; i < allTableItems.length; i++)		
			allTableItems[i].setChecked(false);	
		
		//not all items may be showing therefore cannot just clear hashtable and start again
		for(int i=0; i < fElements.length; i++)	
		{	
			
			renderedStringToCheckedState.put(elementToRenderedString.get(fElements[i]),new Boolean(false));
		}
	}
	
	/**
	 * Listen for selection events on the table items to keep track of current state.
	 * Also listen for double clicks to toggle the check box state.
	 */	
	private SelectionListener fListListener= new SelectionAdapter() {
		public void widgetSelected(SelectionEvent evt) {
			
			if(!(evt.item instanceof TableItem))
				return;
			
			TableItem item = ((TableItem)evt.item);			
			String key = item.getText();
			renderedStringToCheckedState.remove(key);
			renderedStringToCheckedState.put(key, new Boolean(item.getChecked()));			
		}
		
		//double click on item - toggle current value
		public void widgetDefaultSelected(SelectionEvent evt) {
			
			if(!(evt.item instanceof TableItem))
				return;
			
			TableItem item = ((TableItem)evt.item);	
			item.setChecked(!item.getChecked());		
			String key = item.getText();
			renderedStringToCheckedState.remove(key);
			renderedStringToCheckedState.put(key, new Boolean(item.getChecked()));		
		}
	};
}
