package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/RestorableObjects.java, java-model, eclipse-dev, 20011128
// Version 1.13.1.2 (last modified 11/28/01 16:13:33)
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
 * A class which can be used to save and restore "restorable" Model
 * objects e.g. breakpoints, monitored exprs, etc.
 * This is an abstract class which is subclassed by TransientRestorableObjects
 * and PersistentRestorableObjects.
 */

public abstract class RestorableObjects
{
  RestorableObjects(DebuggeeProcess process,
		    Serialization serialization,
		    int flags)
  {
    _process = process;
    _serialization = serialization;
    setSaveFlags(flags);
    setRestoreFlags(flags);
  }

  /**
   * Use this method to save restorable objects. The process containing the
   * objects to be saved was specified when this RestorableObjects object was
   * constructed <i>or</i> by a subsequent call to setProcess().
   * <p>The saveFlags associated with this RestorableObjects object
   * control exactly which of the restorable objects will be saved. These
   * save flags were specified when this RestorableObjects object was
   * constructed <i>or</i> by a subsequent call to setSaveFlags().
   * @param asynchronously Indicates whether the saving of objects is
   * to be done synchronously (false) or asynchronously (true). When done
   * asynchronously, a new thread will be created and the saving of
   * objects will be done on that thread instead of on the thread that
   * called save.
   */

  public void save(boolean asynchronously)
  throws java.io.IOException
  {
    if (asynchronously)
       (new SaveThread()).start();
    else
       save();
  }

  void save()
  throws java.io.IOException
  {
    _savedProcess = null;
    _serialization.saveGraph(_process);
  }

  /**
   * Put the given Language object into the graph of saved objects and then
   * re-save them.
   */

  void resave(Language language)
  throws java.io.IOException
  {
    try
    {
      if (_savedProcess == null)
         _savedProcess = (DebuggeeProcess)_serialization.getGraph();
    }
    catch (java.lang.ClassNotFoundException excp)
    {
    }

    if (_savedProcess == null)
       return;

    _savedProcess.debugEngine().add(language, language.getLanguageID());

    _serialization.saveGraph(_savedProcess);

    // Should I set _savedProcess = null here?
  }

  /**
   * Put the given DebuggeeException[] into the graph of saved objects and then
   * re-save them.
   */

  void resave(DebuggeeException[] exceptions)
  throws java.io.IOException
  {
    try
    {
      if (_savedProcess == null)
         _savedProcess = (DebuggeeProcess)_serialization.getGraph();
    }
    catch (java.lang.ClassNotFoundException excp)
    {
    }

    if (_savedProcess == null)
       return;

    DebugEngine _savedEngine = _savedProcess.debugEngine();

    _savedEngine.setExceptions(exceptions);
    _savedEngine.setSaveAndRestoreExceptionFilters(true);

    _serialization.saveGraph(_savedProcess);

    // Should I set _savedProcess = null here?
  }

  /**
   * Restore the saved objects into the process associated with this
   * RestorableObjects object. The target process into which objects will
   * be restored was specified when this RestorableObjects object was
   * constructed <i>or</i> by a subsequent call to setProcess() and <i>can</i>
   * be (but doesn't have to be) the same process from which those objects
   * were originally saved.
   * <p>The restoreFlags associated with this RestorableObjects object
   * control exactly which of the saved objects will be restored. When a
   * RestorableObjects object is first constructed, the restoreFlags are
   * the same as the saveFlags, so all of the objects that were saved will
   * be restored. However, the restoreFlags can be modified so that only
   * a subset of the saved objects are restored.
   * <p>The restoration of saved objects can be cancelled by calling
   * stopRestoring().
   * @param asynchronously Indicates whether the restoration of objects is
   * to be done synchronously (false) or asynchronously (true). When done
   * asynchronously, a new thread will be created and the restoration of
   * objects will be done on that thread instead of on the thread that
   * called restore. In this case, the return value from restore() is
   * meaningless.
   * @see RestorableObjects#save
   * @see RestorableObjects#setProcess
   * @see RestorableObjects#setRestoreFlags
   * @see RestorableObjects#stopRestoring
   */

  public boolean restore(boolean asynchronously)
  throws java.io.IOException
  {
    if (asynchronously)
    {
       (new RestoreThread()).start();
       return true;
    }
    else
      return restore();
  }

  boolean restore()
  throws java.io.IOException
  {
    try
    {
      if (_savedProcess == null)
         _savedProcess = (DebuggeeProcess)_serialization.getGraph();
    }
    catch (java.lang.ClassNotFoundException excp)
    {
    }
    catch (java.io.IOException excp)
    {
    }

    if (_savedProcess == null)
       return false;

    _stopRestoring = false;

    // Suspend auto-saving while we do the restore o.w. a "save" will be done
    // each time something gets restored:

    boolean autoSaveWasAlreadySuspended = false;

    if (_autoSave != null &&
        !(autoSaveWasAlreadySuspended = _autoSave.isSuspended()))
       _autoSave.suspend();

    _process.debugEngine().setSavedObjectsAreBeingRestored(true);

    // TODO: The rest of the code in this method should probably be moved to a
    // new method in DebuggeeProcess and replaced by a call to that new
    // method on the _savedProcess object:

    if ((_restoreFlags & SaveRestoreFlags.BREAKPOINTS) != 0)
    {
       Breakpoint[] savedBreakpoints = _savedProcess.getBreakpointsArray();

       if (savedBreakpoints != null)
          for (int i = 0; i < savedBreakpoints.length; i++)
          {
              synchronized(this)
              {
                if (_stopRestoring)
                   break;
              }

              Breakpoint breakpoint = savedBreakpoints[i];

              if (breakpoint != null)
                 breakpoint.restore(_process, DebugEngine.sendReceiveSynchronously);
          }
    }

    if ((_restoreFlags & SaveRestoreFlags.STORAGE) != 0)
    {
       Vector savedStorage = _savedProcess.getStorage();

       if (savedStorage != null)
          for (int i = 0; i < savedStorage.size(); i++)
          {
              synchronized(this)
              {
                if (_stopRestoring)
                   break;
              }

              Storage storage = (Storage)savedStorage.elementAt(i);

              if (storage != null)
                 storage.restore(_process, DebugEngine.sendReceiveSynchronously);
          }
    }

    boolean isSecondaryRestore = ((_restoreFlags & SaveRestoreFlags.SECONDARY_RESTORE) != 0);
    DebugEngine savedEngine = _savedProcess.debugEngine();
    DebugEngine targetEngine = _process.debugEngine();

    if ((_restoreFlags & SaveRestoreFlags.DEFAULT_DATA_REPRESENTATIONS) != 0)
    {
       Language[] savedLanguages = savedEngine.getLanguages();

       if (savedLanguages != null)
          for (int i = 0; i < savedLanguages.length; i++)
          {
              synchronized(this)
              {
                if (_stopRestoring)
                   break;
              }

              Language savedLanguage = savedLanguages[i];

              if (savedLanguage != null)
              {
                 Language targetLanguage = targetEngine.
                                           getLanguageInfo(savedLanguage.getLanguageID());

                 if (targetLanguage != null)
                 {
                    if (isSecondaryRestore)
                    {
                       // When the secondaryRestore flag is on, we only restore if
                       // the target is not already marked as having been restored:

                       if (!targetLanguage.getSaveAndRestore())
                          savedLanguage.restore(_process, DebugEngine.sendReceiveSynchronously);
                    }
                    else
                       targetLanguage.setSaveAndRestore(savedLanguage.restore(_process, DebugEngine.sendReceiveSynchronously));
                 }
              }
          }
    }

    if ((_restoreFlags & SaveRestoreFlags.EXCEPTION_FILTERS) != 0)
    {
       DebuggeeException[] savedExceptions = savedEngine.getExceptions();
       DebuggeeException[] targetExceptions = targetEngine.getExceptions();

       boolean success = false;

       // Don't restore if this is a secondary restore AND the target engine's
       // exception filters have already been restored:

       if (savedExceptions != null && !(isSecondaryRestore && targetEngine.getSaveAndRestoreExceptionFilters()))
       {
          for (int i = 0; i < savedExceptions.length; i++)
          {
              synchronized(this)
              {
                if (_stopRestoring)
                   break;
              }

              DebuggeeException savedException = savedExceptions[i];
              DebuggeeException targetException = targetExceptions[i];

              // Set the pending state in each target exception to reflect
              // the state of the saved exceptions:

              if (savedException != null && targetException != null)
                 if (savedException.isEnabled())
                    targetException.enable();
                 else
                    targetException.disable();
          }

          // Commit the pending state changes:

          targetEngine.changeExceptionStatus();

          success = true;
       }

       if (!isSecondaryRestore)
	  targetEngine.setSaveAndRestoreExceptionFilters(success);
    }

    if ((_restoreFlags & SaveRestoreFlags.PROGRAM_MONITORS) != 0)
    {
       MonitoredExpression[] savedExpressions = _savedProcess.getMonitoredExpressionsArray();

       if (savedExpressions != null)
          for (int i = 0; i < savedExpressions.length; i++)
          {
              synchronized(this)
              {
                if (_stopRestoring)
                   break;
              }

              MonitoredExpression expression = savedExpressions[i];

              if (expression != null)
                 expression.restore(_process, DebugEngine.sendReceiveSynchronously);
          }
    }

    if (_autoSave != null && !autoSaveWasAlreadySuspended)
       _autoSave.resume();

    _process.debugEngine().setSavedObjectsAreBeingRestored(false);

    if (_stopRestoring)
       return false;
    else
       return true;
  }

  public void setProcess(DebuggeeProcess process)
  {
    _process = process;

    if (_autoSave != null)
       _autoSave.setObject(process);
  }

  public DebuggeeProcess getProcess()
  {
    return _process;
  }

  /**
   * @see SaveRestoreFlags
   */

  public void setSaveFlags(int saveFlags)
  {
    _serialization.setFlags(saveFlags | SaveRestoreFlags.RESTORABLE_OBJECTS);

    if (_autoSave != null)
    {
       if ((saveFlags & SaveRestoreFlags.AUTOSAVE) != 0)
          _autoSave.resume();
       else
          _autoSave.suspend();
    }
    else
    if ((saveFlags & SaveRestoreFlags.AUTOSAVE) != 0)
       _autoSave = new AutoSaveRestorableObjects(this);
  }

  Serialization getSerialization()
  {
    return _serialization;
  }

  /**
   * @see SaveRestoreFlags
   */

  public int getSaveFlags()
  {
    return _serialization.getFlags();
  }

  /**
   * @see SaveRestoreFlags
   */

  public void setRestoreFlags(int restoreFlags)
  {
    _restoreFlags = restoreFlags | SaveRestoreFlags.RESTORABLE_OBJECTS;
  }

  /**
   * @see SaveRestoreFlags
   */

  public int getRestoreFlags()
  {
    return _restoreFlags;
  }

  /**
   * Cancel the restoration of bkps, monitored exprs, etc. that was started
   * by a call to restore(). If the restore() method is not executing, the
   * call to stopRestoring() will be ignored. This method is intended to
   * provide support for a "Cancel" button (or a similar mechanism) so that
   * the user can cancel the restoration of saved objects.
   */

  public void stopRestoring()
  {
    synchronized(this)
    {
      _stopRestoring = true;
    }
  }

  /*********************/
  /* BEGIN INNER CLASS */
  /*********************/

  class SaveThread extends Thread
  {
    public void run()
    {
      try
      {
        save();
      }
      catch (java.io.IOException excp)
      {
      }
    }
  }

  /*********************/
  /* END   INNER CLASS */
  /*********************/

  /*********************/
  /* BEGIN INNER CLASS */
  /*********************/

  class RestoreThread extends Thread
  {
    public void run()
    {
      try
      {
        restore();
      }
      catch (java.io.IOException excp)
      {
      }
    }
  }

  /*********************/
  /* END   INNER CLASS */
  /*********************/

  protected Serialization _serialization;
  private DebuggeeProcess _process;
  private DebuggeeProcess _savedProcess;
  private int _restoreFlags;
  private boolean _stopRestoring = false;
  private AutoSave _autoSave;
}
