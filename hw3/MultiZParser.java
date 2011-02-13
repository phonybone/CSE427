import java.io.*;
import java.lang.*;
import java.util.*;


class MultiZParser {
    public BufferedReader reader;

    MultiZParser(String multizfile, double c) {
	try {
	    reader=new BufferedReader(new InputStreamReader(new FileInputStream(multizfile)));
	} catch (IOException ioe) {
	    new Die(ioe);
	}

	

    }
