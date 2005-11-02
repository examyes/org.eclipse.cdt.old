package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.pdom.db.Database;
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
		int mem = db.malloc(42);
		assertEquals(-48, db.getInt(mem - 4));
		db.free(mem);
		assertEquals(48, db.getInt(mem - 4));
		assertEquals(mem - 4, db.getInt((48 / 16 - 1) * 4));
		assertEquals(mem - 4 + 48, db.getInt(((4096 - 48) / 16 - 1) * 4));
	}

	public void test2() throws Exception {
		// Tests free block linking
		File f = getTestDir().append("test2.dat").toFile();
		f.delete();
		Database db = new Database(f.getCanonicalPath());
		int mem1 = db.malloc(42);
		int mem2 = db.malloc(42);
		db.free(mem1);
		db.free(mem2);
		assertEquals(mem2 - 4, db.getInt((48 / 16 - 1) * 4));
		assertEquals(0, db.getInt(mem2));
		assertEquals(mem1 - 4, db.getInt(mem2 + 4));
		assertEquals(mem2 - 4, db.getInt(mem1));
		assertEquals(0, db.getInt(mem1 + 4));
	}

}
