package com.ibm.cpp.ui.internal.editor.contentoutliner;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.*;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.ui.resource.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.resources.*;
import org.eclipse.ui.views.bookmarkexplorer.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.*;

import org.eclipse.ui.*;
import org.eclipse.ui.views.contentoutline.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jface.viewers.*;


public class CppContentOutlinePage extends ContentOutlinePage implements IDomainListener, Listener
{
    protected IFile input;
    protected DataElement _dataStore;
    protected IAdaptable _elementRoot;
    protected DataElementAdapter _adapter;
    protected DataElement _expanded;
    private CppPlugin   _plugin = CppPlugin.getDefault();
    

    public CppContentOutlinePage(IFile input)
    {
	super();
	this.input = input;
	_adapter = (DataElementAdapter)input.getAdapter(DataElementAdapter.class);
    }
    
    public void createControl(Composite parent) 
    {
	super.createControl(parent);
	CppPlugin plugin = CppPlugin.getDefault();
	DataStore ds = plugin.getCurrentDataStore();
	getTreeViewer().setLabelProvider(new DataElementLabelProvider(plugin.getImageRegistry()));
	getTreeViewer().setContentProvider(new TreeContentProvider());
	
	getTreeViewer().setInput(getContentOutline(input));
	getTreeViewer().getTree().addListener(SWT.Expand, this);
	ds.getDomainNotifier().addDomainListener(this);
    }

    public void dispose()
    {
	CppPlugin plugin = CppPlugin.getDefault();
	DataStore ds = plugin.getCurrentDataStore();
	ds.getDomainNotifier().removeDomainListener(this);
    }
    
    
    protected IAdaptable getContentOutline(IAdaptable input)
    {
	if (_elementRoot == null)
	    {
		_elementRoot = _adapter.getContentOutline(input);
	    }
	return _elementRoot;
    }
    
    public void update(DataElement parent)
    {
	Control control = getTreeViewer().getControl();
	if (control.isDisposed())
	    {	
	    }
	else
	    {
		control.setRedraw(false);
		getTreeViewer().setInput(_elementRoot);
		getTreeViewer().internalRefresh(_elementRoot);
		control.setRedraw(true);
	    }
    }
    
    public void selectionChanged(SelectionChangedEvent event)
    {
	super.selectionChanged(event);
	
	DataElement selection = ConvertUtility.convert(event);
	if (selection != null)
	    {	
		selection.expandChildren();
		
		IWorkbench Workbench = _plugin.getWorkbench();
		IWorkbenchPage persp= Workbench.getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor = persp.getActiveEditor();
		
		Integer location = (Integer)(selection.getElementProperty(DE.P_SOURCE_LOCATION));
		if (location != null)
		    {
			int loc = location.intValue();
			
			if ((loc > 0) && (editor != null))
			    {	
				((com.ibm.cpp.ui.internal.editor.CppEditor)editor).gotoLine(loc);
			    }
		    }
	    }	
    }
    
    public boolean listeningTo(DomainEvent ev)
    {
	DataElement parent = (DataElement)ev.getParent();
	DataStore dataStore = parent.getDataStore();
	
	_elementRoot = _adapter.getElementRoot((IFile)input);	
	
	if (_elementRoot != null)
	    {	
		if (parent == _elementRoot ||
		    parent.contains((DataElement)_elementRoot))
		    {
			return true;
		    }
	    }

	return false;    
    }
    
    public void domainChanged(DomainEvent ev)
    {
	DataElement parent = (DataElement)ev.getParent();
	DataStore dataStore = parent.getDataStore();
	
	_elementRoot = _adapter.getElementRoot((IFile)input);	
	
	if (_elementRoot != null)
	    {	
		if (parent == _elementRoot ||
		    parent.contains((DataElement)_elementRoot))
		    {
			update(parent);	
			
			if (_expanded == parent || parent.contains(_expanded))
			    {
				getTreeViewer().expandToLevel(_expanded, 1);
			    }
		    }
	    }
    }

  public Shell getShell()
  {
      Control control = getControl();
      if (control != null && !control.isDisposed())
	  {
	      return control.getShell();
	  }

      return null;
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
	    _expanded = selected;
	    selected.expandChildren(true);
	    getTreeViewer().expandToLevel(_expanded, 1);
            break;
          default:
          }
        }
      }

}


