package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
 * BookmarkViewAction constructor comment.
 * @param provider ISelectionProvider
 * @param label java.lang.String
 */
public ActionTarget(TargetsViewer viewer, String name) {
	super(name);
	this.id = name;
	this.viewer = viewer;
}
}
