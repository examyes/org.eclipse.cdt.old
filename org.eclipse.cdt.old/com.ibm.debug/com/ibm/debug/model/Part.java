package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Part.java, java-model, eclipse-dev, 20011128
// Version 1.24.1.2 (last modified 11/28/01 16:11:11)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;
import java.util.Vector;

/**
 * In C/C++ jargon, a part is the same as a compilation unit.
 * (On Intel, a part is equivalent to an .obj file, and on Unix, a part is
 * equivalent to a .o file.)
 * Part objects are contained within Module objects to reflect the fact that a module
 * is made up of one or more parts.
 * <p>Part objects contain View objects which represent the various views
 * that are available for the part. A view is the mechanism by which
 * lines of code within the part can be retrieved (in the form of
 * source and/or disassembly).
 * @see View
 * @see Module#parts()
 */

public class Part extends DebugModelObject
{
  Part(Module owningModule, ERepNextPart epdcPart)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(3, "Creating Part : Name=" + epdcPart.name() + " ID=" + epdcPart.id());

    _owningModule = owningModule;
    change(epdcPart);
  }

  void change(ERepNextPart epdcPart)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "In Part[" + epdcPart.name() + "].change()");

    _epdcPart = epdcPart;

    EViewData[] epdcViews = epdcPart.views();

    short numberOfViews, i;

    if (epdcViews != null && (numberOfViews = (short)epdcViews.length) > 0)
       if (_views == null)
       {
          // Note: EPDC assigns view ids starting at 1, not 0. We'll allocate
          // an array 1 bigger than it has to be so we can find views in
          // the array directly by their ids:

          _views = new View[numberOfViews + 1];

          // We'll only save validated views in the model. The _views array will
          // have an entry for every view but only the validated views will be
          // non-null in this array. The array will allow us to quickly access a
          // view of a given type by index number.

          for (i = 0; i < numberOfViews; i++)
              if (epdcViews[i].validated())
                 _views[i + 1] = new View(this, (short)(i+1), epdcViews[i]);
              else
                 _views[i + 1] = null;
       }
       else // We already have an array of views, so apply changes:
         for (i = 0; i < numberOfViews; i++)
             if (_views[i + 1] != null)
                _views[i + 1].change(epdcViews[i]);
             else // I don't believe that a previously "not validated" view
                  // can subsequently become "validated", but just in case...
               if (epdcViews[i].validated())
                  _views[i + 1] = new View(this, (short)(i+1), epdcViews[i]);
  }

  void breakpointAdded(LocationBreakpoint bkp)
  {
    // Tell all views that the bkp was added.
    // TODO: If it's possible for a view to not exist now but be created later
    // then we should save the bkp in the part and add it to the view when
    // the view is created.

    for (int i = 0; i < _views.length; i++)
    {
      View view = _views[i];

      if (view != null)
         view.breakpointAdded(bkp);
    }
  }

  void breakpointRemoved(LocationBreakpoint bkp)
  {
    // Tell all views that the bkp was removed.

    for (int i = 0; i < _views.length; i++)
    {
      View view = _views[i];

      if (view != null)
         view.breakpointRemoved(bkp);
    }
  }

  /**
   * Get the module which contains this part.
   */

  public Module module()
  {
    return _owningModule;
  }

  /** Note: Parts are numbered starting at 1, not 0.
   */

  short id()
  {
    return _epdcPart.id();
  }

  /**
   * Get the name of the part.
   */

  public String name()
  {
    return _epdcPart.name();
  }

  /**
   * Determine whether or not this part contains debug info. In most cases,
   * in order for this to be true, the user must have compiled the part,
   * <i>and linked</i> the module containing this part,
   * with the debug information option turned on. Also, unless a part
   * contains debug information, it will generally not be possible for the
   * debug engine to build a source view for the part.
   */

  public boolean hasDebugInfo()
  {
    return _epdcPart.hasDebugInfo();
  }

  /**
   *  Determine which programming language was used to write the source for
   *  this part.
   *  The name method from the class returned by this method correspond to
   *  PartLang names defined in com.ibm.debug.epdc.EPDC for part language
   *  e.g. LANG_CPP=2.
   *  Client code should use discretion when it comes to querying the language
   *  of a part since the debug engine does not automatically
   *  provide this information - a separate request must be sent to the debug engine
   *  in order to determine the language.
   *  @see com.ibm.debug.epdc.EPDC#LANG_C
   */

  public Language getLanguage()
  {
    // Force part verification before returning lang since the lang
    // doesn't appear to be set by the debug engine until the part has been
    // verified.

    try
    {
      verify();
    }
    catch(java.io.IOException excp)
    {
    }

    DebugEngine debugEngine = _owningModule.process().debugEngine();
    int langID = (int)_epdcPart.language();

    return debugEngine.getLanguageInfo(langID);
  }

  /** Get a list of all views that are valid for this part. Some
   *  entries in the array may be null.
   */

  public View[] views()
  {
    return _views;
  }

   /**
    * Given a view id, return the corresponding view.
    */

   View getView (int id)
   {
      if (_views == null)
         return null;

      return _views[id];
   }

  /** Given a ViewInformation object, return the corresponding view for this
   *  part. Will return null if the type of view specified by the ViewInformation
   *  arg is not available for this part. This can happen, for example, if
   *  one asks for the source view but the part contains no debug info.
   */

  public View view(ViewInformation viewInformation)
  {
    return _views[viewInformation.index()];
  }

  boolean functionsHaveBeenRetrieved()
  {
    return _functionsHaveBeenRetrieved;
  }

  void setFunctionsHaveBeenRetrieved(boolean isTrue)
  {
    _functionsHaveBeenRetrieved = isTrue;
  }

  /**
   * Use this method to verify the PART. Verifying a part seems to be done by
   * verifying the first file (file #1) in the part.
   */

  boolean verify()
  throws java.io.IOException
  {
    if (_epdcPart.hasBeenVerified())
       return true;

    return verify(1);
  }

  /**
   * Use this method to verify a particular file within a view within the part.
   */

  boolean verify(int index)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "Part[" + name() + "].verify(" + index + ")");

    DebugEngine debugEngine = _owningModule.process().debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_ViewsVerify,
                                         DebugEngine.sendReceiveSynchronously))
        return false;

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request Remote_ViewsVerify");

    if (!debugEngine.processEPDCRequest(
                                   new EReqVerifyViews(_epdcPart.id(), index),
                                   DebugEngine.sendReceiveSynchronously))
        return false;
    else
        return true;
  }

  public Vector getFunctions(String functionName, boolean caseSensitive)
  throws java.io.IOException
  {
     if (Model.TRACE.DBG && Model.traceInfo())
         Model.TRACE.dbg(2, "Part.getFunctions()");

         return _owningModule.process().getFunctions(functionName,
                                                     id(),
                                                     caseSensitive);
   }

   /**
    * Remove references so they can be gc'ed.
    */
   void cleanup()
   {
     _owningModule = null;
     _epdcPart = null;
     if (_views != null)
     {
        int cnt = _views.length;
        for (int i = 0; i < cnt; i++)
        {
           View v = (View)_views[i];
           if (v != null)
              v.cleanup();
           _views[i] = null;
        }
        _views = null;
     }
   }

  private Module _owningModule;
  private ERepNextPart _epdcPart;
  private View[] _views;
  private boolean _functionsHaveBeenRetrieved;
}
