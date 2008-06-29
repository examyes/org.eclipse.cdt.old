package org.eclipse.cdt.msw.debug.dbgeng;

public interface SymOpt {

	public static final int CASE_INSENSITIVE = 0x1;
	public static final int UNDNAME = 0x2;
	public static final int DEFERRED_LOADS = 0x4;
	public static final int NO_CPP = 0x8;
	public static final int LOAD_LINES = 0x10;
	public static final int OMAP_FIND_NEAREST = 0x20;
	public static final int LOAD_ANYTHING = 0x40;
	public static final int IGNORE_CVREC = 0x80;
	public static final int NO_UNQUALIFIED_LOADS = 0x100;
	public static final int FAIL_CRITICAL_ERRORS = 0x200;
	public static final int EXACT_SYMBOLS = 0x400;
	public static final int ALLOW_ABSOLUTE_SYMBOLS = 0x800;
	public static final int IGNORE_NT_SYMPATH = 0x1000;
	public static final int INCLUDE_32BIT_MODULES = 0x2000;
	public static final int PUBLICS_ONLY = 0x4000;
	public static final int NO_PUBLICS = 0x8000;
	public static final int AUTO_PUBLICS = 0x10000;
	public static final int NO_IMAGE_SEARCH = 0x20000;
	public static final int SECURE = 0x40000;
	public static final int NO_PROMPTS = 0x80000;
	public static final int DEBUG = 0x80000000;

}
