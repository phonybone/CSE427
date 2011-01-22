import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ArrayIndexOutOfBoundsException;

class HW2 {
    public static void main(String argv[]) {
	// read query from file:
	String query_file;
	try {
	    query_file=argv[0];
	} catch (ArrayIndexOutOfBoundsException e) {
	    query_file="query.txt";
	}
	String query=read_query(query_file);
	System.err.println("query read: "+query_file+": len="+String.valueOf(query.length()));

	
	//	String subject="AIGVH"; // taken from the beginning of query.txt
	String subject="ADIGVPH"; // taken from the beginning of query.txt
	LocalAlignment la=new LocalAlignment();
	int score=la.score(query,subject);
	System.out.println("score: "+String.valueOf(score));
	System.out.println(la.traceback());
	
    }

    public  String read_query(String query_file) {
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
