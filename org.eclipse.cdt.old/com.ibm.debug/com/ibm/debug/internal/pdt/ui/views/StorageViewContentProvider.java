package com.ibm.debug.internal.pdt.ui.views;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/views/StorageViewContentProvider.java, eclipse, eclipse-dev, 20011128
// Version 1.10 (last modified 11/28/01 16:00:52)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.internal.picl.PICLStorage;
import java.util.Vector;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.internal.ui.BasicContentProvider;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbenchPage;


public class StorageViewContentProvider extends BasicContentProvider implements IDebugEventListener, IStructuredContentProvider {
	Vector lineCache;
	PICLStorage storage;
	String baseAddress;
	TabItem tab;
	
	/**
	 * Constructs a <code>BreakpointsViewContentProvider</code>.
	 */
	public StorageViewContentProvider(PICLStorage newStorage, TabItem newTab) {
		lineCache = new Vector(50);
		storage = newStorage;
		baseAddress = storage.getAddress();
		tab = newTab;
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	/**
	 * @see IContentProvider
	 */
	public void dispose() {
		lineCache = null;
		tab.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}
	/**
	 * Returns the storage blobs as lines
	 */
	public Object[] getElements(Object parent) {
		if (lineCache.isEmpty()) { 
			StorageViewTab sTab = (StorageViewTab)tab.getData();
			
			String calculatedAddress = storage.getAddress();
			
			if ( Long.parseLong(calculatedAddress, 16) <= 16) {
				sTab.TABLE_PREBUFFER = 0;
			} else {
				sTab.TABLE_PREBUFFER = (int)java.lang.Math.min(Long.parseLong(calculatedAddress, 16)/16, (long)sTab.TABLE_PREBUFFER);
			}

			calculatedAddress = Long.toHexString(Long.parseLong(calculatedAddress, 16) - 16*sTab.TABLE_PREBUFFER);
			if (calculatedAddress.length() < 8) {
				for (int j=0; calculatedAddress.length()<8; j++) {
					calculatedAddress = "0" + calculatedAddress;
				}
			}
			calculatedAddress = calculatedAddress.toUpperCase();
			getStorageToFitTable(calculatedAddress, sTab.getNumberOfVisibleLines()+sTab.TABLE_PREBUFFER+sTab.TABLE_POSTBUFFER);
		}
		return lineCache.toArray();
   	}
	/**
	 * @see BasicContentProvider#doGetChildren(Object)
	 */
	protected Object[] doGetChildren(Object parent) {
		return null;
	}

	public PICLStorage getStorage() {
		return storage;
	}
	
	public void forceRefresh() {
		refresh();
	}
	
	/**
	 * @see BasicContentProvider#doHandleDebug(Event)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
		Object obj = event.getSource();
		switch (event.getKind()) {
			case DebugEvent.CHANGE:
				if ((event.getSource() instanceof PICLStorage) && (event.getSource() == storage)) {
					StorageViewTab storageTab = (StorageViewTab)tab.getData();
					if (storageTab != null) {
						if (storage.getAddress().equals(baseAddress)) {
							//let the storageTab reload the table contents based on what's currently visible
							storageTab.reloadTable(storageTab.getTopVisibleAddress(), false);
						} else {
							//but if the address has changed, we want to jump to the new spot...
							storageTab.reloadTable(storage.getAddress(), false);
							baseAddress = storage.getAddress();
						}
					}
				}
				break;
			case DebugEvent.SUSPEND:
				break;
			case DebugEvent.TERMINATE:
				if ((event.getSource() instanceof PICLStorage) && (event.getSource() == storage)) {
					StorageViewTab storageTab = (StorageViewTab)tab.getData();
					storageTab.dispose();
					tab.dispose();
				}
				break;
			default:
				break;
		}
	}


	public void getStorageToFitTable(String startingAddress, int numberOfLines) {	

		int numberOfBytes = numberOfLines * 16;		
		if (startingAddress == null) {
			startingAddress = storage.getAddress();
		}

		//handle '0x' address strings
		if (startingAddress.toUpperCase().startsWith("0X")) {
			startingAddress = startingAddress.substring(2);
		}

		int offset = (int)(Long.parseLong(startingAddress, 16) - Long.parseLong(storage.getAddress(), 16));
				
		StringBuffer[] storageBuffer = storage.getStorageLine(offset, numberOfBytes); 
		StringBuffer rawStorage = storageBuffer[PICLStorage.RAW];						//contains 2*numberOfBytes in chars
		StringBuffer translatedStorage = storageBuffer[PICLStorage.TRANSLATED];	//contains numberOfBytes in chars

		if (!lineCache.isEmpty()) {
			lineCache.removeAllElements();
		}

		//if startingAddress isn't on a double-byte word boundary
//		if (!startingAddress.endsWith("0")) {
//			long prependbits = Long.parseLong(startingAddress.substring(7,8), 16);
//			startingAddress = startingAddress.substring(0, 7) + "0"; //force it to the boundary
//			for (int i=0; i<prependbits; i++) {	//pad the raw storage with dots (prepend)
//				rawStorage.insert(0, ".");
//				translatedStorage.insert(0, ".");
//			}
//		}

		//we requested an evenly divisible number of bytes to fill our lines, but if for some
		//reason the returned raw/translated storage won't fill the requested lines, pad with dots
		if (rawStorage.length() < numberOfLines*16*2) {
			int appendbits = (numberOfLines * 16 * 2) - rawStorage.length();
			for (int i=0; i<appendbits; i++) {
				rawStorage.append(".");
			}
		}
		if (translatedStorage.length() < numberOfLines*16) {
			int appendbits = (numberOfLines * 16) - translatedStorage.length();
			for (int i=0; i<appendbits; i++) {
				translatedStorage.append(".");
			}
		}

		String address = startingAddress;
		String translated = "";
		String f1stByte = "";
		String f2ndByte = "";
		String f3rdByte = "";
		String f4thByte = "";

		for (int i=0; i < numberOfLines; i++) {	//chop the raw storage up 
			f1stByte = rawStorage.substring(0, 8);
			rawStorage.delete(0, 8);
			f2ndByte = rawStorage.substring(0, 8);
			rawStorage.delete(0, 8);
			f3rdByte = rawStorage.substring(0, 8);
			rawStorage.delete(0, 8);
			f4thByte = rawStorage.substring(0, 8);
			rawStorage.delete(0, 8);
			translated = translatedStorage.substring(0,16);  // there is one translated char for every two raw chars
			translatedStorage.delete(0,16);
			lineCache.add(new StorageViewLine(storage, address, f1stByte, f2ndByte, f3rdByte, f4thByte, translated));

			address = Long.toHexString(Long.parseLong(address, 16) + 16);
			if (address.length() < 8) {
				for (int j=0; address.length()<8; j++) {
					address = "0" + address;
				}
			}
			address = address.toUpperCase();
		}
		//System.out.println("TABLE RELOADED: requested " + numberOfLines + " lines (bytes: " + numberOfBytes + ") from " + startingAddress + " (offset: " + offset + ")");
	}
	
}

