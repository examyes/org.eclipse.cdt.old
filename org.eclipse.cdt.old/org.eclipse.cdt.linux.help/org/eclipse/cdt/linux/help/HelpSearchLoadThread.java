package org.eclipse.cdt.linux.help;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;
import java.lang.*;

public class HelpSearchLoadThread extends Thread
{
    ArrayList theList;   
    int theType;
     
    public HelpSearchLoadThread(ArrayList list, int type)
    {
	theList=list;	
	theType=type;
    }

    public void run()
    {	
	HelpSearch.load(theList,theType);
    }

}
