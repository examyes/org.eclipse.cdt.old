/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core;

import java.io.File;
import java.io.FileFilter;

/**
 * A filter class for filtering sample files. This is a wrapper to any UI-specified
 * filter. The main purpose of this class is to find samples of a given counter.
 * @author keiths
 */
public class SampleFileFilter implements FileFilter
{
	// The counter to filter on
	private int _counter;
	
	// The user-specified filter (or null)
	private FileFilter _filter;
	
	private int _what;
	public static final int FILTER_SAMPLEFILES = 1;
	public static final int FILTER_SEPARATEFILES = 2;
	
	/**
	 * Method SampleFileFilter.
	 * @param counter	the counter
	 * @param filter	a user-specified filter (or null)
	 */
	public SampleFileFilter(int what, int counter, FileFilter filter)
	{
		_what = what;
		_counter = counter;
		_filter = filter;
	}

	/**
	 * @see java.io.FileFilter#accept(File)
	 */
	public boolean accept(File file)
	{
		if (file.isFile())
		{
			// Check if this is a sample file for the correct counter
			if (file.toString().endsWith("#" + _counter)) //$NON-NLS-1$
			{
				boolean isSeparateSampleFile = Oprofile.isSeparateSampleFile(file);
				if ((!isSeparateSampleFile && _what == FILTER_SAMPLEFILES)
					|| (isSeparateSampleFile && _what == FILTER_SEPARATEFILES))
				{
					// Now check if it passes the supplied filter, if any
					return (_filter == null ? true : _filter.accept(file));
				}
			}
		}
		else if (file.isDirectory())
		{
			// Check if this session has any matching samples
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; ++i)
			{
				if (accept(files[i]))
					return true;
			}
		}
			
		return false;
	}
}
