package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Module.java, java-model, eclipse-dev, 20011128
// Version 1.23.1.2 (last modified 11/28/01 16:11:08)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Vector;

/**
 * This class represents a module (.exe or .dll) which has been loaded
 * into the debuggee process. To reflect the relationship between
 * modules and the processes in which they are loaded, Module objects are
 * contained within DebuggeeProcess objects. In turn, a Module object contains
 * a list of Part objects; these Part objects represent the compilation units
 * that make up the module.
 * <p>Event listeners can be registered with a Module object by calling the
 * add(ModuleEventListener) method. See the description of that method for
 * more information.
 * @see DebuggeeProcess
 * @see Part
 */

public class Module extends DebugModelObject
{
  Module(DebuggeeProcess owningProcess, int moduleID, String name,
         String qualifiedName, boolean hasDebugInfo)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(1, "Creating Module : ID=" + moduleID + " Name=" + name + " QualifiedName=" + qualifiedName + " HasDebugInfo=" + hasDebugInfo);

    _process = owningProcess;
    _name = name;
    _qualifiedName = qualifiedName;
    _id = moduleID;
    _hasDebugInfo = hasDebugInfo;
  }

  /**
   * Add a module event listener to this Module object. Whenever
   * an event occurs for which there is a corresponding method in the
   * event listener's interface, that method will be called to inform the
   * listener of the event. More than one listener may be added to a given
   * Module object - the listeners will be notified of events in
   * the order in which they were added (i.e. FIFO).
   * @param eventListener The object whose methods will be called when
   * events occur.
   */

  public void addEventListener(ModuleEventListener eventListener)
  {
    _eventListeners.addElement(eventListener);
  }

  /**
   * Remove a module event listener from this Module object so
   * that it no longer receives event notifications.
   * @param eventListener The event listener to be removed from this
   * Module object's list of listeners.
   */

  public void removeEventListener(ModuleEventListener eventListener)
  {
    int index = _eventListeners.indexOf(eventListener);

    if (index != -1)
    {
        try
        {
          _eventListeners.setElementAt(null, index);
        }
        catch(ArrayIndexOutOfBoundsException excp)
        {
        }
    }
  }

  /**
   * Get the process into which this module has been loaded.
   */

  public DebuggeeProcess process()
  {
    return _process;
  }

  /**
   * Determine whether or not this module contains compiler-generated
   * debug information for symbols and types.
   */

  public boolean hasDebugInfo()
  {
    return _hasDebugInfo;
  }

  /**
   * Get the name of this module.
   */

  public String name()
  {
    return _name;
  }

  /**
   * Get the fully qualified name of this module including path information.
   */

  public String getQualifiedName()
  {
    return _qualifiedName;
  }

  /**
   * Get a list of parts (compilation units) contained in this module.
   */

  public Part[] getPartsArray()
  {
    if (_parts == null)
       return null;

    int numberOfParts = _parts.size();

    if (numberOfParts == 0)
       return null;

    Part[] parts = new Part[numberOfParts];

    _parts.copyInto(parts);

    return parts;
  }

  /** Note: EPDC assigns module ids starting at 1, not 0.
   */

  int id()
  {
    return _id;
  }

  void add(Part part)
  {
    _process.add(part);

    _parts.addElement(part);

    DebugEngine debugEngine = _process.debugEngine();

    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new PartAddedEvent(this,
                                                              part,
                                                              requestCode
                                                             ),
                                           _eventListeners
                                          );
  }

  void prepareToDie()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "Module[" + _id + "].prepareToDie()");

    DebugEngine debugEngine = _process.debugEngine();

    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new ModuleUnloadedEvent(this,
                                                                this,
                                                                requestCode
                                                               ),
                                           _eventListeners
                                          );
  }

  void tellChildrenThatOwnerHasBeenDeleted()
  {
    for (int i = 0; i < _parts.size(); i++)
    if (_parts.elementAt(i) != null)
	   ((DebugModelObject)_parts.elementAt(i)).setOwnerHasBeenDeleted();
  }

  /**
   * Remove references so they can be gc'ed.
   */
  void cleanup()
  {
    _process = null;
    _name = null;
    _qualifiedName = null;
    if (_parts != null)
    {
       int cnt = _parts.size();
       for (int i = 0; i < cnt; i++)
       {
          Part p = (Part)_parts.elementAt(i);
          if (p != null)
             p.cleanup();
       }
       _parts.removeAllElements();
       _parts = null;
    }
    if (_eventListeners != null)
       _eventListeners.removeAllElements();
  }

  private DebuggeeProcess _process;
  private String _name;
  private String _qualifiedName;
  private int _id;
  private Vector _parts = new Vector();
  private Vector _eventListeners = new Vector();
  private boolean _hasDebugInfo;
}
