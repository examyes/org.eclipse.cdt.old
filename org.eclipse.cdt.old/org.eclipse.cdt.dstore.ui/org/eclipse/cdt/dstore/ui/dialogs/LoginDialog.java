package org.eclipse.cdt.dstore.ui.dialogs;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.util.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;

import org.eclipse.jface.dialogs.*;


public class LoginDialog extends org.eclipse.jface.dialogs.Dialog
{
  private   Text      _userText;
  private   Text      _passwordText;

  private  String     _user;
  private  String     _password;
    
    private DataStoreUIPlugin _plugin;

  public LoginDialog()
  {
    super(null);
    _plugin = DataStoreUIPlugin.getDefault();
  }

  protected void buttonPressed(int buttonId)
  {
    setReturnCode(buttonId);

    _user = _userText.getText();
    _password = _passwordText.getText();

    close();
  }

  protected void aboutToShow()
      {
      }

  public String getUser()
  {
    return _user;
  }

  public String getPassword()
  {
    return _password;
  }

  public Control createContents(Composite parent)
  {
      super.createContents(parent);
      
      Composite c= (Composite)getDialogArea();
      GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				       GridData.GRAB_HORIZONTAL);
      
      
      GridLayout layout= new GridLayout();
      layout.numColumns = 1;
      layout.marginHeight = 5;
      layout.marginWidth = 5;
      c.setLayout(layout);
      c.setLayoutData(new GridData(GridData.FILL_BOTH));

      Composite u = new Composite(c, SWT.NONE);
      Label userLabel = new Label(u, SWT.NONE);	
      userLabel.setText(_plugin.getLocalizedString("dialog.User_ID:"));
      
      _userText = new Text(u, SWT.SINGLE | SWT.BORDER);
      _userText.setText("");
      _userText.setLayoutData(textData);

      GridLayout uLayout = new GridLayout();
      uLayout.numColumns = 3;
      uLayout.marginHeight = 5;
      uLayout.marginWidth = 5;
      u.setLayout(uLayout);
      u.setLayoutData(new GridData(GridData.FILL_BOTH));


      Composite p = new Composite(c, SWT.NONE);      
      Label passwordLabel = new Label(p, SWT.NONE);	
      passwordLabel.setText(_plugin.getLocalizedString("dialog.Password:"));
      
      _passwordText = new Text(p, SWT.SINGLE | SWT.BORDER);
      _passwordText.setText("");
      _passwordText.setLayoutData(textData);
      _passwordText.setEchoChar('*');

      GridLayout pLayout = new GridLayout();
      pLayout.numColumns = 3;
      pLayout.marginHeight = 5;
      pLayout.marginWidth = 5;
      p.setLayout(pLayout);
      p.setLayoutData(new GridData(GridData.FILL_BOTH));

      getShell().setText(_plugin.getLocalizedString("dialog.Login"));
      
      return c;
  }
}


