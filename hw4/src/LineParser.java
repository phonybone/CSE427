import java.io.*;

abstract class LineParser {
    public BufferedReader reader;

    LineParser(String filename) {
	try {
	    reader=new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
	} catch (IOException ioe) {
	    new Die(ioe);
	}
    }

    abstract String next();
}