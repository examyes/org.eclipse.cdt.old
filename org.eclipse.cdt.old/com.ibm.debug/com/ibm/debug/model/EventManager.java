package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/EventManager.java, java-model, eclipse-dev, 20011128
// Version 1.15.1.2 (last modified 11/28/01 16:11:29)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Vector;

/** The default mode for the EventManager is 'queueEvents' but this can be
 *  changed by using the ctor which takes a mode argument, or by calling
 *  setMode(byte mode). A newly constructed EventManager also has the
 *  following default behaviour: i) If the EventManager is currently firing
 *  queued events, subsequent calls to fireAllQueuedEvents are not allowed -
 *  the method will simply return, and ii) Events are removed from the
 *  event queue after they are fired. These behaviours can be toggled by
 *  calling methods on the EventManager object.
 */

class EventManager
{
  EventManager()
  {
  }

  EventManager(byte mode)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(1, "Creating EventManager(mode: " + mode + ")");

    _mode = mode;
  }

  void addEvent(ModelEvent event, Vector listeners)
  {
    //if (Model.TRACE.DBG && Model.traceInfo())
      //Model.TRACE.dbg(4, "EventManager.addEvent(" + event + ", " + listeners + ")");

    event.setRequestProperty(_requestProperty);

    if (_mode == queueEvents)
       _queuedEvents.addElement(new QueuedEvent(event, listeners));
    else
       fireEvent(event, listeners);
  }

  /** Fire the event 'event' on every listener in the Vector 'listeners'.
   */

  void fireEvent(ModelEvent event, Vector listeners)
  {
    //if (Model.TRACE.DBG && Model.traceInfo())
      //Model.TRACE.dbg(4, "EventManager.fireEvent(" + event + ", " + listeners + ")");

    // As we go through the vector of listeners for the current event, we
    // want to keep track of how many entries are null. If the number
    // of nulls exceeds a threshold (_maxNumberOfNullListeners), then they
    // will be removed from the vector to keep it from getting too big.
    // See fireAllQueuedEvents.

    int numberOfNullListeners = 0;

    ModelEventListener listener = event.getPrivilegedListener();

    // If there's a privileged event listener associated with this
    // event, fire the event on that listener before firing it on
    // the listeners in the normal listener queue. If the privileged
    // listener vetoes the event, don't fire it on the listeners
    // in the normal listener queue:

    if (listener != null)
    {
       event.setIsVetoable(true);
       event.fire(listener);
    }

    event.setIsVetoable(false);

    if (!event.hasBeenVetoed())
    {
       // In the condition part of the following 'for' loop, it's important
       // to call the 'size()' method each time through the loop, rather than
       // get the size once and save it in a variable before entering the loop.
       // This is because the number of listeners may grow as events are being
       // fired. In particular, we want to handle the possibility that new listeners
       // are added BECAUSE the event was fired. Of course, this will only work if
       // the new listeners are added synchronously - if they are added
       // asynchronously (i.e. client launches a thread which adds a listener),
       // then there is no guarantee that the listener will get the event
       // currently being fired because the listener may not have been added
       // by the time we exit this loop.

       for (int i = 0; i < listeners.size(); i++)
       {
          listener = (ModelEventListener)listeners.elementAt(i);

          if (listener != null)
             event.fire(listener);
          else
             ++numberOfNullListeners;

          // If we lost connection with the engine while last event was being
          // fired the mode will have been set to 'abort' by
          // DebugEngine.handleLostConnection. This means "stop firing events":

          if (_mode == abort || event.hasBeenVetoed())
             break;
       }
    }

    _numberOfNullListeners = numberOfNullListeners;
  }

  void fireAllQueuedEvents()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, this + ".fireAllQueuedEvents()");

    if (_currentlyFiringQueuedEvents &&
        !_allowConcurrentFiringOfQueuedEvents) // Don't allow indirect recursion to occur.
       return;

    _currentlyFiringQueuedEvents = true;

    // In the condition part of the following 'for' loop, it's important
    // to call the 'size()' method each time through the loop, rather than
    // get the size once and save it in a variable before entering the loop.
    // This is because the number of events that need to be fired can change
    // as a result of doing synchronous requests during event firing. Such
    // requests may result in additional events being added to the queue.

    for (int i = 0; i < _queuedEvents.size(); i++)
        try
        {
          QueuedEvent queuedEvent = (QueuedEvent)_queuedEvents.elementAt(i);

          if (queuedEvent != null)
          {
	     Vector listeners = queuedEvent.getListeners();

	     fireEvent(queuedEvent.getEvent(), listeners);

             // If we lost connection with the engine while last event was being
             // fired the mode will have been set to 'abort' by
             // DebugEngine.handleLostConnection. This means "stop firing events":

             if (_mode == abort)
                break;

	     // Now that the event has been fired, go through the vector
	     // of listeners and remove null entries so that the list does
	     // not grow too big. Note that we do this AFTER the event has
	     // been fired since it is dangerous to modify the listener
	     // vector while events are being fired on it.

	     if (_numberOfNullListeners > _maxNumberOfNullListeners)
	     {
		synchronized(listeners)
		{
		  if (Model.TRACE.DBG && Model.traceInfo())
		  {
/*
		     Model.TRACE.dbg(3, "Number of null listeners (" +
					_numberOfNullListeners + ") exceeds threshold (" +
					_maxNumberOfNullListeners +
					"). About to remove null entries.");
*/
		     Model.TRACE.dbg(3, "Number of null listeners " +
					"exceeds threshold (" +
					_maxNumberOfNullListeners +
					"). About to remove null entries.");

		     Model.TRACE.dbg(3, "Size of listener vector BEFORE removing nulls=" +
					listeners.size());

		     Model.TRACE.dbg(3, "Listeners:");

		     for (int k=0; k < listeners.size(); k++)
			 if (listeners.elementAt(k) == null)
			    Model.TRACE.dbg(3, "null");
			 else
			    Model.TRACE.dbg(3, listeners.elementAt(k).toString());
		  }

		  int j = 0;

		  while (j < listeners.size())
		  {
		     Object listener = listeners.elementAt(j);

		     if (listener == null)
			listeners.removeElementAt(j);
		     else
			++j;
		  }

		  if (Model.TRACE.DBG && Model.traceInfo())
		  {
		     Model.TRACE.dbg(3, "Size of listener vector AFTER removing nulls=" +
					listeners.size());

		     Model.TRACE.dbg(3, "Listeners:");

		     for (int k=0; k < listeners.size(); k++)
			 if (listeners.elementAt(k) == null)
			    Model.TRACE.dbg(3, "null");
			 else
			    Model.TRACE.dbg(3, listeners.elementAt(k).toString());
		  }
		}
	     }

	     // For performance reasons we won't actually remove the item
	     // from the Vector but instead just set it to null and then
	     // purge the queue after all events have been fired.

	     if (_removeEventsFromQueueAfterFiring)
	        _queuedEvents.setElementAt(null, i);
          }
        }
        catch(ArrayIndexOutOfBoundsException excp)
        {
        }

    _currentlyFiringQueuedEvents = false;

    if (_removeEventsFromQueueAfterFiring)
       purgeEventQueue();
  }

  void purgeEventQueue()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, this + ".purgeEventQueue()");

    _queuedEvents.removeAllElements();
  }

  boolean isCurrentlyFiringQueuedEvents()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, this + ".isCurrentlyFiringQueuedEvents() : " + _currentlyFiringQueuedEvents);

    return _currentlyFiringQueuedEvents;
  }

  byte getMode()
  {
    //if (Model.TRACE.DBG && Model.traceInfo())
      //Model.TRACE.dbg(4, "EventManager.getMode()");

    return _mode;
  }

  void setMode(byte mode)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, this + ".setMode(" + mode + ")");

    _mode = mode;

    //if (Model.TRACE.DBG && Model.traceInfo())
      //Model.TRACE.dbg(4, "EventManger() : Mode = " + ((_mode == queueEvents)?"queueEvents":"fireEvents"));
  }

  void setAllowConcurrentFiringOfQueuedEvents(boolean yesOrNo)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, this + ".setAllowConcurrentFiringOfQueuedEvents(" + yesOrNo + ")");

    _allowConcurrentFiringOfQueuedEvents = yesOrNo;
  }

  boolean getAllowConcurrentFiringOfQueuedEvents()
  {
    //if (Model.TRACE.DBG && Model.traceInfo())
      //Model.TRACE.dbg(4, "EventManager.getAllowConcurrentFiringOfQueuedEvents()");

    return _allowConcurrentFiringOfQueuedEvents;
  }

  void setRemoveEventsFromQueueAfterFiring(boolean yesOrNo)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(4, this + ".setRemoveEventsFromQueueAfterFiring(" + yesOrNo + ")");

    _removeEventsFromQueueAfterFiring = yesOrNo;
  }

  boolean getRemoveEventsFromQueueAfterFiring()
  {
    //if (Model.TRACE.DBG && Model.traceInfo())
      //Model.TRACE.dbg(4, "EventManager.getRemoveEventsFromQueueAfterFiring()");

    return _removeEventsFromQueueAfterFiring;
  }

  void setRequestProperty(Object property)
  {
    _requestProperty = property;
  }

  Object getRequestProperty()
  {
    return _requestProperty;
  }

  private Vector _queuedEvents = new Vector();
  private boolean _currentlyFiringQueuedEvents = false;
  private boolean _allowConcurrentFiringOfQueuedEvents = false;
  private boolean _removeEventsFromQueueAfterFiring = true;
  private byte _mode = queueEvents;
  private int _numberOfNullListeners;
  private Object _requestProperty;

  private static final int _maxNumberOfNullListeners = 3;

  static final byte queueEvents = (byte)0;
  static final byte fireEvents = (byte)1;
  static final byte abort = (byte)2;

}
