/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core;

import java.io.File;

import org.eclipse.swt.widgets.Shell;

/**
 * An interface that all sample-containing "root" objects contain (files and sessions).
 * @author Keith Seitz  <keiths@redhat.com>
 */
public interface ISampleContainer extends ISample
{
	/**
	 * Method getExecutableName().
	 *  @return the name of the executable with which this sample file corresponds.
	 */
	String getExecutableName();
	
	/**
	 * Method setExecutableName().
	 */
	void setExecutableName(String name);
	
	/**
	 * Method getFile().
	 * @return the File representing this container. This is a real File on disk.
	 */
	File getFile();
	
	/**
	 * Method getEvent.
	 * @return the event collected in this container
	 */
	OpEvent getEvent();
		
	/**
	 * Get any sample containers associated with this container.
	 * @param shell the shell to use for progress dialog (or <code>null</code> for none)
	 * @return any associated sample containers (never returns null)
	 */
	ISampleContainer[] getSampleContainers(Shell shell);
	
	/**
	 * Get the samples contained in this container.
	 * @param shell the shell to use for progress dialog (or <code>null</code> for none)
	 * @return samples in this container
	 */
	Sample[] getSamples(Shell shell);
}
