all:
	clang -fpic -luring -shared src/main/c/liburinghelpers.c -o target/liburinghelpers.so