import java.io.*;
import java.lang.*;
import java.util.*;


class MultiZParser {
    public BufferedReader reader;
    public MultiZBlock current;

    MultiZParser(String multizfile) {
	try {
	    reader=new BufferedReader(new InputStreamReader(new FileInputStream(multizfile)));
	    this.current=parseNext();
	} catch (IOException ioe) {
	    new Die(ioe);
	}
    }	

    private MultiZBlock parseNext() {
	String line=null;
	MultiZBlock next=null;
	try {
	    while ((line=reader.readLine())!=null) {
		if (line.matches("^\\s*$")) break;

		if (line.startsWith("a")) { next=new MultiZBlock(); }

		if (line.startsWith("s")) {
		    String[] fields=line.split("\\s+");
		    try {
			String[] org_chr=fields[1].split("\\.");
			String org=org_chr[0];
			String chrom=org_chr[1];
			int start=Integer.valueOf(fields[2]).intValue();
			// fields[3] is length of alignment
			String strand=fields[4];
			// fields[5] is srcSize
			String seq=fields[6];
			next.put(org,new ChromSeq(org,chrom,seq,start));
		    
		    } catch (ArrayIndexOutOfBoundsException e) {
			new Die(e, "malformed line?: "+line);
		    }
		}		
	    }
	}  catch (IOException ioe) {
		new Die(ioe);
	}
	return next;
    }

    public MultiZBlock next() {
	MultiZBlock toReturn=current;
	current=parseNext();
	return toReturn;
    }

    public static void main(String[] argv) {
	MultiZParser mzp=new MultiZParser("chr11.maf");
	System.out.println(mzp.next());
	//	System.out.println(mzp.next());
	//	System.out.println(mzp.next());
    }
}
