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

public class BuildInvocationEntry 
{
    private  Combo  _buildInvocation;
    private  Combo  _cleanInvocation;

    private Control  _control;
    
    public BuildInvocationEntry(Composite parent, String labelText, String defaultEntryText) 
    {
	this.createControl(parent, labelText, defaultEntryText);
    }

    public BuildInvocationEntry(Composite parent, 
				String blabelText, String bdefaultEntryText,
				String clabelText, String cdefaultEntryText) 
    {
	this.createControl(parent, blabelText, bdefaultEntryText, clabelText, cdefaultEntryText);
    }
    
    public ArrayList getBuildInvocations()
    {
	ArrayList items = new ArrayList();
	items.add(new String(_buildInvocation.getText()));
	return items;
    }

    public ArrayList getCleanInvocations()
    {
	ArrayList items = new ArrayList();
	items.add(new String(_cleanInvocation.getText()));
	return items;
    }

    public String getBuildText()
    {
	return _buildInvocation.getText();
    }

    public String getCleanText()
    {
	return _cleanInvocation.getText();
    }

    public void setBuildText(String text)
    {
	_buildInvocation.setText(text);
    }

    public void setCleanText(String text)
    {
	_cleanInvocation.setText(text);
    }

    public void addBuild(String text, int index)
    {
	_buildInvocation.add(text, index);
    }

    public void addClean(String text, int index)
    {
	_cleanInvocation.add(text, index);
    }
 
    public Control createControl(Composite parent, String blabelText, String defaultBuildText) 
    {
	return createControl(parent, blabelText, defaultBuildText, null, null);
    }

    public Control createControl(Composite parent, 
				 String blabelText, String defaultBuildText,
				 String clabelText, String defaultCleanText) 
    {	
	Composite composite= new Composite(parent, SWT.NONE);
	composite.setLayout(new GridLayout());
	
	Composite cnr = new Composite(composite, SWT.NONE);
	
	Label blabel = new Label(cnr, SWT.NULL);
	blabel.setText(blabelText);
	
	_buildInvocation = new Combo(cnr, SWT.SINGLE | SWT.BORDER);
	GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	textData.widthHint = 200;
	_buildInvocation.setLayoutData(textData);	
	_buildInvocation.setText(defaultBuildText);

	if (clabelText != null)
	    {
		Label clabel = new Label(cnr, SWT.NULL);
		clabel.setText(clabelText);
		
		_cleanInvocation = new Combo(cnr, SWT.SINGLE | SWT.BORDER);
		GridData textData2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		textData2.widthHint = 200;
		_cleanInvocation.setLayoutData(textData2);	
		_cleanInvocation.setText(defaultCleanText);
	    }

	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	layout.marginHeight = 5;
	layout.marginWidth = 5;
	layout.verticalSpacing=5;
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
