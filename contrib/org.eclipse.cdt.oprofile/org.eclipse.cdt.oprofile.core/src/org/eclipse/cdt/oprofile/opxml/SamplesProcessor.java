/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.opxml;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.cdt.oprofile.core.Sample;
import org.eclipse.cdt.oprofile.core.SampleFile;
import org.eclipse.cdt.oprofile.core.SampleSession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xml.sax.Attributes;


/**
 * A processor for opxml samples.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class SamplesProcessor extends XMLProcessor {
	/**
	 * The result of the SamplesProcessor. This should be passed as calldata
	 * by the caller.
	 */
	public static class CallData {
		public SampleSession session;
		public IProgressMonitor monitor;

		public CallData(IProgressMonitor mon, SampleSession ses) {
			monitor = mon;
			session = ses;
		}
	}

	// XML tags parsed by this processor
	public static final String DEMANGLED_NAME_TAG = "demangled-name"; //$NON-NLS-1$
	public static final String HEADER_TAG = "header"; //$NON-NLS-1$
	public static final String SAMPLE_TAG = "sample"; //$NON-NLS-1$
	public static final String SAMPLEFILE_TAG = "samplefile"; //$NON-NLS-1$
	private static final String _ATTR_FILENAME = "name"; //$NON-NLS-1$
	
	// The samplefile currently being constructed
	private SampleFile _sfile;
	private ArrayList _samplefiles;
	
	private HeaderProcessor _headerProcessor = new HeaderProcessor();
	private SampleProcessor _sampleProcessor = new SampleProcessor();

	// Separate sample files for _sfile
	private ArrayList _separates;
	private SampleFile _separate;
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.opxml.XMLProcessor#reset(java.lang.Object)
	 */
	public void reset(Object callData) {
		_separates = new ArrayList();
		_samplefiles = new ArrayList();
		_separate = null;
		_sfile = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.opxml.XMLProcessor#startElement(java.lang.String, org.xml.sax.Attributes, java.lang.Object)
	 */
	public void startElement(String name, Attributes attrs, Object callData) {
		if (name.equals(SAMPLEFILE_TAG)) {
			String filename = attrs.getValue(_ATTR_FILENAME); 

			if (_sfile == null) {
				// new samplefile
				_sfile = new SampleFile(filename);
			} else {
				// separate sample file
				_separate = new SampleFile(filename);
			}
		} else if (name.equals(HEADER_TAG)) {
			OprofileSAXHandler.getInstance(callData).push(_headerProcessor);
		} else if (name.equals(SAMPLE_TAG)) {
			// Yich. This would be a lot cleaner if debug info weren't such a pain...
			_sampleProcessor.setSampleFile((_separate == null ? _sfile : _separate));
			OprofileSAXHandler.getInstance(callData).push(_sampleProcessor);
		} else {
			super.startElement(name, attrs, callData);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.opxml.XMLProcessor#endElement(java.lang.String, java.lang.Object)
	 */
	public void endElement(String name, Object callData) {
		if (name.equals(SAMPLE_TAG)) {
			SampleFile file = (_separate == null ? _sfile : _separate);
			Sample sample = _sampleProcessor.getSample();
			file.addSample(sample);
			CallData cdata = (CallData) callData;
			if (cdata.monitor != null) {
				cdata.monitor.worked(sample.getSampleCount());
			}
		} else if (name.equals(SAMPLEFILE_TAG)) {
			if (_separate != null) {
				// Save _separate
				_separates.add(_separate);
				_separate = null;
			} else {
				// Save _separates and _sfile
				SampleFile[] files = new SampleFile[_separates.size()];
				_separates.toArray(files);
				_sfile.setSampleContainers(files);
				_samplefiles.add(_sfile);
				_separates.clear();
				_sfile = null;
			}
		} else if (name.equalsIgnoreCase(HEADER_TAG)) {
			// Save header map into samplefile
			HashMap map = _headerProcessor.getMap();
			SampleFile file = (_separate == null ? _sfile : _separate);
			file.setHeader(map);
		} else if (name.equals(OpxmlConstants.SAMPLES_TAG)) {
			// The end of samples processing. Save files in session
			SampleFile[] files = new SampleFile[_samplefiles.size()];
			_samplefiles.toArray(files);
			SampleSession session = ((CallData) callData).session;
			session.setSampleContainers(files);
		} else if (name.equals(DEMANGLED_NAME_TAG)) {
			SampleFile file = (_separate == null ? _sfile : _separate);
			file.setExecutableName(_characters);
		} else {
			super.endElement(name, callData);
		}
	}
}
