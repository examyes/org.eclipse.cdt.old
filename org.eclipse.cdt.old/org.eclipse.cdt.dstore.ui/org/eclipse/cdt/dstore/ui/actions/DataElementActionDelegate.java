package org.eclipse.cdt.dstore.ui.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.ui.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.cdt.dstore.core.model.*;
import java.util.*;


public abstract class DataElementActionDelegate implements IObjectActionDelegate
{
 
  protected DataStore		_dataStore;
  protected DataElement		_subject;
  protected List			_subjects;
 
  
  public DataElementActionDelegate()
  {
    _subjects = new ArrayList();
  }
  
  public void setActivePart(IAction action, IWorkbenchPart targetPart)
  {
  
  }
  
  public void selectionChanged(IAction action, ISelection selection)
  {
  
	if (selection instanceof IStructuredSelection)
	{
		IStructuredSelection structuredSelection = (IStructuredSelection)selection;
		
		Object first = structuredSelection.getFirstElement();
		if (first instanceof DataElement)
		{
		  _subject = (DataElement)first;
		  _dataStore = _subject.getDataStore();
		}
		
		_subjects.clear();
		Iterator it = structuredSelection.iterator();
		while (it.hasNext())
		{
			Object next = it.next();
			if (next instanceof DataElement)
			{
				_subjects.add(next);
			}
		}
	}
		
 }
  
  
  public void run(IAction action)
  {
    run();
  }
  
  public abstract void run();
  
}