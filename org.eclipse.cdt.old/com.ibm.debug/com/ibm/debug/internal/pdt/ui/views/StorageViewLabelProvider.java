package com.ibm.debug.internal.pdt.ui.views;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/views/StorageViewLabelProvider.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 16:00:53)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class StorageViewLabelProvider
	extends LabelProvider
	implements ITableLabelProvider {

	/**
	 * Constructor for StorageViewLabelProvider
	 */
	public StorageViewLabelProvider() {
		super();
	}

	/**
	 * @see ITableLabelProvider#getColumnImage(Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/**
	 * @see ITableLabelProvider#getColumnText(Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		String columnLabel = null;
//		System.out.println("StorageViewLabelProvider::getColumnText " + columnIndex);
		switch (columnIndex) {
			case 0: {
				columnLabel = ((StorageViewLine)element).getAddress();
				break;
			}
			case 1: {
				columnLabel = ((StorageViewLine)element).get1stByte();
				break;
			}
			case 2: {
				columnLabel = ((StorageViewLine)element).get2ndByte();
				break;
			}
			case 3: {
				columnLabel = ((StorageViewLine)element).get3rdByte();
				break;
			}
			case 4: {
				columnLabel = ((StorageViewLine)element).get4thByte();
				break;
			}
			case 5: {
				columnLabel = ((StorageViewLine)element).getTranslated();
				break;
			}
			default: {
				// throw an exception here
			}
		}
		return columnLabel;

	}

}

