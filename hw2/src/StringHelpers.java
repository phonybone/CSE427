import java.util.*;

class StringHelpers {

    // Chop a string into l-sized bits and return them as a list:
    public static ArrayList chop(String s, int l) {
	// Chop up string and add pieces to list:
	ArrayList a=new ArrayList();
	int i=0;
	while (i+l < s.length()) {
	    String ss=s.substring(i,i+l);
	    a.add(ss);
	    i+=l;
	}
	// Add last bit:
	String ss=s.substring(i);
	a.add(ss);

	return a;
    }


    public static ArrayList chop(StringBuffer s, int l) { return chop(new String(s),l); }


    // Insert a substring into a another string every f characters
    public static String insert_every(String s, String i, int f) {
	ArrayList l=chop(s,f);
	StringBuffer b=new StringBuffer();
	Iterator li=l.iterator();
	while (li.hasNext()) {
	    String ss=(String)li.next();
	    b.append(ss);
	    if (li.hasNext()) b.append(i);
	}
	return new String(b);
	
    }

    public static String insert_every(StringBuffer s, String i, int f) {
	return insert_every(new String(s),i,f);
    }
}