/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.launch;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.oprofile.core.OpEvent;
import org.eclipse.cdt.oprofile.core.Oprofile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;


/**
 * This class represents an oprofile runtime configuration of a counter. It is
 * used to construct arguments for launching op_start on the host.
 * @author keiths
 */
public class OprofileCounter
{
	private static final String COUNTER_STRING = OprofileLaunchMessages.getString("oprofileCounter.counterString"); //$NON-NLS-1$
	private String ARG_COUNTER() { return "--ctr" + getNumber() + "-"; } //$NON-NLS-1$ //$NON-NLS-2$
	private String ARG_EVENT() { return ARG_COUNTER() + "event"; } //$NON-NLS-1$
	private String ARG_COUNT() { return ARG_COUNTER() + "count"; } //$NON-NLS-1$
	private String ARG_PROFILE_KERNEL() { return ARG_COUNTER() + "kernel"; } //$NON-NLS-1$
	private String ARG_PROFILE_USER() { return ARG_COUNTER() + "user"; } //$NON-NLS-1$
	private String ARG_UNIT_MASK() { return ARG_COUNTER() + "unit-mask"; } //$NON-NLS-1$
	private String ARG_NONE() { return "none"; }; //$NON-NLS-1$

	private int _number;

	private boolean _enabled;
	private OpEvent _event;
	private boolean _profileKernel;
	private boolean _profileUser;
	private int _count;
	private OpEvent[] _eventList = null;

	/**
	 * Constructor for OprofileCounter.
	 * @param nr	the counter number
	 */
	public OprofileCounter(int nr)
	{
		_number = nr;
		_enabled = false;
		_profileKernel = true;
		_profileUser = true;
		_count = -1;
		_eventList = Oprofile.getEvents(_number);
	}

	/**
	 * Constructs all of the counters in  the given launch configuration.
	 * @param config the launch configuration
	 * @return an array of all counters
	 */
	public static OprofileCounter[] getCounters(ILaunchConfiguration config)
	{
		OprofileCounter[] ctrs = new OprofileCounter[Oprofile.getNumberOfCounters()];
		for (int i = 0; i < ctrs.length; i++)
		{
			ctrs[i] = new OprofileCounter(i);
			if (config != null)
				ctrs[i].loadConfiguration(config);
		}
		
		return ctrs;
	}

	/**
	 * Method setEnabled.
	 * @param enabled	whether to set this counter as enabled
	 */
	public void setEnabled(boolean enabled)
	{
		_enabled = enabled;
	}
	
	/**
	 * Method setEvent.
	 * @param event	the event for this counter
	 */
	public void setEvent(OpEvent event)
	{
		_event = event;
	}
	
	/**
	 * Method setProfileKernel.
	 * @param profileKernel	whether this counter should count kernel events
	 */
	public void setProfileKernel(boolean profileKernel)
	{
		_profileKernel = profileKernel;
	}
	
	/**
	 * Method setProfileUser.
	 * @param profileUser	whether this counter should count user events
	 */
	public void setProfileUser(boolean profileUser)
	{
		_profileUser = profileUser;
	}
	
	/**
	 * Method setCount.
	 * @param count	the number of events between samples for this counter
	 */
	public void setCount(int count)
	{
		_count = count;
	}
		
	/**
	 * Saves this counter's configuration into the specified launch
	 * configuration.
	 * @param config	the launch configuration
	 */
	public void saveConfiguration(ILaunchConfigurationWorkingCopy config)
	{
		config.setAttribute(LaunchPlugin.ATTR_COUNTER_ENABLED(_number), _enabled);
		if (_event != null)
		{
			config.setAttribute(LaunchPlugin.ATTR_COUNTER_EVENT(_number), _event.getText());
			config.setAttribute(LaunchPlugin.ATTR_COUNTER_UNIT_MASK(_number), _event.getUnitMask().getMaskValue());
		}
		config.setAttribute(LaunchPlugin.ATTR_COUNTER_PROFILE_KERNEL(_number), _profileKernel);
		config.setAttribute(LaunchPlugin.ATTR_COUNTER_PROFILE_USER(_number), _profileUser);
		config.setAttribute(LaunchPlugin.ATTR_COUNTER_COUNT(_number), _count);
	}
	
	/**
	 * Loads a counter configuration from the specified launch configuration.
	 * @param config	the launch configuration
	 */
	public void loadConfiguration(ILaunchConfiguration config)
	{
		try
		{
			_enabled = config.getAttribute(LaunchPlugin.ATTR_COUNTER_ENABLED(_number), false);

			String str = config.getAttribute(LaunchPlugin.ATTR_COUNTER_EVENT(_number), ""); //$NON-NLS-1$
			_event = _eventFromString(str);

			int maskValue =  config.getAttribute(LaunchPlugin.ATTR_COUNTER_UNIT_MASK(_number), -1);
			if (maskValue > -1)
				_event.getUnitMask().setMaskValue(maskValue);
			
			_profileKernel = config.getAttribute(LaunchPlugin.ATTR_COUNTER_PROFILE_KERNEL(_number), false);
			_profileUser = config.getAttribute(LaunchPlugin.ATTR_COUNTER_PROFILE_USER(_number), false);
			
			// NOTE: -1 means "query oprofile library"
			_count = config.getAttribute(LaunchPlugin.ATTR_COUNTER_COUNT(_number), -1);
		}
		catch (CoreException ce)
		{
		}
	}
	
	/**
	 * Returns a textual label for this counter (used by UI)
	 * @return the label to use in widgets referring to this counter
	 */
	public String getText()
	{
		Object[] args = new Object[] {new Integer(_number)};
		return MessageFormat.format(COUNTER_STRING, args);
	}
	
	/**
	 * Method getNumber.
	 * @return the counter's number
	 */
	public int getNumber()
	{
		return _number;
	}
	
	/**
	 * Method getEnabled.
	 * @return whether this counter is enabled
	 */
	public boolean getEnabled()
	{
		return _enabled;
	}

	/**
	 * Method getEvent.
	 * @return the event for this counter
	 */
	public OpEvent getEvent()
	{
		return _event;
	}
	
	/**
	 * Method getProfileKernel.
	 * @return whether this counter is counting kernel events
	 */
	public boolean getProfileKernel()
	{
		return _profileKernel;
	}
	
	/**
	 * Method getProfileUser.
	 * @return whether this counter is counting user events
	 */
	public boolean getProfileUser()
	{
		return _profileUser;
	}

	/**
	 * Method getCount.
	 * @return the number of events between samples for this counter
	 */
	public int getCount()
	{
		if (_count == -1) {
			// Used to do this, but this is ill-advised... It can make the machine really busy.
			//_count = _event.getMinCount();
			
			// This is what Oprofile does in oprof_start.cpp:
			double speed = Oprofile.getCpuFrequency();
			if (speed == 0.0) {
				_count = _event.getMinCount() * 100;
			} else {
				_count = (int) speed * 500;
			}
		}
		
		return _count;
	}
	
	/**
	 * Method getEvents.
	 * @return an array of all events that this counter can monitor
	 */
	public OpEvent[] getValidEvents()
	{		
		return _eventList;
	}

	// Returns the event with the same label as the parameter STR
	private OpEvent _eventFromString(String str)
	{
		for (int i = 0; i < _eventList.length; i++)
		{
			if (_eventList[i].getText().equals(str))
				return _eventList[i];
		}
		
		return null;
	}
	
	/**
	 * Converts this counter configuration into a Collection of arguments
	 * that may be passed to op_start.
	 * @return a Collection of arguments
	 */
	public Collection toArguments()
	{
		ArrayList args = new ArrayList();
		if (getEnabled()) {
			args.add(_argEvent());
			args.add(_argCount());
			args.add(_argProfileKernel());
			args.add(_argProfileUser());
			args.add(_argUnitMask());
		} else {
			args.add(_argDisabled());
		}
		return args;
	}

	// 	returns the cli parameter for the event to monitor
	private String _argEvent()
	{
		return new String(ARG_EVENT() + "=" + getEvent().getText()); //$NON-NLS-1$
	}

	// returns the cli parameter for the count
	private String _argCount()
	{
		return new String(ARG_COUNT() + "=" + getCount()); //$NON-NLS-1$
	}
	
	// returns the cli parameter for counting kernel events
	private String _argProfileKernel()
	{
		return new String(ARG_PROFILE_KERNEL() + "=" + (getProfileKernel() ? "1" : "0")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	// returns the cli parameter for counting user events
	private String _argProfileUser()
	{
		return new String(ARG_PROFILE_USER() + "=" + (getProfileUser() ? "1" : "0")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	// returns the cli parameter for the unit mask
	private String _argUnitMask()
	{
		return new String(ARG_UNIT_MASK() + "=" + getEvent().getUnitMask().getMaskValue()); //$NON-NLS-1$
	}

	// returns the cli parameter for a disabled counter
	private String _argDisabled() {
		return new String(ARG_EVENT() + "=" + ARG_NONE());
	}
}

