package com.ibm.dstore.ui.widgets;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.*;
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

public class ExtendedTreeViewer extends TreeViewer 
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
    
    public ExtendedTreeViewer(ObjectWindow parent, Tree tree, ViewToolBar toolBar)
    {
	super(tree);
	
	_parent = parent;
	
	_property = null;
	_viewFilter = new ViewFilter();
	addFilter(_viewFilter);
	
	setContentProvider(new TreeContentProvider(toolBar));     
	tree.addListener(SWT.Expand, this);
	_isShowing = false;
	_isWorking = false;
	
    }

    public void setListener(ObjectSelectionChangedListener listener)
    {
	_listener = listener;
        addSelectionChangedListener(listener);
    }

    public void removeListener(ObjectSelectionChangedListener listener)
    {
	_listener = null;
	removeSelectionChangedListener(listener);
    }
    
  public void setBackground(int r, int g, int b)
  {
    Tree tree = getTree();
   
    Display display = tree.getDisplay();
    tree.setBackground(new Color(display, r, g, b));    
  }

  public void setForeground(int r, int g, int b)
  {
    Tree tree = getTree();
   
    Display display = tree.getDisplay();
    tree.setForeground(new Color(display, r, g, b));    
  }

    public void setFont(FontData data)
    {
	Tree tree = getTree();
	
	Display display = tree.getDisplay();
	tree.setFont(new Font(display, data));    
    }

    public void setFocus()
    {
	getTree().setFocus();
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
        DataElement selected = (DataElement)e.item.getData();
        if (selected != null)
	    {
		switch (e.type)
		    {
		    case SWT.Expand:
			doExpand(selected);
			break;
		    default:
		    }
	    }
    }
    
    public String getSchemaPath()
    {
	String path = _parent.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH);
	
	return path; 
    }
    
    public Shell getShell()
    {
	if (!getControl().isDisposed())
	    return getControl().getShell();
	else
	    return null;
    }
    
    public boolean listeningTo(DomainEvent ev)
    {
	DataElement parent = (DataElement)ev.getParent();
	if (parent != null && _currentInput != null)
	    {
		if ((parent == _selected) || 
		    (parent == _currentInput) || 
		    (parent == _expanded) ||
		    (_currentInput.contains(parent, _property, 2)))
		    {
			if ((getTree() != null) && !getTree().isDisposed())
			    {
				return true;
			    }
		    }
	    }

	return false;
    }   

    private Item findItemFor(Widget widget, Object res)
    {
	Item[] items  = getChildren(widget);
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
			else
			    {
				Item result = findItemFor(child, res);
				if (result != null)
				    {
					return result;
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
			synchronized(parent)
			    {			
				try
				    {
					getTree().setRedraw(false);
					remove(parent);
					getTree().setRedraw(true);				      
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
	    }
	else		     
	    {
		synchronized(parent)
		    {
			
			try
			    {
				Tree tree = getTree();
				tree.setRedraw(false);

				/***/
				Item item = findItemFor(tree, parent);
				if (item != null)
				    {
					updateItem(item, parent);
					updateChildren(item, parent, parent.getAssociated(_property).toArray());
				    }
				/****/
				
				//internalRefresh(parent);					  
				getTree().setRedraw(true);
			    }
			catch (Exception e)
			    {
				//System.out.println(e);
				//setInput(_currentInput);
			    }
			
			
			if ((_expanded == parent))
			    {
				expandToLevel(parent, 1);
			    }
		    }
	    }
	
	_isWorking = false;
	if (selectionListening)
	    {
		_listener.enable(true); 
	    }
    }	

    
  protected Item newItem(Widget parent, int flags, int ix)  
      {
	  if (parent instanceof Tree)
	      {
		  return new TreeItem((Tree) parent, flags);
	      }
	  else
	      {
		  return new TreeItem((TreeItem) parent, flags);
	      }
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


    protected void inputChanged(Object object, Object oldInput)
    {
	boolean selectionListening = _listener.isEnabled();
	if (selectionListening)
	    {
		if (object == null)
		    {
			_currentInput = null;
			getTree().setRedraw(false);
			getTree().removeAll();
			getTree().setRedraw(true);
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
	Control tree = getTree();
	if (tree != null)
	    {
		synchronized(tree)
		    {
			try
			    {
				tree.setRedraw(false);
				getTree().removeAll();
				tree.setRedraw(true);	
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
			Control tree = getTree();
			if (tree != null)
			    {
				synchronized(tree)
				    {
					try
					    {
						tree.setRedraw(false);
						internalRefresh(parent);
						tree.setRedraw(true);

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
		    Control tree = getTree();
		    if (tree != null)
			{
			    synchronized (tree)
				{
				    try
					{
					    tree.setRedraw(false);
					    internalRefresh(_currentInput);
					    tree.setRedraw(true);
					    DataElement selected = getSelected();
					    select(selected);

					}
				    catch (Exception e)
					{
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
		//_selected = findElement(_selected);
	    }

	return _selected;
    }

    public Object getInput()
    {
	if ((_currentInput != null) && _currentInput.isDeleted())
	    {
		//_currentInput = findElement(_currentInput);

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

    public Widget findWidget(Widget parent, String type, String name)
    {
	Widget widget = null;
	Item[] items  = getChildren(parent);

	if (items != null) 
	    {
		for (int i= 0; i < items.length; i++) 
		    {
			Item child = items[i];
			DataElement element = (DataElement)child.getData();
			if ((element != null) && element.getType().equals(type))
			    {
				if (element.getName().equals(name))
				    {
					widget = child;
					return widget;
				    }
			    }
			
			widget = findWidget(child, type, name);
			if (widget != null)
			    {
				return widget;
			    }
		    }
	    }

	return widget;
    }

    public void select(DataElement object)
    {
	if (object != null && (_currentInput != null))
	    {
		String type = object.getType();
		String name = object.getName();
		//***reveal(object);
		Widget widget = findItem(object);		

		if (widget != null)
		    {
			ArrayList selection = new ArrayList();
			selection.add(widget);
			try
			    {
				setSelection(selection);
			    }
			catch (ArrayStoreException e)
			    {
				System.out.println(e);
				getTree().setRedraw(false);
				getTree().removeAll();
				_selected = null;
				getTree().setRedraw(true);
				resetView();
			    }
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
	resetView();
    }
  }

  public DataElement getFilter()
  {
    return _viewFilter.getType();
  }

  public void setProperty(DataElement property)
  {
    TreeContentProvider provider = (TreeContentProvider)getContentProvider();

    if (property != provider.getProperty())
    {      
      _property = property;
      provider.setProperty(property);
       
      resetView();
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
	
	_openEditorAction.setSelected(_selected);
	_openEditorAction.run();
    }
  
    public void refreshView(DataElement filter)
    {
	setFilter(filter);
    }

    public boolean isWorking()
    {
	return _isWorking;
    }
}
