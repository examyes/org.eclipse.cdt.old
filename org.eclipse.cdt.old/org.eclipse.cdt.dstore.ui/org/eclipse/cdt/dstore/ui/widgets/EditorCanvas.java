package com.ibm.dstore.ui.widgets;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.actions.RemoteOperation;
import com.ibm.dstore.ui.actions.CloseSectionAction;
import com.ibm.dstore.ui.actions.OpenSectionAction;
import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;

import java.util.ArrayList;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;

public class EditorCanvas extends Composite implements ILinkable
{
  private TestUI.NavigationPage _parent;
  private OldEditor _editor;

  private Composite _toolBarContainer;
  private Composite _editorContainer;

  private Label  _fileLabel;
  private Button _maxButton;
  private Button _minButton;

  private boolean _isLinked;

  private class EditorLayout extends Layout 
  {	
    
    public EditorLayout()
        {          
          super(); 
        }
    
    public Point computeSize(Composite c, int w, int h, boolean force) 
        {
          return new Point(100, 100);
        }
    
    public void layout(Composite composite, boolean force) 
        {      
          Rectangle r= composite.getClientArea();
          int height = r.height;
          int width  = r.width;

          int height1 = 22;
          int height2 = height - height1;

          int x1 = r.x;
          int y1 = r.y;
          int y2 = r.y + height1;
          
          _toolBarContainer.setBounds(x1, y1, width, height1);
          _editorContainer.setBounds(x1, y2, width, height2);
        }
  }

  private class ToolBarLayout extends Layout 
  {	
    public ToolBarLayout()
        {
          super();
        }
    
    public Point computeSize(Composite c, int w, int h, boolean force) 
        {
          int width = c.getParent().getClientArea().width;
          return new Point(width, 20);
        }
    
    public void layout(Composite composite, boolean force) 
        {      
          Rectangle r= composite.getClientArea();
          int height = r.height;
          int width  = r.width;

          int buttonSize = 18;
          int x1 = r.x;
          int x2 = (width - 2* buttonSize) - 2;
          int x3 = (width - buttonSize) - 2;

          int y1 = r.y + 2;   
          
          _fileLabel.setBounds(x1 + 2, y1, width - (3 * buttonSize), height);
          _minButton.setBounds(x2, y1, buttonSize, buttonSize);
          _maxButton.setBounds(x3, y1, buttonSize, buttonSize);
        }
  }

  public EditorCanvas(Composite container, int style, TestUI.NavigationPage parent)
  {
    super(container, style);
    _parent = parent;
    createContents();
  }

  public void setInput(DataElement object)
      {
        if (isVisible())
        {
          _fileLabel.setText((String)object.getElementProperty(DE.P_SOURCE_NAME));
          _editor.setInput(object);
        }
      }

    public void resetView()
    {
        _fileLabel.setText("");        
    }
    
    public boolean isLinked()
    {
        return _isLinked;
    }
    
    public boolean isLinkedTo(ILinkable to)
    {
	return false;
    }

    public void setLinked(boolean flag)
    {
        _isLinked = flag;
    }
    
    public void linkTo(ILinkable v)
    {
    }
    
    public void unlinkTo(ILinkable v)
    {
    }

  public boolean isVisible()
      {
        double ratio = _parent.getVSplitRatio();
        return (ratio < 0.90);
      }

  public void maximize()
      {
        _parent.setVSplitRatio(0.01);
      }

  public void minimize()
      {
        _parent.setVSplitRatio(0.99);
      }

  protected Control createContents() 
      {
        Composite parent = this;
        GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | 
                                         GridData.GRAB_HORIZONTAL);
        
        
        _toolBarContainer = new Composite(parent, SWT.NULL);
        _fileLabel = new Label(_toolBarContainer, SWT.NULL);
        _fileLabel.setLayoutData(textData);

        _maxButton = new Button(_toolBarContainer, SWT.PUSH);
        _maxButton.setImage(new Image(_toolBarContainer.getDisplay(), 
                                      _parent.getSchemaPath() + java.io.File.separator + "icons" + java.io.File.separator + "maximize.gif"));
        _maxButton.addSelectionListener(
                                        new SelectionAdapter()
                                        {
                                          public void widgetSelected(SelectionEvent e) 
                                              {
                                                   maximize();
                                              }
                                        }
                                        );

        _minButton = new Button(_toolBarContainer, SWT.PUSH);
        _minButton.setImage(new Image(_toolBarContainer.getDisplay(), 
                                      _parent.getSchemaPath() + java.io.File.separator + "icons" + java.io.File.separator + "restore.gif"));
        _minButton.addSelectionListener(
                                        new SelectionAdapter()
                                        {
                                          public void widgetSelected(SelectionEvent e) 
                                              {
                                                   minimize();
                                              }
                                        }
                                        );
        
        _toolBarContainer.setLayout(new ToolBarLayout());

        _editorContainer = new Composite(parent, SWT.BORDER);
        _editor = new OldEditor(_editorContainer, null, SWT.NULL);
        _editorContainer.setLayout(new FillLayout());

        parent.setLayout(new EditorLayout());

        return parent;
      }        
}
