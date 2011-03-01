import java.io.*;
import java.util.*;

class Alignment {
    public int n_rows;
    public int n_cols;
    public char[][] alignment;
    public int match_threshold;
    public int[] matches;	// index is column

    private BufferedReader reader;

    Alignment(String alignment_file, int mt) {
	try {
	    reader=new BufferedReader(new InputStreamReader(new FileInputStream(alignment_file)));
	} catch (IOException ioe) {
	    new Die(ioe);
	}
	this.match_threshold=mt;
    }

    public Alignment read() {
	String line;

	ArrayList<StringBuffer> a=new ArrayList<StringBuffer>();
	a.add(new StringBuffer()); // first row
	n_rows=0;		 // count rows
	n_cols=0;

	int r=0; // r for "row"; points to current entry in array list to append to
	try {
	    while ((line=reader.readLine())!=null) {
		String[] fields=line.trim().split("\\s+");
		
		// Check to see if r needs resetting:
		if (fields.length==0 || (fields.length==1 && fields[0].matches("^\\s*$"))) {
		    r=0;
		    continue;
		}

		if (fields.length != 2) continue;
		if (fields[1].matches("^[A-Z-]+$")) {
		    // Get row to append to:
		    while (r>n_rows) {
			a.add(new StringBuffer());
			n_rows++;
		    }
		    a.get(r).append(fields[1]);
		    if (a.get(r).length() > n_cols) n_cols=a.get(r).length();
		    r++;
		}
	    }
	} catch (IOException ioe) {
	    new Die(ioe);
	}

	// convert arraylist to char[][]:
	alignment=new char[n_rows][];
	for (r=0; r<n_rows; r++) {
	    alignment[r]=new String(a.get(r)).toCharArray();
	}

	// Annotate matching cols
	this.annotate_matches();

	return this;
    }


    private void annotate_matches() {
	matches = new int[n_cols];

	for (int c=0; c<n_cols; c++) {
	    matches[c]=0;
	    for (int r=0; r<n_rows; r++) {
		if (alignment[r][c]!='-') matches[c]++;
	    }
	}
    }

    public boolean is_match_col(int i) {
	return matches[i] >= match_threshold;
    }


    public String toString() {
	StringBuffer buf=new StringBuffer();
	buf.append(String.format("%d X %d\n", n_rows, n_cols));
	
	return new String(buf);
    }

    public String dump() {
	StringBuffer buf=new StringBuffer(this.toString());
	buf.append("\n");
	for (int r=0; r<n_rows; r++) {
	    buf.append(new String(row(r)));
	    buf.append("\n");
	}
	for (int c=0; c<n_cols; c++) { buf.append(is_match_col(c)? '*':' '); }
	buf.append("\n");
	return new String(buf);
    }

    public char[] row(int n) {
	return alignment[n];
    }

    public String rowAsString(int n) {
	return new String(row(n));
    }

    public static void main(String[] argv) {
	Alignment a=new Alignment("hw2-muscle17.txt.short",8).read();
	System.out.println(a.dump());
    }

}