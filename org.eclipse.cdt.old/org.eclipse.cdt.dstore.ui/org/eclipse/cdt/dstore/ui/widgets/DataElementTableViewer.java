package org.eclipse.cdt.dstore.ui.widgets;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;

import java.util.*;

import org.eclipse.ui.part.*;
import org.eclipse.ui.*;
import org.eclipse.core.resources.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*; 

import org.eclipse.swt.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;

public class DataElementTableViewer extends TableViewer 
    implements ISelected, Listener, IDataElementViewer, SelectionListener, ControlListener
{
    private class DelayedRefresher extends Thread
    {
	private ArrayList _elements;
	private Shell _shell;
	public DelayedRefresher(ArrayList elements)
	{
	    _shell = _currentInput.getDataStore().getDomainNotifier().findShell();
	    _elements = elements;
	}

	public void run()
	{
	    try
		{
		    Thread.currentThread().sleep(1000);
		}
	    catch (InterruptedException e)
		{
		    System.out.println(e);
		}
	    
	    if (_shell != null)
		{
		    Display d= _shell.getDisplay();
		    d.asyncExec(new Runnable()
			{
			    public void run()
			    {
				System.out.println("updating...");
			       	//updateItems(getTable(), _elements, new ArrayList(0));
			    }
			});
		}

	    _refresher = null;

	}
	
    }



    private   ViewFilter   _viewFilter;
    
    private   DataElement  _currentDescriptor;
    private   DataElement  _currentInput;
    
    private   ObjectWindow _parent; 
    
    private   DataElement  _selected;
    private   DataElement  _expanded;    
    private   DataElement  _property;
    
    private   IOpenAction  _openEditorAction;
    
    private boolean _isShowing;
    private boolean _isWorking;

    private ObjectSelectionChangedListener _listener;

    private DelayedRefresher _refresher = null;
    private boolean _isContainable = false;
    
    private TableLayout _layout;
    private ArrayList   _attributeColumns;

    public DataElementTableViewer(ObjectWindow parent, Table table)
    {
	super(table);
	
	_layout = new TableLayout();

	computeLayout();

	_parent = parent;
	
	_property = null;
	_viewFilter = new ViewFilter();
	_viewFilter.setEnableContents(false);
	_viewFilter.setDepth(1);
	addFilter(_viewFilter);
	 
	setContentProvider(new DataElementTableContentProvider());     
	_isShowing = false;
	_isWorking = false;
	
	_attributeColumns = new ArrayList();

	setVisibility(false);

    }

    /**
     * Sent when the location (x, y) of a control changes relative
     * to its parent (or relative to the display, for <code>Shell</code>s).
     *
     * @param e an event containing information about the move
     */
    public void controlMoved(ControlEvent e)
    {
    }
    
    /**
     * Sent when the size (width, height) of a control changes.
     *
     * @param e an event containing information about the resize
     */
    public void controlResized(ControlEvent e)
    {	
	computeLayout();
    }
    
    
    public void setFocus()
    {
	getTable().setFocus();
    }
    
    public void setListener(ObjectSelectionChangedListener listener)
    {
	_listener = listener;
        addSelectionChangedListener(listener);
    }
    
    public void removeListener(ObjectSelectionChangedListener listener)
    {
	_listener = null;
	if (listener != null)
	    {
		removeSelectionChangedListener(listener);
	    }
    }
    
    public void setBackground(int r, int g, int b)
    {
	Table table = getTable();
	
	Display display = table.getDisplay();
	table.setBackground(new Color(display, r, g, b));    
    }
    
    public void setForeground(int r, int g, int b)
    {
	Table table = getTable();
	
	Display display = table.getDisplay();
	table.setForeground(new Color(display, r, g, b));    
    }
    
    public void setFont(FontData data)
    {
	Table table = getTable();
	
	Display display = table.getDisplay();
	table.setFont(new Font(display, data));    
    }
    
    public void setShowing(boolean flag)
    {
	_isShowing = flag;
    }
    
    public boolean isShowing()
    {
	return _isShowing;
    }
    
    public void handleEvent(Event e)
    {
        Widget widget = e.widget;        
    }
    
    public String getSchemaPath()
    {
	String path = _parent.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH);
	
	return path; 
    }
    
    public Shell getShell()
    {
	if (!getControl().isDisposed())
	    {
		try
		    {
			return getControl().getShell();
		    }
		catch (Exception e)
		    {
		    }
	    }
	
	return null;
    }
    
    public boolean listeningTo(DomainEvent ev)
    {
    	if (!getTable().isDisposed())
    	{
	DataElement parent = (DataElement)ev.getParent();
	
	if ((parent == _selected) || 
	    (parent == _currentInput) || 
	    (parent == _expanded) ||
	    ((_currentInput != null) && (_currentInput.contains(parent, _property)))
	    )
	    {
		return true;
	    }
    	}
	return false;   
    }
    
    private Item findItemFor(Table widget, Object res)
    {
	Item[] items  = widget.getItems();
	if (items != null) 
	    {
		for (int i= 0; i < items.length; i++) 
		    {
			Item child = items[i];
			if (!child.isDisposed()) 
			    {
				Object data = child.getData();
				if (data == res)
				    {
					return child;
				    }
			    }
		    }
	    }
	
	return null;
    }
    
    
    public void domainChanged(DomainEvent ev)
    {
	_isWorking = true;
	
	boolean selectionListening = _listener.isEnabled();
	if (selectionListening)
	    {
		_listener.enable(false);
	    }
	
	DataElement parent = (DataElement)ev.getParent();   
	if (parent.isDeleted())
	    {
		if (parent == _currentInput)
		    {
			clearView();
		    }
		else
		    {
			setSelectionToWidget((ISelection)null, false);
			internalRefresh(_currentInput);
		    }
	    }
	else if (_currentInput == null)
	    {
		_currentInput = null;
		setInput(null);
	    }
	else if (_currentInput == parent)
	    {
	    	getTable().setRedraw(false);
		internalRefresh(parent);
		getTable().setRedraw(true);
	    }
	else		     
	    {
		synchronized(parent)
		    {
			
			try
			    {
				Table table = getTable();
				table.setRedraw(false);
				Item item = findItemFor(table, parent);
				if (item != null)
				    {
					updateItem(item, parent);
				    }
				table.setRedraw(true);
			    }
			catch (Exception e)
			    {
				System.out.println(e);
			    }			
			
		    }
	    }
	
	_isWorking = false;
	if (selectionListening)
	    {
		_listener.enable(true); 
	    }
    }	
    
    public void enable(boolean flag)
    {
	if (flag)
	    {
	//	internalRefresh(_currentInput);
	    }
    }

    /*
    private void internalRefresh(DataElement parent)
    {
	try
	    {
		DataElementTableContentProvider contentProvider = (DataElementTableContentProvider)getContentProvider();
		
		Table table = getTable();
		    {					
			ArrayList associated = contentProvider.getList(_currentInput);

			
			// remove those that are gone	
			TableItem[] items = table.getItems();
			ArrayList toRemove = new ArrayList();
			for (int i = 0; i < items.length; i++) 
			    {
				TableItem item = items[i];
				if (item != null)
				    {
					DataElement data = (DataElement)item.getData();
					if (!associated.contains(data))
					    {
						toRemove.add(item);
					    }
					else if (data == null || data.isDeleted() ||
						 !_viewFilter.select(this, data, null))
					    {
						toRemove.add(item);
					    }
				    }
			    }
			
			
			ScrollBar vBar = table.getVerticalBar();
			ScrollBar hBar = table.getHorizontalBar();

			if (_listener.isEnabled())
			    {
				vBar.setEnabled(false);
				hBar.setEnabled(false);
				table.setRedraw(false);
			    }
			
			updateItems(table, associated, toRemove);

			if (_listener.isEnabled())
			    {
				vBar.setEnabled(true);
				hBar.setEnabled(true);
				table.setRedraw(true);
			    }
			
			if (table.getItemCount() == 0) 
			    {
				table.removeAll();
			    }
		    }
	    }
	catch (Exception e)
	    {
		e.printStackTrace();
		System.out.println(e);
	    }				
    }    
 
    private synchronized void updateItems(Table table, ArrayList elements, ArrayList recycled)
    {
	int maxAdd = (recycled.size() > 100) ? recycled.size() : 100;
	int numAdded = 0;

	// first update existing ones
	for (int i = (elements.size() - 1); i >= 0; i--)
	    {
		DataElement child = (DataElement)elements.get(i);	
		TableItem item = (TableItem)findItemFor(table, child);
		if (item != null)
		    {
			if (!item.getText().equals(child.getValue()))
			    {
				updateItem(item, child);
			    }
			
			// remove from list
			elements.remove(child);
		    }
	    }



       	int totalItems = table.getItemCount();	  

	// then create new ones
	for (int i = (elements.size() - 1); (i >= 0) && (numAdded < maxAdd); i--)
	    {		
		DataElement child = (DataElement)elements.get(i);			
		TableItem item = null;
		if (_viewFilter.select(this, child, null))
		    { 							
			if (recycled.size() > 0)
			    {
				item = (TableItem)recycled.get(0);
				recycled.remove(0);
			    }
			else
			    {
 				item = (TableItem)newItem(table, SWT.NONE,totalItems +  numAdded);
				numAdded++;
			    }
			
			// remember element we are showing
			if (true) 
			    {
				associate(child, item);
			    } 
			else 
			    {
				item.setData(child);
				mapElement(child, item);	
			    }

			DataElementLabelProvider provider = (DataElementLabelProvider)getLabelProvider();
			// need to really do an update though to register properly
			item.setText(0, provider.getColumnText(child, 0));
			item.setImage(0, provider.getColumnImage(child, 0));
			ArrayList attributes = child.getAssociated("attributes");
			for (int a = 0; a < attributes.size(); a++)
			    {
				DataElement attribute = (DataElement)attributes.get(a);
				item.setText(a + 1, attribute.getName());
			    }

			//***updateItem(item, child); 			
		    }
		elements.remove(child);
	    }
  
	
	if (recycled.size() > 0)
	    {		
		table.setRedraw(false);
		
		for (int i = 0; i < recycled.size(); i++)
		    {
			TableItem item = (TableItem)recycled.get(i);
			item.dispose();
			item = null;
		    }
		
		table.setRedraw(true);
		
		recycled.clear();
	    }

	if (elements.size() > 0)
	    {	    
		if (_refresher == null)
		    {
			_refresher = new DelayedRefresher(elements);
			_refresher.start();
		    }
	    }	
    }
    */
    protected Item newItem(Widget parent, int flags, int ix)  
    {
	return new TableItem((Table)parent, flags, ix);
    }
    
    public void doExpand(DataElement obj)
    {
        DataElement root = obj.dereference();
        setSelected(obj);
        setExpanded(root);               
        root.expandChildren();
    }

    public void setExpanded(DataElement element)
    {
        _expanded = element;
      }

    public ObjectWindow getParent()
    {
	return _parent;
    }

    public void setInput(DataElement object)
    {
	inputChanged(object, _currentInput);
    }

    protected void inputChanged(Object object, Object oldInput)
    {
	if (object == null)
	    {
		_currentInput = null;
		setVisibility(false);
		return;
	    }
	
	if (getTable().isDisposed())
	    {
		return;	
	    }			    
	
	boolean selectionListening = _listener.isEnabled();
	if (selectionListening)
	    {
		if (_currentInput != object)
		    {
			if (_currentInput == null)
			    {
				setVisibility(true);				
			    }
			
			boolean newType = _currentInput == null || 
			    (_currentInput.getDescriptor() != ((DataElement)object).getDescriptor());
			_currentInput = (DataElement)object;
			
			if (newType)
			    {
				computeLayout();
			    }

			internalRefresh(_currentInput);
			_isShowing = true;
		    }
	    }
    }

    
    public void setVisibility(boolean flag)
    {
	Table table = getTable();
	if (table != null)
	    table.setVisible(flag);		
    }

    public void clearView()
    {
	Control table = getTable();
	if (table != null)
	    {
		synchronized(table)
		    {
			try
			    {
				table.setRedraw(false);
				getTable().removeAll();
				table.setRedraw(true);	
			    }
			catch (Exception e)
			    {
				System.out.println(e);
			    }
		    }
	    }
    }
    
    public synchronized void resetView(DataElement parent)
    {
	if (_isShowing && _currentInput != null && parent != null)
	    {
		if (parent.isDeleted())
		    {
			parent = null;
			resetView();
		    }
		else
		    {
			Control table = getTable();
			if (table != null)
			    {
				try
				    {
					internalRefresh(parent);
					
					DataElement selected = getSelected();
					select(selected);
				    }
				catch (Exception e)
				    {
					System.out.println(e);
					setInput(_currentInput);
				    }
			    }
		    }
	    }
    }

    public synchronized void resetView()
    {
	if (_isShowing && _currentInput != null)
	    {
		boolean selectionListening = _listener.isEnabled();
		if (selectionListening)
		{
		    Control table = getTable();
		    if (table != null)
			{
			    try
				{
				    internalRefresh(_currentInput);
				    DataElement selected = getSelected();
				    select(selected);
				    
				}
			    catch (Exception e)
				{
				    System.out.println(e);
				    setInput(_currentInput);
				}
			}
		}
	    }
    }


    public void setSelected(DataElement selected)
    { 
	_selected = selected;
    }
    
    public DataElement getSelected()
    {
	return _selected;
    }

    public Object getInput()
    {
	return _currentInput;
    }

    public DataElement findElement(DataElement oldElement)
    {
	DataElement result = oldElement.getDataStore().replaceDeleted(oldElement);
	if (result != null)
	    {
		return result;
	    }
	else
	    {
		return oldElement;
	    }
    }


    public void select(DataElement object)
    {
	if (object != null && (_currentInput != null))
	    {
		String type = object.getType();
		String name = object.getName();
		reveal(object);
		Widget widget = findItem(object);		

		if (widget != null)
		    {
			ArrayList selection = new ArrayList();
			selection.add(widget);
		    }
	    }
    }

    public void setFilter(DataElement type)
    {
	DataElement oldType = _viewFilter.getType();
	if ((oldType != type) &&
	    (type != null))
	    {
		_viewFilter.setType(type);
	    }
    }
    
    public DataElement getFilter()
    {
	return _viewFilter.getType();
    }
    
    public void setProperty(DataElement property)
    {
	DataElementTableContentProvider provider = (DataElementTableContentProvider)getContentProvider();
	
	if (property != null && 
	    provider != null && 
	    property != provider.getProperty())
	    {      
		_property = property;
		provider.setProperty(property);       
	    }	
    }
    
    public DataElement getProperty()
    {
        return _property;
      }

    public void setOpenAction(IOpenAction action)
    {
	_openEditorAction = action;
    }
    
    public void setContainable(boolean isContainable)
    {
	_isContainable = isContainable;
    }

    protected void handleDoubleSelect(SelectionEvent event) 
    {  
	if (_openEditorAction == null)
	    {
		_openEditorAction = new OpenEditorAction(_selected);
	    }
	
	if (_selected != null)
	    {
		DataElement type = _selected.getDescriptor();
		if (type != null && _isContainable)
		    {
			if (type.isOfType("Container Object", true))
			    {
				_selected.expandChildren();
				_parent.setInput(_selected);
			    }
		    }
		
		_openEditorAction.setSelected(_selected);
		_openEditorAction.run();
	    }
    }
  
    public void refreshView(DataElement relation, DataElement filter)
    {
	setFilter(filter);
	setProperty(relation);
	computeLayout();
	internalRefresh(_currentInput);
    }


    public boolean isWorking()
    {
	return _isWorking;
    }

    public void dispose()
    {
	getTable().dispose();
    }

    private void computeLayout()
    {
	if (_currentInput != null)
	    {
		_attributeColumns.clear();

		Table table = getTable();
		table.removeControlListener(this);
		
		// get column information
		
		// find first column type
		DataElement col1Type = _viewFilter.getType();
		TableColumn[] columns = table.getColumns();
		int numColumns = 0;
		if (columns != null)
		    numColumns = columns.length;

		if (col1Type != null)
		    {
			TableColumn tc = null;
			if (numColumns > 0 && columns[0] != null)
			    {
				tc = columns[0];
			    }
			else
			    {
				_layout.addColumnData(new ColumnWeightData(200));
				tc = new TableColumn(table, SWT.NONE, 0);				
				tc.addSelectionListener(this);
			    }

			DataElementLabelProvider provider = (DataElementLabelProvider)getLabelProvider();

			tc.setText(provider.getText(col1Type));

			_attributeColumns.add(col1Type);
			
			// find attributes of filter type
			ArrayList attributeTypes = col1Type.getAssociated("attributes");
			for (int i = 0; i < attributeTypes.size(); i++)
			    {
				DataElement attributeType = (DataElement)attributeTypes.get(i);
				
				TableColumn atc = null;
				if (numColumns > i+ 1)
				    {
					atc = (TableColumn)columns[i + 1];
				    }
				else
				    {
					_layout.addColumnData(new ColumnWeightData(100));
					
					int alignment = SWT.RIGHT;
					ArrayList attributeFormats = attributeType.getAssociated("attributes");
					if (attributeFormats.size() > 0)
					    {
						DataElement attributeFormat = (DataElement)attributeFormats.get(0);
						if (attributeFormat.getName().equals("String"))
						    {
							alignment = SWT.LEFT;
						    }
					    }

					atc = new TableColumn(table, alignment, i + 1);
					atc.addSelectionListener(this);
				    }
				
				_attributeColumns.add(attributeType);

				atc.setText(provider.getText(attributeType));
			    }

			while (attributeTypes.size() + 1 < numColumns)
			    {
				columns[numColumns - 1].dispose();
				numColumns--;
			    }


			// compute widths
			columns = table.getColumns();
			Rectangle clientA = table.getClientArea();
			int totalWidth = clientA.width;
			int averageWidth = totalWidth / columns.length;
			int firstWidth = averageWidth;
			if (averageWidth < 150)
			    {
				int difference = 150 - averageWidth;
				firstWidth = 150;
				averageWidth -= difference / columns.length;

				if (averageWidth < 50)
				    {
					averageWidth = 50;
				    }
			    }
			
			for (int i = columns.length - 1; i >= 0; i--)
			    {
				if (i == 0)
				    {
					columns[i].setWidth(firstWidth);
				    }
				else
				    {
					try
					    {
						columns[i].setWidth(averageWidth);
					    }
					catch (Exception e)
					    {
						e.printStackTrace();
					    }
				    }
			    }

			if (attributeTypes.size() == 0)
			    {
				table.setHeaderVisible(false);
			    }
			else
			    {
				table.setHeaderVisible(true);	
			    }
		    }
		else
		    {
			table.setHeaderVisible(false);	
		    }

		table.setLayout(_layout);
		table.addControlListener(this);		
	    }
    }

    public void widgetDefaultSelected(SelectionEvent e)
    {
	widgetSelected(e);
    }

    public void widgetSelected(SelectionEvent e)
    {
	Widget source = e.widget;
	
	TableColumn col = (TableColumn)source;
	int index = getTable().indexOf(col);

	DataElementSorter sorter = (DataElementSorter)getSorter();
	if (sorter == null)
	    {
		sorter = new DataElementSorter(DE.P_NAME);
	    }

	if (index > 0)
	    {
		sorter.setSortAttribute((DataElement)_attributeColumns.get(index));
	    }
	else
	    {
		sorter.setSortAttribute(null);		
	    }

	setSorter(sorter);
	internalRefresh(_currentInput);
    }
}
