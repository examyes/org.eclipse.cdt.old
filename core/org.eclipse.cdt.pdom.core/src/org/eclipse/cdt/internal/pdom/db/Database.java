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

/**
 * @author Doug Schaefer
 *
 */
public class Database {

	private Chunk[] toc;
	private final RandomAccessFile file;
	
	// public for tests only, you shouldn't need these
	public static final int CHUNK_SIZE = 4096;
	public static final int MIN_SIZE = 16;
	public static final int INT_SIZE = 4;
	public static final int CHAR_SIZE = 2;
	public static final int PREV_OFFSET = INT_SIZE;
	public static final int NEXT_OFFSET = INT_SIZE * 2;
	public static final int DATA_AREA = CHUNK_SIZE / MIN_SIZE * INT_SIZE;
	
	public Database(String filename) throws IOException {
		file = new RandomAccessFile(filename, "rw"); //$NON-NLS-1$
		
		// Allocate chunk table, make sure we have at least one
		long nChunks = file.length() / CHUNK_SIZE;
		if (nChunks == 0) {
			create();
			++nChunks;
		}
		
		toc = new Chunk[(int)nChunks];
		
		// Load in the magic chunk zero
		toc[0] = new Chunk(file, 0);
	}

	/**
	 * Create the database, including chunk zero and the b-trees.
	 */
	private void create() throws IOException {
		file.seek(0);
		file.write(new byte[CHUNK_SIZE]); // the header chunk
	}
	
	/**
	 * Return the Chunk that contains the given offset.
	 * 
	 * @param offset
	 * @return
	 */
	Chunk getChunk(int offset) throws IOException {
		// TODO - Eventually, we'll need to do caching so that we
		// don't end up loading the entire database into memory
		int index = offset / CHUNK_SIZE;
		if (toc[index] == null)
			toc[index] = new Chunk(file, index * CHUNK_SIZE);
		return toc[index];
	}

	/**
	 * Allocate a block out of the database.
	 * 
	 * @param size
	 * @return
	 */ 
	public int malloc(int size) throws IOException {
		// Which block size
		int freeblock = 0;
		int blocksize;
		int matchsize = 0;
		for (blocksize = MIN_SIZE; blocksize <= CHUNK_SIZE; blocksize += MIN_SIZE) {
			if (blocksize - INT_SIZE >= size) {
				if (matchsize == 0) // our real size
					matchsize = blocksize;
				freeblock = getFirstBlock(blocksize);
				if (freeblock != 0)
					break;
			}
		}
		
		// get the block
		Chunk chunk;
		if (freeblock == 0) {
			// Out of memory, allocate a new chunk
			int i = createChunk();
			freeblock = i * CHUNK_SIZE;
			blocksize = CHUNK_SIZE;
			chunk = toc[i];
		} else {
			chunk = getChunk(freeblock);
			removeBlock(chunk, matchsize, freeblock);
		}
 
		if (blocksize != matchsize) {
			// Add in the unused part of our block
			addBlock(chunk, blocksize - matchsize, freeblock + matchsize);
		}
		
		// Make our size negative to show in use
		chunk.putInt(freeblock, - matchsize);
		
		return freeblock + INT_SIZE;
	}
	
	private int createChunk() throws IOException {
		int offset = (int)file.length();
		file.seek(offset);
		file.write(new byte[CHUNK_SIZE]);
		Chunk[] oldtoc = toc;
		int i = oldtoc.length;
		toc = new Chunk[i + 1];
		System.arraycopy(oldtoc, 0, toc, 0, i);
		toc[i] = new Chunk(file, offset);
		return i;
	}
	
	private int getFirstBlock(int blocksize) {
		return toc[0].getInt((blocksize / MIN_SIZE - 1) * INT_SIZE);
	}
	
	private void setFirstBlock(int blocksize, int block) {
		toc[0].putInt((blocksize / MIN_SIZE - 1) * INT_SIZE, block);
	}
	
	private void removeBlock(Chunk chunk, int blocksize, int block) throws IOException {
		int prevblock = chunk.getInt(block + PREV_OFFSET);
		int nextblock = chunk.getInt(block + NEXT_OFFSET);
		if (prevblock != 0)
			putInt(prevblock + NEXT_OFFSET, nextblock);
		else // we were the head
			setFirstBlock(blocksize, nextblock);
			
		if (nextblock != 0)
			putInt(nextblock + PREV_OFFSET, prevblock);
	}
	
	private void addBlock(Chunk chunk, int blocksize, int block) {
		// Mark our size
		chunk.putInt(block, blocksize);

		// Add us to the head of the list
		int prevfirst = getFirstBlock(blocksize);
		chunk.putInt(block + PREV_OFFSET, 0);
		chunk.putInt(block + NEXT_OFFSET, prevfirst);
		if (prevfirst != 0)
			chunk.putInt(prevfirst + PREV_OFFSET, block);
		setFirstBlock(blocksize, block);
	}
	
	/**
	 * Free an allocate block.
	 * 
	 * @param offset
	 */
	public void free(int offset) throws IOException {
		// TODO - look for opportunities to merge blocks
		int block = offset - INT_SIZE;
		Chunk chunk = getChunk(block);
		int blocksize = - chunk.getInt(block);
		addBlock(chunk, blocksize, block);
	}

	public void putInt(int offset, int value) throws IOException {
		Chunk chunk = getChunk(offset);
		chunk.putInt(offset, value);
	}
	
	public int getInt(int offset) throws IOException {
		Chunk chunk = getChunk(offset);
		return chunk.getInt(offset);
	}

	public int putString(String value) throws IOException {
		int record = malloc((value.length() + 1) * CHAR_SIZE);
		Chunk chunk = getChunk(record);
		chunk.putString(record, value);
		return record;
	}
	
	public String getString(int offset) throws IOException {
		Chunk chunk = getChunk(offset);
		return chunk.getString(offset);
	}
	
	public char getChar(int offset) throws IOException {
		Chunk chunk = getChunk(offset);
		return chunk.getChar(offset);
	}
}
