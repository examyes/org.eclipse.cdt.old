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

		wchar_t dbgengDLL[1024];
		DWORD buffLen = sizeof(dbgengDLL);
        rc = RegQueryValueEx(key, L"WinDbg", NULL, NULL, (LPBYTE)dbgengDLL, &buffLen);
		if ((rc != ERROR_SUCCESS) || (buffLen > sizeof(dbgengDLL))) {
			MessageBox(NULL, L"Debugging Tools for Windows not installed correctly", L"DebugCreate", MB_ICONERROR);
            return E_FAIL;
		}

		RegCloseKey(key);

		// Load the DLL from that install
		wcscat(dbgengDLL, L"\\dbgeng.dll");
		HMODULE dbgengLib = LoadLibrary(dbgengDLL);
		if (dbgengLib == NULL) {
			MessageBox(NULL, L"Unable to load dbgeng.dll", L"DebugCreate", MB_ICONERROR);
			return E_FAIL;
		}
		
		// Get the proc address for DebugCreate
		debugCreate = (DebugCreateProc)GetProcAddress(dbgengLib, "DebugCreate");
		if (debugCreate == NULL) {
			MessageBox(NULL, L"Unable to find DebugCreate proc", L"DebugCreate", MB_ICONERROR);
			return E_FAIL;
		}

	}
	
	return debugCreate(InterfaceId, Interface);
}
