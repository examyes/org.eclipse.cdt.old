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

	private final Chunk[] toc;
	private final RandomAccessFile file;
	
	static final int CHUNK_SIZE = 4096;
	static final int MIN_SIZE = 16;
	static final int INT_SIZE = 4;
	static final int PREV_OFFSET = INT_SIZE;
	static final int NEXT_OFFSET = INT_SIZE * 2;
	
	public Database(String filename) throws IOException {
		file = new RandomAccessFile(filename, "rw"); //$NON-NLS-1$
		
		// Allocate chunk table, make sure we have at least one
		long nChunks = file.length() / CHUNK_SIZE;
		if (nChunks == 0) {
			byte[] emptyChunk = new byte[CHUNK_SIZE]; 
			file.seek(0);
			file.write(new byte[CHUNK_SIZE]); // the header chunk
			++nChunks;
		}
		
		toc = new Chunk[(int)nChunks];
		
		// Load in chunk zero and one
		toc[0] = new Chunk(file, 0);
	}

	/**emptyChunk
	 * Return the Chunk that contains the given offset.
	 * 
	 * @param offset
	 * @return
	 */
	private Chunk getChunk(int offset) {
		int index = offset / CHUNK_SIZE;
		
		if (index >= toc.length) {
			// grow 
		}

		return null;
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
				freeblock = toc[0].getInt(blocksize / MIN_SIZE - 1);
				if (freeblock != 0)
					break;
			}
		}
		
		// get the block
		Chunk chunk;
		if (freeblock == 0) {
			// Out of memory, allocate a new chunk
			blocksize = CHUNK_SIZE;
			freeblock = (int)file.length();
			file.seek(freeblock);
			file.write(new byte[CHUNK_SIZE]);
			chunk = new Chunk(file, freeblock);
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
	
	private int getFirstBlock(int blocksize) {
		return toc[0].getInt((blocksize / MIN_SIZE - 1) * INT_SIZE);
	}
	
	private void setFirstBlock(int blocksize, int block) {
		toc[0].putInt((blocksize / MIN_SIZE - 1) * INT_SIZE, block);
	}
	
	private void removeBlock(Chunk chunk, int blocksize, int block) {
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
		chunk.putInt(block + PREV_OFFSET, 0);
		chunk.putInt(block + NEXT_OFFSET, getFirstBlock(blocksize));
		setFirstBlock(blocksize, block);
	}
	
	/**
	 * Free an allocate block.
	 * 
	 * @param offset
	 */
	public void free(int offset) {
		// TODO - look for opportunities to merge blocks
		int block = offset - INT_SIZE;
		Chunk chunk = getChunk(block);
		int blocksize = - chunk.getInt(block);
		addBlock(chunk, blocksize, block);
	}

	public void putInt(int offset, int value) {
		Chunk chunk = getChunk(offset);
		chunk.putInt(offset, value);
	}
	
	/**
	 * Get an int at the given offset.
	 * 
	 * @param offset
	 * @return
	 */
	public int getInt(int offset) {
		Chunk chunk = getChunk(offset);
		return chunk.getInt(offset);
	}

}
