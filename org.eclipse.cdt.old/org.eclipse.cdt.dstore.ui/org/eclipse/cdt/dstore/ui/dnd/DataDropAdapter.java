package com.ibm.dstore.ui.dnd;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.part.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

public class DataDropAdapter extends ViewerDropAdapter  
{
  protected Shell shell;
  protected long hoverStart = 0;
  protected static final long hoverThreshold = 1500;
  private DataStore _dataStore;
  
  public DataDropAdapter(StructuredViewer viewer, DataStore dataStore) 
  {
    super(viewer);
    _dataStore = dataStore;    
  }


  public boolean performDrop(Object data) 
  {
      /***
    byte[] result = ((PluginTransferData)data).getData();
    String str = new String(result);

    if (getCurrentTarget() != null)
    {
      DataElement dropTarget = (DataElement)getCurrentTarget();      

      DataStore currentDataStore = dropTarget.getDataStore();
      DataElement dropSource = currentDataStore.find(str);

      DataElement command = getCommand(getCurrentOperation(), dropTarget);

      if (command != null && dropSource != null)
      {
	((IDataElementViewer)getViewer()).setSelected(dropTarget);

	DataElement sourceRef = currentDataStore.createElement();
	sourceRef.reInit(dropSource);
        currentDataStore.command(command, sourceRef, dropTarget);
      }
    }
      ***/
    return false;   
  }


  public boolean validateDrop(Object target, int operation, TransferData transferType) 
  {
    if (target instanceof DataElement)
      {	
	DataElement object = (DataElement) target;	
        DataElement command = getCommand(operation, object);
        if (command != null)
        {
          return true;	
        }
      }

    return false;	
  }
 
  public DataElement getSelected()
  {
    DataElement selected = ConvertUtility.convert(getViewer().getSelection());    
    return selected; 
  }

  public DataElement getCommand(int operation, DataElement dropTarget)
      {
        switch (operation)
        {
        case DND.DROP_MOVE:
          return dropTarget.getCommandFor("C_MOVE");            
        case DND.DROP_COPY:
          return dropTarget.getCommandFor("C_COPY");                      
        default:
          break;
        }

        return null;
      }

}
