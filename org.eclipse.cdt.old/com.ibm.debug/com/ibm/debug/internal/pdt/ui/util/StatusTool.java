package com.ibm.debug.internal.pdt.ui.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/util/StatusTool.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 15:58:45)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.runtime.IStatus;
import com.ibm.debug.internal.pdt.ui.util.MessageLine;
import org.eclipse.jface.dialogs.DialogPage;


public class StatusTool {

	/**
	 * Applies the status to the status line of a dialog page
	 */
	public static void applyToStatusLine(MessageLine messageLine, IStatus status) {
		String[] messages= getErrorMessages(status);
		messageLine.setErrorMessage(messages[0]);
		messageLine.setMessage(messages[1]);
	}
	/**
	 * Applies the status to the status line of a dialog page
	 */
	public static void applyToStatusLine(DialogPage page, IStatus status) {
		String[] messages= getErrorMessages(status);
		page.setErrorMessage(messages[0]);
		page.setMessage(messages[1]);
	}
	/**
	 * Returns error-message / warning-message for a status
	 */
	public static String[] getErrorMessages(IStatus status) {
		String message= status.getMessage();
		if (status.matches(IStatus.ERROR) && !"".equals(message)) {
			return new String[] { message, null };
		} else if (status.matches(IStatus.WARNING | IStatus.INFO)) {
			return new String[] { null, message };
		} else {
			return new String[] { null, null };
		}
	}
	/**
	 * Compare two IStatus. The more severe is returned:
	 * An error is more severe than a warning, and a warning is more severe
	 * than ok.
	 */
	public static IStatus getMoreSevere(IStatus s1, IStatus s2) {
		if (s1.getSeverity() > s2.getSeverity()) {
			return s1;
		} else {
			return s2;
		}
	}
	/**
	 * Finds the most severe status from a array of status
	 * An error is more severe than a warning, and a warning is more severe
	 * than ok.
	 */
	public static IStatus getMostSevere(IStatus[] status) {
		IStatus max= null;
		for (int i= 0; i < status.length; i++) {
			IStatus curr= status[i];
			if (curr.matches(IStatus.ERROR)) {
				return curr;
			}
			if (max == null || curr.getSeverity() > max.getSeverity()) {
				max= curr;
			}
		}
		return max;
	}
}
