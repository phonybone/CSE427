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

    // Do the same for a StringBuffer:
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

    public static int occurences_of(String s, char a) {
	int l=s.length();
	int count=0;
	for (int i=0; i<l; i++) {
	    if (s.charAt(i)==a) count++;
	}
	return count;
    }


    // Take an array of (long) strings and divide them up into pieces of length width
    // Print (to a stringbuffer) each piece portion of the string
    public static String alignStrings(String[] block, String[] headers, String footers[], int offset,
				      int width, int header_width, int footer_width) {
	StringBuffer buf=new StringBuffer();
	if (block.length==0) return new String(buf);
	int align_length=block[0].length();
	for (int i=1; i<block.length; i++) {
	    assert (block[i].length()==align_length); // make sure all strings to be aligned are the same length
	}
	assert(headers.length==block.length); // make sure we have the right number of headers...
	assert(footers.length==block.length); // ...and footers

	String offset_str=String.valueOf(offset);
	int offset_width=offset_str.length()+1;
	String header_format=String.format("%%%ds %%%dd ", header_width, offset_width);
	String middle_format=String.format("%%-%ds", width);
	String footer_format=String.format("%%%dd %%%ds",offset_width, footer_width);
	
	int i=0;
	while (i<align_length) {
	    int end_index=i+width-1 < align_length? i+width : align_length;
	    for (int j=0; j<block.length; j++) {
		buf.append(String.format(header_format, headers[j], i+offset));
		buf.append(String.format(middle_format, block[j].substring(i,end_index)));
		buf.append(String.format(footer_format, i+width-1+offset, footers[j]));
		buf.append("\n");
	    }
	    buf.append("\n");
	    i+=width;
	}
	return new String(buf);
    }

    public static void main(String[] argv) {
	String[] alignment={"abcdefghijklmnopqrstuvwxyz",
			    "bcdefghijklmnopqrstuvwxyza",
			    "cdefghijklmnopqrstuvwxyzab",
			    "defghijklmnopqrstuvwxyzabc",
			    "efghijklmnopqrstuvwxyabcdz"
	};
	String[] headers={"1", "2", "3", "4", "5"};
	String[] footers={"1", "2", "3", "4", "5"};
	System.err.println(alignStrings(alignment, headers, footers, 1200, 7, 1, 1));
	System.err.println(alignStrings(alignment, headers, footers, 1200, 13, 1, 1));
    }
}