/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;

/**
 * SampleContainer is an abstract base class that is used to provide common
 * functionality to classes which contain samples (SampleFiles, which contain samples, and
 * SampleSessions, which contain SampleFiles).
 * @author Keith Seitz  <keiths@redhat.com>
 */
public abstract class SampleContainer implements ISampleContainer
{
	// The samples in this container
	protected ArrayList _samples;
	
	// The disk image of this container
	protected File _file;
	
	// The counter for this container
	protected int _counter;
	
	// The demangled name
	protected String _demangled_name = null;
	
	// The list of sample containers associated with this container (if any)
	protected ISampleContainer[] _containers;
	
	/**
	 * Constructor SampleContainer.
	 * @param counter the counter number
	 * @param file the disk image of this container
	 */
	public SampleContainer(int counter, File file)
	{
		_file = file;
		_counter = counter;
		_containers = null;
		_samples = new ArrayList();
	}
	
	/**
	 * Constructor. The counter number is derived from the filename.
	 * @param file the disk image of this container
	 */
	public SampleContainer(File file)
	{
		this(Oprofile.sampleFileCounter(file), file);
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.core.ISampleContainer#getExecutableName()
	 */
	public String getExecutableName()
	{
		return _demangled_name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.ISampleContainer#setExecutableName(java.lang.String)
	 */
	public void setExecutableName(String name) {
		_demangled_name = name;
	}
	
	/**
	 * @see org.eclipse.cdt.oprofile.core.ISampleContainer#getFile()
	 */
	public File getFile()
	{
		return _file;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.ISampleContainer#getSampleContainers()
	 */
	public ISampleContainer[] getSampleContainers(Shell shell) {
		return _containers;
	}
	
	/**
	 * Set the sample containers this container contains (i.e., "--separate-libs")
	 * @param containers the containers
	 */
	public void setSampleContainers(ISampleContainer[] containers) {
		_containers = containers;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.oprofile.core.ISampleContainer#getSamples()
	 */
	public Sample[] getSamples(Shell shell)
	{
		Sample[] samples = new Sample[_samples.size()];
		_samples.toArray(samples);
		return samples;
	}
	
	/**
	 * Add a sample to this container.
	 * @param smpl the sample
	 */
	public void addSample(Sample smpl) {
		_samples.add(smpl);
	}
	
	/**
	 * Get the counter for this session
	 * @return the counter number
	 */
	public int getCounter() {
		return _counter;
	}
}
