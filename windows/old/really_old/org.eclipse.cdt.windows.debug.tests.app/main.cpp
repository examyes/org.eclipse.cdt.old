#include <windows.h>

void foo() {
	MessageBox(NULL, "Hi there", "Test", MB_OK);
}

int main(int argc, char **argv) {
	foo();
	return 42;
}
