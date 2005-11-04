package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.pdom.db.Database;
import org.eclipse.cdt.internal.pdom.db.StringBTree;
import org.eclipse.cdt.pdom.core.PDOMCorePlugin;
import org.eclipse.core.runtime.IPath;

public class DBTest extends TestCase {

	protected IPath getTestDir() {
		IPath path = PDOMCorePlugin.getDefault().getStateLocation().append("tests/");
		File file = path.toFile();
		if (!file.exists())
			file.mkdir();
		return path;
	}
	
	public void test1() throws Exception {
		// Tests block size and simple first block
		File f = getTestDir().append("test1.dat").toFile();
		f.delete();
		Database db = new Database(f.getCanonicalPath());

		final int realsize = 42;
		final int blocksize = (realsize / Database.MIN_SIZE + 1) * Database.MIN_SIZE;
		
		int mem = db.malloc(realsize);
		assertEquals(-blocksize, db.getInt(mem - Database.INT_SIZE));
		db.free(mem);
		assertEquals(blocksize, db.getInt(mem - Database.INT_SIZE));
		assertEquals(mem - Database.INT_SIZE, db.getInt((blocksize / Database.MIN_SIZE - 1) * Database.INT_SIZE));
		assertEquals(mem - Database.INT_SIZE + blocksize, db.getInt(((Database.CHUNK_SIZE - blocksize) / Database.MIN_SIZE - 1) * Database.INT_SIZE));
	}

	public void test2() throws Exception {
		// Tests free block linking
		File f = getTestDir().append("test2.dat").toFile();
		f.delete();
		Database db = new Database(f.getCanonicalPath());
		
		final int realsize = 42;
		final int blocksize = (realsize / Database.MIN_SIZE + 1) * Database.MIN_SIZE;

		int mem1 = db.malloc(realsize);
		int mem2 = db.malloc(realsize);
		db.free(mem1);
		db.free(mem2);
		assertEquals(mem2 - Database.INT_SIZE, db.getInt((blocksize / Database.MIN_SIZE - 1) * Database.INT_SIZE));
		assertEquals(0, db.getInt(mem2));
		assertEquals(mem1 - Database.INT_SIZE, db.getInt(mem2 + Database.INT_SIZE));
		assertEquals(mem2 - Database.INT_SIZE, db.getInt(mem1));
		assertEquals(0, db.getInt(mem1 + Database.INT_SIZE));
	}
	
	public void testStrings() throws Exception {
		// Tests inserting and retrieving strings
		File f = getTestDir().append("testStrings.dat").toFile();
		f.delete();
		Database db = new Database(f.getCanonicalPath());

		String[] names = {
				"ARLENE",
				"BRET",
				"CINDY",
				"DENNIS",
				"EMILY",
				"FRANKLIN",
				"GERT",
				"HARVEY",
				"IRENE",
				"JOSE",
				"KATRINA",
				"LEE",
				"MARIA",
				"NATE",
				"OPHELIA",
				"PHILIPPE",
				"RITA",
				"STAN",
				"TAMMY",
				"VINCE",
				"WILMA",
				"ALPHA",
				"BETA"
		};
		
		StringBTree btree = new StringBTree(db, Database.DATA_AREA);
		for (int i = 0; i < names.length; ++i) {
			String name = names[i];
			int record = db.putString(name);
			btree.insert(record);
		}
		
		for (int i = 0; i < names.length; ++i) {
			String name = names[i];
			int record = btree.find(name);
			assertTrue(record != 0);
			String rname = db.getString(record);
			assertEquals(name, rname);
		}
	}

}
