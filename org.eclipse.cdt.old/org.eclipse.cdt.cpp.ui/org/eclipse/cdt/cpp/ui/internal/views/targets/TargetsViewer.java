package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.cpp.ui.internal.*;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;
import org.eclipse.core.resources.*;
import java.text.Collator;
import java.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import java.util.List;
import org.eclipse.swt.custom.*;
import org.eclipse.jface.action.IStatusLineManager;

public class TargetsViewer extends Viewer implements ISelectionChangedListener, IDomainListener
{
	// Input objects for the viewer
	private Object[] input;

	// The root entry for the viewer
	private IPropertySheetEntry rootEntry;

	// The current catagory
	 private PropertySheetCategory[] categories;
	
	//NL Enablement
	private static CppPlugin pluginInstance = CppPlugin.getPlugin();
	private static String Table_COLUMN_1 = "TargetsViewer.First_Column.Title";
	private static String Table_COLUMN_2 = "TargetsViewer.Second_Column.Title";
	private static String Table_COLUMN_3 = "TargetsViewer.Third_Column.Title";
	private String Element_Key = "TargetsViewer.RootElement.Key";

	// SWT widgets
	protected TableTree tableTree;
	private TableTreeEditor tableTreeEditor;
		private static String[] columnLabels = {pluginInstance.getLocalizedString(Table_COLUMN_1), pluginInstance.getLocalizedString(Table_COLUMN_2),pluginInstance.getLocalizedString(Table_COLUMN_3)};
	private static String MISCELLANEOUS_CATEGORY_NAME = "Misc";
	
	// Cell editor support.
	protected int columnToEdit = 1;
	protected CellEditor cellEditor;
	protected IPropertySheetEntryListener entryListener;

	//Flag to indicate if categories should be shown
	boolean isShowingCategories = false;
	//Flag to indicate if expert properties should be shown
	boolean isShowingExpertProperties = false;
	// The status line manager for showing messages
	private IStatusLineManager statusLineManager;

	// host page
	private TargetsPage hostPage;


public TargetsViewer(Composite parent, TargetsPage host) {

	hostPage = host;
	
	tableTree = new TableTree(parent,SWT.FULL_SELECTION|SWT.SINGLE|SWT.HIDE_SELECTION);
	// configure the widget
	Table table = tableTree.getTable();
	table.setLinesVisible(true);
	table.setHeaderVisible(true);
	
	// add listener to the tableViewer
	TableViewer tableViewer = new TableViewer(table);
	tableViewer.addSelectionChangedListener(this);
	
	// configure the columns
	addColumns();

	// add our listeners to the widget
	hookControl();
	
	// create a new table tree editor	
	tableTreeEditor = new TableTreeEditor(tableTree);

	// create the entry listener
	createEntryListener();


	com.ibm.cpp.ui.internal.CppPlugin.getPlugin().getCurrentDataStore().getDomainNotifier().addDomainListener(this);
	// filling tool bar action menu 
	
}
/**
 * Activate a cell editor for the given selected table tree item.
 *
 * @param item the selected table tree item
 */
protected void activateCellEditor(TableTreeItem item) {
	// ensure the cell editor is visible
	tableTree.showSelection();

	// Get the entry for this item
	IPropertySheetEntry activeEntry = (IPropertySheetEntry) item.getData();

	// Get the cell editor for the entry.
	// Note that the editor parent must be the Table control 
	// that is underneath the TableTree
	cellEditor = activeEntry.getEditor(tableTree.getTable());

	if (cellEditor == null) 
		// unable to create the editor
		return; 

	// activate the cell editor
	cellEditor.activate();

	// if the cell editor has no control we can stop now
	Control control = cellEditor.getControl();
	if (control == null) {
		cellEditor.deactivate();
		cellEditor = null;
		return;
	}

	// set the layout of the table tree editor to match the cell editor
	CellEditor.LayoutData layout = cellEditor.getLayoutData();
	tableTreeEditor.horizontalAlignment = layout.horizontalAlignment;
	tableTreeEditor.grabHorizontal = layout.grabHorizontal;
	tableTreeEditor.minimumWidth = layout.minimumWidth;
	tableTreeEditor.setEditor(control, item, columnToEdit);

	// set the error text from the cel editor
	setErrorMessage(cellEditor.getErrorMessage());
	
	// give focus to the cell editor
	cellEditor.setFocus();
}
/**
 * Add columns to  (TableTree)
 * and set up the layout manager accordingly.
 */
private void addColumns() {
	Table table = tableTree.getTable();
	// create the columns
	TableColumn[] columns = table.getColumns();
	for (int i = 0; i < columnLabels.length; i++) {
		String string = columnLabels[i];
		if (string != null) {
			TableColumn column;
			if (i < columns.length) {
				column = columns[i];
			} else {
				column = new TableColumn(table, 0);
			}
			column.setText(string);
		}
	}

	// property Column
	ColumnLayoutData c1Layout = new ColumnWeightData(28, false);

	// value Column
	ColumnLayoutData c2Layout = new ColumnWeightData(36, true);

		// value Column
	ColumnLayoutData c3Layout = new ColumnWeightData(36, false);


	// set columns in Table layout
	TableLayout layout = new TableLayout();
	layout.addColumnData(c1Layout);
	layout.addColumnData(c2Layout);
	layout.addColumnData(c3Layout);
	table.setLayout(layout);
}
/**
 * Ask the entry currently being edited to apply its
 * current cell editor value.
 */
private void applyEditorValue() {
	IPropertySheetEntry entry = (IPropertySheetEntry)tableTreeEditor.getItem().getData();
	entry.applyEditorValue();
}
/**
 * Clear the entire TargetsViewer display and contents.
 *
 */
protected void clearAll() {
	deactivateCellEditor();
	rootEntry = null;
	// Remove the table tree nodes
	tableTree.removeAll();
}
/**
 * Creates the child items for the given widget (item or table tree).
 * This method is called when the item is expanded for the first time or
 * when an item is assigned as the root of the table tree.
 */
private void createChildren(Widget widget) {
	// get the current child items
	TableTreeItem[] childItems;
	if (widget == tableTree)
		childItems = tableTree.getItems();
	else {
		childItems = ((TableTreeItem)widget).getItems();
	}

	if (childItems.length > 0) {
		Object data = childItems[0].getData();
		if (data != null)
			// children already there!
			return; 
		else
			// remove the dummy
			childItems[0].dispose();
	}

	// get the children and create their table tree items
	Object node = widget.getData();
	List children = getChildren(node);
	if (children.isEmpty()) 
		// this item does't actually have any children
		return;
	for (int i = 0; i < children.size(); i++) {
			// create a new table tree item
			createItem(children.get(i), widget, i);
	}
}
/** 
 
 */
private void createEntryListener() {
	entryListener = new IPropertySheetEntryListener() {
		public void childEntriesChanged(IPropertySheetEntry entry) {
			// update the children of the given entry
			if (entry == rootEntry)
				updateChildrenOf(entry, tableTree);
			else {
				TableTreeItem item = findItem(entry);
				if (item != null)
					updateChildrenOf(entry, item);
			}
		}
		public void valueChanged(IPropertySheetEntry entry) {
			// update the given entry
			TableTreeItem item = findItem(entry);
			if (item != null)
				updateEntry(entry, item);
		}
		public void errorMessageChanged(IPropertySheetEntry entry) {
			// update the error message
			setErrorMessage(entry.getErrorText());
		}
	};
}
/**
 * Creates a new table tree item, sets the given entry or
 * category (node)in its user data field, and adds a listener to
 * the node if it is an entry.
 *
 * @param node the entry or category associated with this item
 * @param parent the parent widget
 * @param index indicates the position to insert the item 
 *    into its parent
 */
private void createItem(Object node, Widget parent, int i) {
	// create the item
	TableTreeItem item;
	if (parent instanceof TableTreeItem)
		item = new TableTreeItem((TableTreeItem) parent, SWT.NONE, i);
	else
		item = new TableTreeItem((TableTree) parent, SWT.NONE, i);

	// set the user data field	
	item.setData(node);

	// add our listener
	if (node instanceof IPropertySheetEntry)
		((IPropertySheetEntry)node).addPropertySheetEntryListener(entryListener);

	// update the visual presentation	
	if (node instanceof IPropertySheetEntry)
		updateEntry((IPropertySheetEntry)node, item);
	else	
		updateCategory((PropertySheetCategory)node, item);
}
/**
 * Deactivate the currently active cell editor.
 */
/*package*/ void deactivateCellEditor() {
	tableTreeEditor.setEditor(null, null, columnToEdit);
	if (cellEditor != null) {
		cellEditor.deactivate();
		cellEditor = null;
	}
	// clear any error message from the editor
	setErrorMessage(null);
}
    public boolean listeningTo(DomainEvent ev)
    {
	DataElement parent = (DataElement)ev.getParent();  
	for(int i = 0; i< hostPage.targetStore.projectList.size(); i++)
	  {
	      RootElement obj = (RootElement)hostPage.targetStore.projectList.elementAt(i);
	      Vector children = obj.getTargets();
	      for (int j=0; j < children.size(); j++)
		  {
		      TargetElement subChild = (TargetElement)children.elementAt(j);
		      DataElement status = subChild.getStatus();
		      if (status == parent)
			  {
			      return true;
			  }
		  }
	    }

	return false;
    }

  public void domainChanged(DomainEvent ev)
  {
	DataElement parent = (DataElement)ev.getParent();  
	ArrayList domChildren = ev.getChildren();

	for(int i = 0; i< hostPage.targetStore.projectList.size(); i++)
	  {
	RootElement obj = (RootElement)hostPage.targetStore.projectList.elementAt(i);
	Vector children = obj.getTargets();
	for (int j=0; j < children.size(); j++)
	  {
	    TargetElement subChild = (TargetElement)children.elementAt(j);
	    DataElement status = subChild.getStatus();
	    if (status == parent)
	    {
	   
		clearAll();///////////////////////////////////////////////////////////
		
		ArrayList list = new ArrayList();
		list.add(obj);
		
		setInput(list.toArray());
		return;
	      }	    
	  }	
	  }
  }  
/**
 * Return a table tree item in the targets view that has 
 * the same entry in its user data field as the supplied
 * entry. Return <code>null</code> if there is no such item.
 *
 * @param entry the entry to serach for
 */
private TableTreeItem findItem(IPropertySheetEntry entry) {
	// Iterate through tableTreeItems to find item
	TableTreeItem[] items = tableTree.getItems();
	for (int i = 0; i < items.length; i++) {
		TableTreeItem item = items[i];
		TableTreeItem findItem = findItem(entry, item);
		if (findItem != null)
			return findItem;
	}
	return null;
}
/**

 * @param entry the entry to search for
 * @param item the item look in
 */
private TableTreeItem findItem(IPropertySheetEntry entry, TableTreeItem item) {
	// compare with current item
	if (entry == item.getData())
		return item;
		
	// recurse over children
	TableTreeItem[] items = item.getItems();
	for (int i = 0; i < items.length; i++) {
		TableTreeItem childItem = items[i];
		TableTreeItem findItem = findItem(entry, childItem);
		if (findItem != null)
			return findItem;
	}
	return null;
}
/**
 * Returns the child entries of the given entry
 *
 * @return the children of the given entry
 */
private List getChildren(IPropertySheetEntry entry) {
	// if the entry is the root and we are showing categories,
	// return the categories
	if (entry == rootEntry && isShowingCategories)
		return Arrays.asList(categories); 

	// return the filtered child entries
	return getFilteredEntries(entry.getChildEntries());
}
/**
 * Returns the child entries of the given category
 *
 * @return the children of the given category
 */
private List getChildren(PropertySheetCategory category) {
	return getFilteredEntries(category.getChildEntries());
}
/**
 * Returns the children of the given category or entry
 *
 * @node a category or entry
 * @return the children of the given category or entry
 */
private List getChildren(Object node) {
	// cast the entry or category	
	IPropertySheetEntry entry =  null;
	PropertySheetCategory category = null;
	if (node instanceof IPropertySheetEntry)
		entry = (IPropertySheetEntry)node;
	else
		category = (PropertySheetCategory)node;

	// get the child entries or categories
	List children;
	if (category == null)
		children = getChildren(entry);
	else 
		children = getChildren(category);

	return children;
}
/**
 * Returns the primary control associated with this viewer.
 */
public Control getControl() {
	return tableTree;
}
/**
 * Returns the entries which match the current filter.
 *
 * @entries the entries to filter
 */
private List getFilteredEntries(IPropertySheetEntry[] entires) {
	// if no filter just return all entries
	if (!isShowingExpertProperties)
		return Arrays.asList(entires);

	// check each entry for the filter
	List filteredEntries = new ArrayList(entires.length);
	for (int i = 0; i < entires.length; i++) {
		String[] filters = ((IPropertySheetEntry)entires[i]).getFilters();
		boolean expert = false;
		if (filters != null) {
			for (int j = 0; j < filters.length; j++){
				if (filters[j].equals(IPropertySheetEntry.FILTER_ID_EXPERT)) {
					expert = true;
					break;
				}
			}
		}
		if (!expert)
			filteredEntries.add(entires[i]);
	}
	return filteredEntries;
}
/**
 * Return the current input of this viewer.
 * The input is a Vector of IElements.
 */
public Object getInput() {

	return input;
}
/**
 * Returns the root entry for this targets viewer
 * The root entry is not visible in the viewer.
 * 
 * @return the root entry or <code>null</code>.
 */
public IPropertySheetEntry getRootEntry() {
	return rootEntry;
}
/**
 
 */
public ISelection getSelection() {
	if (tableTree.getSelectionCount() == 0)
		return StructuredSelection.EMPTY;
	TableTreeItem[] sel = tableTree.getSelection();
	List entries = new ArrayList(sel.length);
	for (int i = 0; i < sel.length; i++) {
		TableTreeItem ti = sel[i];
		Object data = ti.getData();
		if (data instanceof IPropertySheetEntry)
			entries.add(data);
	}
	return new StructuredSelection(entries);
}
/**
 * Convert the SWT TableTree selection into an element selection.
 */
protected void getSelectionFromWidget(Vector v) {
	if (tableTree == null || tableTree.isDisposed())
		return;
	TableTreeItem[] sel = tableTree.getSelection();
	for (int i = 0; i < sel.length; i++) {
		TableTreeItem ti = sel[i];
		if (ti != null && !ti.isDisposed()) {
			IPropertySheetEntry e = (IPropertySheetEntry) ti.getData();
			if (e != null)
				v.addElement(e);
		}
	}
}
public Shell getShell(){
	return null;
}
/**
 * Selection in the viewer occurred. 
 * Check if there is an active cell editor. 
 * If yes, deactivate it and check if a new cell editor must be activated.
 *
 * @param event the selection event
 */
private void handleSelect(SelectionEvent event) {
	// deactivate the current cell editor
	if (cellEditor != null) {
		applyEditorValue();
		deactivateCellEditor();
	}

	// get the new selection
	TableTreeItem[] sel = tableTree.getSelection();
	if (sel.length == 0) {
		setMessage(null);
		setErrorMessage(null);
		return;
	}

	Object object = sel[0].getData(); // assume single selection
	if (object instanceof IPropertySheetEntry) {
		// get the entry for this item
		IPropertySheetEntry activeEntry = (IPropertySheetEntry)object;

		// display the description for the item
		setMessage(activeEntry.getDescription());

		// activate a cell editor on the selection
		activateCellEditor(sel[0]);
	}
}
/**
 * The expand icon for a node in the TargetsVieweer has been
 * selected to collapse the subtree. Remember that the node
 * is collapsed.
 */
private void handleTreeCollapse(TreeEvent event) {
	if (cellEditor != null) {
		applyEditorValue();
		deactivateCellEditor();
	}
}
/**
 * The expand icon for a node in the TargetsViewer has been
 * selected to expand the subtree. Create the children 1 level deep.
 *
 */
private void handleTreeExpand(TreeEvent event) {
	createChildren(event.item);
}
/**
 * Hide the categories.
 */
void hideCategories() {
	isShowingCategories = false;
	categories = null;
	refresh();	
}
/**
 * Hide the expert properties.
 */
void hideExpert() {
	isShowingExpertProperties = false;
	refresh();
}
/**
 * Establish this viewer as a listener on the control
 */
private void hookControl() {
	// Handle selections in the TableTree
	tableTree.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			handleSelect(e);
		}
	});

	// Add a tree listener to expand and collapse which
	// allows for lazy creation of children
	tableTree.addTreeListener(new TreeListener() {
		public void treeExpanded(final TreeEvent event) {
			handleTreeExpand(event);
		}
		public void treeCollapsed(final TreeEvent event) {
			handleTreeCollapse(event);
		}
	});
}
/**
 * Called when input to this viewer's pane has been changed.
 * Populate the view showing the properties of the input elements.
 * Use the content provider to obtain <code>IPropertySource<\code>(s) for
 * the input elements.
 *
 * @param input The vector of input elements or 
 * 				null if the view should be cleared.
 */

public void inputChanged(Object obj, Object oldobj) {

	
	if(obj instanceof Object[])
	{
		Object[] input = (Object[])obj;
		// Clear the view if there is no input (IElement)
		if (input == null) {
			clearAll();
			return;
		}
		deactivateCellEditor();

		// Create the root entry and the tableTree entries
		if (rootEntry == null) {
			setRootEntry(new TargetsEntry());
		}
		rootEntry.setValues(input);

		Table table = tableTree.getTable();
		TableViewer tv = new TableViewer(table);

		// maintaining the status values
		if(input.length>0)
		{
			int targetCounter = 0;
			RootElement rootElement = (RootElement)input[0];
			for(int i = 0; i < table.getItemCount(); i++)
			{
				if(rootElement!=null)
				 {
					if(!table.getItem(i).getText().equals(pluginInstance.getLocalizedString(Element_Key)))
						i++;
					else
					{
						String status = new String(((TargetElement)(rootElement.getTargets().elementAt(targetCounter++))).getStatus().getValue());
						if(status.equals("name")) // has to be changed - discuss with Dave
							table.getItem(i).setText(2," ");
						else
							table.getItem(i).setText(2,status); // update from targetelement
					}
				}
			}
		}
		return;
	}
}
/**
 * Updates all of the items in the tree.
 * <p>
 * Note that this means ensuring that the tree items reflect the state
 * of the model  it does not mean telling the model to update 
 * itself.
 * </p>
 */
public void refresh() {
	if (rootEntry != null) {
		updateChildrenOf(rootEntry, tableTree);
	}
}
/**
 * Remove the given item from the table tree.
 * Remove our listener if the item's user data is a
 * an entry then set the user data to null
 *
 * @param item the item to remove
 */
private void removeItem(TableTreeItem item) {
	Object data = item.getData();
	if (data instanceof IPropertySheetEntry) 
		((IPropertySheetEntry)data).removePropertySheetEntryListener(entryListener);
	item.setData(null);
	item.dispose();
}
/**
 * Reset the selected properties to their default values.
 */
public void resetProperties() {
	// Determine the selection
	IStructuredSelection selection = (IStructuredSelection)getSelection();

	// Iterate over entries and reset them
	Iterator enum = selection.iterator();
	while (enum.hasNext())
		 ((IPropertySheetEntry) enum.next()).resetPropertyValue();
}
/*

*/
public void selectionChanged(SelectionChangedEvent evt){
	Vector vec = new Vector();
	TableTreeItem obj = (TableTreeItem)tableTree.getTable().getSelection()[0].getData();
	if(obj.getText(0).equals(pluginInstance.getLocalizedString(Element_Key)))
	{
		hostPage.buildAction.setEnabled(true);
		hostPage.removeAction.setEnabled(true);
		hostPage.removeAllAction.setEnabled(true);
	}
	else
	{
		hostPage.buildAction.setEnabled(false);
		hostPage.removeAction.setEnabled(false);
		hostPage.removeAllAction.setEnabled(false);
	}
}
/**
 * Sets the error message to be displayed in the status line.
 *
 * @param errorMessage the message to be displayed, or <code>null</code>
 */
private void setErrorMessage(String errorMessage) {
	// show the error message
	if (statusLineManager != null) 
		statusLineManager.setErrorMessage(errorMessage);
}
/**
 
 */
public void setInput(Object newInput) {
	// deactivate our cell editor
	deactivateCellEditor();

	// set the new input to the root entry
	input = (Object[])newInput;
	if (input == null)
		input = new Object[0];
		
	if (rootEntry != null) {
		rootEntry.setValues(input);
		// ensure first level children are visible
		updateChildrenOf(rootEntry, tableTree);
	}
	inputChanged(newInput,null);
}
/**
 * Sets the message to be displayed in the status line. This message
 * is displayed when there is no error message.
 *
 * @param message the message to be displayed, or <code>null</code>
 */
private void setMessage(String message) {
	// show the message
	if (statusLineManager != null) 
		statusLineManager.setMessage(message);
}
/**
 * Sets the root entry for this targets viewer.
 * The root entry is not visible in the viewer.
 * 
 * @param root the root entry
 */
public void setRootEntry(IPropertySheetEntry root) {
	// If we have a root entry, remove our entry listener
	if (rootEntry != null)
		rootEntry.removePropertySheetEntryListener(entryListener);
		
	rootEntry = root;

	// Set the root as user data on the tableTree
	tableTree.setData(rootEntry);
	
	// Add an IPropertySheetEntryListener to listen for entry change notifications
	rootEntry.addPropertySheetEntryListener(entryListener);

	// Pass our input to the root, this will trigger entry change
	// callbacks to update this viewer
	setInput(input);
}
/**
 */
public void setSelection(ISelection selection, boolean reveal) {
	
}
/**
 * Sets the status line manager this view will use to show messages.
 *
 * @param manager the status line manager
 */
public void setStatusLineManager(IStatusLineManager manager) {
	statusLineManager = manager;
}
/**
 * Show the categories.
 */
void showCategories() {
	isShowingCategories = true;
	refresh();
}
/**
 * Show the expert properties.
 */
public void showExpert() {
	isShowingExpertProperties = true;
	refresh();
}

protected boolean targetHasBeenBuilt(Vector vec, int index)  {
	for(int i = 0; i < vec.size(); i++)
	{
		if(index == ((Integer)vec.elementAt(i)).intValue())
			return true;
	}
	return false;

}
/**
 * Updates the categories.
 * Reuses old categories if possible.
 */
private void updateCategories(){
	// lazy initialize
	if (categories == null)
		categories = new PropertySheetCategory[0];

	// get all the filtered child entries of the root
	List childEntries = getFilteredEntries(rootEntry.getChildEntries());

	// if the list is empty, just set an empty categories array
	if (childEntries.size() == 0) {
		categories = new PropertySheetCategory[0];
		return;
	} 

	// cache old categories by their descriptor name
	Map categoryCache = new HashMap (categories.length*2+1);
	for (int i = 0; i < categories.length; i++) {
		categories[i].removeAllEntries();
		categoryCache.put(categories[i].getCategoryName(), categories[i]);
	}

	// create a list of categories to get rid of
	List categoriesToRemove = new ArrayList(Arrays.asList(categories));
		
	// Determine the categories
	PropertySheetCategory misc = (PropertySheetCategory)categoryCache.get(MISCELLANEOUS_CATEGORY_NAME);
	if (misc == null) 
		misc = new PropertySheetCategory(MISCELLANEOUS_CATEGORY_NAME);
	boolean addMisc = false;
		
	for (int i = 0; i < childEntries.size(); i++) {
		IPropertySheetEntry childEntry = (IPropertySheetEntry)childEntries.get(i);
		String categoryName = childEntry.getCategory();
		if (categoryName == null) {
			misc.addEntry(childEntry);
			addMisc = true;
			categoriesToRemove.remove(misc);
		} else {
			PropertySheetCategory category = (PropertySheetCategory)categoryCache.get(categoryName);
			if (category == null) {
				category = new PropertySheetCategory(categoryName);
				categoryCache.put(categoryName, category);
			} else {
				categoriesToRemove.remove(category);
			}
			category.addEntry(childEntry);
		}
	}

	// Add the PSE_MISC category if it has entries
	if (addMisc)
		categoryCache.put(MISCELLANEOUS_CATEGORY_NAME, misc);

	// Sort the categories	
	List list = new ArrayList(categoryCache.values());
	for (int i = 0; i < categoriesToRemove.size() ; i++) 
		list.remove(categoriesToRemove.get(i));
	Collections.sort(list, new Comparator() {
		Collator coll = Collator.getInstance(Locale.getDefault());
		public int compare(Object a, Object b) {
			PropertySheetCategory c1, c2;
			String dname1, dname2;
			c1 = (PropertySheetCategory) a;
			dname1 = c1.getCategoryName();
			c2 = (PropertySheetCategory) b;
			dname2 = c2.getCategoryName();
			return coll.compare(dname1, dname2);
		}
	});

	categories = (PropertySheetCategory[])list.toArray(new PropertySheetCategory[list.size()]);
}
/**
 * Update the category (but not its parent or children).
 *
 */
private void updateCategory(PropertySheetCategory category, TableTreeItem item) {
	// ensure that backpointer is correct
	item.setData(category);
	
	// Update the name and value columns
	item.setText(0, category.getCategoryName());
	item.setText(1, "");

	// update the "+" icon	
	updatePlus(category, item);
}
/**
 * Update the child entries or categories of the given entry or category.
 * If the given node is the root entry and we are showing categories 
 * then the child entries are categories, otherwise they are entries.
 *
 * @param node the entry or category whose children we will update
 * @param widget the widget for the given entry, either a
 *  <code>TableTree</code> if the node is the root node or a
 *  <code>TableTreeItem</code> otherwise.
 */
private void updateChildrenOf(Object node, Widget widget) {
	// cast the entry or category	
	IPropertySheetEntry entry =  null;
	PropertySheetCategory category = null;
	if (node instanceof IPropertySheetEntry)
		entry = (IPropertySheetEntry)node;
	else
		category = (PropertySheetCategory)node;
	
	// get the current child table tree items
	TableTreeItem item = null;
	TableTreeItem[] childItems;
	if (node == rootEntry) {
		childItems = tableTree.getItems();
	} else {
		item = (TableTreeItem)widget;
		childItems = item.getItems();
	}

	// optimization! prune collapsed subtrees
	if (item != null && !item.getExpanded()) {
		// remove all children
		for (int i= 0; i < childItems.length; i++) {
			if (childItems[i].getData() != null) {
				removeItem(childItems[i]);
			}
		}

		// append a dummy if necessary
		if ((category != null || entry.hasChildEntries()) && childItems.length == 0) // may already have a dummy
			// its is either a category (which always has at least one child)
			// or an entry with chidren.
			// Note that this test is not perfect, if we have filtering on
			// then there in fact may be no entires to show when the user
			// presses the "+" expand icon. But this is an acceptable compromise.
			new TableTreeItem(item, SWT.NULL);
		
		return;
	}

	// get the child entries or categories
	if (node == rootEntry && isShowingCategories)
		// update the categories
		updateCategories();
	List children = getChildren(node);

	// remove items
	Set set = new HashSet(childItems.length*2+1);
	
	for (int i= 0; i < childItems.length; i++) {
		Object data = childItems[i].getData();
		if (data != null) {
			Object e = data;
			int ix = children.indexOf(e); 
			if (ix < 0) {	// not found
				removeItem(childItems[i]);
			} else { // found
				set.add(e);
			}
		} else if (data == null) {	// the dummy
			item.dispose();
		}
	}
	
	// WORKAROUND
	int oldCnt = -1;
	if (widget == tableTree)
		oldCnt = tableTree.getItemCount();
		
	// add new items
	int newSize = children.size();
	for (int i = 0; i < newSize; i++) {
		Object el = children.get(i);
		if (!set.contains(el))
			createItem(el, widget, i);
	}

	// WORKAROUND
	if (widget == tableTree && oldCnt == 0 && tableTree.getItemCount() == 1) {
		tableTree.setRedraw(false);
		tableTree.setRedraw(true);
	}

	// get the child table tree items after our changes
	if (entry == rootEntry) 
		childItems = tableTree.getItems();
	else
		childItems = item.getItems();
	
	// update the child items
	// This ensures that the children are in the correct order
	// are showing the correct values.
	for (int i = 0; i < newSize; i++) {
		Object el = children.get(i);
		if (el instanceof IPropertySheetEntry)
			updateEntry((IPropertySheetEntry)el, childItems[i]);
		else {
			updateCategory((PropertySheetCategory)el, childItems[i]);
			updateChildrenOf((PropertySheetCategory)el, childItems[i]);
		}
	}
}
/**
 * Update the given entry (but not its children or parent)
 *
 * @param node the entry we will update
 * @param item the tree item for the given entry
 */
private void updateEntry(IPropertySheetEntry entry, TableTreeItem item) {
	// ensure that backpointer is correct
	item.setData(entry);
	
	// update the name and value columns
	item.setText(0, entry.getDisplayName());
	item.setText(1, entry.getValueAsString());
	Image image = entry.getImage();
	if (image != null)
		item.setImage(1, image);

	// update the "+" icon	
	updatePlus(entry, item);
}
/**
 * Updates the "+"/"-" icon of the tree item from the given entry
 * or category.
 *
 * @parem node the entry or category
 * @param item the table tree item being updated
 */
private void updatePlus(Object node, TableTreeItem item) {
	// cast the entry or category	
	IPropertySheetEntry entry =  null;
	PropertySheetCategory category = null;
	if (node instanceof IPropertySheetEntry)
		entry = (IPropertySheetEntry)node;
	else
		category = (PropertySheetCategory)node;

	boolean hasPlus = item.getItemCount() > 0;
	boolean needsPlus = category != null || entry.hasChildEntries();
	boolean removeAll = false;
	boolean addDummy = false;

	if (hasPlus != needsPlus) {
		if (needsPlus) {
			addDummy = true;
		} else {
			removeAll = true;
		}
	}
	if (removeAll) {
		// remove all children
		TableTreeItem[] items = item.getItems();
		for (int i = 0; i < items.length; i++) {
			removeItem(items[i]);
		}
	}

	if (addDummy) {
		new TableTreeItem(item, SWT.NULL); // append a dummy to create the plus sign
	}
}
}
