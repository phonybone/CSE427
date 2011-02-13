import java.io.*;
import java.util.*;

class HW3 {
    public static void main(String[] argv) {
	Date start_time=new Date();
	
	try {
	    String[] human_chrs={"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
	    // String[] human_chrs={"11"};	// fixme; debugging aid

	    //Construct the BufferedWriter object
	    String filename="hcr.txt";
	    BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

	    for (int i=0; i<human_chrs.length; i++) {
		String phylofile=String.format("/projects/instr/11wi/cse427/phylop/chr%s.phyloP46way.wigFix",human_chrs[i]);
		//	String phylofile="/projects/instr/11wi/cse427/phylop/chr11.phyloP46way.wigFix";
		//	String phylofile="chr1.phyloP46way.wigFix.100K";
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


		    ArrayList<Interval> hcc_al=b.mergeXY(); // hcc for "highly conserved candidates"
		    if (hcc_al.size()==0) continue;		// shouldn't happen, but just in case

		    Interval[] hcc=hcc_al.toArray(new Interval[hcc_al.size()]);
		    Interval longest=hcc[0];
		    for (int j=1; j<hcc.length; j++) {
			if (hcc[j].length() > longest.length()) longest=hcc[j];
		    }
		    longest=longest.shiftedBy(b.start);
		    System.out.println(String.format("block=%s: longest hcc=%s len=%d",
						     b.headerString(), longest, longest.length()));
		    writer.write(String.format("block=%s: longest hcc=%s len=%d\n",
					       b.headerString(), longest, longest.length()));

		    if (fuse-- == 0) break;
		    continue;
		    // Everything below here is playing around with merging interval lists:

		    /*
		      if (hcc.length==0) continue;
		      System.out.println(String.format("%s: Interval list (unmerged) (len=%d)",b.headerString(), hcc.length));
		      for (int j=0; j<hcc.length; j++) {
		      System.out.println(String.format("%s (%d)",hcc[j].toString(),hcc[j].length()));
		      }

		      Interval[] merged=Interval.merge_overlaps(hcc);
		      System.out.println(String.format("%s: Interval list (merged) (len=%d)",b.headerString(), merged.length));
		      for (int j=0; j<merged.length; j++) {
		      System.out.println(String.format("m: %s (%d)",merged[j].toString(),merged[j].length()));
		      }
		      System.out.println("");
		    */
		}
	    
		System.out.println(String.format("chr%s: %d skipped blocks\n", human_chrs[i], n_skipped));
	    }
	    writer.flush();
	    writer.close();
	} catch (IOException ioe ) {
	    new Die(ioe);
	}

	Date end_time=new Date();
	System.err.println(String.format("execution time: %s",new TimeSpan(start_time,end_time)));
    }
}