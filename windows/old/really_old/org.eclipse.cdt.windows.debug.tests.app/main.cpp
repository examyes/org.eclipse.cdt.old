#include <windows.h>

void foo() {
	MessageBox(NULL, "Hello World", "Debug Test", MB_OK);
}

int main(int argc, char **argv) {
	foo();
	return 42;
}
