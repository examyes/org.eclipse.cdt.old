package org.eclipse.cdt.cpp.miners.managedproject.amparser;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.*;

public class Targets
{
	private HashMap _theTargets;
 
	public Targets()
	{
		reset();
	}
	
	private void reset()
	{
		_theTargets = new HashMap();
	}
	
	public Target addTarget(String name, int type)
	{
		if ( (type < Am.TARGETTYPE_START) || (type > Am.TARGETTYPE_END))
			type = Am.PROGRAMS; //Default to PROGRAMS if no type was specified
		Target theTarget = new Target(name, type);
		_theTargets.put(name, theTarget);
		return theTarget;
	}
 
	public void addTargetAttribute(String targetName, int attType, String attValue)
	{
		Target theTarget = (Target)_theTargets.get(targetName);
		if (theTarget == null)
			theTarget = addTarget(targetName, -1);
		theTarget.addAttribute(attType,attValue);
	}

	public ArrayList getTargets()
	{
		return new ArrayList (_theTargets.values());
	}
}

