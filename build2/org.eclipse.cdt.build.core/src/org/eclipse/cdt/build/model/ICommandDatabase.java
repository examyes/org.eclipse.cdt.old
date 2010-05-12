/**
 * 
 */
package org.eclipse.cdt.build.model;

import org.eclipse.core.resources.IResource;

/**
 * A database that stores the discovered commands from scanner discovery.
 * 
 * @author dschaefer
 */
public interface ICommandDatabase {

	/**
	 * Record a command for the given resource.
	 * 
	 * @param command
	 * @param resource
	 */
	void addCommand(DiscoveredCommand command, IResource resource);

	/**
	 * Get the command that was used to build the resource.
	 * 
	 * @param resource
	 * @return command
	 */
	DiscoveredCommand getCommand(IResource resource);
	
}
