package com.ibm.cpp.ui.internal.editor.contentoutliner;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.views.*;

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

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.dnd.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;


import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;


public class CppContentOutlinePage extends ContentOutlinePage implements IDomainListener, Listener, IMenuListener
{
    protected IFile input;
    protected DataElement _elementRoot;
    protected DataElementAdapter _adapter;
    protected DataElement _expanded;
    private MenuHandler         _menuHandler;

    private CppPlugin   _plugin = CppPlugin.getDefault();
    

    public CppContentOutlinePage(IFile input)
    {
	super();
	this.input = input;
	_adapter = (DataElementAdapter)input.getAdapter(DataElementAdapter.class);

	_menuHandler = new MenuHandler(new CppActionLoader());
    }
    
    public void createControl(Composite parent) 
    {
	super.createControl(parent);
	CppPlugin plugin = CppPlugin.getDefault();
	DataStore ds = plugin.getCurrentDataStore();

	TreeViewer treeViewer = getTreeViewer();
	treeViewer.setLabelProvider(new DataElementLabelProvider(plugin.getImageRegistry()));

	DataElementTreeContentProvider provider = new DataElementTreeContentProvider(); 
	provider.setProperty(ds.findDescriptor(DE.T_RELATION_DESCRIPTOR, "contents"));
	treeViewer.setContentProvider(provider);
		
	IAdaptable adp = getContentOutline(input);
	if (adp != null)
	    {
		setViewInput((DataElement)adp);
	    }

	treeViewer.getTree().addListener(SWT.Expand, this);
	ds.getDomainNotifier().addDomainListener(this);
	
	getControl().addKeyListener(
				    new KeyAdapter() 
					{
					    public void keyPressed(KeyEvent e) 
					    {
						handleKeyPressed(e);
					    }
					});
		

	// menu
	// add menu handling
        MenuManager menuMgr = new MenuManager("#PopupMenu");
	menuMgr.setRemoveAllWhenShown(true);
	menuMgr.addMenuListener(this);
        Menu menu = menuMgr.createContextMenu(treeViewer.getTree());
        treeViewer.getTree().setMenu(menu);
    }

    private void setViewInput(DataElement input)
    {
	TreeViewer treeViewer = getTreeViewer();
	Object cinput = treeViewer.getInput();
	if (cinput != input)
	    {
		treeViewer.setInput(input);
	    }
    }

    public void dispose()
    {
	CppPlugin plugin = CppPlugin.getDefault();
	DataStore ds = plugin.getCurrentDataStore();
	ds.getDomainNotifier().removeDomainListener(this);
    }
    
    
    protected IAdaptable getContentOutline(IAdaptable input)
    {
	if (_elementRoot == null || _elementRoot.isDeleted())
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
		setViewInput(_elementRoot);

		control.setRedraw(false);
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

	if (_elementRoot == null || _elementRoot.isDeleted())
	    {
		IFile fileInput = (IFile)input;
		IProject project = fileInput.getProject();
		if (project == null || !project.isOpen())
		    {
			dataStore.getDomainNotifier().removeDomainListener(this);
			return false;
		    }
		else
		    {
			_elementRoot = _adapter.getElementRoot((IFile)input);		
			if (_elementRoot != null)
			    {
				setViewInput(_elementRoot);
				
				getTreeViewer().internalRefresh(_elementRoot);
				
				return true;
			    }
		    }
	    }
	else
	    {	
		if (parent == _elementRoot || parent.contains((DataElement)_elementRoot))
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
	
	if (_elementRoot != null)
	    {	
		if (parent == _elementRoot || parent.contains((DataElement)_elementRoot))
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

  public void menuAboutToShow(IMenuManager menu) 
      {
	  IStructuredSelection es= (IStructuredSelection) getTreeViewer().getSelection();
	  if (es.size() > 1)
	      {
		  _menuHandler.multiFillContextMenu(menu, es);
	      }
	  else
	      {
		  DataElement selected = (DataElement)es.getFirstElement();
		  if (_elementRoot != null )
		      {
			  _menuHandler.fillContextMenu(menu, (DataElement)_elementRoot, selected);
		      }
	      }
      }

    private void handleKeyPressed(KeyEvent event) 
    {
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


