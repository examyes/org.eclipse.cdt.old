/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;

/**
 * Stores information for a breakpoint.  This is the superclass for specific 
 * types of breakpoints (line breakpoints, method breakpoints)
 */
public abstract class Breakpoint
{
   public Breakpoint(DebugSession debugSession, int bkpID, int gdbBkID, int bkpType, int bkpAttr) 
   {
      _debugSession  = debugSession;
      _debugEngine   = _debugSession.getDebugEngine();
      _engineSession = _debugEngine.getSession();
      _bkpID         = bkpID;
      _gdbBkID		 = gdbBkID;
      _bkpType       = bkpType;
      _bkpTypeQual   = "";
      _bkpAttr       = bkpAttr;

      _bkpNew     = true;
      _bkpChanged = true;
      _bkpEnabled = true;
      _bkpDeleted = false;
   }

    public void modify(int bkpID, int bkpAttr)
    {
      _bkpID         = bkpID;
      _bkpAttr       = bkpAttr;

      _bkpChanged = true;
    }

   /**
    * Delete this breakpoint
    */
   public void deleteBreakpoint()
   {
      _bkpChanged = true;
      _bkpDeleted = true;
   }

   /**
    * Disable this breakpoint
    */
   public void disableBreakpoint() {
      _bkpChanged = true;
      _bkpEnabled = false;
   }

   /**
    * Enable this breakpoint
    */
   public void enableBreakpoint() {
      _bkpChanged = true;
      _bkpEnabled = true;
   }

   /**
    * Return whether this breakpoint is enabled
    */
   public boolean isEnabled() {
      return _bkpEnabled;
   }

   /**
    * Get the change item for this breakpoint.
    */
   public ERepGetNextBkp getBreakpointChangeItem() {
      int bkpFlags = _bkpNew ? EPDC.BkpNew : 0;
      bkpFlags += _bkpChanged ? EPDC.BkpChanged : 0;
      bkpFlags += _bkpEnabled ? EPDC.BkpEnabled : 0;
      bkpFlags += _bkpDeleted ? EPDC.BkpDeleted : 0;

//      ModuleManager mm = _debugSession.getModuleManager();

      ERepGetNextBkp bkpChangeItem = new ERepGetNextBkp(
          _engineSession, _bkpID, bkpFlags, _bkpType, _bkpTypeQual, _bkpAttr);

      this.fillBreakpointChangeItem(bkpChangeItem);

      _bkpNew = false;
      _bkpChanged = false;
      return bkpChangeItem;
   }

   /**
    * Fill in breakpoint type specific information in the change item.
    */
   abstract void fillBreakpointChangeItem(ERepGetNextBkp bkpChangeItem);

   /**
    * Return the breakpoint type
    */
   public int bkpType() {
      return _bkpType;
   }

   /**
    * Return the breakpoint id
    */
   public int bkpID() {
      return _bkpID;
   }

   public int attribute()
   {
     return _bkpAttr;
   }

   public boolean isLineBreakpoint()
   {
     return (_bkpType == EPDC.LineBkpType);
   }

   public boolean isMethodBreakpoint()
   {
     return (_bkpType == EPDC.EntryBkpType);
   }

   public boolean isDeferred()
   {
     return (_bkpAttr & EPDC.BkpDefer) != 0;
   }
   
	
	/**
	 * Gets the gdbBkID.
	 * @return Returns a int
	 */
	public int getGdbBkID() {
		return _gdbBkID;
	}

	/**
	 * Sets the gdbBkID.
	 * @param gdbBkID The gdbBkID to set
	 */
	public void setGdbBkID(int gdbBkID) {
		_gdbBkID = gdbBkID;
	}   
	
	
	/**
	 * Gets the bkpAddress.
	 * @return Returns a String
	 */
	public String getBkpAddress() {
		return _bkpAddress;
	}

	/**
	 * Sets the bkpAddress.
	 * @param bkpAddress The bkpAddress to set
	 */
	public void setBkpAddress(String bkpAddress) {
		_bkpAddress = bkpAddress;
	}

   // Data fields
   protected DebugSession         _debugSession;
   protected DebugEngine          _debugEngine;
   protected EPDC_EngineSession  _engineSession;

   private int    _bkpID;
   private int    _gdbBkID;
   private int    _bkpType;
   private String _bkpTypeQual;
   private int    _bkpAttr;
   private String  _bkpAddress;

   private boolean _bkpNew;
   private boolean _bkpChanged;
   private boolean _bkpEnabled;
   private boolean _bkpDeleted;

}
