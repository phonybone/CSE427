import java.util.*;

class HW3 {
    public static void main(String[] argv) {
	// String[] human_chrs={"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
	String[] human_chrs={"11"};	// fixme; debugging aid
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
		Interval[] hcc=hcc_al.toArray(new Interval[hcc_al.size()]);
		if (hcc.length==0) continue;
		System.out.println(String.format("%s: Interval list (unmerged) (len=%d)",b.headerString(), hcc.length));
		for (int j=0; j<hcc.length; j++) {
		    System.out.println(String.format("%s (%d)",hcc[j].toString(),hcc[j].length()));
		}
		if (fuse-- == 0) break;
	    }
	    System.out.println(String.format("chr%s: %d skipped blocks\n", human_chrs[i], n_skipped));
	}
    }
}