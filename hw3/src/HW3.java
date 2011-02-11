import java.util.*;

class HW3 {
    public static void main(String[] argv) {
	//	String phylofile="/projects/instr/11wi/cse427/phylop/chr11.phyloP46way.wigFix";
	String phylofile="chr1.phyloP46way.wigFix.100K";
	double c=4.8;
	int min_nc=100;
	PhyloParser pp=new PhyloParser(phylofile,c);
	PhyloBlock b;
	int n_skipped=0;
	while ((b=pp.nextBlock())!=null) {
	    System.err.println(b.headerString());
	    if (b.n_c<min_nc) {
		n_skipped++;
		continue;
	    }
	    b.rFromQ();
	    b.XFromR();
	    b.YFromR();

	}
	System.err.println(String.format("%d skipped blocks\n",n_skipped));
    }
}