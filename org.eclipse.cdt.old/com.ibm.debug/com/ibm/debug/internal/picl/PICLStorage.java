package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLStorage.java, eclipse, eclipse-dev, 20011129
// Version 1.12 (last modified 11/29/01 14:15:58)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;

import com.ibm.debug.model.DebugEngine;
import com.ibm.debug.model.Storage;
import com.ibm.debug.model.StorageChangedEvent;
import com.ibm.debug.model.StorageColumn;
import com.ibm.debug.model.StorageDeletedEvent;
import com.ibm.debug.model.StorageEventListener;
import com.ibm.debug.model.StorageLine;
import com.ibm.debug.model.StorageLineChangedEvent;
import com.ibm.debug.model.StorageLineEventListener;


/**
 * Represents monitored storage
 */

public class PICLStorage
	extends PICLDebugElement
	implements StorageEventListener, StorageLineEventListener {

	public static final int RAW = 0;
	public static final int TRANSLATED = 1;
	public static final int NUM_BYTES = 16;   // base number of bytes to monitor

	private Storage fStorage = null;
	private int fNumStorageLines = 0;
	private	int fTotalNumBytesMonitored = 0;
	private	int fLowestOffsetMonitored = 0;
	private int fHighestOffsetMonitored = 0;
	private StringBuffer fStorageBuffer = null;
	private StringBuffer fTranslatedStorageBuffer = null;
	private boolean fStorageBufferCurrent = false;

	private Vector fMonitoredStorageLines = null;

	/**
	 * Constructor for PICLStorage
	 */
	public PICLStorage(IDebugElement parent, Storage storage) {
		super(parent, PICLDebugElement.STORAGE);

		fStorage = storage;
		fStorage.addEventListener(this);
		calculateStorageValues();

		// add ourselves as an event listener on the storage lines

		fMonitoredStorageLines = fStorage.getStorageLines();
		monitorStorageLines();
	}

	/**
	 * Delete this storage monitor
	 */
	public boolean delete() {


		if (fStorage == null)   // only valid if this has a monitored expression
			return false;

		MonitorStorageDeleteRequest request = new MonitorStorageDeleteRequest((PICLDebugTarget)getDebugTarget(),
																					this);
		try {
			request.execute();
		} catch(PICLException pe) {
			return false;
		}

		return true;
	}

	/**
	 * Enable this storage monitor
	 */
	public boolean enable() {
		if (fStorage == null)
			return false;
		else
			try {
				return fStorage.enable();
			} catch(IOException ioe) {
				return false;
			}
	}

	/**
	 * Disable this storage monitor
	 */
	public boolean disable() {
		if (fStorage == null)
			return false;
		else
			try {
				return fStorage.disable();
			} catch(IOException ioe) {
				return false;
			}
	}


	/**
	 * Update raw storage contents
	 * @param storage type (see PICLStorage.RAW and PICLStorage.TRANSLATED
	 * @param offset from original storage location. NOTE: this MUST be within the storage requested
	 * @param new storage contents in the same format as the raw storage
	 * @return if successful sending request.   NOTE: a change event will be signalled when the change
	 * occurs.
	 */
	public boolean updateStorage(int storageType, int offset, String newContents) {


		// the 2 types of storage are handled differently.   In RAW each byte (2 characters) occupies a
		// StorageColumn.   In TRANSLATED storage the text equivalent of the storage is in a single column in the
		// the storageline.

		// in order to update storage the correct storageline/storagecolumn must be obtained.
		int line = offset/NUM_BYTES;
		if (offset < 0)  // for -ve offsets subtract 1 to get to the previous storage line
			line--;

		// bump by offset in the storage object to get to get the correct array index
		line -= fStorage.getFirstLineOffset();

		int offsetInLine = 0;
		// now calculate the offset into the initial storage line
		if (offset >= 0)
			offsetInLine = offset - (offset/NUM_BYTES * NUM_BYTES);
		else
			offsetInLine = NUM_BYTES + offset - (offset/NUM_BYTES * NUM_BYTES);

		int translatedColumn = fStorage.getNumberOfUnitsPerLine();

		boolean done = false;
		int startPosn = offsetInLine;
		int endPosn = 0;

		int leftToChange = newContents.length();
		if (storageType == RAW)
			leftToChange /= 2;

		StringBuffer columnContents = null;

		while (!done) {  // start to process the changes
			if (startPosn + leftToChange > NUM_BYTES) {
				leftToChange -= NUM_BYTES - startPosn;
				endPosn = NUM_BYTES;
			} else {
				done = true;
				endPosn = startPosn + leftToChange;
			}


			StorageLine stgLine = (StorageLine)fMonitoredStorageLines.get(line);

			StorageColumn cols[] = stgLine.getStorageColumns();

			MonitorStorageUpdateRequest req = null;

			if (storageType == RAW) {
				for (int i = startPosn, j = 0; i < endPosn; i++,j+=2 ) {
					req = new MonitorStorageUpdateRequest((PICLDebugTarget)getDebugTarget(),
															cols[i],
															newContents.substring(j,j+2));
					try {
						req.execute();
					} catch(PICLException pe) {
						return false;
					}
//						cols[i].update(newContents.substring(j,j+2),DebugEngine.sendReceiveSynchronously);
				}

			} else {

				// translated storage is always the last column
				columnContents = new StringBuffer(cols[translatedColumn].getStorageContents());

				columnContents.replace(startPosn,endPosn, newContents);

				// do the update in here
				req = new MonitorStorageUpdateRequest((PICLDebugTarget)getDebugTarget(),
													   cols[translatedColumn],
													   columnContents.toString());
				try {
					req.execute();
				} catch(PICLException pe) {
					return false;
				}

//					cols[translatedColumn].update(columnContents.toString(),DebugEngine.sendReceiveSynchronously);
			}
			if (req !=null && req.isError())
				return false;
			startPosn = 0;
			line++;
		}



		return true;
	}

	/**
	 * @see PICLDebugElement#doCleanupDetails()
	 */
	protected void doCleanupDetails() {

		releaseStorageLines();

		fStorage.removeEventListener(this);
		fStorage = null;
	}

	/**
	 * @see PICLDebugElement#getLabel(boolean)
	 */
	public String getLabel(boolean qualified) {
		return fStorage.getExpression();
	}

	/**
	 * @see StorageEventListener#storageDeleted(StorageDeletedEvent)
	 */
	public void storageDeleted(StorageDeletedEvent event) {
		PICLUtils.logEvent("Storage deleted",this);

		((PICLStorageParent)getParent()).removeChild(this);
	}

	/**
	 * @see StorageEventListener#storageChanged(StorageChangedEvent)
	 */
	public void storageChanged(StorageChangedEvent event) {
		PICLUtils.logEvent("Storage changed",this);
		// make sure that the storage values are up-to-date
		fMonitoredStorageLines = event.getStorage().getStorageLines();
		calculateStorageValues();
		monitorStorageLines();   // monitor the new storage lines
		fStorageBufferCurrent = false;

		fireChangeEvent();
	}

	/**
	 * @see StorageLineEventListener#storageLineChanged(StorageLineChangedEvent)
	 */
	public void storageLineChanged(StorageLineChangedEvent event) {
		PICLUtils.logEvent("StorageLine changed", this);
		fStorageBufferCurrent = false;

		// NOTE: currently this only monitors the base storage line
		fireChangeEvent();
	}

	/**
	 * @see IDebugElement#getName()
	 */
	public String getName() throws DebugException {
		return null;
	}

	/**
	 * Return the storage contents
	 * @param start offset.  This can be negative if storage before the address is required
	 * @param number of bytes to return.   The buffer returned will contain 2x the number of
	 * bytes requested because each byte is represented by 2 characters
	 * @return array of buffers with contents of storage
	 * Use PICLStorage.RAW and PICLStorage.TRANSLATED to get the correct stringbuffer from the returned
	 * values.
	 */
	public StringBuffer[] getStorageLine(int offset, int numberOfBytes) {

		// check to make sure that we have enough storage to satisfy the request
		// NOTE: anytime storage is requested an additional storage line before and
		// after is obtained.   The requested offset can therefore be -ve

		int adjustedOffset = offset - fLowestOffsetMonitored;
		int adjustedEndOffset = adjustedOffset + numberOfBytes;

		PICLUtils.logText("(GET)Requested offset= " + offset + " numberOfBytes= " + numberOfBytes);
		PICLUtils.logText("(GET)Adjusted  offset= " + adjustedOffset + " adjusted endoffset= " + adjustedEndOffset);
		PICLUtils.logText("(GET)Storage first line/last line =(" + fStorage.getFirstLineOffset() + "," + fStorage.getLastLineOffset() + ")");
		PICLUtils.logText("(GET)offset + # bytes= " + (offset + numberOfBytes));

		if (adjustedOffset < 0 || (offset + numberOfBytes) > fHighestOffsetMonitored) {
			PICLUtils.logText("Storage requested outside what is available.  Get new storage");
			// calculate the new range required.   Take the offset + or - and determine how many NUM_BYTES storage lines
			// are required.   subtract 1 to handle rounding.
			int newFirstLine = offset/NUM_BYTES - 1;   // -1 is to handle rounding

			// Next take the offset requested and add the number of bytes.   This is the total number of bytes above
			// the storage address requested.   Add one for rounding and this represents the number of storage lines
			// above the requested address
			int newLastLine = ((offset + numberOfBytes)/NUM_BYTES + 1);

			try {
				releaseStorageLines();  // remove this object as listener for storage lines
				fStorage.setRange(newFirstLine,newLastLine,DebugEngine.sendReceiveSynchronously);
				calculateStorageValues();
				fStorageBufferCurrent = false;
				adjustedOffset = offset - fLowestOffsetMonitored;
				adjustedEndOffset = adjustedOffset + numberOfBytes;
			} catch(IOException ioe) {
				return null;   // error condition
			}

		}

		if (fStorageBuffer == null)
			fStorageBuffer = new StringBuffer(fTotalNumBytesMonitored * 2);  // there are 2 chars per byte

		if (fTranslatedStorageBuffer == null)
			fTranslatedStorageBuffer = new StringBuffer(fTotalNumBytesMonitored);

		if (!fStorageBufferCurrent) {

			fStorageBuffer.setLength(0);    // clear the buffer
			fTranslatedStorageBuffer.setLength(0);    // clear the buffer

			// get the lines from the Storage object
			fMonitoredStorageLines = fStorage.getStorageLines();
			Enumeration enum = fMonitoredStorageLines.elements();

			while (enum.hasMoreElements()) {
				StorageLine contents = (StorageLine)enum.nextElement();
				fStorageBuffer.append((contents.getStorage())[RAW]);
				fTranslatedStorageBuffer.append((contents.getStorage())[TRANSLATED]);
			}
//			PICLUtils.logText("Raw Storage 		= ("+ fStorageBuffer.length() +")" + fStorageBuffer.toString());
//			PICLUtils.logText("Xlated Storage 	= ("+ fTranslatedStorageBuffer.length() +")" + fTranslatedStorageBuffer.toString());
			fStorageBufferCurrent = true;
		}

		PICLUtils.logText("RAW 			buffer is " + fStorageBuffer.length());
		PICLUtils.logText("TRANSLATED 	buffer is " + fTranslatedStorageBuffer.length());

		StringBuffer buffers[] = new StringBuffer[2];
		buffers[RAW] = new StringBuffer(fStorageBuffer.substring(adjustedOffset * 2, adjustedEndOffset * 2));
		buffers[TRANSLATED] = new StringBuffer(fTranslatedStorageBuffer.substring(adjustedOffset, adjustedEndOffset));


		// ++++++ test code ++++++++

		boolean b = false;
		if (b)
			updateStorage(RAW,-100,"1234");

		// ++++++ test code ++++++++


		return buffers;
	}

	/**
	 * Return the address expression used to compute the address
	 * @return A string that represents the original expression
	 */
	public String getAddress() {
		String storageAddress = fStorage.getAddress();
		if (storageAddress.toUpperCase().startsWith("0X")) {
			storageAddress = storageAddress.substring(2);
		}
		return storageAddress;
		//return fStorage.getAddress();
	}


	/**
	 * Gets the storage
	 * @return Returns a Storage
	 */
	public Storage getStorage() {
		return fStorage;
	}

	private void calculateStorageValues() {
		fNumStorageLines = Math.abs(fStorage.getFirstLineOffset() - fStorage.getLastLineOffset()) +1;
		fTotalNumBytesMonitored = fNumStorageLines * fStorage.getNumberOfUnitsPerLine();

		fLowestOffsetMonitored = fStorage.getNumberOfUnitsPerLine() * fStorage.getFirstLineOffset();
		fHighestOffsetMonitored = fLowestOffsetMonitored + fTotalNumBytesMonitored - 1; // check to see if the
		// last byte is included in the monitored list.
		PICLUtils.logText("Storage first line/last line =(" + fStorage.getFirstLineOffset() + "," + fStorage.getLastLineOffset() + ")");
		PICLUtils.logText("Calculated values: #lines		= " + fNumStorageLines);
		PICLUtils.logText("Calculated values: #total bytes	= " + fTotalNumBytesMonitored);
		PICLUtils.logText("Calculated values: #lowest offset= " + fLowestOffsetMonitored);
		PICLUtils.logText("Calculated values: #highest offset= " + fHighestOffsetMonitored);

	}

	private void releaseStorageLines() {
		Enumeration enum = fMonitoredStorageLines.elements();

		while (enum.hasMoreElements()) {
			StorageLine sl = (StorageLine)enum.nextElement();
//			if (sl.getLineOffset() == 0)  // this is the base source line
				sl.removeEventListener(this);
		}
	}

	private void monitorStorageLines() {
		Enumeration enum = fMonitoredStorageLines.elements();

		while (enum.hasMoreElements()) {
			StorageLine sl = (StorageLine)enum.nextElement();
//			if (sl.getLineOffset() == 0) // this is the base source line
				sl.addEventListener(this);
		}
	}


}

