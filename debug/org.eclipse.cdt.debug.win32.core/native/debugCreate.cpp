#include <dbgeng.h>

typedef HRESULT (* DebugCreateProc) (__in REFIID InterfaceId, __out PVOID* Interface);

DebugCreateProc debugCreate = NULL;

STDAPI DebugCreate(__in REFIID InterfaceId, __out PVOID* Interface) {
	if (debugCreate == NULL) {
		// Find the debugging tools for Windows in the registry
		HKEY key;
		LONG rc = RegOpenKeyEx(HKEY_CURRENT_USER,
			L"Software\\Microsoft\\DebuggingTools",
			0, KEY_QUERY_VALUE, &key);
		if (rc != ERROR_SUCCESS) {
			MessageBox(NULL, L"Debugging Tools for Windows not installed", L"DebugCreate", MB_ICONERROR);
			return E_FAIL;
		}

		wchar_t dllpath[1024];
		DWORD buffLen = sizeof(dllpath);
        rc = RegQueryValueEx(key, L"WinDbg", NULL, NULL, (LPBYTE)dllpath, &buffLen);
		if ((rc != ERROR_SUCCESS) || (buffLen > sizeof(dllpath))) {
			MessageBox(NULL, L"Debugging Tools for Windows not installed correctly", L"DebugCreate", MB_ICONERROR);
            return E_FAIL;
		}

		RegCloseKey(key);

		// First we need to load in dbghelp from here
		int len = wcslen(dllpath);
		wcscpy(dllpath + len, L"\\dbghelp.dll");
		LoadLibrary(dllpath);

		// Now the good stuff
		wcscpy(dllpath + len, L"\\dbgeng.dll");
		HMODULE dbgeng = LoadLibrary(dllpath);
		if (dbgeng == NULL) {
			MessageBox(NULL, L"Unable to load dbgeng.dll", L"DebugCreate", MB_ICONERROR);
			return E_FAIL;
		}
		
		// Get the proc address for DebugCreate
		debugCreate = (DebugCreateProc)GetProcAddress(dbgeng, "DebugCreate");
		if (debugCreate == NULL) {
			MessageBox(NULL, L"Unable to find DebugCreate proc", L"DebugCreate", MB_ICONERROR);
			return E_FAIL;
		}

	}
	
	return debugCreate(InterfaceId, Interface);
}
