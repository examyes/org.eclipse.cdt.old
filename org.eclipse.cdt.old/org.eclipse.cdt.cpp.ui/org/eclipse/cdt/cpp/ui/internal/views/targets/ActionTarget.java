package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;

/**
 * An abstract class for all target view actions.
 */
abstract public class ActionTarget extends Action {
	protected TargetsViewer viewer;
	private String id;
/**
 *
 */
	public ActionTarget(TargetsViewer viewer, String name) 
	{
		super(name);
		this.id = name;
		this.viewer = viewer;
	}
}
