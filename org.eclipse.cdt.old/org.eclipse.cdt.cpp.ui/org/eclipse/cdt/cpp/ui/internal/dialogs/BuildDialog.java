package com.ibm.cpp.ui.internal.dialogs;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;

import java.io.*;
import java.util.*;
import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.window.*;
import org.eclipse.core.resources.*;

public class BuildDialog extends org.eclipse.jface.dialogs.Dialog
{

	class ActivationListener extends ShellAdapter {
		public void shellActivated(ShellEvent e) {
                  fActiveWindow= (Window) e.widget.getData();
                  updateButtonState();
		}

          public void shellDeactivated(ShellEvent e) {
            fActiveWindow= null;
            updateButtonState();
          }
	};

  public static final int HISTORY_SIZE= 10;

  protected ArrayList    _history;
  protected String    _historyProperty;
  protected IResource _resource;
  private String      _invocation;

  protected Window fParentWindow;
  protected Window fActiveWindow;

  protected ActivationListener fActivationListener= new ActivationListener();

  protected Combo fCommandField;
  protected Rectangle fDialogPositionInit;


   private final static int SIZING_SELECTION_WIDGET_WIDTH = 100;
   private final static int SIZING_TEXT_FIELD_WIDTH = 400;


  public BuildDialog(IResource selectedResource, String historyProperty)
      {
        super(null);
        _historyProperty = historyProperty;
        _resource = selectedResource;


        _history = new ArrayList(HISTORY_SIZE - 1);
        _history.removeAll(_history);
        _history = CppPlugin.readProperty(_resource, _historyProperty);

        fParentWindow= null;
        fDialogPositionInit= null;

	if (!_history.isEmpty())
        {
          _invocation= (String)(_history.get(0));
        }
        else
        {
          _invocation= new String("");
        }
      }

  public String getInvocation()
  {
    return _invocation;
  }


  public void setReturnCode(int i)
  {
    super.setReturnCode(i);
  }

  protected Composite createButtonSection(Composite parent)
      {
        Composite panel= new Composite(parent, SWT.NULL);

        GridLayout layout= new GridLayout();
        layout.numColumns= 2;
        layout.makeColumnsEqualWidth= true;
        panel.setLayout(layout);

        return panel;
      }

    public Control createContents(Composite parent)
    //  public Control createDialogArea(Composite parent)
      {
	 super.createContents(parent);

	Composite panel= (Composite)getDialogArea();

        GridLayout layout= new GridLayout();
        layout.numColumns= 1;
        layout.makeColumnsEqualWidth= true;
        panel.setLayout(layout);

        Composite inputPanel= createInputPanel(panel);
        setGridData(inputPanel, GridData.FILL, true, GridData.CENTER, false, SIZING_TEXT_FIELD_WIDTH);

        updateButtonState();

	getShell().setText("Issue build command");

        return panel;
      }
	
  protected Composite createInputPanel(Composite parent)
      {
        ModifyListener listener= new ModifyListener()
        {
          public void modifyText(ModifyEvent e)
          {
            updateButtonState();
          }
        };

        Composite panel= new Composite(parent, SWT.NULL);
        GridLayout layout= new GridLayout();
        layout.numColumns= 2;
        panel.setLayout(layout);

        Label commandLabel= new Label(panel, SWT.LEFT);
        commandLabel.setText("Build Command");
        setGridData(commandLabel, GridData.BEGINNING, false, GridData.CENTER, false, 0);

        fCommandField= new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
        setGridData(fCommandField, GridData.FILL, true, GridData.CENTER, false, 0);
        fCommandField.addModifyListener(listener);

        for (int i = 0; i < _history.size(); i++)
        {
          String item = (String)_history.get(i);
          if (item != null)
          {
            fCommandField.add(item, i);
          }

	  if (i == 0)
	    {	
	      fCommandField.setText(item);
	    }
	
        }

	

        return panel;
      }


  protected ArrayList getCommandHistory()
      {
        return _history;
      }

  public String getCommandString()
      {
          _invocation = new String(fCommandField.getText());
        return _invocation;
      }

  public boolean close()
      {
        String command = fCommandField.getText();
        if (_history.contains(command))
        {
          _history.remove(command);
        }
        _history.add(0, command);
        CppPlugin.writeProperty(_resource, _historyProperty, _history);
        return super.close();
      }


  public void initCommandStringFromSelection()
      {
          if ("".equals(fCommandField.getText()))
          {
            if (!_invocation.equals(""))
              fCommandField.setText(_invocation);
          }
          fCommandField.setFocus();
      }

  private void initHistory(ArrayList history, ArrayList init)
      {
        history.removeAll(history);
        for (int i= 0; i < init.size() && i < HISTORY_SIZE - 1; i++) {
          history.add(init.get(i));
        }
      }

  protected Button makeButton(Composite parent, int id, String text, boolean dfltButton, SelectionListener listener)
      {
        return createButton(parent, id, text, dfltButton);
      }


	protected void setGridData(Control component, int horizontalAlignment, boolean grabExcessHorizontalSpace, int verticalAlignment, boolean grabExcessVerticalSpace, int widthHint) {
		GridData gd= new GridData();
		gd.horizontalAlignment= horizontalAlignment;
		gd.grabExcessHorizontalSpace= grabExcessHorizontalSpace;
		gd.verticalAlignment= verticalAlignment;
		gd.grabExcessVerticalSpace= grabExcessVerticalSpace;

      if (widthHint != 0)
      	gd.widthHint = widthHint;

		component.setLayoutData(gd);
	}

  public void updateButtonState()
      {
          String selectedText= null;

          boolean selection= (selectedText != null && selectedText.length() > 0);

          String str= getCommandString();
          boolean commandString= (str != null && str.length() > 0);
      }

  private void updateCombo(Combo combo, ArrayList content)
      {
        combo.removeAll();
        for (int i= 0; i < content.size(); i++) {
          combo.add(content.get(i).toString());
        }
      }


  protected void updateCommandHistory()
      {
          updateHistory(fCommandField);
      }

  private void updateHistory(Combo combo)
      {
        String invocation = getCommandString();
        int index= _history.indexOf(invocation);
        if (index != 0)
        {
          if (index != -1)
          {
            _history.remove(index);
          }
          _history.add(0, invocation);
          updateCombo(combo, _history);
          combo.setText(invocation);
        }

      }

  protected void updateWindow(Window window)
      {
        if (window != fParentWindow)
        {
          if (fParentWindow != null)
          {
            Shell s= fParentWindow.getShell();
            s.removeShellListener(fActivationListener);
            s.dispose();
          }

          fParentWindow= window;
	  //***          fParentWindow.addFocusChangedListener(fFocusListener);
          Shell s= fParentWindow.getShell();
          s.addShellListener(fActivationListener);
          open();
        }
      }
}
