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
 * @author Doug Schaefer
 *
 */
public class IntBTree extends BTree {

	private final int offset;
	
	/**
	 * @param db
	 * @param rootPointer
	 */
	public IntBTree(Database db, int rootPointer, int offset) {
		super(db, rootPointer);
		this.offset = offset;
	}

	public int find(int key) throws IOException {
		int root = db.getInt(rootPointer);
		if (root == 0)
			return 0;
		else
			return find(root, key);
	}

	private int find(int node, int key) throws IOException {
		Chunk chunk = db.getChunk(node);
		
		int i;
		for (i = 0; i < NUM_RECORDS; ++i) {
			int record = getRecord(chunk, node, i);
			if (record == 0) {
				// past the end
				break;
			} else {
				int key1 = db.getInt(record + offset);
				if (key1 == key)
					// found it
					return record;
				else if (key1 > key)
					// past it
					break;
			}
		}

		int	child = getChild(chunk, node, i);
		return child == 0 ? 0 : find(child, key); 
	}

	protected int compare(int record1, int record2) throws IOException {
		int key1 = db.getInt(record1 + offset);
		int key2 = db.getInt(record2 + offset);
		
		if (key1 < key2)
			return -1;
		else if (key1 > key1)
			return 1;
		else
			return 0;
	}

}
