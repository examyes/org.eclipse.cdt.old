/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core.opxml;

import org.eclipse.cdt.oprofile.core.OpInfo;

/**
 * XML handler class for opxml's "debug-info".
 * @see org.eclipse.cdt.oprofile.core.opxml.OpxmlRunner
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class NumCountersProcessor extends XMLProcessor {	
	// XML tags recognized by this processor
	private static final String _COUNTERS_TAG = "counters"; //$NON-NLS-1$
	
	/**
	 * @see org.eclipse.cdt.oprofile.core.XMLProcessor#reset()
	 */
	public void reset(Object callData) {
		OpInfo info = (OpInfo) callData;
		info.setNrCounters(0);
	}
		
	/**
	 * @see org.eclipse.cdt.oprofile.core.XMLProcessor#endElement(String)
	 */
	public void endElement(String name, Object callData) {		
		if (name.equals(_COUNTERS_TAG)) {
			OpInfo info = (OpInfo) callData;
			info.setNrCounters(Integer.parseInt(_characters));
		} else if (name.equals(OpInfoProcessor.NUM_COUNTERS_TAG)) {
			OprofileSAXHandler.getInstance(callData).pop(name);
		}
	}
}
