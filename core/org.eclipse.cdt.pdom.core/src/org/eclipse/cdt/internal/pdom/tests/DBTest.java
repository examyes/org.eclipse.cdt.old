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
		File f = getTestDir().append("test1.dat").toFile();
		f.delete();
		Database db = new Database(f.getCanonicalPath());
		int mem = db.malloc(42);
		db.free(mem);
	}

}
