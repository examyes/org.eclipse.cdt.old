/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.db;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

/**
 * @author Doug Schaefer
 *
 */
public class Chunk {

	MappedByteBuffer buffer;
	
	Chunk(RandomAccessFile file, int offset) throws IOException {
		buffer = file.getChannel().map(MapMode.READ_WRITE, offset, Database.CHUNK_SIZE);
	}
	
	void putInt(int offset, int value) {
		buffer.putInt(offset % Database.CHUNK_SIZE, value);
	}
	
	int getInt(int offset) {
		return buffer.getInt(offset % Database.CHUNK_SIZE);
	}
	
	void putChar(int offset, char value) {
		buffer.putChar(offset % Database.CHUNK_SIZE, value);
	}
	
	char getChar(int offset) {
		return buffer.getChar(offset % Database.CHUNK_SIZE);
	}
	
	void putString(int offset, String value) {
		buffer.position(offset % Database.CHUNK_SIZE);
		int n = value.length();
		for (int i = 0; i < n; ++i)
			buffer.putChar(value.charAt(i));
		buffer.putChar('\0');
	}
	
	String getString(int offset) {
		StringBuffer strbuf = new StringBuffer();
		buffer.position(offset % Database.CHUNK_SIZE);
		char c = buffer.getChar();
		while (c != '\0') {
			strbuf.append(c);
			c = buffer.getChar();
		}
		return strbuf.toString();
	}

}
