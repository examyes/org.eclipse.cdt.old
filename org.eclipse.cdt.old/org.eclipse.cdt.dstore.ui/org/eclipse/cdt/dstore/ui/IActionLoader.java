package com.ibm.dstore.ui;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
