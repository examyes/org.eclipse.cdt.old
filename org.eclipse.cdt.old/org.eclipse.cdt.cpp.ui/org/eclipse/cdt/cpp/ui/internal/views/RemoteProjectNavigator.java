package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.actions.*; 
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.vcm.*;

import com.ibm.dstore.ui.actions.UICommandAction;
import com.ibm.dstore.ui.actions.CustomAction;

import com.ibm.dstore.ui.*; 
import com.ibm.dstore.ui.widgets.*; 
import com.ibm.dstore.ui.resource.*;
import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.core.model.*;

import org.eclipse.ui.internal.*;

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

import org.eclipse.ui.model.*;

public class RemoteProjectNavigator extends TreeViewer  implements ISelected, IDomainListener
{
  private DataElement  _selected;
  private RemoteProjectAdapter  _currentInput;

  private OpenEditorAction _openEditorAction;

  public RemoteProjectNavigator(Tree parent)
      {
	super(parent);

        CppPlugin plugin = CppPlugin.getPlugin();
	setLabelProvider(new DataElementLabelProvider(plugin.getImageRegistry()));
	setContentProvider(new WorkbenchContentProvider());

	Display display = parent.getDisplay();
	parent.setBackground(new Color(display, 255, 255, 255));
      }


  public void inputChanged(Object input, Object oldInput)
  {
    if (input != null)
      {	
	super.inputChanged(input, oldInput);
	_currentInput = (RemoteProjectAdapter)input;
      }
  }

  public RemoteProjectAdapter getCurrentInput()
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
	DataStore dataStore = parent.getDataStore();

	String type = parent.getType();
	if (type.equals(dataStore.getLocalizedString("model.directory")) || 
	    type.equals(dataStore.getLocalizedString("model.file")))
	    {
		IProject[] projects = _currentInput.getProjects();
		for (int i = 0; i < projects.length; i++)	    
		    {
			Repository repository  = (Repository)projects[i];
			if (repository != null)
			    {			
				DataElement repositoryElement = repository.getRemoteElement();
				if (repositoryElement == parent)
				    {
					return true;
				    }
				else
				    {
					if (repositoryElement.contains(parent, 3))
					    {
						return true;
					    }
				    }
			    }
		    }
	    }

	return false;
    }

  public void domainChanged(DomainEvent ev)
      {
	  DataElement parent = (DataElement)ev.getParent();
	  if (parent.isDeleted())
	      {
		  parent = parent.getParent();
	      }

	  IProject[] projects = _currentInput.getProjects();
	  for (int i = 0; i < projects.length; i++)	    
	    {
		Repository repository  = (Repository)projects[i];
		DataElement repositoryElement = repository.getRemoteElement();
		if (repositoryElement == parent)
		    {
			Tree tree = getTree();
			tree.setRedraw(false);
			internalRefresh(_currentInput);
			expandToLevel(_currentInput, 1);
			tree.setRedraw(true);				
			return;
		    }
		else 
		    {
			ResourceElement resource = repository.findResource(parent);
			if (resource != null)
			    {				
				Tree tree = getTree();
				tree.setRedraw(false);
				internalRefresh(resource);
				tree.setRedraw(true);	
				return;
			    }
		    }

	    }
      }
	
    public Shell getShell()      
    {
	if (!getControl().isDisposed())
	    {
		return getControl().getShell();
	    }
	else
	    {
		return null;
	    }
    }


  public void selectionChanged(SelectionChangedEvent e)
  {
    DataElement selected = ConvertUtility.convert(e);
    if (selected != null)
      {
	setSelected(selected);
      }
  }

  protected Item newItem(Widget parent, int flags, int ix) 
      {
	TreeItem item;
	if (ix >= 0) 
        {
          if (parent instanceof TreeItem)
          {
            item = new TreeItem((TreeItem) parent, flags);
          }
          else
          {
            item = new TreeItem((Tree) parent, flags);
          }
	} 
        else 
        {
          if (parent instanceof TreeItem)
          {
            item = new TreeItem((TreeItem) parent, flags);
          }
          else
          {
            item = new TreeItem((Tree) parent, flags);
          }
	}
	return item;
	}

    public void setBackground(int r, int g, int b)
    {
	Tree tree = getTree();
	
	Display display = tree.getDisplay();
	tree.setBackground(new Color(display, r, g, b));    
    }
    
    public void setForeground(int r, int g, int b)
    {
	Tree tree = getTree();
	
	Display display = tree.getDisplay();
	tree.setForeground(new Color(display, r, g, b));    
    }

    public void setFont(FontData data)
    {
	Tree tree = getTree();
	
	Display display = tree.getDisplay();
	tree.setFont(new Font(display, data));    
    }

}

