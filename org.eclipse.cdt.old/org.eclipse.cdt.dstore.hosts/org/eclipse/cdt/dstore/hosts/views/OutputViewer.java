package org.eclipse.cdt.dstore.hosts.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.hosts.*;

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.hosts.actions.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.window.*;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class OutputViewer
	extends TableViewer
	implements ISelected, ISelectionChangedListener, IDomainListener, IMenuListener
{

	protected DataElement _selected;
	protected DataElement _currentInput;
	private int _maxWidth;
	private int _charWidth;
	private OpenEditorAction _openEditorAction;
	private OpenPerspectiveAction _openPerspectiveAction;
	private MenuHandler _menuHandler;

	protected HostsPlugin _plugin;

	private static int MAX_BUFFER = 1000;

	public OutputViewer(Table parent, IActionLoader loader)
	{
		super(parent);

		_plugin = HostsPlugin.getPlugin(); 
		setContentProvider(new DataElementTableContentProvider());

		if (loader == null)
			{
			loader = _plugin.getActionLoader();
		}

		setLabelProvider(
			new DataElementLabelProvider(_plugin.getImageRegistry(), loader));
		addSelectionChangedListener(this);

		_menuHandler = new MenuHandler(_plugin.getActionLoader());

		_maxWidth = 200;
		_charWidth = 8;

		Table table = getTable();

		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this);
		Menu menu = menuMgr.createContextMenu(table);
		table.setMenu(menu);

		Display display = table.getDisplay();
		table.setBackground(new Color(display, 255, 255, 255));

		table.setVisible(false);
	}

	protected void handleDoubleSelect(SelectionEvent event)
	{
		if (_openEditorAction == null)
			{
			_openEditorAction = new OpenEditorAction(_selected);
		}

		DataElement type = _selected.getDescriptor();
		boolean isContainer = false;

		if (isContainer)
			{
			_selected.expandChildren();
			setInput(_selected);
		}

		_openEditorAction.setSelected(_selected);
		_openEditorAction.run();
	}

	public void handleLinkEvent(SelectionChangedEvent event)
	{
		IStructuredSelection sel = (IStructuredSelection) event.getSelection();
		if (sel.isEmpty())
			return;
		IElement input = (IElement) sel.getFirstElement();

		if (input instanceof DataElement)
			setInput((DataElement) input);
	}

	private void clearInput()
	{
		Table table = getTable();
		table.setRedraw(false);
		table.removeAll();
		table.setRedraw(true);

		TableColumn column = table.getColumn(0);
		column.setText("");
		table.setVisible(false);
	}

	public void clear()
	{
		_currentInput = null;
		getTable().setVisible(false);
	}

	public void setInput(DataElement input)
	{
		super.setInput((IElement) input);
		if (input != null)
			{
			if (_currentInput == null)
				{
				getTable().setVisible(true);
			}
			_currentInput = input;

			DataStore dataStore = _currentInput.getDataStore();

			if (_currentInput.getType().equals("status"))
				{
				DataElement commandInstance = _currentInput.getParent();
				if (commandInstance != null)
				    {
					String commandValue = commandInstance.getAttribute(DE.A_VALUE);
					
					TableColumn column = getTable().getColumn(0);
					column.setText(commandValue);
				    }	
			}
			else
				{
				String value = _currentInput.getAttribute(DE.A_VALUE);

				TableColumn column = getTable().getColumn(0);
				column.setText(value);
			}

			Table table = getTable();
			if (table != null)
				{
				table.setRedraw(false);
				table.removeAll();
				updateChildren();
				table.setRedraw(true);
			}
		}
	}

	public void menuAboutToShow(IMenuManager menu)
	{
		menu.removeAll();

		fillContextMenu(menu);
	}

	public void fillContextMenu(IMenuManager menu)
	{
		DataElement selected = ConvertUtility.convert(getSelection());
		_menuHandler.fillContextMenu(menu, _currentInput, selected);
	}

	public DataElement getCurrentInput()
	{
		return _currentInput;
	}

	public void setSelected(DataElement selected)
	{
		_selected = selected;
	}

	public DataElement getSelected()
	{
		return _selected;
	}

	public boolean listeningTo(DomainEvent ev)
	{
		if (_currentInput == null || _currentInput.isDeleted())
			{
			_currentInput = null;
			return false;
		}
		else
			{
			DataElement parent = (DataElement) ev.getParent();
			if (parent == _currentInput)
				{
				return true;
			}
			else
				{
				return false;
			}
		}
	}

	public void domainChanged(DomainEvent ev)
	{
		DataElement parent = (DataElement) ev.getParent();
		Table table = getTable();
		if (table != null && !table.isDisposed())
			{
			// we should only update periodically	
			int ecount = ev.getChildrenCount();
			int tcount = table.getItemCount();
			
	
			{
				updateChildren();

				TableColumn column = table.getColumn(0);

				if (column.getWidth() < _maxWidth)
				{
					table.setRedraw(false);
					column.setWidth(_maxWidth);
					table.setRedraw(true);
				}
			}
		}

	}

	public Shell getShell()
	{
		if (!getControl().isDisposed())
			return getControl().getShell();
		else
			return null;
	}

	public synchronized void updateChildren()
	{
		Table table = getTable();
		table.setRedraw(false);
		int index = table.getItemCount();
		if (index > MAX_BUFFER)
			{
			clearFirstItems(index - (MAX_BUFFER / 2));

		}

		table.setRedraw(true);

		index = table.getItemCount();

		TableItem latestItem = null;
		ArrayList children = _currentInput.getAssociated("contents");
		for (int i = index; i < children.size(); i++)
			{
			DataElement child = (DataElement) children.get(i);
			if (child != null && child.isReference())
				{
				child = child.dereference();
			}
			if (child != null)
				{
				child.setUpdated(true);
				//if (doFindItem(child) == null)
					{

					TableItem newItem = (TableItem) newItem(table, SWT.NONE, index);
					updateItem(newItem, child);
					index++;

					int charLen = child.getName().length();
					int itemWidth = charLen * _charWidth;

					if (_maxWidth < itemWidth)
						_maxWidth = itemWidth;

					latestItem = newItem;
				}
			}
		}

		if (latestItem != null)
			{
			table.showItem(latestItem);
		}

	}

	private void clearFirstItems(int items)
	{
		Table table = getTable();
		synchronized (table)
		{
			int count = table.getItemCount();
			table.remove(0, items);
			for (int i = 0; i < items; i++)
				{
				DataElement item = _currentInput.get(i);
				ArrayList nestedData = _currentInput.getNestedData();
				synchronized (nestedData)
				{
					nestedData.remove(item);
				}
			}
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

	public void fillLocalToolBar(IToolBarManager toolBarManager)
	{
	}

	public void selectionChanged(SelectionChangedEvent e)
	{
		DataElement selected = ConvertUtility.convert(e);
		if (selected != null)
			{
			setSelected(selected);
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

		_charWidth = data.getHeight();
		setFont(new Font(display, data));
	}

	public void setFont(Font font)
	{
		getTable().setFont(font);
	}

	public void enableActions()
	{
	}

	public void dispose()
	{
		Table table = getTable();
		if (table != null)
			{
			table.dispose();
		}
	}

}
