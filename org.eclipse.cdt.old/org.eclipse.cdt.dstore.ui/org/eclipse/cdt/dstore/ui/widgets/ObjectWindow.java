package com.ibm.dstore.ui.widgets;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
 
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.ui.dnd.*; 

import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;

import com.ibm.dstore.extra.internal.extra.*;

import org.eclipse.jface.resource.*;

import org.eclipse.core.runtime.IAdaptable;
import java.util.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.dnd.*;

import org.eclipse.jface.*;
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
	
	public SortElementsAction(String property)
	{
	    super(property);      
	    _property = property;      
	    _sorter = new DataElementSorter(_property);
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
	    if ((_viewer != null) && (_property != null))
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
	    _viewer.refresh();
	    _currentViewByAction = this;
	}
    }
  
    private IDataElementViewer  _viewer;
    private ViewToolBar         _toolBar;
    private DataStore           _dataStore;
    private ImageRegistry       _imageRegistry;
    private TestUI              _workbook;
    private ArrayList           _outLinks;
    private boolean             _isLinked;
    private IActionLoader       _loader;
    
    private CustomAction        _openPerspectiveAction;
    private ObjectSelectionChangedListener _selectionListener;

    private ResourceBundle      _resourceBundle;  

    private NoSortElementsAction _noSort;

    private SortElementsAction[] _sortByAction;
    private SortElementsAction   _currentSortAction;

    private SetLabelAction[]   _viewByAction;
    private SetLabelAction     _currentViewByAction;

    private boolean             _isTable;

    public ObjectWindow(Composite container, int style, 
			DataStore dataStore, 
			ImageRegistry imageRegistry,
			IActionLoader loader)
    {
	super(container, style);

	if (dataStore != null)
	    {
		_dataStore = dataStore;
	    }
	else
	    {
		_dataStore = com.ibm.dstore.core.DataStoreCorePlugin.getCurrentDataStore();
	    }
	
	_imageRegistry = imageRegistry;
	_loader = loader;
	initialize(false);
    }

    public ObjectWindow(Composite container, int style, 
			DataStore dataStore, 
			ImageRegistry imageRegistry,
			IActionLoader loader, 
			boolean isTable)
    {
	super(container, style);

	if (dataStore != null)
	    {
		_dataStore = dataStore;
	    }
	else
	    {
		_dataStore = com.ibm.dstore.core.DataStoreCorePlugin.getCurrentDataStore();
	    }
	
	_imageRegistry = imageRegistry;
	_loader = loader;
	
	initialize(isTable);
    }

    public void initialize(boolean isTable)
    {
	_outLinks = new ArrayList();
	_isTable = isTable;
	
	// setup resource bundle
	try
	    {
		_resourceBundle = ResourceBundle.getBundle("com.ibm.dstore.ui.UIResources");
	    }
	catch (MissingResourceException mre)
	    {
		_resourceBundle = null;
	    }
	
	createContents();
	createViewActions();
    } 

    public void createViewActions()
    {
	_noSort      = new NoSortElementsAction("Do not sort");

	_sortByAction = new SortElementsAction[4];

	_sortByAction[0] = new SortElementsAction(DE.P_TYPE);
	_sortByAction[1] = new SortElementsAction(DE.P_NAME);
	_sortByAction[2] = new SortElementsAction(DE.P_VALUE);
	_sortByAction[3] = new SortElementsAction(DE.P_SOURCE_NAME);

	_currentSortAction = _sortByAction[0];
	
	_viewByAction = new SetLabelAction[4];
	_viewByAction[0]  = new SetLabelAction(DE.P_NAME);
	_viewByAction[1]  = new SetLabelAction(DE.P_VALUE);
	_viewByAction[2]  = new SetLabelAction(DE.P_TYPE);
	_viewByAction[3]  = new SetLabelAction(DE.P_SOURCE_NAME);
	_currentViewByAction = _viewByAction[1];


	_viewer.setSorter(new DataElementSorter(_currentSortAction.getProperty()));
	((DataElementLabelProvider)_viewer.getLabelProvider()).setLabelProperty(_currentViewByAction.getProperty());
    }

    public void setSorter(String property)
    {
	for (int i = 0; i < _sortByAction.length; i++)
	    {
		if (_sortByAction[i].getProperty().equals(property))
		    {
			_sortByAction[i].run();
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
	if (enable)
	    {
		DataElement selected = _viewer.getSelected();
		if (selected != null)
		    {
			_viewer.select(selected);
		    }		
	    }
	_selectionListener.enable(enable);
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

  public void setWorkbook(TestUI ui)
  {
    _workbook = ui;
  }
  

  public DataStore getDataStore()
      {
        return _dataStore;
      }

    public IDataElementViewer getViewer()
    {
        return _viewer;
    }
    
    public boolean setFocus()
    {
	if (!_viewer.isShowing())
	    {
		_viewer.setShowing(true);		
		_viewer.resetView();
	    }
	
	_viewer.setFocus();
	return true;
    }

    public void resetInput()
    {
	DataElement input = (DataElement)_viewer.getInput();
	if (input != null)
	    {
		_viewer.resetView();
	    }
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
			  _toolBar.setInput(object);
		      }
	      }
	  else
	      {
		  if (_dataStore != null)
		      {
			  _toolBar.setInput(null);
			  //_viewer.clearView();
		      }
	      }
      }

    public DataElement getInput()
    {
	return (DataElement)_viewer.getInput();
    }

  public void fixateOnRelationType(String relationType)
      {
        _toolBar.fixateOnRelationType(relationType);
      }

  public void fixateOnObjectType(String objectType)
      {
        _toolBar.fixateOnObjectType(objectType);
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
  
  public void openSection(DataElement element)
  {
    if (_workbook != null)
    {
      _workbook.openSection(element);   
    }
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

        _toolBar = new ViewToolBar(this, toolBarContainer);
        
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
		widget = new Table(treeContainer, SWT.NULL);
		widget.setLayoutData(textData);
		
		_viewer = new ExtendedTableViewer(this, (Table)widget, _toolBar);
	    }
	else
	    {
		widget = new Tree(treeContainer, SWT.NULL);
		widget.setLayoutData(textData);
		
		_viewer = new ExtendedTreeViewer(this, (Tree)widget, _toolBar);
	    }
		
	_viewer.setLabelProvider(new DataElementLabelProvider(_imageRegistry));
	if (_dataStore != null)
	    {
		_dataStore.getDomainNotifier().addDomainListener(_viewer);
		_dataStore.getDomainNotifier().addDomainListener(_toolBar);
	    }

	IOpenAction openAction = _loader.getOpenAction();
	_viewer.setOpenAction(openAction);
	_selectionListener = new ObjectSelectionChangedListener(this, openAction); 
	_viewer.setListener(_selectionListener);
 
        _toolBar.setOutLink(_viewer);

	initDragAndDrop();
	
        // add menu handling
        MenuManager menuMgr = new MenuManager("#PopupMenu");
	menuMgr.setRemoveAllWhenShown(true);
	menuMgr.addMenuListener(this);
        Menu menu = menuMgr.createContextMenu(widget);
        widget.setMenu(menu);

        return parent;
      }

  public void menuAboutToShow(IMenuManager menu) 
      {
	fillContextMenu(menu);
      }

  public void fillContextMenu(IMenuManager menu)
      {
	DataElement selected = ConvertUtility.convert(_viewer.getSelection());
        DataElement input = (DataElement)_viewer.getInput();

        if (selected == null)
        {
          selected = input;
        }

	if (selected != null)
	  {	    
	      if (_workbook != null)
		  {
		      menu.add(new Separator(getLocalizedString("ui.Perspective")));
		      menu.add(new OpenSectionAction(getLocalizedString("ui.Open_Section_On"), selected, this));
		  }
	      else
		  {
		      if (_openPerspectiveAction == null)
			  {
			      _openPerspectiveAction = _loader.loadAction("com.ibm.dstore.ui.actions.OpenPerspectiveAction", 
									  getLocalizedString("ui.Open_Perspective_On"));
			  }

		      if (_openPerspectiveAction != null)
			  {
			      _openPerspectiveAction.setSubject(selected);
			      menu.add(_openPerspectiveAction);
			  }
		  }

	      MenuManager zoom = new MenuManager(getLocalizedString("ui.Zoom"), "#ZoomMenu");
	      zoom.add(new ZoomInAction(getLocalizedString("ui.in"), selected, this));
	      zoom.add(new ZoomInAction(getLocalizedString("ui.out"), input.getParent(), this));
	      menu.add(zoom);

	    menu.add(new Separator(getLocalizedString("ui.Object_Actions")));	    
	    DataElement descriptor = selected.getDescriptor();
	    fillContextMenuHelper(menu, selected, descriptor);
	  }
	
	menu.add(new Separator("#View"));
	MenuManager sort = new MenuManager(getLocalizedString("ui.Sort_by"), "#SortMenu");
	sort.add(_noSort);
	for (int i = 0; i < _sortByAction.length; i++)
	    {
		_sortByAction[i].setChecked(false);
		sort.add(_sortByAction[i]);
	    }
	menu.add(sort);

	MenuManager label = new MenuManager(getLocalizedString("ui.View"), "#ViewMenu");
	for (int i = 0; i < _viewByAction.length; i++)
	    {
		_viewByAction[i].setChecked(false);
		label.add(_viewByAction[i]);
	    }
	menu.add(label);	
	_currentSortAction.setChecked(true);
	_currentViewByAction.setChecked(true);
      }

  public void fillContextMenuHelper(IMenuManager menu, DataElement object, DataElement descriptor)
      {
	if (object != null)
	  {    	
	    if (descriptor != null)
	      {	
		  // add actions for contained command descriptors
		  ArrayList subDescriptors = descriptor.getAssociated(_dataStore.getLocalizedString("model.contents"));		  
		  for (int i = 0; i < subDescriptors.size(); i++)
		  { 
		      DataElement subDescriptor = (DataElement)subDescriptors.get(i);
		      String type = subDescriptor.getType();
		      if (type.equals(DE.T_COMMAND_DESCRIPTOR) && (subDescriptor.depth() > 0))
			  {
			      String name = subDescriptor.getName();
			      menu.add(new UICommandAction(object, name, subDescriptor, object.getDataStore()));
			  }
		      else if (type.equals(DE.T_ABSTRACT_COMMAND_DESCRIPTOR))
			  {
			      String name = subDescriptor.getName();
			      MenuManager cascade = new MenuManager(name, name);
			      fillContextMenuHelper(cascade, object, subDescriptor);
			      menu.add(cascade);
			  }
		      else if (type.equals(DE.T_UI_COMMAND_DESCRIPTOR))
			  {
			      CustomAction action = _loader.loadAction(object, subDescriptor);
			      if (action != null)
				  menu.add(action);
			  }
		  }
		  // inherit actions from abstract object descriptors
		  menu.add(new Separator(_dataStore.getLocalizedString("model.abstracted_by")));

		  ArrayList baseDescriptors = descriptor.getAssociated(_dataStore.getLocalizedString("model.abstracted_by"));
		  for (int j = 0; j < baseDescriptors.size(); j++)
		      {
			  DataElement baseDescriptor = (DataElement)baseDescriptors.get(j);
			  fillContextMenuHelper(menu, object, baseDescriptor);			  
		      }
	      }
	  }
      }

  public void dispose()
      {
	  if (_dataStore != null)
	      {
		  _dataStore.getDomainNotifier().removeDomainListener(_viewer);
		  _dataStore.getDomainNotifier().removeDomainListener(_toolBar);
	      }

	  _viewer.removeListener(_selectionListener);
	  _selectionListener.enable(false);
	  _selectionListener = null;
	  _viewer = null;

	  _outLinks.clear();
	  _isLinked = false;

	  //        super.dispose();
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
