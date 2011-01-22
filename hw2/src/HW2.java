import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ArrayIndexOutOfBoundsException;

class HW2 {
    public static void main(String argv[]) {
	// read query from file:
	String query_file=null;
	String prots_file=null;
	try {
	    query_file=argv[0];
	    prots_file=argv[1];
	} catch (ArrayIndexOutOfBoundsException e) {
	    if (query_file==null) query_file="query.txt";
	    if (prots_file==null) prots_file="NC_011660.faa";
	}
	String query=read_query(query_file);
	System.err.println("query read: "+query_file+": len="+String.valueOf(query.length()));


	// Test the traceback algorithm and exit:
	// test_traceback(query);

	// Iterate through all proteins:
	ProtStream prots=new ProtStream(prots_file);
	String subject;
	int best_score=0;
	String best_prot=null;

	LocalAlignment best_alignment=null;
	int fuse=-1;
	String winner=">gi|217964381|ref|YP_002350059.1| chaperone protein DnaK [Listeria monocytogenes HCC23]";
	while ((subject=prots.next())!=null) {
	    if (! prots.prot_name().equals(winner)) continue;

	    LocalAlignment la=new LocalAlignment();
	    int score=la.score(query,subject);
	    System.out.println(prots.prot_name()+": score: "+String.valueOf(score));
	    if (score>best_score) {
		best_score=score;
		best_alignment=la;
		best_prot=prots.prot_name();

		
	    }
	    if (--fuse==0) break;
	}

	System.out.println("Best Alignment: score="+String.valueOf(best_score));
	System.out.println(best_prot); // actually is name of protein
	System.out.println("alignment:\n"+best_alignment.traceback());

	// Print out traceback of best alignment:
    }

    public static String read_query(String query_file) {
	StringBuffer query=new StringBuffer();
	try {
	    FileInputStream fis=new FileInputStream(query_file);
	    BufferedReader reader=new BufferedReader(new InputStreamReader(fis));
	    String line;
	    while ((line=reader.readLine()) != null) {
		query.append(line.trim());
	    }
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    System.exit(1);
	}

	return query.toString();
    }
    

    // Query and subject are reversed in this instance
    public static void test_traceback(String query) {
	//	String subject="ADIGVPH"; // taken from the beginning of query.txt
	String subject="VLRLIHEPSAQCALFBLAYGIGQDSPTGKSNILVFKGTSLSLSVMSFNSGIYVLSTNTDDNIGGAHFTETLAQYLASERSFKHDRGNARAMMK";
	LocalAlignment la=new LocalAlignment();
	int score=la.score(subject,query); // reverse order; not that it should really matter
	System.out.println("Query: "+query);
	System.out.println("Subject: "+subject);
	System.out.println("score: "+String.valueOf(score));
	System.out.println("alignment:\n"+la.traceback());
	System.exit(1);
    }
}
