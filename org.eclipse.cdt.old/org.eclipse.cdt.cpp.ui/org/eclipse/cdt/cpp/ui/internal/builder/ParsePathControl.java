package org.eclipse.cdt.cpp.ui.internal.builder;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.hosts.dialogs.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;

import org.eclipse.core.resources.*;
import java.io.*;
import java.util.ArrayList;

public class ParsePathControl extends Composite implements Listener
{
  private Button _addButton;
  private Button _removeButton;
  private Button _browseButton;

  private Button _upButton;
  private Button _downButton;

  private Text   _pathEntry;
  private List   _pathList;
    
    private IProject _project;

  public ParsePathControl(Composite cnr, int style)
  {
    super(cnr, style);

    _project = null;
    Group group = new Group(this, SWT.NULL);
    group.setText("Parse Include Path");
    group.setLayout(new GridLayout());
    group.setLayoutData(new GridData(GridData.GRAB_VERTICAL | GridData.FILL_BOTH));

    // entry cmp
    Composite entryCmp = new Composite(group, SWT.NULL);
    _pathEntry      = new Text(entryCmp, SWT.BORDER);
    _pathEntry.addListener(SWT.Modify, this);
    _pathEntry.addListener(SWT.FocusIn, this);
    GridData dp0 = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    _pathEntry.setLayoutData(dp0);

    _browseButton      = new Button(entryCmp, SWT.PUSH);
    _browseButton.setText("Browse");
    _browseButton.addListener(SWT.Selection, this);
    GridData dp1 = new GridData(GridData.HORIZONTAL_ALIGN_END);
    dp1.widthHint = 80;
    _browseButton.setLayoutData(dp1);

    // p1
    Composite p1    = new Composite(group, SWT.NULL);
    GridLayout p1layout = new GridLayout();
    p1layout.numColumns = 2;
    p1.setLayout(p1layout);

    _pathList       = new List(p1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    _pathList.addListener(SWT.FocusIn, this);
    _pathList.addListener(SWT.Selection, this);
    GridData dp2 = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
    dp2.heightHint = 100;
    _pathList.setLayoutData(dp2);

    // p2
    Composite p2    = new Composite(p1, SWT.NULL);

    _addButton      = new Button(p2, SWT.PUSH);
    _addButton.setText("Add");
    _addButton.addListener(SWT.Selection, this);
    GridData dp3 = new GridData(GridData.HORIZONTAL_ALIGN_END);
    dp3.widthHint = 80;
    _addButton.setLayoutData(dp3);

    _removeButton   = new Button(p2, SWT.PUSH);	
    _removeButton.setText("Remove");
    _removeButton.addListener(SWT.Selection, this);
    GridData dp4 = new GridData(GridData.HORIZONTAL_ALIGN_END);
    dp4.widthHint = 80;
    _removeButton.setLayoutData(dp4);

    _upButton      = new Button(p2, SWT.PUSH);
    _upButton.setText("Up");
    _upButton.addListener(SWT.Selection, this);
    GridData gdUp = new GridData(GridData.HORIZONTAL_ALIGN_END);
    gdUp.widthHint = 80;
    _upButton.setLayoutData(gdUp);


    _downButton      = new Button(p2, SWT.PUSH);
    _downButton.setText("Down");
    _downButton.addListener(SWT.Selection, this);
    GridData gdDown = new GridData(GridData.HORIZONTAL_ALIGN_END);
    gdDown.widthHint = 80;
    _downButton.setLayoutData(gdDown);


    // layouts
    GridLayout l1 = new GridLayout();
    l1.numColumns = 3;
    entryCmp.setLayout(l1);
    entryCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    GridLayout l2 = new GridLayout();
    l2.numColumns = 2;
    p1.setLayout(l2);
    p1.setLayoutData(new GridData(GridData.FILL_BOTH));

    GridLayout l3 = new GridLayout();
    l3.numColumns = 1;
    p2.setLayout(l3);
    p2.setLayoutData(new GridData(GridData.FILL_VERTICAL));

    if (_pathList.getItemCount() == 0)
    {
       _addButton.setEnabled(false);
       _removeButton.setEnabled(false);
       _upButton.setEnabled(false);
       _downButton.setEnabled(false);
    }

  }

    public void setContext(IProject project)
    {
	_project = project;
    }

    public void setRemote(boolean flag)
    {
	_browseButton.setVisible(!flag);
    }

  public void handleEvent(Event e)
  {
    Widget source = e.widget;

    if (source == _browseButton)
    {
	String selectedDirectory = null;
	if (_project != null)
	    {
		if (_project instanceof Repository)
		    {
			DataElement input = ((Repository)_project).getRemoteElement();
			DataElementFileDialog dialog = new DataElementFileDialog("Select Directory", input);
			dialog.open();
			if (dialog.getReturnCode() == dialog.OK)
			    {
				DataElement selected = dialog.getSelected();
				if (selected != null)
				    {
					selectedDirectory = selected.getSource();
				    }
			    }
		    }		
	    }
	else
	    {
		DirectoryDialog dialog = new DirectoryDialog(this.getShell(), SWT.SAVE);
		dialog.setMessage("Select the include directory.");
		dialog.setFilterPath("*.*");
		
		selectedDirectory = dialog.open();
	    }

        if (selectedDirectory != null)
     	  {
          _pathEntry.setText(selectedDirectory);	
          _addButton.setEnabled(true);
        }
    }
    else if (source == _pathEntry)
    {
          _removeButton.setEnabled(false);
          _upButton.setEnabled(false);
          _downButton.setEnabled(false);

          if (_pathEntry.getText() == "")
          {
             _addButton.setEnabled(false);
          }
          else
          {
             _addButton.setEnabled(true);
             for (int i = 0; i < _pathList.getItemCount(); i++)
             {
                if (_pathList.isSelected(i))
                {
                   _pathList.deselect(i);
                   break;
                }
             }
          }
    }
    else if (source == _pathList)
    {
       _addButton.setEnabled(false);

       for (int i = 0; i < _pathList.getItemCount(); i++)
       {
          if (_pathList.isSelected(i))
          {
             _removeButton.setEnabled(true);

             if (i == 0)
                _upButton.setEnabled(false);
             else
                _upButton.setEnabled(true);

             if (i == _pathList.getItemCount() - 1)
                _downButton.setEnabled(false);
             else
                _downButton.setEnabled(true);

             break;
          }
       }


    }
    else if (source == _addButton)
    {
      String text = _pathEntry.getText();
      addPath(text);
      _pathEntry.setFocus();
    }
    else if (source == _removeButton)
    {
      for (int i = 0; i < _pathList.getItemCount(); i++)
      {
        if (_pathList.isSelected(i))
        {
          _pathList.remove(i);
        }
      }
    }
    else if (source == _upButton)
    {
      String tempString;
      int    j;

      _downButton.setEnabled(true);

      if (_pathList.isSelected(1))
         _upButton.setEnabled(false);
      for (int i = 1; i < _pathList.getItemCount(); i++)
      {
        if (_pathList.isSelected(i))
        {
          tempString = _pathList.getItem(i);
          _pathList.remove(i);
          j = i - 1;
          _pathList.add(tempString, j);
          _pathList.select(j);
          break;
        }
      }
    }
    else if (source == _downButton)
    {
      String tempString;
      int    j;

      _upButton.setEnabled(true);

      if (_pathList.isSelected(_pathList.getItemCount() - 2))
         _downButton.setEnabled(false);
      for (int i = 0; i < _pathList.getItemCount() - 1; i++)
      {
        if (_pathList.isSelected(i))
        {
          tempString = _pathList.getItem(i);
          _pathList.remove(i);
          j = i + 1;
          _pathList.add(tempString, j);
          _pathList.select(j);
          break;
        }
      }
    }
  }

  public void addPath(String path)
      {
        _pathList.add(path);
      }

  public void setPaths(ArrayList paths)
      {
      	_pathList.removeAll();
        for (int i = 0; i < paths.size(); i++)
        {
          addPath((String)paths.get(i));
        }
      }

  public ArrayList getPaths()
      {
        ArrayList result = new ArrayList();
        for (int i = 0; i < _pathList.getItemCount(); i++)
        {
          result.add(_pathList.getItem(i));
        }
        return result;
      }
}
