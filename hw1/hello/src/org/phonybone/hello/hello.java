package org.phonybone.hello;

import static java.lang.System.out;
import static java.lang.System.err;

class Hello {
    public static void main(String argv[]) {
	out.println("Hello, world.");
	err.println("Barf");
	System.exit(0);
    }
}

