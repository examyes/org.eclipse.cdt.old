package com.ibm.dstore.ui;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.actions.*;
import org.eclipse.jface.action.*;
import java.util.*;

public interface IActionLoader
{
    public IOpenAction getOpenAction();
    public CustomAction loadAction(String source, String name);
    public CustomAction loadAction(DataElement object, DataElement descriptor);
    public CustomAction loadAction(List objects, DataElement descriptor);
}
