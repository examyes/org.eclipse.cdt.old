package org.eclipse.cdt.dstore.ui.widgets;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
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

public class DataElementTreeViewer extends TreeViewer 
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
    
    public DataElementTreeViewer(ObjectWindow parent, Tree tree)
    {
	super(tree);
	
	_parent = parent;
	
	_property = null;
	_viewFilter = new ViewFilter();
	_viewFilter.setEnableContents(true);
	addFilter(_viewFilter);
	
	tree.addListener(SWT.Expand, this);
	_isShowing = false;
	_isWorking = false;
	
	setVisibility(false);
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
	if (parent != null && _currentInput != null && !_currentInput.isDeleted())
	    {
		synchronized(_currentInput)
		    {
			if ((parent == _selected) || 
			    (parent == _currentInput) || 
			    (parent == _expanded) ||
			    (_currentInput.contains(parent, _property, 1)))
			    {
				if ((getTree() != null) && !getTree().isDisposed())
				    {
					return true;
				    }
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
	Tree tree = getTree();


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
					tree.setRedraw(false);
					internalRefresh(parent.getParent());					  
					tree.setRedraw(true);				      
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
					tree.setRedraw(false);				
					internalRefresh(parent);				
					tree.setRedraw(true);
				}
			catch (Exception e)
			    {
			    	e.printStackTrace();
			    	System.out.println(e);
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


    protected void doUpdateItem(Item item, Object element) 
    {
	// update icon and label
	ILabelProvider provider = (ILabelProvider) getLabelProvider();
	String text = provider.getText(element); 
	if (text != null && !text.equals(item.getText()))
	    {
		item.setText(text);
	    }

	Image image = provider.getImage(element);
	if (image != null) 
	    {
		item.setImage(image);
	    }
    }


    public void enable(boolean flag)
    {
	if (flag)
	    {	       
		//refresh();
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

    public void setVisibility(boolean flag)
    {
	Tree tree = getTree();
	if (tree != null)
	    tree.setVisible(flag);		
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

	boolean selectionListening = _listener.isEnabled();
	if (selectionListening)
	    {
		if (_currentInput != object)
		    {
			if (_currentInput == null)
			    {
				setVisibility(true);
			    }

			_currentInput = (DataElement)object;

			super.inputChanged(object, oldInput);
			_isShowing = true;
			_viewFilter.reset();
		    }
	    }
    }

    public void clearView()
    {
	Tree tree = getTree();
	if (tree != null)
	    {
		synchronized(tree)
		    {
			try
			    {
				tree.setRedraw(false);
				tree.removeAll();
				tree.setRedraw(true);	
			    }
			catch (Exception e)
			    {
				System.out.println(e);
			    }
		    }
	    }
    }
    
    public void resetView(DataElement parent)
    {
    	resetView(parent, true);	
    }
    
    public synchronized void resetView(DataElement parent, boolean refreshLabels)
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
			Tree tree = getTree();
			if (tree != null)
			    {
				synchronized(tree)
				    {
					try
					    {
						internalRefresh(parent, refreshLabels);

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
    
   public Tree getTree()
   {
   	  return super.getTree();
   } 


  public void resetView()
  {
    resetView(true);	
  }
  
  public synchronized void resetView(boolean refreshLabels)
    {
	if (_isShowing && _currentInput != null)
	    {
		boolean selectionListening = _listener.isEnabled();
		if (selectionListening)
		{

		    Tree tree = getTree();
		    if (tree != null)
			{
			    synchronized (tree)
				{
				    try
					{
					    internalRefresh(_currentInput, refreshLabels);
					    DataElement selected = getSelected();
					    select(selected);

					}
				    catch (Exception e)
					{
					    System.out.println(e);
					}
				}
			}
		}
	    }
    }

    public void internalRefresh(DataElement element)
    {
		super.internalRefresh(element, false);
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

    protected Widget internalExpand(Object element, boolean expand) 
    {	
	if (element == null)
	    return null;
	
	return findItem(element);
    }

    public void select(DataElement object)
    {
    	/*
	if (object != null && (_currentInput != null))
	    {
		String type = object.getType();
		String name = object.getName();
		//reveal(object);
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
				Tree tree = getTree();
				tree.setRedraw(false);
				tree.removeAll();
				_selected = null;
				tree.setRedraw(true);
				resetView();
			    }
		    }
	    }
	    */
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
	DataElementTreeContentProvider provider = (DataElementTreeContentProvider)getContentProvider();

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

    public void setContainable(boolean flag)
    {
	// sure
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
  
    public void refreshView(DataElement relation, DataElement filter)
    {
	setFilter(filter);
	setProperty(relation);	
	resetView();
    }

    public boolean isWorking()
    {
	return _isWorking;
    }

    public void dispose()
    {
	//***	getTree().dispose();
    }
    
    
  
}
