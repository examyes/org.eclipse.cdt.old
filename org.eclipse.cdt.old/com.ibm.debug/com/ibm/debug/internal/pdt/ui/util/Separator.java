package com.ibm.debug.internal.pdt.ui.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/util/Separator.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 15:58:37)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class Separator extends DialogField {

	private Label fSeparator;
	private int fStyle;

	public Separator() {
		this(0);
	}

	public Separator(int style) {
		super();
		fStyle= style;
	}

	// ------- layout helpers

	public Control[] doFillIntoGrid(Composite parent, int nColumns, int height) {
		assertEnoughColumns(nColumns);

		Control separator= getSeparator(parent);
		separator.setLayoutData(gridDataForSeperator(nColumns, height));

		return new Control[] { separator };
	}

	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		return doFillIntoGrid(parent, nColumns, 4);
	}

	public int getNumberOfControls() {
		return 1;
	}

	protected static MGridData gridDataForSeperator(int span, int height) {
		MGridData gd= new MGridData();
		gd.horizontalAlignment= gd.FILL;
		gd.verticalAlignment= gd.BEGINNING;
		gd.heightHint= height;
		gd.horizontalSpan= span;
		return gd;
	}

	// ------- ui creation

	public Control getSeparator(Composite parent) {
		if (fSeparator == null) {
			assertCompositeNotNull(parent);
			fSeparator= new Label(parent, fStyle);
		}
		return fSeparator;
	}

}
