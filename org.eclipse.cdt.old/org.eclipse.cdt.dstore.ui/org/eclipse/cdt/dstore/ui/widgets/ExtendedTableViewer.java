package com.ibm.dstore.ui.widgets;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;

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

public class ExtendedTableViewer extends TableViewer 
    implements ISelected, Listener, IDataElementViewer
{
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
    
    public ExtendedTableViewer(ObjectWindow parent, Table table, ViewToolBar toolBar)
    {
	super(table);
	
	_parent = parent;
	
	_property = null;
	_viewFilter = new ViewFilter();
	addFilter(_viewFilter);
	 
	setContentProvider(new TableContentProvider(toolBar));     
	_isShowing = false;
	_isWorking = false;
	
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
	DataElement parent = (DataElement)ev.getParent();

	if ((parent == _selected) || 
	    (parent == _currentInput) || 
	    (parent == _expanded) ||
	    ((_currentInput != null) && (_currentInput.contains(parent, _property)))
	    )
	    {
		if ((getTable() != null) && !getTable().isDisposed())
		    return true;
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
			Object data = child.getData();
			if (data == res)
			    {
				return child;
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
			synchronized(parent)
			    {
				try
			    {
				getTable().setRedraw(false);
				remove(parent);
				getTable().setRedraw(true);				      
			    }
				catch (Exception e)
				    {
					e.printStackTrace();
					System.out.println(e);
				    }
			    }
		    }
	    }
	else if (_currentInput == null)
	    {
		_currentInput = null;
		setInput(null);
	    }
	else if (_currentInput == parent)
	    {
		internalRefresh(parent);
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

    private void internalRefresh(DataElement parent)
    {
	try
	    {
		Table table = getTable();
		
		// remove those that are gone		
		ArrayList associated = parent.getAssociated(_property);

		TableItem[] children = table.getItems();
		ArrayList toRemove = new ArrayList();
		for (int i = 0; i < children.length; i++) 
		    {
			TableItem item = children[i];
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
		

		table.setRedraw(false);
		
		for (int i = 0; i < toRemove.size(); i++)
		    {
			TableItem removee = (TableItem)toRemove.get(i);
			removee.dispose();
			removee = null;
		    }
				
		// add new or modified
		for (int i = 0; i < associated.size(); i++)
		    {
			DataElement child = (DataElement)associated.get(i);			
			synchronized(child)
			    {
				if (_viewFilter.select(this, child, null))
				    {
					
					Item item = findItemFor(table, child);
					if (item != null)
					    {
						updateItem(item, child);
					    }
					else
					    {
						item = newItem(table, SWT.NONE, i);
						updateItem(item, child);
					    }
				    }
			    }		
		    }
		table.setRedraw(true);

	    }
	catch (Exception e)
	    {
		System.out.println(e);
	    }				
    }
    
    protected Item newItem(Widget parent, int flags, int ix)  
    {
	if (parent instanceof Table)
	    {
		return new TableItem((Table) parent, flags);
	    }

	return null;
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
		return;
	    }

	boolean selectionListening = _listener.isEnabled();
	if (selectionListening)
	    {
		if (object == null)
		    {
			_currentInput = null;
			getTable().setRedraw(false);
			getTable().removeAll();
			getTable().setRedraw(true);
		    } 
		else if (_currentInput != object)
		    {
			_currentInput = (DataElement)object;

			super.inputChanged(object, oldInput);
			_isShowing = true;
		    }
	    }
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
				synchronized(table)
				    {
					try
					    {
						table.setRedraw(false);
						internalRefresh(parent);
						table.setRedraw(true);

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
			    synchronized (table)
				{
				    try
					{
					    table.setRedraw(false);
					    internalRefresh(_currentInput);
					    table.setRedraw(true);
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
    }


    public void setSelected(DataElement selected)
    { 
	_selected = selected;
    }
    
    public DataElement getSelected()
    {
	if ((_selected != null) && _selected.isDeleted())
	    {
		//		_selected = findElement(_selected);
	    }

	return _selected;
    }

    public Object getInput()
    {
	if ((_currentInput != null) && _currentInput.isDeleted())
	    {
		//		_currentInput = findElement(_currentInput);

	    }
	
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
    TableContentProvider provider = (TableContentProvider)getContentProvider();

    if (property != provider.getProperty())
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

    protected void handleDoubleSelect(SelectionEvent event) 
    {  
	if (_openEditorAction == null)
	    {
		_openEditorAction = new OpenEditorAction(_selected);
	    }
	
	if (_selected != null)
	    {
		DataElement type = _selected.getDescriptor();
		boolean isContainer = false;
		if (type != null)
		    {
			ArrayList contents = type.getAssociated("contents");
			for (int i = 0; (i < contents.size()) && !isContainer; i++)
			    {
				DataElement contained = (DataElement)contents.get(i);
				if (contained.getType().equals(DE.T_OBJECT_DESCRIPTOR))
				    {
					isContainer = true;
				    }		    
			    }
		    }
		
		if (isContainer)
		    {
			_selected.expandChildren();
			_parent.setInput(_selected);
		    }

		_openEditorAction.setSelected(_selected);
		_openEditorAction.run();
	    }
    }
  
    public void refreshView(DataElement relation, DataElement filter)
    {
	setFilter(filter);
	setProperty(relation);
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
}
