package com.ibm.dstore.hosts.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.hosts.*;

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.ui.widgets.TableContentProvider;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.core.model.*;

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

public class OutputViewer extends TableViewer  
    implements ISelected, ISelectionChangedListener, IDomainListener, IMenuListener
{
  public class CancelAction extends Action
  {      
    public CancelAction(String name, ImageDescriptor image)
    {
      super(name, image);
      
    }

      public void run()
      {
	  DataElement input = getCurrentInput();
	  if (input != null)
	      {
		  DataElement command = input.getParent();
		  cancel(command);
	      }
      }
      
      public void cancel(DataElement command)
      {
	  DataStore dataStore = command.getDataStore();
	  DataElement commandDescriptor = dataStore.find(dataStore.getDescriptorRoot(), DE.A_NAME, "Cancel");
	  if (commandDescriptor != null)
	      {	
		  dataStore.command(commandDescriptor, command, false, true);
	      }
      }
  }

  public class HistoryAction extends Action
  {
    private DataElement _status;
    private int         _increment;

    public HistoryAction(String name, ImageDescriptor image, int increment)
    {
      super(name, image);
      _increment = increment;
    }

      public void run()
    {
      DataElement status = _currentInput;
      if (status != null)
      {
        DataElement command = status.getParent();

        DataStore dataStore = command.getDataStore();
        DataElement logRoot = dataStore.getLogRoot();

        ArrayList commands = logRoot.getNestedData();
        int thisIndex = commands.indexOf(command);

        DataElement newStatus = null;
        int newIndex = thisIndex + _increment;
        boolean found = false;
        while (!found && (newIndex > -1) && (newIndex < commands.size()))
        {
          DataElement newCommand = (DataElement)commands.get(newIndex);	
	  String commandName = newCommand.getName();
	  if (commandName.equals("C_COMMAND") ||
	      commandName.equals("C_SEARCH") ||
	      commandName.equals("C_SEARCH_REGEX"))
	    {	
	      newStatus  = newCommand.get(newCommand.getNestedSize() - 1);
	      if (newStatus != null)
		{	
		  found = (newStatus.getNestedSize() > 1);
		}	
	    }
	  newIndex += _increment;
	}
	
        if (found && newStatus != null)
        {
          setInput(newStatus);
        }
      }
    }
  }

    protected DataElement           _selected;
    protected DataElement           _currentInput;
    private int                   _maxWidth;
    private int                   _charWidth;
    private OpenEditorAction      _openEditorAction;
    private OpenPerspectiveAction _openPerspectiveAction;
    private MenuHandler           _menuHandler;
    
    protected HostsPlugin           _plugin;

  public OutputViewer(Table parent)
      {
	super(parent);

        _plugin = HostsPlugin.getPlugin();
        setContentProvider(new TableContentProvider());
	setLabelProvider(new DataElementLabelProvider(_plugin.getImageRegistry()));
       	addSelectionChangedListener(this);

	_menuHandler = new MenuHandler(_plugin.getActionLoader());

	_maxWidth = 100;	

	Table table = getTable();

	MenuManager menuMgr = new MenuManager("#PopupMenu");
	menuMgr.setRemoveAllWhenShown(true);
	menuMgr.addMenuListener(this);
	Menu menu = menuMgr.createContextMenu(table);
	table.setMenu(menu);

	Display display = table.getDisplay();
	table.setBackground(new Color(display, 255, 255, 255));

      }

  protected void handleDoubleSelect(SelectionEvent event)
  {
    if (_openEditorAction == null)
      {
	_openEditorAction = new OpenEditorAction(_selected);
      }

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
	IElement input = (IElement)sel.getFirstElement();

	if (input instanceof DataElement)
	  setInput((DataElement)input);
      }

  public void setInput(DataElement input)
  {
    super.setInput((IElement)input);
    if (input != null)
      {
	  _currentInput = input;
	  DataStore dataStore = _currentInput.getDataStore();	
	  
	  if (_currentInput.getType().equals("status"))
	      {
		  DataElement commandInstance = _currentInput.getParent();
		  String commandValue = commandInstance.getAttribute(DE.A_VALUE);
	
		  TableColumn column = getTable().getColumn(0);
		  column.setText(commandValue);	
	      }
	  else
	      {
		  String value = _currentInput.getAttribute(DE.A_VALUE);
	
		  TableColumn column = getTable().getColumn(0);
		  column.setText(value);			  
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
        //***fillContextMenuHelper(menu, selected);
	_menuHandler.fillContextMenu(menu, _currentInput, selected);
      }


  public void fillContextMenuHelper(IMenuManager menu, DataElement object)
      {
	if (object != null)
	  {    	
	      if (_openPerspectiveAction == null)
		  {
		      _openPerspectiveAction = new OpenPerspectiveAction(_plugin.getLocalizedString("Open Perspective On"));
		  }
	      _openPerspectiveAction.setSubject(object);
	      menu.add(_openPerspectiveAction);
	  } 
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
       DataElement parent = (DataElement)ev.getParent();
       if (parent == _currentInput)
	   {
	       return true;
	   }
       else
	   {
	       return false;
	   }
    }
    
    public void domainChanged(DomainEvent ev)
    {
        if (listeningTo(ev))
	    {
		DataElement parent = (DataElement)ev.getParent();
		ArrayList children = ev.getChildren();
		if (children != null)
		    {
			Table table = getTable();
			if (table != null && !table.isDisposed())
			    {
				table.setRedraw(false);	
				updateChildren(children);
				TableColumn column = table.getColumn(0);
				if (column.getWidth() < _maxWidth)
				    {
					column.setWidth(_maxWidth);		
				    }
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

  public void updateChildren(ArrayList children)
      {
        Table table = getTable();

        int index = table.getItemCount();
        for (int i = 0; i < children.size(); i++)
        {
          DataElement child = ((DataElement)children.get(i)).dereference();
          if ((child != null) && !child.isUpdated())
          {
            child.setUpdated(true);
	    if (doFindItem(child) == null)	
	      {		
		  TableItem newItem = (TableItem)newItem(table, SWT.NONE, index);
		  updateItem(newItem, child);
		  index++;

		/***
		int charLen = ((String)child.getElementProperty(DE.P_NAME)).length();		
		int itemWidth = charLen * _charWidth;
		
		if (_maxWidth < itemWidth) _maxWidth = itemWidth;		
		***/
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
    toolBarManager.add(new HistoryAction(_plugin.getLocalizedString("OutputViewer.back"), 
					 _plugin.getImageDescriptor("back.gif"), -1));
    toolBarManager.add(new HistoryAction(_plugin.getLocalizedString("OutputViewer.forward"), 
					 _plugin.getImageDescriptor("forward.gif"), 1));
    toolBarManager.add(new CancelAction(_plugin.getLocalizedString("OutputViewer.Cancel"), 
					_plugin.getImageDescriptor("cancel.gif")));
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
	table.setFont(new Font(display, data));    
    }

    public void setFont(Font font)
    {
	getTable().setFont(font);    
    }
}

