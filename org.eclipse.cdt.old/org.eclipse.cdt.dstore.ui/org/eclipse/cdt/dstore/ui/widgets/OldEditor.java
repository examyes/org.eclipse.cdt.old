package org.eclipse.cdt.dstore.ui.widgets;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
 
import java.io.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;

import org.eclipse.swt.graphics.Color;


import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import java.io.ByteArrayInputStream;


public class OldEditor extends SourceViewer implements ILinkable
{
  private DataElement _input;


  public OldEditor(Composite parent, IVerticalRuler ruler, int styles) 
      {
	super(parent, ruler, styles);
      }


  public void resetView()
  {
  }
  

  public boolean canSaveContents()
      {
        return true;
      }

  public boolean isDirty()
  {    
    return false;
    
  }

  protected void createControl(Composite parent, int styles) 
      {
        super.createControl(parent, styles);
        Font theFont = new Font(parent.getDisplay(), "Courier New", 8, SWT.NULL);
        getTextWidget().setFont(theFont);
      }

  public void setInput(DataElement input)
      {
        _input = input;	

	//        setDocument((TextDocument)_input.getElementProperty(DE.P_SOURCE));
        Integer location = (Integer)_input.getElementProperty(DE.P_SOURCE_LOCATION);
        setTop(location.intValue() - 1);	
      }

  public boolean isLinked()
      {
        return false;
      }

    public boolean isLinkedTo(ILinkable to)
    {
	return false;
    }

  public void setLinked(boolean flag)
      {
      }

  public void linkTo(ILinkable v)
  {
  }
  
  public void unlinkTo(ILinkable v)
  {
  }

  public void setTop(int nTop)
      {
	getTextWidget().setTopIndex(nTop);
      }

  public void viewerInstalled()
      {
      }

  /*
 public boolean doSaveContents(Object element)
      {
        DataElement theElement = (DataElement)element;
        String contents = new String(fTextWidget.getText());
        if (theElement != null)
        {
          DataStore dataStore = theElement.getDataStore();

          String sourceString = theElement.getSource();
          int indexOfLocation = sourceString.lastIndexOf(":");
          if (indexOfLocation > 1)
          {	
            sourceString = sourceString.substring(0, indexOfLocation);
          }

          // do local save
          dataStore.saveFile(sourceString, contents);

          // do remote save
          dataStore.replaceFile(sourceString, contents);
        }
        return true;
      }
  */
  /*
  public void fillContextMenu(ContextMenuManager menu)
  {
    if (menu.isEmpty())
      {
	super.fillContextMenu(menu);
       
	//contributeAction(menu, "Code Assist", "CodeAssist");
      }
    else
      {	
	super.fillContextMenu(menu);
      }
  }
  protected IAction createAction(String actionId)
  {
    if ("CodeAssist".equals(actionId))
      {
	return new CodeAssistAction(this, actionId);
      }
    return super.createAction(actionId);
  }
  */

}
