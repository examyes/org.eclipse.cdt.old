package com.ibm.dstore.ui.widgets;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.DataStoreCorePlugin;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;

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
    
    private   Button       _zoomOutButton;
    
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
    
    private   IDataElementViewer _outLink;
    
    private class ToolLayout extends Layout 
    {	
	public ToolLayout()
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
	    int iconSize = 15;
	    int x1 = r.x;
	    int x2 = r.x + 3;
	    int x3 = x2 + iconSize + 2;
	    int y1 = r.y + 4;   
	    int textWidth = width - x3;
	    
	    _inputIconLabel.setBounds(x2, y1, iconSize, iconSize);
	    _inputTextLabel.setBounds(x3, y1, textWidth, height);
        }
    }
    
    public ViewToolBar(ObjectWindow parent, Composite window)
  {
      _parent = parent;
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
	if (!getControl().isDisposed())
	    return getControl().getShell();
	else
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
	String name = (String)_currentInput.getElementProperty(DE.P_NAME);	  
	if ((_inputTextLabel != null) && 
	    !_inputTextLabel.isDisposed() &&
	    !_inputTextLabel.getText().equals(name))
	    {	      
		if (name != null)
		    {
			_inputTextLabel.setText(name);
		    }
		else
		    {
			_inputTextLabel.setText("null");
		    }
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
    
	_viewMenu = new ViewMenu(this, _topContainer);
	
	_toolBarContainer.setLayout(new ToolLayout());
	
	return _toolBarContainer;
    }
    
    public String getPluginPath()
    {
	DataStoreCorePlugin plugin = DataStoreCorePlugin.getInstance();
	String path = plugin.getInstallLocation() + java.io.File.separator + "com.ibm.dstore.core";
	return path; 
    }
    
    public ObjectWindow getParent()
    {
	return _parent;
    }    
    
    public void fixateOnRelationType(String relationType)
    {
        _viewMenu.fixateOnRelationType(relationType);
    }
    
    public void fixateOnObjectType(String objectType)
    {
        _viewMenu.fixateOnObjectType(objectType);
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
		_currentInput = null;
		String imageStr = getPluginPath() + java.io.File.separator + 
		    "icons" + java.io.File.separator + "default.gif";
		DataStoreCorePlugin plugin = DataStoreCorePlugin.getInstance();
		if (plugin != null)
		    {
			_inputIconLabel.setImage(plugin.getImage(imageStr));
		    }
		_inputTextLabel.setText(getLocalizedString("view.No_input"));	      
	    }
	else if (_currentInput != object)
	    {
		_currentInput = (DataElement)object;	
		if (_currentInput != null && !_currentInput.isDeleted())
		  {	
		      String imageStr = DataElementLabelProvider.getImageString(_currentInput);
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
				      _inputTextLabel.setText(name);
				  }
			      else
				  {
				      _inputTextLabel.setText("null");
				  }
			  }

		      if (_viewMenu != null)
			  {
			      _viewMenu.setInput(_currentInput);
			  }		
		  }
		
		_toolBarContainer.layout(true);
		_currentInput.expandChildren();

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
}
