package com.ibm.debug.internal.pdt.ui.editor;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/editor/DebuggerParser.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 15:59:33)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.lpex.core.LpexCommonParser;
import com.ibm.lpex.core.LpexPaletteAttributes;
import com.ibm.lpex.core.LpexView;

public class DebuggerParser extends LpexCommonParser {
	// two element classes
	static final String
		CLASS_EXECUTABLE    = "executable",
		CLASS_NONEXECUTABLE = "nonexecutable";

	// the two element classes as bit-masks, and a mask for all our classes
	private long
		classExecutable,
		classNonExecutable,
		classAll;
	private boolean[] lineIsExecutable;

	/**
	 * Constructor for SourceHighlightingParser
	 */
	public DebuggerParser(LpexView lpexView) {
		super(lpexView);

		lineIsExecutable = null;

		String toBackground = LpexPaletteAttributes.background(view);
		// executable style = blue/white
		String attributes = LpexPaletteAttributes.convert("0 0 255 255 255 255",
		                                                  BACKGROUND_COLOR,
		                                                  toBackground);
		view.doDefaultCommand("set styleAttributes.e " + attributes);
		// nonexecutable style = green/white
		attributes = LpexPaletteAttributes.convert("0 128 128 255 255 255",
		                                           BACKGROUND_COLOR,
		                                           toBackground);
		view.doDefaultCommand("set styleAttributes.n " + attributes);

		// enable the "executable" and "nonexecutable" element classes and
		// get the bits allocated for them
		classExecutable    = view.registerClass(CLASS_EXECUTABLE);
		classNonExecutable = view.registerClass(CLASS_NONEXECUTABLE);

		// keep a bit-mask of all our element classes
		classAll = classExecutable | classNonExecutable;

	}

	/**
	 * @see LpexCommonParser#parseElement(int)
	 */
	public void parseElement(int element) {
		parseOneElement(element);
	}

	/**
	 * @see LpexCommonParser#parseAll()
	 */
	public void parseAll() {

		if (lineIsExecutable == null)
			return;

		for (int element = 1; element <= view.elements(); element++)
			parseOneElement(element);
	}

	private void parseOneElement(int element) {
		// query element's text and current classes (as possibly set by others)
		String text    = view.elementText(element);
		long   classes = view.elementClasses(element) & ~classAll;

		// establish the new styles, and which of our element classes to set
		String styles = "";

		if (lineIsExecutable == null)
			return;

		if (element < lineIsExecutable.length && lineIsExecutable[element])	{
			styles = styleString('e', text.length());
			classes |= classExecutable;
		} else {
			styles = styleString('n', text.length());
			classes |= classNonExecutable;
		}

		// set the element's display styles, and our element classes
		view.setElementStyle(element, styles);
		view.setElementClasses(element, classes);
	}

	public void setLineIsExecutable(boolean newArray[]) {
		lineIsExecutable = newArray;
	}
}
