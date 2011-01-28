import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ArrayIndexOutOfBoundsException;

class HW2 {
    public static void main(String argv[]) {
	// read query from file, and get list of subject protein sequences:
	String query_file=null;
	String prots_file=null;
	try {
	    prots_file=argv[0];
	    query_file=argv[1];
	} catch (ArrayIndexOutOfBoundsException e) {
	    if (prots_file==null) prots_file="NC_011660.faa";
	    if (query_file==null) query_file="query.txt";
	}
	String query=read_query(query_file);
	System.err.println("query read: "+query_file+": len="+String.valueOf(query.length()));
	System.err.println("subject file: "+prots_file);

	// Iterate through all proteins:
	ProtStream prots=new ProtStream(prots_file);
	String subject;
	int best_score=0;
	String best_prot=null;

	LocalAlignment best_alignment=null;
       	int fuse=-1;		// debugging aid
	while ((subject=prots.next())!=null) {
	    LocalAlignment la=new LocalAlignment();
	    int score=la.score(query,subject);
	    //	    System.out.println(prots.prot_name()+": score: "+String.valueOf(score));
	    if (score>best_score) {
		best_score=score;
		best_alignment=la;
		best_prot=prots.prot_name();
	    }
	    if (--fuse==0) break;
	}

	// Print the winner:
	System.out.println("Best Alignment: score="+String.valueOf(best_score));
	System.out.println(best_prot); // actually is name of protein
	System.out.println("alignment:\n"+best_alignment.traceback());
	System.exit(0);		// and we're done.
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
}
