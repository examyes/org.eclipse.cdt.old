package com.ibm.debug.internal.pdt.ui.views;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/views/StorageViewCellModifier.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 16:00:52)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.internal.picl.PICLStorage;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Item;


public class StorageViewCellModifier implements ICellModifier {
	public boolean canModify(Object element, String property) {
		return true;
	}
	public Object getValue(Object element, String property) {
		// give back the value of the column
		StorageViewLine line = (StorageViewLine)element;
		if (line.P_ADDRESS.equals(property))
		   return line.getAddress();
		else if (line.P_1STBYTE.equals(property))
		   return line.get1stByte();
	    else if (line.P_2NDBYTE.equals(property))
		   return line.get2ndByte();
        else if (line.P_3RDBYTE.equals(property))
		   return line.get3rdByte();
		else if (line.P_4THBYTE.equals(property))
		   return line.get4thByte();
		else if (line.P_TRANSLATED.equals(property))
		   return line.getTranslated();
		else
		   return null;
	}
	public void modify(Object element, String property, Object value) {
		if (value == null) { return; } //the value is not valid, so we shouldn't update

		String newValue = (String)value;
		StorageViewLine line = (StorageViewLine)((Item)element).getData();
		PICLStorage storage = line.getStorage();

		if (line.P_TRANSLATED.equals(property) && newValue.length() < 16) {
			for (int i=0; (newValue).length() < 16; i++)
				newValue = " " + newValue; //prepend some spaces
		} else if (newValue.length() < 8) {
			for (int i=0; (newValue).length() < 8; i++)
				newValue = "0" + newValue; //prepend some zeros
		}

		if (line.P_ADDRESS.equals(property)) {
			//do nothing here.  the cellValidatorListener takes care of "editing" addresses
		}
		else if (line.P_1STBYTE.equals(property))
			storage.updateStorage(PICLStorage.RAW, getOffset(storage, line.getAddress(), 0), newValue);
	    else if (line.P_2NDBYTE.equals(property))
			storage.updateStorage(PICLStorage.RAW, getOffset(storage, line.getAddress(), 4), newValue);
        else if (line.P_3RDBYTE.equals(property))
			storage.updateStorage(PICLStorage.RAW, getOffset(storage, line.getAddress(), 8), newValue);
		else if (line.P_4THBYTE.equals(property))
			storage.updateStorage(PICLStorage.RAW, getOffset(storage, line.getAddress(), 12), newValue);
		else if (line.P_TRANSLATED.equals(property))
			storage.updateStorage(PICLStorage.TRANSLATED, getOffset(storage, line.getAddress(), 0), newValue);
	}

	private int getOffset(PICLStorage storage, String lineAddress, int lineOffset) {
		int offset = (int)(Long.parseLong(lineAddress, 16) - Long.parseLong(storage.getAddress(), 16));
		return offset + lineOffset;
	}
}

