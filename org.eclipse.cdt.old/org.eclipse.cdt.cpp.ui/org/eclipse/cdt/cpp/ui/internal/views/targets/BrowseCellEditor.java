package com.ibm.cpp.ui.internal.views.targets;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.dialogs.*;

import com.ibm.cpp.ui.internal.*;

public class BrowseCellEditor extends org.eclipse.jface.viewers.DialogCellEditor {
	
	private Composite directoryEditor;
	private String title;
	private Label fText;
	private Label fIcon;
	private static final int GAP= 6;	// between image and text
	// NL enablement
	private CppPlugin pluginInstance = CppPlugin.getPlugin();
	private String BUTTON_TITLE = "TargetsViewer.CellEditor.ButtonTitle.Browse";
	private String DIALOG_TITLE = "TargetsViewer.CellEditor.DialogBox.Title";
	private class BrowseCellLayout extends Layout 
	{
		public Point computeSize(
			Composite editor, 
			int wHint, 
			int hHint, 
			boolean force) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);
			Point imageSize = fIcon.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
			Point textSize = fText.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
			return new Point(
				imageSize.x + GAP + textSize.x, 
				Math.max(imageSize.y, textSize.y)); 
		}
		public void layout(Composite editor, boolean force) 
		{
			Rectangle bounds = editor.getClientArea();
			Point imageSize = fIcon.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
			Point textSize = fText.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
			int ty = (bounds.height - textSize.y) / 2;
			if (ty < 0)
				ty = 0;
			fIcon.setBounds(-1, 0, imageSize.x, imageSize.y);
			fText.setBounds(
				imageSize.x + GAP - 1, 
				ty, 
				bounds.width - imageSize.x - GAP, 
				bounds.height); 
		}
	}
	
	
	private TargetSelectionDialog dialog ;

/**
 * BrowseCellEditor constructor comment.
 */
	protected BrowseCellEditor(Composite parent, String buttonTitle) 
	{
		super(parent);
		title = new String(buttonTitle);
	}
/**
 * 
 */
	protected Button createButton(Composite parent) 
	{
		Button result = new Button(parent, SWT.PUSH);
		result.setText(pluginInstance.getLocalizedString(BUTTON_TITLE));
		return result;
	}
	protected Control createContents(Composite cell) 
	{
		if(directoryEditor==null)
		{
			Color bg = cell.getBackground();
			directoryEditor = new Composite(cell, SWT.NONE);
			directoryEditor.setBackground(bg);
			directoryEditor.setLayout(new BrowseCellLayout());
				fIcon = new Label(directoryEditor, SWT.LEFT);
			fIcon.setBackground(bg);
			fText = new Label(directoryEditor, SWT.LEFT);
			fText.setBackground(bg);
			fText.setFont(cell.getFont());		
		}	

		return directoryEditor;		

	}
/**
 * .
 */
	protected Object openDialogBox(Control cellEditorWindow) 
	{
		 // to  initialize target container root
		IContainer containerRoot = (IContainer)NavigatorSelection.selection;
	
		dialog = new TargetSelectionDialog(cellEditorWindow.getShell(),containerRoot,true,pluginInstance.getLocalizedString(DIALOG_TITLE));
		dialog.open();
	
		if(dialog.getResult()!=null)
			if(dialog.getResult()[0]!=null) // this is to check if the user has not selected a container while pressing the OK button
			{
				String selFromDialog = dialog.getResult()[0].toString();
				int b = selFromDialog.indexOf('/',1);
				int e = selFromDialog.length();
				String endPath = new String("");
				if( b < 1 ) // means no '/'
					endPath = new String("");
				else
					endPath = selFromDialog.substring(b,e);
				
				String path = new String(containerRoot.getLocation().toString()+endPath);
				return path;
			}
		return fText.getText(); // return existing working directory value
	}
	protected void updateContents(Object value) {
		if(value!=null)
			fText.setText(value.toString());
	}
}
