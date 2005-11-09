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

/**
 * Btree for String records.
 * 
 * @author Doug Schaefer
 */
public class StringBTree extends BTree {

	private final int offset;
	
	/**
	 * Constructor.
	 * 
	 * @param db
	 * @param rootPointer
	 * @param offset offset into the record of the string
	 */
	public StringBTree(Database db, int rootPointer, int offset) {
		super(db, rootPointer);
		this.offset = offset;
	}

	/**
	 * Returns the offset of the record matching the key.
	 * 
	 * @param key
	 * @return zero if not found, otherwise the offset of the
	 *              matching record.
	 */
	public int find(String key) throws IOException {
		int root = db.getInt(rootPointer);
		if (root == 0)
			return 0;
		else
			return find(root, key);
	}

	private int find(int node, String key) throws IOException {
		Chunk chunk = db.getChunk(node);
		
		int i;
		for (i = 0; i < NUM_RECORDS; ++i) {
			int record = getRecord(chunk, node, i);
			if (record == 0) {
				// past the end
				break;
			} else {
				int compare = compare(record, key);
				if (compare == 0)
					// found it
					return record;
				else if (compare > 0)
					// past it
					break;
			}
		}

		int	child = getChild(chunk, node, i);
		return child == 0 ? 0 : find(child, key); 
	}
	
	private int compare(int record, String key) throws IOException {
		Chunk chunk = db.getChunk(record);
		
		int i1 = record + offset;
		int i2 = 0;
		int n2 = key.length();
		char c1 = chunk.getChar(i1);
		char c2 = i2 < n2 ? key.charAt(i2) : 0;
		
		while (c1 != 0 && c2 != 0) {
			if (c1 < c2)
				return -1;
			if (c1 > c2)
				return 1;
			
			i1 += 2;
			i2 += 1;
			c1 = chunk.getChar(i1);
			c2 = i2 < n2 ? key.charAt(i2) : 0;
		}

		if (c1 == c2)
			return 0;
		else if (c1 == 0)
			return -1;
		else
			return 1;
	}
	
	protected int compare(int record1, int record2) throws IOException {
		// Prefetch the chunks
		Chunk chunk1 = db.getChunk(record1);
		Chunk chunk2 = db.getChunk(record2);
		
		int i1 = record1 + offset;
		int i2 = record2 + offset;
		char c1 = chunk1.getChar(i1);
		char c2 = chunk2.getChar(i2);
		
		while (c1 != 0 && c2 != 0) {
			if (c1 < c2)
				return -1;
			if (c1 > c2)
				return 1;
			
			i1 += 2;
			i2 += 2;
			c1 = chunk1.getChar(i1);
			c2 = chunk2.getChar(i2);
		}

		if (c1 == c2)
			return 0;
		else if (c1 == 0)
			return -1;
		else
			return 1;
	}

	/**
	 * Visit all nodes beginning with and after the key until
	 * the visitor returns falls.
	 * 
	 * @param visitor
	 * @param key
	 */
	public void visit(BTreeVisitor visitor, String key) throws IOException {
		int root = db.getInt(rootPointer);
		if (root == 0)
			return;

		visit(root, visitor, key, false);
	}
	
	private boolean visit(int node, BTreeVisitor visitor, String key, boolean found) throws IOException {
		// if found is false, we are still in search mode
		// once found is true visit everything
		// return false when ready to quit
		Chunk chunk = db.getChunk(node);

		if (found) {
			int child = getChild(chunk, node, 0);
			if (child != 0)
				if (!visit(child, visitor, key, true))
					return false;
		}
		
		int i;
		for (i = 0; i < NUM_RECORDS; ++i) {
			int record = getRecord(chunk, node, i);
			if (record == 0)
				return true;
			
			if (found) {
				if (!visitor.visit(record))
					return false;
				if (!visit(getChild(chunk, node, i + 1), visitor, key, true))
					return false;
			} else {
				int compare = compare(record, key);
				if (compare > 0) {
					// start point is to the left
					if (!visit(getChild(chunk, node, i), visitor, key, false))
						return false;
					if (!visitor.visit(record))
						return false;
					if (!visit(getChild(chunk, node, i + 1), visitor, key, true))
						return false;
					found = true;
				} else if (compare == 0) {
					if (!visitor.visit(record))
						return false;
					if (!visit(getChild(chunk, node, i + 1), visitor, key, true))
							return false;
					found = true;
				} // else skip over this one
			}
		}
		
		return true;
	}
	
}
