package com.ibm.cpp.ui.internal.wizards;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

/**
 *
 */
import org.eclipse.swt.layout.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import java.util.ArrayList;
import java.io.File;

public class BuildInvocationEntry {

public  Combo  _invocation;
  Control  _control;

/**
 */

public BuildInvocationEntry(Composite parent, String labelText, String defaultEntryText) {
	this.createControl(parent, labelText, defaultEntryText);
}

/**
 * createControl method comment.
 */
 public ArrayList getInvocations()
 {
  ArrayList items = new ArrayList();
  items.add(new String(_invocation.getText()));
 return items;
 }

    public String getText()
    {
	return _invocation.getText();
    }

    public void setText(String text)
    {
	_invocation.setText(text);
    }

    public void add(String text, int index)
    {
	_invocation.add(text, index);
    }
 
 public Control createControl(Composite parent, String labelText, String defaultEntryText ) {

   Composite composite= new Composite(parent, SWT.NONE);
   composite.setLayout(new GridLayout());

   Composite cnr = new Composite(composite, SWT.NONE);

   Label label = new Label(cnr, SWT.NULL);
   label.setText(labelText);

   _invocation = new Combo(cnr, SWT.SINGLE | SWT.BORDER);
   GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
   textData.widthHint = 200;
   _invocation.setLayoutData(textData);

   _invocation.setText(defaultEntryText);

   GridLayout layout = new GridLayout();
   layout.numColumns = 2;
   layout.marginHeight = 5;
   layout.marginWidth = 5;
   layout.verticalSpacing=0;
   cnr.setLayout(layout);
	GridData data = (GridData)cnr.getLayoutData();
	cnr.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

   _control = composite;
   return composite;
}

public Control getControl()
{
    return _control;
}

}
