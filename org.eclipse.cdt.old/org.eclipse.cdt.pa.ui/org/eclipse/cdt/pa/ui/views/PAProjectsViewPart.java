package org.eclipse.cdt.pa.ui.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.*;

import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*; 

import org.eclipse.ui.*;
import org.eclipse.cdt.cpp.ui.internal.views.*;


public class PAProjectsViewPart extends CppProjectsViewPart {

    public PAProjectsViewPart()
    {
	 super();
    }
    
    public IActionLoader getActionLoader()
    {
	 IActionLoader loader = PAActionLoader.getInstance();
	 return loader;
    }
    
}
