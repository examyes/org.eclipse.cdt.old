/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.cdt.oprofile.opxml.HeaderProcessor;


/**
 * A class which represents a real sample file on disk
 * (i.e., /var/lib/oprofile/samples/}usr}bin}ls#0).
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class SampleFile extends SampleContainer {
	// SampleFile Header
	public class Header {
		// The cpu type ("PIII")
		public String cpuType;
		
		// The counter that this file collected
		public int counter;
		
		// The counter reset value
		public int count;
		
		// The event collected
		public int event;
		
		// The unit mask used during collection
		public int unitMask;
		
		// The approximate cpu speed in MHz
		public float cpuSpeed;
	}
	
	// The header of this samplefile
	protected Header _header = null;
		
	// TODO: these should go into ISample?
	
	// The count of all samples in this container
	protected int _count = -1;
	
	/**
	 * Constructor SampleFile.
	 * @param filename the disk filename of this SampleFile
	 */
	public SampleFile(String filename) {
		super(new File(filename));
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.core.ISample#getSampleCount()
	 */
	public int getSampleCount() {
		if (_count < 0) {
			_count = 0;
			for (Iterator i = _samples.iterator(); i.hasNext(); ) {
				Sample s = (Sample) i.next();
				_count += s.getSampleCount();
			}
		}
		
		return _count;
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.core.ISampleContainer#getEvent()
	 */
	public OpEvent getEvent() {
		return Oprofile.findEvent(_header.counter, _header.event);
	}
	
	/**
	 * Set the header for this SampleFile.
	 * @param map the map of values from the HeaderProcessor
	 */
	public void setHeader(HashMap map) {
		_header = new Header();
		_header.counter = Integer.parseInt((String) map.get(HeaderProcessor.COUNTER));
		_header.event = Integer.parseInt((String) map.get(HeaderProcessor.EVENT));
		_header.count = Integer.parseInt((String) map.get(HeaderProcessor.COUNT));
		_header.cpuSpeed = Float.parseFloat((String) map.get(HeaderProcessor.CPU_SPEED));
		_header.cpuType = (String) map.get(HeaderProcessor.CPU_TYPE);
		_header.unitMask = Integer.parseInt((String) map.get(HeaderProcessor.UNIT_MASK));
	}

	/**
	 * Fetches the debug info for this samplefile's samples
	 */
	public void getDebugInfo() {
		Oprofile.getDebugInfo(this);
	}
}
