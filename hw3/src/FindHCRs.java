import java.io.*;
import java.util.*;

/*
  This program finds areas of highly conserved alignments from phyloP scores.
  It writes the output to a file named "hcr.txt", and also to stdout.
*/

class HW3 {
    public static void main(String[] argv) {
	Date start_time=new Date();
	try {
	    String[] human_chrs={"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
	    //String[] human_chrs={"11"};	// debugging aid

	    //Construct the BufferedWriter object
	    String filename="hcr.txt"; // output file
	    BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

	    for (int i=0; i<human_chrs.length; i++) {
		String phylofile=String.format("/projects/instr/11wi/cse427/phylop/chr%s.phyloP46way.wigFix",human_chrs[i]);
		double c=4.8;
		PhyloParser pp=new PhyloParser(phylofile,c);
		PhyloBlock b;
		int n_skipped=0;
		int fuse=-3;
		while ((b=pp.nextBlock())!=null) {
		    if (b.max_score < c) {
			n_skipped++;
			continue;
		    }

		    b.rFromQ();
		    b.XFromR();
		    b.YFromR();
		    // Do the merge, return if no hcc's found:
		    ArrayList<Interval> hcc_al=b.mergeXY(); // hcc for "highly conserved candidates"
		    if (hcc_al.size()==0) continue;

		    // For overlapping hcc's, select the longest
		    Interval[] hcc=hcc_al.toArray(new Interval[hcc_al.size()]);
		    Interval longest=hcc[0];
		    ArrayList<Interval> final_hccs=new ArrayList();
		    boolean add_last=false;
		    for (int j=1; j<hcc.length; j++) {
			Interval it=hcc[j];
			if (it.overlaps(longest)) {
			    if (it.length() > longest.length()) longest=it;
			    add_last=true;
			} else {
			    final_hccs.add(longest);
			    longest=it;
			    add_last=false;
			}
		    }
		    if (add_last) { final_hccs.add(hcc[hcc.length-1]); }
		    
		    // Print out intervals
		    for (int j=0; j<final_hccs.size(); j++) {
			System.out.println(String.format("block=%s: longest hcc=%s len=%d",
							 b.headerString(), final_hccs.get(j), final_hccs.get(j).length()));
			writer.write(String.format("block=%s: longest hcc=%s len=%d\n",
						   b.headerString(), final_hccs.get(j), final_hccs.get(j).length()));
		    }

		    if (fuse-- == 0) break;
		}	    
	    }
	    writer.flush();
	    writer.close();
	    System.err.println(filename+" written");

	} catch (IOException ioe ) {
	    new Die(ioe);
	}

	Date end_time=new Date();
	System.err.println(String.format("execution time: %s",new TimeSpan(start_time,end_time)));
    }
}