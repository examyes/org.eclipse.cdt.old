package com.ibm.dstore.ui.widgets;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.DataStoreCorePlugin;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;

import java.util.*;
import java.io.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;
 
import org.eclipse.swt.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*; 
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;


public class ViewMenu implements IMenuListener
{
    private Composite   _toolBar;
    
    private ToolBar     _viewToolBar;
    private ToolItem   _relationLabel, _filterLabel;
    
    private ArrayList      _filterItems, _relationItems;
    
    private DataElement _filterSelected, _relationSelected;
    
    private String      _pluginPath;
    
    private MenuManager _menuMgr;
    
    private ViewToolBar _parent;
    
    private DataElement _input;
    private DataElement _inputDescriptor; 
    private DataStore   _dataStore;

    private boolean     _viewingRelation;
    
    private DataElement _defaultFilter;
    
    private String      _fixatedRelationType, _fixatedObjectType;
    private String      _relGif, _filGif;

    private boolean     _isEnabled;
    
    public class MenuSelectFilterAction extends Action
    {
    private DataElement _filter;
    public MenuSelectFilterAction(DataElement filter)
        {
          super(filter.getName());
          _filter = filter;

	  if (_filter == _filterSelected)
	      setChecked(true);
        }

    public MenuSelectFilterAction(DataElement filter, ImageDescriptor image)
        {
          super(filter.getName(), image);
          _filter = filter;

	  if (_filter == _filterSelected)
	      setChecked(true);
        }

    public void run()
        {
          setFilter(_filter);
	  updateVisibility();
        }   
  }

  public class MenuSelectRelationAction extends Action
  {
    private DataElement _relation;

    public MenuSelectRelationAction(DataElement relation)
        {
          super(relation.getName());
          _relation = relation;

	  if (_relation == _relationSelected)
	      setChecked(true);
        }

    public MenuSelectRelationAction(DataElement relation, ImageDescriptor image)
        {
          super(relation.getName(), image);
          _relation = relation;

	  if (_relation == _relationSelected)
	      setChecked(true);
        }

    public void run()
        {
          setRelation(_relation);
          getFilterItems();    

	  updateVisibility();
        }   
  }

  private class MenuLayout extends Layout 
  {	
    public MenuLayout()
        {
          super();
        }
    
    public Point computeSize(Composite c, int w, int h, boolean force) 
        {
          int width = c.getParent().getClientArea().width;
          return new Point(width, 22);
        }
    
    public void layout(Composite composite, boolean force) 
        {      
          Rectangle r= composite.getClientArea();
          int height = r.height;
          int width  = r.width;
          
          int viewHeight = height;
          int viewWidth = 80;

          int y1 = r.y;

          int iconSize = 15;
	  int menuWidth1 = 80;

          int menuWidth2 = menuWidth1;
 
	  int x4 = width - (menuWidth2 + 16);
          int x3 = x4 - iconSize;
          int x2 = x3 - menuWidth1;
          int x1 = x2 - iconSize;

	  _viewToolBar     .setBounds(x4, y1, width, viewHeight);
        }
  }

  public ViewMenu(ViewToolBar parent, Composite toolBar)
      {
        _parent = parent;
	_dataStore = parent.getDataStore();
        _toolBar = toolBar;
	_isEnabled = true;

        GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | 
                                         GridData.GRAB_HORIZONTAL);

	_pluginPath = parent.getPluginPath() + java.io.File.separator;
	_relGif  = _pluginPath 
	    + File.separator + "icons" + File.separator 
	    + "relation.gif";
	
	_filGif  = _pluginPath 
	    + File.separator + "icons" + File.separator 
	    + "filter.gif";
	

        _filterItems = new ArrayList();
        _relationItems = new ArrayList();


	DataStoreCorePlugin plugin = DataStoreCorePlugin.getInstance();

        // relation graphics
        _viewToolBar = new ToolBar(toolBar, SWT.FLAT);
        _relationLabel = new ToolItem(_viewToolBar, SWT.DROP_DOWN, 0);
	if (plugin != null)
	    {
		_relationLabel.setImage(plugin.getImage(_relGif));
	    }
	else
	    {
		_relationLabel.setText(_dataStore.getLocalizedString("model.contents"));
	    }

	_relationLabel.addSelectionListener(
                                     new SelectionAdapter()
				     {
                                       public void widgetSelected(SelectionEvent e) 
                                           {
                                             showRelationMenu();
                                           }
				     }
				   );
        _filterLabel = new ToolItem(_viewToolBar, SWT.DROP_DOWN, 1);
	if (plugin != null)
	    {
		_filterLabel.setImage(plugin.getImage(_filGif));
	    }
	else
	    {
		_filterLabel.setText(_dataStore.getLocalizedString("model.all"));
	    }

	_filterLabel.addSelectionListener(
                                          new SelectionAdapter()
                                          {
                                            public void widgetSelected(SelectionEvent e) 
                                                {
                                                  showFilterMenu();
                                                }
                                          }
                                          );

        toolBar.setLayout(new FillLayout());
	_viewToolBar.setLayout(new GridLayout());

        
        _menuMgr = new MenuManager("#ViewMenu");
	_menuMgr.setRemoveAllWhenShown(true);
	
	_menuMgr.addMenuListener(this);

      }
 
  public void showRelationMenu()
      {
        if (_relationItems.size() > 0)
        {
          Menu aMenu = _menuMgr.createContextMenu(_toolBar);
          
          _viewingRelation = true;
          Point topLeft = new Point(0, 0);
          topLeft = _viewToolBar.toDisplay(topLeft);
          aMenu.setLocation(topLeft.x, topLeft.y + 20);
          aMenu.setVisible(true);
        }
      } 

  public void showFilterMenu()
      {
        if (_filterItems.size() > 0)
        {
          Menu aMenu = _menuMgr.createContextMenu(_toolBar);
          
          _viewingRelation = false;
          Point topLeft = new Point(0, 0);
          topLeft = _viewToolBar.toDisplay(topLeft);
          aMenu.setLocation(topLeft.x, topLeft.y + 20);
          aMenu.setVisible(true);        
        }
      }

  public synchronized void setInput(DataElement object)
      {
        _input = object;
	_dataStore = _input.getDataStore();
	
	if ((_inputDescriptor == null) || (object.getDescriptor() == null) ||
	    (!object.getDescriptor().getName().equals(_inputDescriptor.getName())))
        {
          _inputDescriptor = object.getDescriptor();          
          getRelationItems();
          setDefaultRelation();          

	  getFilterItems();    
	  if (object.getDescriptor() != _inputDescriptor)
	      {
		  setDefaultFilter();
	      }

	  updateVisibility();
        }
      }

    public void updateVisibility()
    {
        boolean showRelation = _relationItems.size() > 1;
        boolean showFilter = _filterItems.size() > 1;
	if (!showFilter)
	    {
		setDefaultFilter();
	    }

        _toolBar.layout(true);
        _toolBar.redraw();

	if (showFilter)
	    {
		_parent.getViewer().refreshView(_relationSelected, _filterSelected);
	    }
    }

  public void fixateOnRelationType(String relationType)
      {
	  _fixatedRelationType = relationType;
      }

  public void fixateOnObjectType(String objectType)
      {
        _fixatedObjectType = objectType;
      }


  public DataElement getFilter()
      {
        return _filterSelected;
      }

  public DataElement getRelation()
      {
        return _relationSelected;
      }

  public void setDefaultRelation()
      {
        DataElement relation = null;
        if (_relationItems.size() > 0)
        {
	    relation = (DataElement)_relationItems.get(0);
        }

        setRelation(relation);
      }

  public void setDefaultFilter()
      {
        DataElement filter = null;
	getFilterItems();
	
        if (_filterItems.size() > 0)
        {
	    filter = (DataElement)_filterItems.get(0);
        }

        setFilter(filter);
      }

  public void setRelation(DataElement object)
      {
	  String property = _dataStore.getLocalizedString("model.contents");
	  if (object != null)
	      {
		  _relationSelected = object.dereference();
		  property = (String)object.getElementProperty(DE.P_NAME);
		  
		  _parent.getViewer().setProperty(object);
		  setDefaultFilter();
	      }
	  else 
	      {
		  _relationSelected = null;
		  _parent.getViewer().setProperty(null);
          
		  setDefaultFilter();
	      }

	  DataStoreCorePlugin plugin = DataStoreCorePlugin.getInstance();
	  if (plugin != null)
	      {
		  String imageStr = null;
		  if (_relationSelected != null)
		      {
			  imageStr = DataElementLabelProvider.getImageString(_relationSelected);
		      }
		  
		  if (imageStr == null)
		      {
			  imageStr = _relGif;
		      }
		  
		  Image image = plugin.getImage(imageStr);
		  if (image == null)
		      {
			  imageStr = _relGif;
			  image = plugin.getImage(imageStr);
		      }

		  _relationLabel.setEnabled(false);
		  _relationLabel.setImage(image);
		  _relationLabel.setToolTipText(property);
		  _relationLabel.setEnabled(true);
		  _viewToolBar.redraw();		  
	      }
	  else
	      {
		  _relationLabel.setText(property);
	      }
      }

  public void setFilter(DataElement object)
      {
        if (object == null)
        {
          if (_defaultFilter == null)
          {
            _defaultFilter = _input.getDataStore().findObjectDescriptor(_dataStore.getLocalizedString("model.all"));
          }
        }
        
	String filter = null;
        if (object != null)
	    {
		_filterSelected = object.dereference();
		filter = _filterSelected.getName();
	    }
        else
	    {
		_filterSelected = _defaultFilter;
	    }
	
	DataStoreCorePlugin plugin = DataStoreCorePlugin.getInstance();
	if (plugin != null)
	    {
		String imageStr = DataElementLabelProvider.getImageString(_filterSelected);
		
		if (!_filterLabel.isDisposed())
		    {
			_filterLabel.setEnabled(false);
		    }
		
		Image image = plugin.getImage(imageStr);
		if (image == null)
		    {
			imageStr = _filGif;
			image = plugin.getImage(imageStr);
		    }
		_filterLabel.setImage(image);
		_filterLabel.setToolTipText(filter);
		_filterLabel.setEnabled(true);
	    }
	else
	    {
		_filterLabel.setText(filter);
	    }
      }
    

  public void getRelationItems()
      {
        DataElement descriptor = _inputDescriptor;
	_relationItems.clear();
	if (descriptor != null)
	    {
		ArrayList items = descriptor.getDataStore().getRelationItems(descriptor, _fixatedObjectType);
		for (int i = 0; i < items.size(); i++)
		    {
			DataElement item = (DataElement)items.get(i);
			if (item != null)
			    {
				int depth = item.depth();
				if (depth > 0)
				    {
					if (!_relationItems.contains(item))
					    {
						_relationItems.add(item);
					    }
				    }
			    }
		    }
	    }
	  _relationItems = Sorter.sort(_relationItems);

      }


  public void getFilterItems()
      {
	  DataStore dataStore = _input.getDataStore();
	  DataElement descriptor = _inputDescriptor;
	  _filterItems.clear();
	  ArrayList items = dataStore.getFilterItems(descriptor, _fixatedRelationType, _relationSelected);
	  for (int i = 0; i < items.size(); i++)
	      {
		  DataElement item = (DataElement)items.get(i);
		  int depth = item.depth();
		  if (depth > 0)
		      {
			  if (!_filterItems.contains(item))
			      {
				  _filterItems.add(item);
			      }
		      }
	      }	
	  
	    DataElement allD = dataStore.findObjectDescriptor(_dataStore.getLocalizedString("model.all"));
	    if (!_filterItems.contains(allD))
		{
		    _filterItems.add(allD);
		}

	  _filterItems = Sorter.sort(_filterItems);
      }


  public void menuAboutToShow(IMenuManager menu) 
      {
	fillContextMenu(menu);
      }

  public void fillContextMenu(IMenuManager menu)
      {
        if (_viewingRelation)
        {
          fillRelationMenu(menu);
        }
        else 
        {
          fillFilterMenu(menu);
        }
      }

  
    public static class Sorter
    {
	public static ArrayList sort(ArrayList list)
	    {
		ArrayList sortedList = new ArrayList(list.size());
		while (list.size() > 0)
		    {
			DataElement first = findFirst(list);
			sortedList.add(first);
		    }

		return sortedList;
	    }


	private static DataElement findFirst(ArrayList list)
	    {
		DataElement result = null;
		for (int i = 0; i < list.size(); i++)
		    {
			DataElement item = (DataElement)list.get(i);
			int depth = item.depth();
			if ((result == null) || (depth > result.depth()))
			    {
				result = item;
			    }			
		    }
		
		list.remove(result);
		return result;			
	    }
    }


  public void fillFilterMenu(IMenuManager menu)
      {
	  for (int i = 0; i < _filterItems.size(); i++)
	      {
		  DataElement object = (DataElement)_filterItems.get(i);
		  DataStoreCorePlugin plugin = DataStoreCorePlugin.getInstance();
		  String imageStr = DataElementLabelProvider.getImageString(object);
		  if (plugin != null)
		      {
			  ImageDescriptor image = plugin.getImageDescriptor(imageStr, false);
			  menu.add(new MenuSelectFilterAction(object, image));        
		      }
		  else
		      {
			  menu.add(new MenuSelectFilterAction(object));        
		      }
	      }        	  
      }

  public void fillRelationMenu(IMenuManager menu)
      {
	  for (int i = 0; i < _relationItems.size(); i++)
	      { 
		  DataElement object = (DataElement)_relationItems.get(i);		
		  
		  DataStoreCorePlugin plugin = DataStoreCorePlugin.getInstance();
		  String imageStr = DataElementLabelProvider.getImageString(object);
		  if (plugin != null)
		      {
			  ImageDescriptor image = plugin.getImageDescriptor(imageStr, false);
			  menu.add(new MenuSelectRelationAction(object, image));      
		      }
		  else
		      {
			  menu.add(new MenuSelectRelationAction(object));      
		      }
	      }        
      }
    
    public void selectRelationship(String relationship)
    {
	for (int i = 0; i < _relationItems.size(); i++)
	    {
		DataElement object = (DataElement)_relationItems.get(i);
		if (object.getName().equals(relationship))
		    {
			if (object != _relationSelected)
			    {
				setRelation(object);
				updateVisibility();
			    }
		    }
	    }
    }
    
    public void selectFilter(String filter)
    {
	for (int i = 0; i < _filterItems.size(); i++)
	    {
		DataElement object = (DataElement)_filterItems.get(i);
		if (object.getName().equals(filter))
		    {
			if (object != _filterSelected)
			    {
				setFilter(object);
				updateVisibility();
			    }
		    }
	    }
    }
    
    public void dispose()
    {
	if (_filterLabel != null)
	    _filterLabel.dispose();
	if (_relationLabel != null)
	    _relationLabel.dispose();
	if (_viewToolBar != null)
	    _viewToolBar.dispose();

	_filterLabel = null;
	_relationLabel = null;
	_viewToolBar = null;    
    }

    public void enable(boolean flag)
    {
	_isEnabled = flag;
	if (!_filterLabel.isDisposed())
	    {
		_filterLabel.setEnabled(flag);
	    }

	if (!_relationLabel.isDisposed())
	    {
		_relationLabel.setEnabled(flag);
	    }
    }
}









