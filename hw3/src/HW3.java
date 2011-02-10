import java.util.*;

class HW3 {
    public static void main(String[] argv) {
	String phylofile="/projects/instr/11wi/cse427/phylop/chr11.phyloP46way.wigFix";
	double c=4.8;
	PhyloParser pp=new PhyloParser(phylofile,c);
	PhyloBlock b;
	int n_skipped=0;
	while ((b=pp.nextBlock())!=null) {
	    if (b.max_score<c) {
		n_skipped++;
		continue;
	    }
	    System.err.println(b.headerString());
	}
	System.err.println(String.format("%d skipped blocks\n",n_skipped));
    }
}