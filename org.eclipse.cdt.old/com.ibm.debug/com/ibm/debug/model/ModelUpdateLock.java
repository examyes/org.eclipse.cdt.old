package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ModelUpdateLock.java, java-model, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:11:24)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * A ModelUpdateLock object is used to coordinate access to the Model
 * in a multi-threaded application. The following are some important
 * points regarding these locks:
 * <ul>
 * <li>Client code obtains a lock by calling DebugEngine.getLock.
 * <li>At most, one valid lock can exist per debug engine at any given
 * time. (There can be zero).
 * <li>Client code releases a lock that it owns by calling
 * DebugEngine.releaseLock. Once a lock is released it is no longer valid
 * and cannot be used to access the Model.
 * <li>A lock is associated with a given debug engine (i.e. a DebugEngine
 * object) - it cannot be used with other debug engines within the Model.
 * <li>Only one thread at a time can use a lock. This thread is referred
 * to as the "current owner" of the lock.
 * <li>When a lock is first created, it is owned by the thread on which it
 * was created.
 * <li>When a thread asks for a lock, the request will be denied if:
 *     <ul>
 *     <li>There already exists an outstanding valid lock
 *         <br>OR...
 *     <li>The debug engine is busy
 *         <br>OR...
 *     <li>The Model is currently being updated
 *     </ul>
 * <li>When a thread asks for a lock, it can choose what should happen if the
 * request is denied:
 *     <ul>
 *     <li>It can block until the lock is available
 *         <br>OR...
 *     <li>The request can return with an indication of failure
 *     </ul>
 * <li>If a thread accesses the Model without first obtaining a lock,
 * the following caveats must be observed:
 *     <ul>
 *     <li>Calling a method in the Model which would potentially
 *         cause the Model to be updated will fail if any of the following are
 *         true: i) Some other thread has a lock on the Model, ii) The debug
 *         engine is busy servicing a previous request, or iii) The Model is
 *         currently being updated. If any of these conditions are true, the
 *         method call will return with an indication of failure meaning that
 *         the requested action cannot be performed.
 *     <li>There is no guarantee regarding the state of the Model - another
 *         thread could perform an action which would cause the Model to
 *         be updated and therefore become temporarily inconsistent, incomplete,
 *         indeterminate, etc. The state of the Model can be queried via
 *         methods such as DebugEngine.modelIsBeingUpdated(), but these methods
 *         only provide an instantaneous snapshot of the state of the Model -
 *         there is no
 *         guarantee regarding how long the reported state will continue to
 *         exist after the query method has returned since some other thread
 *         may cause the Model to change immediately after the method returns.
 *     </ul>
 * <li>On the other hand, immediately after obtaining a lock on the Model, the
 * following is guaranteed:
 *     <ul>
 *     <li>It is "safe" to make a request which would potentially cause the
 *         the Model to be updated. Since locks can only be obtained when
 *         i) No other thread owns one, ii) The debug engine is not busy, and
 *         iii) The Model is not being updated, the request is guaranteed not
 *         to be rejected for any of these reasons. <i>However</i>, this
 *         guarantee applies only to the very first such request after
 *         obtaining the lock - subsequent requests might still fail due to
 *         ii) and iii), above. A given lock <i>can</i> be used for subsequent
 *         requests, but extra steps are required on the part of client code
 *         to either wait until the debug engine is not
 *         busy and the Model is not being updated, or handle the fact that the
 *         request might be rejected because of these things. Releasing the
 *         lock and then getting it again is one way of waiting until it is
 *         "safe" again, but this introduces the possibility that some other
 *         thread will get the lock before your thread has a chance to get it
 *         back. If this would be problematic because your thread needs to do
 *         two or more requests without being interrupted, you must hold onto
 *         the lock and not release it until after your second request has
 *         been made - this is the only way to guarantee that some other
 *         thread won't grab control of the Model in the meantime. Holding on
 *         to a lock in this way allows client code to perform a complete
 *         "transaction" (two or more requests which must be carried out
 *         without interruption) as opposed to just single, independent
 *         requests.
 *     <li>No other thread can cause the Model to be updated until the lock is
 *         released. This gives the thread that owns the lock the assurance
 *         that it can perform some long-running query of the Model without
 *         having to worry about objects in the Model changing and therefore
 *         becoming inconsistent, unreliable, etc.
 *     </ul>
 * <li>Client code can transfer ownership of a lock from one thread to another
 *     by calling ModelUpdateLock.setCurrentThread. Only the thread which
 *     currently owns the lock can call methods in the Model which would
 *     potentially cause the Model to be updated.
 * </ul>
 */

public class ModelUpdateLock
{
  ModelUpdateLock(Thread currentThread)
  {
    _currentThread = currentThread;
  }

  ModelUpdateLock()
  {
    _currentThread = Thread.currentThread();
  }

  /**
   * Query which thread currently owns this lock.
   */

  public Thread getCurrentThread()
  {
    return _currentThread;
  }

  /**
   * If the lock is valid, sets the lock's current thread to be the thread
   * doing this call.
   */

  public void setCurrentThread()
  {
    if (_currentThread != null)
       _currentThread = Thread.currentThread();
  }

  /**
   * If the lock is valid, sets the lock's current thread to be the given
   * thread.
   */

  public void setCurrentThread(Thread thread)
  {
    if (_currentThread != null)
       _currentThread = thread;
  }

  /**
   * Determine if this lock is valid. Only valid locks can be used to perform
   * an action (i.e. call a method in the Model) which would potentially
   * cause the Model to be updated. A lock is valid when first created and
   * remains valid until it is returned/released by the client code that
   * has it.
   */

  public boolean isValid()
  {
    return _currentThread != null;
  }

  void invalidate()
  {
    _currentThread = null;
  }

  private Thread _currentThread;
}
