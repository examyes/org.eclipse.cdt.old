package org.eclipse.cdt.dstore.ui.widgets;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.dialogs.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.ui.dnd.*; 

import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.cdt.dstore.extra.internal.extra.*;

import org.eclipse.jface.resource.*;

import org.eclipse.core.runtime.IAdaptable;
import java.util.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.dnd.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;

import org.eclipse.core.resources.*;
import org.eclipse.ui.part.*;

public class ObjectWindow extends Composite implements ILinkable, IMenuListener
{		
    public class NoSortElementsAction extends Action
    {
	public NoSortElementsAction(String name)
	{
	    super(name);      
	}
	
	public void run()
	{
	    if (_viewer != null)
		{
		    _viewer.setSorter(null);
		}
	}
    }

    public class SortElementsAction extends Action
    {
	private String _property;
	private DataElementSorter _sorter;
	
	public SortElementsAction(String name, String property)
	{
	    super(name);      
	    _property = property;      
	    if (property == null)
		{
		    _sorter = null;
		}
	    else
		{
		    _sorter = new DataElementSorter(_property);
		}
	}

	public SortElementsAction(String property)
	{
	    super(property);      
	    _property = property;      
	    if (property == null)
		{
		    _sorter = null;
		}
	    else
		{
		    _sorter = new DataElementSorter(_property);
		}
	}
	
	public String getProperty()
	{
	    return _property;
	}
	
	
	public DataElementSorter getSorter()
	{
	    return _sorter;
	}

	public void run()
	{
	    if ((_viewer != null)/*allow null sorts && (_property != null)*/)
		{
		    _viewer.setSorter(_sorter);
		    _currentSortAction = this;
		}
	}
  }

    public class SetLabelAction extends Action
    {
	private String _property; 
	
	public SetLabelAction(String property)
	{
	    super(property);      
	    _property = property;      
	}
	
	public String getProperty()
	{
	    return _property;
	}
	
	public void run()
	{ 
	    ((DataElementLabelProvider)_viewer.getLabelProvider()).setLabelProperty(_property);
	    _currentViewByAction = this;
	}
    }

    public static int           TREE  = 0;
    public static int           TABLE = 1;
  
    private IDataElementViewer  _viewer;
    private ViewToolBar         _toolBar;
    private DataStore           _dataStore;
    private ImageRegistry       _imageRegistry;
    private ArrayList           _outLinks;
    private boolean             _isLinked;
    private IActionLoader       _loader;

    private MenuHandler         _menuHandler;
    private MenuManager         _menuManager;

    private ObjectSelectionChangedListener _selectionListener;

    private ResourceBundle      _resourceBundle;  

    private SortElementsAction[] _sortByAction;
    private SortElementsAction   _currentSortAction;

    private SetLabelAction[]   _viewByAction;
    private SetLabelAction     _currentViewByAction;

    private DataElementContentProvider _provider;


    private boolean             _isTable;
    private boolean             _isLocked;

    public ObjectWindow(Composite container, int style, 
			DataStore dataStore, ImageRegistry imageRegistry,
			IActionLoader loader)
    {
	super(container, 0);
	
	if (dataStore != null)
	    {
		_dataStore = dataStore;
	    }
	else
	    {
		_dataStore = org.eclipse.cdt.dstore.core.DataStoreCorePlugin.getCurrentDataStore();
	    }
	
	_imageRegistry = imageRegistry;
	_loader = loader;
	_provider = null;

	boolean isTable = ((style & TABLE) != 0);
	initialize(isTable);
    }

    public ObjectWindow(Composite container, int style, 
			DataStore dataStore, ImageRegistry imageRegistry,
			IActionLoader loader, DataElementContentProvider provider)
    {
	super(container, 0);
	
	if (dataStore != null)
	    {
		_dataStore = dataStore;
	    }
	else
	    {
		_dataStore = org.eclipse.cdt.dstore.core.DataStoreCorePlugin.getCurrentDataStore();
	    }
	
	_imageRegistry = imageRegistry;
	_loader = loader;
	_provider = provider;

	boolean isTable = ((style & TREE) == 0);
	initialize(isTable);
    }


    public void initialize(boolean isTable)
    {
	_menuHandler = new MenuHandler(_loader);

	_outLinks = new ArrayList();
	_isTable = isTable;
	_isLocked = false;

	// setup resource bundle
	try
	    {
		_resourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.dstore.ui.UIResources");
	    }
	catch (MissingResourceException mre)
	    {
		_resourceBundle = null;
	    }
	
	createContents();
	createViewActions();
    } 

    public void setContainable(boolean flag)
    {
	if (_viewer != null)
	    {
		_viewer.setContainable(flag);
	    }
    }


	
	
    public void setActionLoader(IActionLoader loader)
    {
		_loader = loader;
		_menuHandler.setActionLoader(loader);
		
		DataElementLabelProvider lprovider = (DataElementLabelProvider)_viewer.getLabelProvider();
	 	lprovider.setLoader(loader);
	 	       
        _toolBar.setLoader(loader);
	 	resetInput();	
	 	 	
    }

    public void createViewActions()
    {	
	_sortByAction = new SortElementsAction[5];

	_sortByAction[0] = new SortElementsAction("Do not sort", null);
	_sortByAction[1] = new SortElementsAction(DE.P_TYPE);
	_sortByAction[2] = new SortElementsAction(DE.P_NAME);
	_sortByAction[3] = new SortElementsAction(DE.P_VALUE);
	_sortByAction[4] = new SortElementsAction(DE.P_SOURCE_NAME);

	_currentSortAction = _sortByAction[0];
	
	_viewByAction = new SetLabelAction[4];
	_viewByAction[0]  = new SetLabelAction(DE.P_NAME);
	_viewByAction[1]  = new SetLabelAction(DE.P_VALUE);
	_viewByAction[2]  = new SetLabelAction(DE.P_TYPE);
	_viewByAction[3]  = new SetLabelAction(DE.P_SOURCE_NAME);
	_currentViewByAction = _viewByAction[1];


	_viewer.setSorter(null);
	((DataElementLabelProvider)_viewer.getLabelProvider()).setLabelProperty(_currentViewByAction.getProperty());
    }

    public void setSorter(String property)
    {
	if (property.equals("null"))
	    {
		_sortByAction[0].run();
	    }
	else
	    {
		for (int i = 1; i < _sortByAction.length; i++)
		    {
			if (_sortByAction[i].getProperty().equals(property))
			    {
				_sortByAction[i].run();
			    }
		    }	
	    }
    }

    public void setSorter(DataElementSorter sorter)
    {	
	_viewer.setSorter(sorter);
    }

    public void setViewer(String property)
    {
	for (int i = 0; i < _viewByAction.length; i++)
	    {
		if (_viewByAction[i].getProperty().equals(property))
		    {
			_viewByAction[i].run();
		    }
	    }	
    }

    public String getLocalizedString(String key)
    {
	try
	    {
		if (_resourceBundle != null && key != null)
		    {
			return _resourceBundle.getString(key);
		    }
	    }
	catch (MissingResourceException mre)
	    {
	    }
	
	return "";
    }

    public DataElement getSelected()
    {
	return _viewer.getSelected();
    }

    public void enableSelection(boolean enable)
    {
	_selectionListener.enable(enable);
	_toolBar.enable(enable);
	_viewer.enable(enable);
	if (enable)
	    {

		DataElement selected = _viewer.getSelected();
		if (selected != null)
		    {
			_viewer.select(selected);			
		    }		
	    }
    }

    public void setSelected(DataElement element, boolean permiate)
    {
	_viewer.setSelected(element);

	if (permiate && _outLinks.size() > 0)
	    {
		for (int i = 0; i < _outLinks.size(); i++)
		    {
			ILinkable viewer = (ILinkable)_outLinks.get(i);
			if (viewer != null)
			    {
				if (viewer.isLinked())
				    {
					viewer.setInput(element);
				    }
				else
				    {
					unlinkTo(viewer);
				    }
			    }
		    }	       
	    }
    }


  public DataStore getDataStore()
      {
        return _dataStore;
      }

    public IDataElementViewer getViewer()
    {
        return _viewer;
    }
    
    public IStructuredSelection getSelection()
    {
	return (IStructuredSelection)_viewer.getSelection();
    }

    public boolean setFocus()
    {
	if (_viewer != null && !_viewer.isShowing())
	    {
		_viewer.setShowing(true);		
		_viewer.resetView();
	    }
	
	_viewer.setFocus();
	return true;
    }

    public void resetInput()
    {
	if (!_isLocked)
	    {
		DataElement input = (DataElement)_viewer.getInput();
		if (input != null)
		    {
			_viewer.resetView();
		    }
	    }
    }

    public boolean isLocked()
    {
	return _isLocked;
    }

    public void toggleLock()
    {
	_isLocked = !_isLocked;
    }
  
    public void clearView()
    {
	_viewer.clearView();
    }

  public void setInput(IAdaptable adp)
      {
        // convert the resource
        DataElement object = null;

        if (adp instanceof DataElement)
	    {
		setInput((DataElement)adp);
	    }
      }

  public void setInput(DataElement object)
      {
	  if (!_isLocked)
	      {
		  if (object != null)
		      {
			  if (object.getDataStore() != _dataStore)
			      {
				  if (_dataStore != null)
				      {
					  if (_viewer != null)
					      {
						  _viewer.clearView();
						  _dataStore.getDomainNotifier().removeDomainListener(_viewer);
						  _dataStore.getDomainNotifier().removeDomainListener(_toolBar);
					      }
				      }
				  
				  _dataStore = object.getDataStore();
				  _dataStore.getDomainNotifier().addDomainListener(_viewer);
				  _dataStore.getDomainNotifier().addDomainListener(_toolBar);
			      }

			  if (_viewer != null)
			      {
			      	DataElement des = object.getDescriptor();
			      	if (des != null)
			      	{
			          	ArrayList relationships = _dataStore.getRelationItems(des, null);	
				      	if (relationships.size() == 0)
				      	{
					    return;
				      	}
				      	else
				      	{
				      		boolean visible = false;
				      		for (int i = 0; (i < relationships.size()) && !visible; i++)
				      		{
				      			DataElement relationship = (DataElement)relationships.get(i);
				      			if (relationship.depth() > 0)
				      			{
				      				visible = true;
				      			}
				      		}
				      		
				      		if (visible == false)
				      		{						    
						    return;	
				      		}
				      	}
					}
					
			      	
				  _toolBar.setInput(object);
			      }
		      }
		  else
		      {
			  _toolBar.setInput(null);
		      }
	      }


      }
	  
    public DataElement getInput()
    {
	return (DataElement)_viewer.getInput();
    }

  public void fixateOn(String relationType, String objectType)
    {
	_toolBar.fixateOn(relationType, objectType);
    }

  public void fixateOnRelationType(String relationType)
      {
        _toolBar.fixateOnRelationType(relationType);
      }

  public void fixateOnObjectType(String objectType)
      {
        _toolBar.fixateOnObjectType(objectType);
      }

    public void selectRelationship(String relationship)
    {
	_toolBar.selectRelationship(relationship);
    }

    public void selectFilter(String filter)
    {
	_toolBar.selectFilter(filter);
    }

    public DataElement getSelectedRelationship()
    {
	return _toolBar.getSelectedRelationship();
    }

    public DataElement getSelectedFilter()
    {
	return _toolBar.getSelectedRelationship();
    }

    public boolean isSpecialized()
    {
	return _toolBar.isSpecialized();
    }

  public void resetView()
      {
        _toolBar.resetView();
      }

  public boolean isLinked()
      {
        return _isLinked;
      }
  
    public boolean isLinkedTo(ILinkable to)
    {
	return _outLinks.contains(to);
    }

  public void setLinked(boolean flag)
      {
        _isLinked = flag;
      }

  public void linkTo(ILinkable to)
  {
      if (!isLinkedTo(to) && !to.isLinkedTo(this))
	  {
	      _outLinks.add(to); 
	      to.setLinked(true);
	  }
  }
  
  public void unlinkTo(ILinkable to)
  {
    _outLinks.remove(to); 
  }

  public ArrayList getOutLinks()
  {
    return _outLinks; 
  }
  

  
  protected Control createContents() 
      {
        Composite parent = this;

	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	layout.verticalSpacing=0;
	parent.setLayout(layout);

	GridData data0 = new GridData();
	data0.horizontalAlignment = GridData.FILL;
	data0.grabExcessHorizontalSpace = true;
        parent.setLayoutData(data0);
        GridData textData = new GridData(GridData.FILL_BOTH);

        Composite toolBarContainer = new Composite(parent, SWT.NULL);

        _toolBar = new ViewToolBar(this, toolBarContainer, _loader);
        
	GridLayout layout1 = new GridLayout();
	layout1.numColumns = 5;
	layout1.marginHeight = 0;
	layout1.marginWidth = 0;
	layout1.verticalSpacing=0;
        toolBarContainer.setLayout(layout1);        

	GridData data1 = new GridData();
	data1.horizontalAlignment = GridData.FILL;
	data1.grabExcessHorizontalSpace = true;
        toolBarContainer.setLayoutData(data1);
        
        Composite treeContainer = new Composite(parent, SWT.BORDER);
        GridLayout layout2 = new GridLayout();
	layout2.numColumns = 1;
	layout2.marginHeight = 0;
	layout2.marginWidth = 0;
	layout2.verticalSpacing=0;
	treeContainer.setLayout(layout2);        
        treeContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
 
	

	Composite widget = null;
	if (_isTable)
	    {
		widget = new Table(treeContainer, SWT.H_SCROLL | SWT.V_SCROLL | 
				   SWT.MULTI | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
	
		widget.setLayoutData(textData);
		
		_viewer = new DataElementTableViewer(this, (Table)widget);

		if (_provider == null)
		    {
			_provider = new DataElementTableContentProvider();
		    }
	    }
	else
	    {
		widget = new Tree(treeContainer, SWT.MULTI);
		widget.setLayoutData(textData);
		
		_viewer = new DataElementTreeViewer(this, (Tree)widget);

		if (_provider == null)
		    {
			_provider = new DataElementTreeContentProvider();
		    }
	    }
		

	_viewer.setContentProvider(_provider);     
	_viewer.setLabelProvider(new DataElementLabelProvider(_imageRegistry, _loader));
	if (_dataStore != null)
	    {
		_dataStore.getDomainNotifier().addDomainListener(_viewer);
		_dataStore.getDomainNotifier().addDomainListener(_toolBar);
	    }

	IOpenAction openAction = null;
	if (_loader != null)
	    {
		openAction = _loader.getOpenAction();
		_viewer.setOpenAction(openAction);
	    }

	_selectionListener = new ObjectSelectionChangedListener(this, openAction); 
	_viewer.setListener(_selectionListener);
 
        _toolBar.setOutLink(_viewer);

	initDragAndDrop();
	
        // add menu handling
        _menuManager = new MenuManager("#PopupMenu");
	_menuManager.setRemoveAllWhenShown(true);
	_menuManager.addMenuListener(this);
        Menu menu = _menuManager.createContextMenu(widget);
        widget.setMenu(menu);

        return parent;
      }

    public MenuManager getMenuManager()
    {
	return _menuManager;
    }

  public void menuAboutToShow(IMenuManager menu) 
      {
	IStructuredSelection es= (IStructuredSelection) _viewer.getSelection();
	
	
	if (es.size() > 1)
	    {
		_menuHandler.multiFillContextMenu(menu, es);
	    }
	else
	    {
		DataElement selected = (DataElement)es.getFirstElement();
		DataElement input = (DataElement)_viewer.getInput();
		_menuHandler.fillContextMenu(menu, input, selected);


		_currentSortAction.setChecked(true);
		_currentViewByAction.setChecked(true);		
	    }	    	  
      }


  public void dispose()
      {
	  _outLinks.clear();
	  _isLinked = false;

	  if (_dataStore != null)
	      {
		  _dataStore.getDomainNotifier().removeDomainListener(_viewer);
		  _dataStore.getDomainNotifier().removeDomainListener(_toolBar);
	      }

	  if (_selectionListener != null)
	      {
		  _viewer.removeListener(_selectionListener);
		  _selectionListener.enable(false);
		  _selectionListener = null;
	      }

	  if (_toolBar != null)
	      {
		  _toolBar.dispose();
	      }
	  
	  if (_viewer != null)
	      {
		  _viewer.dispose();
	      }

	  _toolBar = null;
	  _viewer = null;
      }

  protected void initDragAndDrop() 
  {
      /*
    int ops = DND.DROP_COPY | DND.DROP_MOVE;
    Transfer[] transfers = new Transfer[] {PluginTransfer.getInstance()};
    _viewer.addDragSupport(ops, transfers, new DataDragAdapter((ISelectionProvider)_viewer));
    _viewer.addDropSupport(ops, transfers, new DataDropAdapter(_viewer, _dataStore));
      */
  }
  
}
