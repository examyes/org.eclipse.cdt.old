package org.eclipse.cdt.cpp.ui.internal.editor.contentoutliner;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.views.*;

import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.ui.resource.*;

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

import java.util.*;

public class CppContentOutlinePage
	extends ContentOutlinePage
	implements IDomainListener, Listener, IMenuListener, ICppProjectListener
{
	protected IFile _input;
	protected DataElement _elementRoot;
	protected DataElementAdapter _adapter;
	protected DataElement _expanded;
	private MenuHandler _menuHandler;

	private CppPlugin _plugin = CppPlugin.getDefault();
	private ModelInterface _api;

	public CppContentOutlinePage(IFile input)
	{
		super();
		this._input = input;
		_adapter =
			(DataElementAdapter) _input.getAdapter(DataElementAdapter.class);

		_menuHandler = new MenuHandler(new CppActionLoader());
	}

	public void createControl(Composite parent)
	{
		super.createControl(parent);
		CppPlugin plugin = CppPlugin.getDefault();
		DataStore ds = plugin.getCurrentDataStore();

		TreeViewer treeViewer = getTreeViewer();
		treeViewer.setLabelProvider(new DataElementLabelProvider(plugin.getImageRegistry(),
									CppActionLoader.getInstance()));

		DataElementTreeContentProvider provider = new DataElementTreeContentProvider();
		provider.setProperty(ds.getContentsRelation());
		treeViewer.setContentProvider(provider);

		IAdaptable adp = getContentOutline(_input);
		if (adp != null)
		{
			setViewInput((DataElement) adp);
		}

		treeViewer.getTree().addListener(SWT.Expand, this);
		ds.getDomainNotifier().addDomainListener(this);

		getControl().addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				handleKeyPressed(e);
			}
		});
		
		_api = plugin.getModelInterface();
		CppProjectNotifier notifier = _api.getProjectNotifier();
		notifier.addProjectListener(this);

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
			treeViewer.refresh(input);
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
			hackRedraw();
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
			IWorkbenchPage persp =
				Workbench.getActiveWorkbenchWindow().getActivePage();
			IEditorPart editor = persp.getActiveEditor();

			Integer location =
				(Integer) (selection.getElementProperty(DE.P_SOURCE_LOCATION));
			if (location != null)
			{
				int loc = location.intValue();

				if ((loc > 0) && (editor != null))
				{
					(
						(
							org
								.eclipse
								.cdt
								.cpp
								.ui
								.internal
								.editor
								.CppEditor) editor)
								.gotoLine(
						loc);
				}
			}
		}
	}

	public boolean listeningTo(DomainEvent ev)
	{
		DataElement parent = (DataElement) ev.getParent();
		if (_elementRoot != null 
			&& (parent == _elementRoot || _elementRoot.contains(parent)) 
			&& !_elementRoot.isDeleted())
		{
			return true;	
		}		
		else if (parent.getType().equals("status"))
		{
			if (parent.getName().equals("done"))
			{
				DataElement cmd = parent.getParent();
				if (cmd.getName().equals("C_PARSE"))
				{
					return canUpdate(parent);
				}		
			}				
		} 
		else if (parent.getType().equals("Parsed Files"))
		{
			return canUpdate(parent);
		}
		return false;
	}

	private boolean canUpdate(DataElement element)
	{
		DataStore dataStore = element.getDataStore();

			if (_elementRoot == null || _elementRoot.isDeleted())
			{
				IFile fileInput =  _input;
				IProject project = fileInput.getProject();
				if (project == null || !project.isOpen())
				{
					dataStore.getDomainNotifier().removeDomainListener(this);
					return false;
				} 
				else
				{
					DataElement newElementRoot = _adapter.getElementRoot(_input);
					if (newElementRoot != null && !newElementRoot.isDeleted())
					{
						if (_elementRoot == null)
						{
							_elementRoot = newElementRoot;
							setViewInput(_elementRoot);	
						
							return true;	
						}
						else if (_elementRoot == newElementRoot)
						{
							return false;						
						}
						else if (newElementRoot != null)
						{
							_elementRoot = newElementRoot;				
							return true;
						}
					}
				}
			} 	
			
			return false;
		
	}



	public void domainChanged(DomainEvent ev)
	{
		DataElement parent = (DataElement) ev.getParent();
		DataStore dataStore = parent.getDataStore();

		if (_elementRoot != null)
		{
			if (parent == _elementRoot || _elementRoot.contains(parent))
			{
				update(_elementRoot);

				if (_expanded == parent)
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
		IStructuredSelection es =
			(IStructuredSelection) getTreeViewer().getSelection();
		if (es.size() > 1)
		{
			_menuHandler.multiFillContextMenu(menu, es);
		} else
		{
			DataElement selected = (DataElement) es.getFirstElement();
			if (_elementRoot != null)
			{
				_menuHandler.fillContextMenu(
					menu,
					(DataElement) _elementRoot,
					selected);
			}
		}
	}

	private void handleKeyPressed(KeyEvent event)
	{
	}

	public void handleEvent(Event e)
	{
		Widget widget = e.widget;
		DataElement selected = (DataElement) e.item.getData();
		if (selected != null)
		{
			switch (e.type)
			{
				case SWT.Expand :
					_expanded = selected;
					selected.expandChildren(true);
					getTreeViewer().expandToLevel(_expanded, 1);
					break;
				default :
					}
		}
	}

	private void hackRedraw()
	{
		TreeViewer treeViewer = getTreeViewer();
		Tree tree = treeViewer.getTree();
					
		TreeItem[] items = tree.getItems();
		ArrayList newItems = _elementRoot.getAssociated(_elementRoot.getDataStore().getContentsRelation());
		if (newItems.size() > 0)
		{
		for (int i = 0; i < items.length; i++)
		{
			TreeItem item = items[i];
			Object data = item.getData();
			DataElement oldElement = (DataElement)data;
			
			if (newItems.size() > i)
			{
				DataElement newElement = (DataElement)newItems.get(i);	
				if (oldElement != newElement)
				{
					item.setData(newElement);
					DataElementLabelProvider provider = (DataElementLabelProvider)treeViewer.getLabelProvider();
					// need to really do an update though to register properly
					item.setText(provider.getText(newElement));
					item.setImage(provider.getImage(newElement));	
					treeViewer.refresh(newElement);	
				}	
			}
			else
			{
				if (oldElement.isDeleted())
				{
					treeViewer.remove(oldElement);
				}
			}
		}
		
		for (int j = items.length; j < newItems.size(); j++)
		{
			DataElement newElement = (DataElement)newItems.get(j);
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setData(newElement);
			
			DataElementLabelProvider provider = (DataElementLabelProvider)treeViewer.getLabelProvider();
			// need to really do an update though to register properly
			item.setText(provider.getText(newElement));
			item.setImage(provider.getImage(newElement));
			treeViewer.refresh(newElement);
			
		}
		} 
			
	}
   public void projectChanged(CppProjectEvent event)
    {
	int type = event.getType();
	IProject project = event.getProject();
	
	if (_input.getProject().equals(project))
	{
	switch (type)
	    {
	    case CppProjectEvent.OPEN:
		{
			_api.parse(_input, false);		
		}
		break;
	    case CppProjectEvent.CLOSE:
	    case CppProjectEvent.DELETE:
		{
	
		}
		break;
		
	    default:
		break;
	    }
    }
    }
        
}