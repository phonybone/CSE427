public class TestProtStream {
    public static void main(String argv[]) {
	String prot_file=new String();
	String usage=new String("usage: java TestProtStream <prot_file>");
	try {
	    prot_file=argv[0];
	    System.err.println("reading from "+prot_file);
	} catch (ArrayIndexOutOfBoundsException e) {
	    System.err.println(usage);
	    System.exit(1);
	}
	
	ProtStream ps=new ProtStream(prot_file);
	//	ps.dump(System.out);
	//	System.out.println("----");
	

	String prot;
	//	int fuse=3;
	while ((prot=ps.next())!=null) {
	    System.out.println(ps.prot_name());
	    System.out.println(nice_prot(prot,70));
	    //	    ps.dump(System.out);
	    //	    System.out.println();

	    //	    if (--fuse==0) break;
	    //	    System.exit(1);
	}
    }

    public static String nice_prot(String prot, int line_len) {
	int l=prot.length();
	int i=0;
	StringBuffer nice=new StringBuffer();
	while (i<l) {
	    int j = i+line_len<=l ? i+line_len : l;
	    nice.append(prot.substring(i,j));
	    nice.append("\n");
	    //	    StringBuffer b=new StringBuffer(prot.substring(i,j));
	    //	    System.out.println(b);
	    i+=line_len;
	}
	return new String(nice).trim();
    }

}