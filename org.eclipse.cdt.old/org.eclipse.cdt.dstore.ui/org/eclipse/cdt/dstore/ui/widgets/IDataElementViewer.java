package org.eclipse.cdt.dstore.ui.widgets;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;

import java.util.*;

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

public interface IDataElementViewer extends IDomainListener
{
    public void setListener(ObjectSelectionChangedListener listener);    
    public void removeListener(ObjectSelectionChangedListener listener);

    public void setBackground(int r, int g, int b);
    public void setForeground(int r, int g, int b);
    public void setFont(FontData data);
    public void setShowing(boolean flag);
    public boolean isShowing();    
    public void handleEvent(Event e);
    public String getSchemaPath();
    public ObjectWindow getParent();

    public void dispose();
    public void clearView();    
    public void resetView(DataElement parent);
    public void resetView();
    public void refreshView(DataElement relation, DataElement filter);

    public void setSelected(DataElement selected);
    public DataElement getSelected();
    
    public void setInput(Object obj);
    public Object getInput();
    public DataElement findElement(DataElement oldElement);
    public void select(DataElement object);

    public void setFilter(DataElement type);
    public DataElement getFilter();

    public void setProperty(DataElement property);
    public DataElement getProperty();

    public void setOpenAction(IOpenAction action);
    public boolean isWorking();


    public Control getControl();

    public ISelection getSelection();
    public void setSelection(ISelection selection);
    public void addSelectionChangedListener(ISelectionChangedListener listener);

    public void setSorter(ViewerSorter sorter);
    public IBaseLabelProvider getLabelProvider();
    public void setLabelProvider(IBaseLabelProvider provider);

    public IContentProvider getContentProvider();
    public void setContentProvider(IContentProvider provider);
    public void refresh();
    public void setFocus();
    public void enable(boolean flag);

    public void setContainable(boolean flag);
}
