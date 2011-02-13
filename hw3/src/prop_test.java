import java.util.*;

class prop_test {
    public static void main(String[] argv) {
	for (int i=0; i<argv.length; i++) {
	    System.out.printf("%d: %s\n", i, argv[i]);
	}

	Properties props = System.getProperties();
	Enumeration it=props.keys();
	while (it.hasMoreElements()) {
	    String key=(String)it.nextElement();
	    String value=(String)props.get(key);
	    // System.out.printf("%s=%s\n", key, value);
	}
    }
}