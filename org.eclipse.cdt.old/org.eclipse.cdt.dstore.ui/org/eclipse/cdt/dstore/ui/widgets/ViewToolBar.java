package org.eclipse.cdt.dstore.ui.widgets;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.DataStoreCorePlugin;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;

import java.util.*;
import java.lang.reflect.*;

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

public class ViewToolBar extends Viewer implements IDomainListener
{
    private   Label        _inputIconLabel;
    private   Label        _inputTextLabel;
    private   String       _viewName = null;
    
    private   ViewMenu     _viewMenu;
    
    private   DataElement  _currentDescriptor;
    private   DataElement  _currentInput;
    
    private   Composite    _toolBarContainer;
    
    private   Composite    _inputTextContainer;
    private   Composite    _topContainer;
    
    private   ObjectWindow _parent;
    
    private   DataElement  _selected;
    private   DataElement  _expanded;
    
    private   String       _property;
    private   String       _pluginPath = null;
    
    private   IDataElementViewer _outLink;
    private   IActionLoader _loader;
    
    private class ToolLayout extends Layout 
    {	
	public ToolLayout()
        {
	    super();
        }
    
	public Point computeSize(Composite c, int w, int h, boolean force) 
        {
	    int width = c.getParent().getClientArea().width;
	    int height = c.getParent().getClientArea().height;
	    return new Point(width, 22);
        }
	
	public void layout(Composite composite, boolean force) 
        {      
	    Rectangle r= composite.getClientArea();
	    int height = r.height;
	    int width  = r.width;
	    
	    int viewHeight = height;
	    int width2 = 80;
	    int width1 = width - width2;
	    
	    int x1 = r.x;
	    int x2 = r.x + width1;
	    
	    int y1 = r.y;
	    int y2 = r.y;
	    
	    _topContainer      .setBounds(x2, y2, width2, viewHeight);
	    _inputTextContainer.setBounds(x1, y1, width1, viewHeight);
        }
    }
    
    private class InputLayout extends Layout 
    {	
	public InputLayout()
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
	    
	    int buttonSize = 18;
	    int iconSize = 16;
	    int x1 = r.x;
	    int x2 = r.x + 3;
	    int x3 = x2 + iconSize + 5;
	    int y1 = r.y + 4;   
	    int textWidth = width - x3;
	    
	    _inputIconLabel.setBounds(x2, y1, iconSize + 3, iconSize);
	    _inputTextLabel.setBounds(x3, y1, textWidth, height);
        }
    }
    
    public ViewToolBar(ObjectWindow parent, Composite window, IActionLoader loader)
  {
      _parent = parent;
      _loader = loader;
      doCreateControl(window);

      DataStore dataStore = getDataStore();
      if (dataStore != null)
	  {
	      _property = new String(getLocalizedString("view.contents"));
	  }
      else
	  {
	      _property = "conents";
	  }
  }
    
    public DataStore getDataStore()
    {
	return _parent.getDataStore();
    }
    
    public void setOutLink(IDataElementViewer viewer)
    {
        _outLink = viewer;
    }
    
    public Shell getShell()
    {
    	Control control = getControl();
		if (!control.isDisposed())
		{
	   	 	return control.getShell();
		}
	    return null;
    }
    
    public boolean listeningTo(DomainEvent ev)
    {
	DataElement parent = (DataElement)ev.getParent();
	if (parent == _currentInput)	    
	    {
		return true;
	    }
	
	return false;
    }   
    
    public void domainChanged(DomainEvent ev)
    {
        DataElement parent = (DataElement)ev.getParent();   
	String name = _currentInput.getName();	  
	if (!name.equals(_viewName))
	{
	    _viewName = name;
	    _inputTextLabel.setText(name);
	}
    }
    

    public Object getInput()
    {
	return _currentInput;
    }

    public ISelection getSelection()
    {
	return null;
    }

    public void setSelection(ISelection sel, boolean flag)
    {
    }

    public void refresh()
    {
    	_toolBarContainer.redraw();
    }
    
    public void setLoader(IActionLoader loader)
    {
    	if (_loader != loader)
    	{
    		_loader = loader;
    		_viewMenu.setLoader(loader);
    	
    		refresh();
    		_viewMenu.updateVisibility();
    	}
    }

  protected Control doCreateControl(Composite parent)
    {
	GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | 
					 GridData.GRAB_HORIZONTAL);
	
	_toolBarContainer = new Composite(parent, SWT.PUSH);
	
	_inputTextContainer = new Composite(_toolBarContainer, SWT.NULL);
	_inputIconLabel = new Label(_inputTextContainer, SWT.NULL);
	
	DataStoreCorePlugin plugin = DataStoreCorePlugin.getInstance();
	if (plugin != null)
	    {
		String imageStr = getPluginPath() + "icons" + java.io.File.separator + "default.gif";
		_inputIconLabel.setImage(plugin.getImage(imageStr));
	    }
	
	_inputTextLabel = new Label(_inputTextContainer, SWT.NULL);
	_inputTextLabel.setText(getLocalizedString("view.No_input"));
	_inputTextLabel.setLayoutData(textData);
	
	_inputTextContainer.setLayout(new InputLayout());
	
	_topContainer = new Composite(_toolBarContainer, SWT.BAR);
    
	_viewMenu = new ViewMenu(this, _topContainer, _loader);
	
	_toolBarContainer.setLayout(new ToolLayout());
	_toolBarContainer.setVisible(false);
	
	return _toolBarContainer;
    }
    
    public String getPluginPath()
    {
	if (_pluginPath == null)
	    {
		DataStoreCorePlugin plugin = DataStoreCorePlugin.getInstance();
		_pluginPath = plugin.getInstallLocation() + java.io.File.separator + "org.eclipse.cdt.dstore.core";
	    }

	return _pluginPath; 
    }
    
    public ObjectWindow getParent()
    {
	return _parent;
    }    
    
    public void enable(boolean flag)
    {
	_viewMenu.enable(flag);
    }

    public void setInput(Object input)
    {
        if (_outLink != null)
	    {
		if (_outLink.getContentProvider() != null)
		    {
			_outLink.setInput(input);
		    }
	    }

	
	inputChanged(input, null);
    }
    
    public void resetView()
    {
        if (_outLink != null)
	    {
		_outLink.resetView();
	    }
    }

    private String getLocalizedString(String key)
    {
	return _parent.getLocalizedString(key);
    }
    
  

    protected void inputChanged(Object object, Object oldInput)
    {
	if (object == null)
	    {
		_toolBarContainer.setVisible(false);
	    }
	else if (_currentInput == null)
	    {
		_toolBarContainer.setVisible(true);
	    }

	if (object == null)
	    {
		_currentInput = null;
		String imageStr = getPluginPath() + java.io.File.separator + 
		    "icons" + java.io.File.separator + "default.gif";
		DataStoreCorePlugin plugin = DataStoreCorePlugin.getInstance();
		if (plugin != null)
		    {
			_inputIconLabel.setImage(null);
		    }
		_viewName = getLocalizedString("view.No_input");
		_inputTextLabel.setText(_viewName);	      
	    }
	else if (_currentInput != object)
	    {
		_currentInput = (DataElement)object;	

		if (_currentInput != null && !_currentInput.isDeleted())
		  {	
		      String imageStr = _loader.getImageString(_currentInput);
		      DataStoreCorePlugin plugin = DataStoreCorePlugin.getInstance();
		      if (plugin != null)
			  {
			      if (!_inputIconLabel.isDisposed())
				  {
				      _inputIconLabel.setImage(plugin.getImage(imageStr));
				  }
			  }

		      if (!_inputTextLabel.isDisposed())
			  {		      
			      String name = (String)_currentInput.getElementProperty(DE.P_NAME);
			      if (name != null)
				  {
				      _viewName = name;
				      _inputTextLabel.setText(name);
				  }
			      else
				  {
				      _viewName = "null";
				      _inputTextLabel.setText(_viewName);
				  }
			  }
		      if (_viewMenu != null)
			  {
			      _viewMenu.setInput(_currentInput);
			  }		

		  }
		
		_toolBarContainer.layout(true);

	    }
    }
    
    public Control getControl()
    {
        return _topContainer;
    }

    public IDataElementViewer getViewer()
    {
	return _outLink; 
    }

    public void selectRelationship(String relationship)
    {
	_viewMenu.selectRelationship(relationship);
    }

    public void selectFilter(String filter)
    {
	_viewMenu.selectFilter(filter);
    }

    public void fixateOn(String relationType, String objectType)
    {
 	fixateOnRelationType(relationType);
 	fixateOnObjectType(relationType);
    }

    public void fixateOnRelationType(String relationType)
    {
        _viewMenu.fixateOnRelationType(relationType);
    }
    
    public void fixateOnObjectType(String objectType)
    {
        _viewMenu.fixateOnObjectType(objectType);
    }

    public DataElement getSelectedRelationship()
    {
	return _viewMenu.getRelation();
    }

    public DataElement getSelectedFilter()
    {
	return _viewMenu.getFilter();
    }
    
    public boolean isSpecialized()
    {
	return _viewMenu.isSpecialized();
    }

    public void dispose()
    {
	_toolBarContainer.dispose();
	_viewMenu.dispose();
	_inputTextContainer.dispose();
	_topContainer.dispose();

	_toolBarContainer = null;
	_viewMenu = null;
	_inputTextContainer = null;
	_topContainer = null;

    }
}
