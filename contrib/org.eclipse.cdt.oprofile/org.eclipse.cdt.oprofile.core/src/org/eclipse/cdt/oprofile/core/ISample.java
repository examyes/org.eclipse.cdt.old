/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.core;

/**
 * This is a simple interface that the UI can use to get sample counts
 * for all objects which contain samples.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public interface ISample
{
	/**
	 * Computes the total of all samples in the implementing class.
	 * @return	the total count of all samples
	 */
	int getSampleCount();
}
