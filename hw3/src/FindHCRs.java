import java.io.*;
import java.util.*;

/*
  This program finds areas of highly conserved alignments from phyloP scores.
  It writes the output to a file named "hcrs.ser", and also to stdout.
*/

class FindHCRs {
    // public static String[] human_chrs={"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
    public static String[] human_chrs={"11"};	// debugging aid
    public static HashMap chr2HCRs=new HashMap();


    public static void main(String[] argv) {
	Date start_time=new Date();

	findHCRs();
	findMultiZBlocks();
	writeMultiZBlocks();
	Date end_time=new Date();
	System.err.println(String.format("execution time: %s",new TimeSpan(start_time,end_time)));
    }

    public static findHCRs() {
	try {
	    for (int i=0; i<human_chrs.length; i++) {
		String chrom="chr"+human_chrs[i];
		String phylofile=String.format("/projects/instr/11wi/cse427/phylop/%s.phyloP46way.wigFix",chrom);
		double c=4.8;
		PhyloParser pp=new PhyloParser(phylofile,c);
		PhyloBlock b;
		int n_skipped=0;
		int fuse=-3;
		ArrayList<HCR> final_hcrs=new ArrayList();
		while ((b=pp.nextBlock())!=null) {
		    if (b.max_score < c) {
			n_skipped++;
			continue;
		    }

		    b.rFromQ();
		    b.XFromR();
		    b.YFromR();
		    // Do the merge, return if no hcr's found:
		    ArrayList<Interval> hcr_al=b.mergeXY(); // hcr for "highly conserved candidates"
		    if (hcr_al.size()==0) continue;

		    // For overlapping hcr's, select the longest
		    Interval[] hcr_ints=hcr_al.toArray(new Interval[hcr_al.size()]);
		    Interval longest=hcr_ints[0];
		    boolean add_last=false;

		    for (int j=1; j<hcr_ints.length; j++) {
			Interval it=hcr_ints[j];
			if (it.overlaps(longest)) {
			    if (it.length() > longest.length()) longest=it;
			    add_last=true;
			} else {
			    HCR hcr=new HCR(chrom, longest, b.phyloPOver(longest,c), c);
			    final_hcrs.add(hcr);
			    longest=it;
			    add_last=false;
			    System.out.println("found "+hcr.toString());
			}
		    }
		    if (add_last) { 
			HCR hcr=new HCR(chrom, longest, b.phyloPOver(longest,c), c);
			final_hcrs.add(hcr);
			System.out.println("found "+hcr.toString());
		    }		    

		    if (fuse-- == 0) break;
		}
		
		// Build a HashMap of all our HCRs: k=chrom, v=HCR[]
		HCR[] HCRs=final_hcrs.toArray(new HCR[final_hcrs.size()]);
		chr2HCRs.put(chrom,HCRs);
		final_hcrs.clear();
		System.out.println(String.format("Added %d HCRs to %s",HCRs.length,chrom));
	    }

	    // Serialize chr2HCRs:
	    String object_file="hcrs.ser";
	    ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(object_file));
	    oos.writeObject(chr2HCRs);
	    oos.close();
	    System.out.println(object_file+" written");

	} catch (IOException ioe ) {
	    new Die(ioe);
	}
    }




    public static findMultiZBlocks() {
	// uses chr2HCRs as "input"
	    String multiz_file=String.format("/projects/instr/11wi/cse427/multiz/%s.maf",chrom);
	    MultiZParser parser=new MultiZParser(multiz_file);
	    

    }


    // Add MultiZBlocks to 
    public static void findZBlocks() {
	for (int c=0; c<human_chrs.length; c++) {
	    String chrom="chr"+human_chrs[c];
	    String multiZfile=String.format("/projects/instr/11wi/cse427/multiz/%s.maf",chrom);
	    MultiZParser parser=new MultiZParser(multiZfile);
	    MultiZBlock zBlock=null;

	    HCR[] hcrs=(HCR[])chr2HCRs.get(chrom);

	    while ((zBlock=parser.next())!=null) {
		ChromSeq cs=zBlock.get("hg19"); 
		if (cs==null) continue;
		
		for (int j=0; j<hcrs.length; j++) {
		    if (cs.interval.overlaps(hcrs[j].interval)) {
			hcrs[j].zBlocks.add(zBlock);
			System.out.println();
		    }
		}
	    }
	}
    }

}