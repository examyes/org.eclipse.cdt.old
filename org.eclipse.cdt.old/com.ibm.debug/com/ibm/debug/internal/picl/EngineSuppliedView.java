package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/EngineSuppliedView.java, eclipse, eclipse-dev, 20011129
// Version 1.9 (last modified 11/29/01 13:42:09)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.Line;
import com.ibm.debug.model.Location;
import com.ibm.debug.model.ViewFile;
import com.ibm.debug.model.ViewInformation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Vector;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * <code>EngineSuppliedView</code> class is used to store the texted supplied
 * by a debug engine for a "source view" (e.g. disassembly, mixed)
 */

public class EngineSuppliedView implements IStorage {

	private ByteArrayInputStream data = null;
	public String name = null;
	private Vector fLines = null;		// cache of lines obtained from the model cache, used to populate the buffer

	// This view could be based either on a stackframe or a Location
	private PICLStackFrame fStackFrame = null;
	private Location fLocation = null;

	private int fLineNumber;			// current line number (relative to the editor buffer, not the actual file)
	private int fFileStartLine;			// start line for full disassembly storage content
	private int fFileEndLine;			// end line for full disassembly storage content
	private int fBufferStartLine;		// start line for buffered portion of content
	private int fBufferEndLine;			// end line for buffered portion of content
	private boolean lineIsExecutable[];
	/**
	 * Constructor for EngineSuppliedView
	 */
	public EngineSuppliedView(PICLStackFrame frame) {
		super();
		fStackFrame = frame;
	}

	/**
	 * Constructor for EngineSuppliedView
	 */
	public EngineSuppliedView(Location location) {
		super();
		fLocation = location;
	}


	public void initEngineSuppliedView(int start, int end) {
		fFileStartLine = start;
		fFileEndLine = end;
		// fLines[0] not used so next extra slot at end of vector
		fLines = new Vector(end - start + 2);
		for (int i=start; i <= end+1; i++) {
			fLines.addElement(null);
		}
	}

	/**
	 * refreshes this EngineSuppliedView with information from the specified stackframe
	 */
	public void setStackFrame(PICLStackFrame frame) {
		fStackFrame = frame;
	}
	
	/**
	 * @see IStorage#getContents()
	 */
	public InputStream getContents() throws CoreException {
		if (data != null)
			// Move pointer to start of storage
			data.reset();
		return data;
	}

	/**
	 * @see IStorage#getFullPath()
	 */
	public IPath getFullPath() {
		return null;
	}

	/**
	 * @see IStorage#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see IStorage#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class arg0) {
		return null;
	}

	public int getLineNumber() {
		if (fStackFrame != null) {
			// Must be building view for a stack frame.
			ViewInformation viewInfo = fStackFrame.getViewInformation();
			if (viewInfo == null) {return -1;}
			Location loc = fStackFrame.getLocation(viewInfo);
			if (loc == null) {return -1;}
			fLineNumber = loc.lineNumber();
		} else if (fLocation != null) {
			// Must be building view from a Location
			fLineNumber = fLocation.lineNumber();
		} else {
			fLineNumber = 1;
		}

		// Make sure linenumber does not exceed max line in file
		if (fLineNumber > fFileEndLine) {
			fLineNumber = fFileEndLine;
		}

		// Make sure linenumber not less than min line in file
		if (fLineNumber < fFileStartLine) {
			fLineNumber = fFileStartLine;
		}

		// Lastly make sure line number is not negative (which could
		// indicate a problem in the engine)
		if (fLineNumber < 0)
			fLineNumber = 0;

		//if the current line isn't in the cache and buffer, put it there
		if (fLines.elementAt(fLineNumber) == null) {
			int startLine = 1;
			int endLine = fFileEndLine;
			if (fLineNumber >  80) {
				startLine = fLineNumber - 80;
				}
			if (endLine > fLineNumber + 80) {
				endLine = fLineNumber + 80;
			}
			try {
				getLines(startLine, endLine);
			} catch (IOException ioe) {}
			//showLines(1, endLine);
			showLines(startLine, endLine);
		}

		fLineNumber = fLineNumber - fBufferStartLine + 1;  // convert actual line number to relative buffer line number
		return fLineNumber;
	}

	public void setLineNumber(int lineNum) {
		fLineNumber = lineNum;
	}

	// this method gets the line from the model/engine and updates the cache
	// it does not update the editor buffer
	/**
	 * ensures the local UI cache contains the requested line
	 */
	public boolean getLine(int lineNum) throws IOException {
		if ((lineNum < fFileStartLine) || (lineNum > fFileEndLine))
			return false;
		if (fLines.elementAt(lineNum) != null)
			return true;

		ViewFile vfile = getViewFile();
		if (vfile == null)
			return false;

		determineName(vfile);

		fFileStartLine = vfile.firstLineNumber();
		fFileEndLine =  vfile.lastLineNumber();

		//try to get it from model cache
		Line srcLine = vfile.getLineFromCache(lineNum);
		if (srcLine == null) {  //not cached in model yet
			vfile.getLinesFromEngine(lineNum, lineNum);
			srcLine = vfile.getLineFromCache(lineNum);
		}
		fLines.setElementAt(srcLine, lineNum);
		return true;

	}

	// this method gets the lines from the model/engine
	// it does not update the editor buffer
	/**
	 * ensures the local UI cache contains the requested lines and updates the editor buffer
	 */
	public boolean getLines(int startLine, int endLine) throws IOException {
		if (startLine < fFileStartLine)
			return false;
		if (endLine > fFileEndLine)
			return false;
		if (endLine < startLine)
			return false;

		ViewFile vfile = getViewFile();
		if (vfile == null) {return false;}

		determineName(vfile);

		for (int i = startLine; i<= endLine; i++){

			if (fLines.elementAt(i) == null) {
				//get the line from the engine/model
				Vector srcLines = vfile.getLinesFromCache(startLine, endLine - startLine + 1);
				if (srcLines == null) {  //not cached in model yet
					vfile.getLinesFromEngine(startLine, endLine - startLine + 1);
					srcLines = vfile.getLinesFromCache(startLine, endLine - startLine + 1);
				}

				if (srcLines == null)
					// Could not get lines from engine, maybe it cannot find the source file
					return false;
				for (int j=0; j < endLine - startLine + 1; j++) {
					fLines.setElementAt(srcLines.elementAt(j), startLine + j);
				}
				break;
			}
		}
		return true;
	}


	/**
	 * sets the ByteArrayInputStream to the specific range of lines from the cache.
	 * if the specified lines are not in the cache, they are retrieved and cached
	 */
	public boolean showLines(int start, int end) {

		// If we are showing a source file, we want the editor to
		// start at the first line of the file so breakpoints
		// show up properly.
		int fileStart = getFileStartLine();
		if (start > fileStart) {
			ViewFile vFile = getViewFile();
			if (vFile != null) {
				if (vFile.view().isSourceView()) {
					// force start line to be first line in file
					start = getFileStartLine();
				}
			}
		}
		
		boolean cacheSuccess = false;
		try {
			cacheSuccess = getLines(start, end);
		} catch (IOException ioe) {return false;}
		if (!cacheSuccess) {return false;}

		int numLines = end - start + 2;
		lineIsExecutable = new boolean[numLines];
		StringBuffer allLines = new StringBuffer (25000);
		for (int i = start; i <= end; i++)
		{
			Line line = (Line) fLines.get(i);
			lineIsExecutable[i - start + 1] = line.isExecutable();
			allLines.append(line.lineText() + "\n");
		}

		byte bytes[] = allLines.toString().getBytes();
		data = new ByteArrayInputStream(bytes);
		if (data == null) {return false;}
		fBufferStartLine = start;
		fBufferEndLine = end;
		return true;
//		fLineNumber = location.lineNumber();

	}

	/**
	 * Gets the bufferStartLine
	 * @return Returns a int
	 */
	public int getBufferStartLine() {
		return fBufferStartLine;
	}
	/**
	 * Sets the bufferStartLine
	 * @param bufferStartLine The bufferStartLine to set
	 */
//	public void setBufferStartLine(int bufferStartLine) {
//		fBufferStartLine = bufferStartLine;
//	}
	/**
	 * Gets the bufferEndLine
	 * @return Returns a int
	 */
	public int getBufferEndLine() {
		return fBufferEndLine;
	}
	/**
	 * Sets the bufferEndLine
	 * @param bufferEndLine The bufferEndLine to set
	 */
//	public void setBufferEndLine(int bufferEndLine) {
//		fBufferEndLine = bufferEndLine;
//	}
	/**
	 * Gets the fileStartLine
	 * @return Returns a int
	 */
	public int getFileStartLine() {
		return fFileStartLine;
	}
	/**
	 * Sets the fileStartLine
	 * @param fileStartLine The fileStartLine to set
	 */
//	public void setFileStartLine(int fileStartLine) {
//		fFileStartLine = fileStartLine;
//	}
	/**
	 * Gets the fileEndLine
	 * @return Returns a int
	 */
	public int getFileEndLine() {
		return fFileEndLine;
	}
	/**
	 * Sets the fileEndLine
	 * @param fileEndLine The fileEndLine to set
	 */
//	public void setFileEndLine(int fileEndLine) {
//		fFileEndLine = fileEndLine;
//	}


	/**
	 * Gets the lineIsExecutable
	 * @return Returns a boolean[]
	 */
	public boolean[] getLineIsExecutable() {
		return lineIsExecutable;
	}
	/**
	 * Sets the lineIsExecutable
	 * @param lineIsExecutable The lineIsExecutable to set
	 */
	public void setLineIsExecutable(boolean[] lineIsExecutable) {
		this.lineIsExecutable = lineIsExecutable;
	}

	private void determineName (ViewFile vfile) {
		if (vfile == null)
			return;

		String tmpName = null;
		try
		{
			tmpName = vfile.baseFileName();

		}
		catch (IOException e)
		{
			tmpName = PICLUtils.getResourceString("EngineSuppliedView.labelUnknown");
		}
		name = tmpName;
	}

	/**
	 * Find a ViewFile to use from either the stackframe or part, which ever is available.
	 *
	 */
	private ViewFile getViewFile() {
		ViewFile vfile = null;
		if (fStackFrame != null) {
			ViewInformation viewInfo = fStackFrame.getViewInformation();
			if (viewInfo == null)
				return null;

			Location loc = fStackFrame.getLocation(viewInfo);
			if (loc == null)
				return null;

			vfile = loc.file();

		} else if (fLocation != null) {
			vfile = fLocation.file();
		}
		return vfile;
	}

}


