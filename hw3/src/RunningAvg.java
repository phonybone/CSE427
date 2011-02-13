import java.io.*;
import java.util.*;
import java.lang.ArrayIndexOutOfBoundsException;

public class RunningAvg {
    public static void main(String[] argv) {
	String seq_file="";
	try {
	    seq_file=argv[0];
	} catch (ArrayIndexOutOfBoundsException e) {
	    String usage="usage: java RunningAvg <seq_file>";
	    System.err.println(usage);
	    System.exit(1);
	}

	try {
	    FileInputStream fis=new FileInputStream(seq_file);
	    BufferedReader reader=new BufferedReader(new InputStreamReader(fis));
	    String line;
	    double sum=0;
	    int window=10;
	    double thresh=50;
	    LinkedList<Double> fifo=new LinkedList<Double>();

	    for (int i=0; i<window; i++) {
		line=reader.readLine();
		if (line==null) break;
		Double v=Double.valueOf(line.trim());
		fifo.add(v);
		sum+=v;
	    }
	    double running_avg=sum/window;
	    System.out.printf("starting avg: %5.3f\n", running_avg);

	    boolean in_seq=running_avg>thresh;
	    int start =in_seq? 0:window;
	    int stop=window;
	    
	    while ((line=reader.readLine()) != null) {
		double v=Double.valueOf(line.trim());
		stop++;

		fifo.add(v);
		sum+=(v-fifo.remove());
		running_avg=sum/window;
		if (in_seq && running_avg<thresh) { // end seq:
		    in_seq=false;
		    System.out.printf("seq: %d-%d\n", start,stop);
		} else if (!in_seq && running_avg>thresh) {
		    in_seq=true;
		    start=stop;
		}

	    }
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	    System.exit(1);
	}
    }
}
