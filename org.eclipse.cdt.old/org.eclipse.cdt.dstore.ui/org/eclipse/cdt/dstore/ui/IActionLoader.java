package org.eclipse.cdt.dstore.ui;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.jface.action.*;
import java.util.*;

public interface IActionLoader
{
    public IOpenAction getOpenAction();
    public CustomAction getOpenPerspectiveAction();
    public CustomAction loadAction(String source, String name);
    public CustomAction loadAction(DataElement object, DataElement descriptor);
    public CustomAction loadAction(List objects, DataElement descriptor);

    public void loadCustomActions(IMenuManager menu, DataElement input, DataElement descriptor); 
    public String getImageString(DataElement object);     
    public String getImageString(String name);
}
