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
 * This class finds sample files which "belong" to another sample file. It is used
 * to deal with oprofile's "--separate" option, which can output files like "}foo}bar}baz.exe#0"
 * and "}foo}bar}baz.exe}}}lib}i686}libc-2.2.93#0"
 * @author keiths
 */
public class SeparateSampleFileFilter implements FileFilter
{
	// The filename of the "parent" executable
		private String _filename;
	
	/**
	 * Constructor SeparateSampleFileFilter.
	 * @param file	the SampleFile for which to get separates
	 */
	public SeparateSampleFileFilter(String exeName)
	{
		// Strip counter number
		int index = exeName.indexOf("#"); //$NON-NLS-1$
		_filename = exeName.substring(0, index);
	}

	public boolean accept(File file)
	{
		// Accept only if file starts with _filename. Other details should be handled
		// by SampleFileFilter.
		if (file.isFile() && file.toString().startsWith(_filename))
			return true;
		
		return false;
	}
}
