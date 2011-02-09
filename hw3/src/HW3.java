import java.util.*;

class HW3 {
    public static void main(String[] argv) {
	String phylofile="/projects/instr/11wi/cse427/phylop/chr1.phyloP46way.wigFix";
	double c=4.8;
	PhyloParser pp=new PhyloParser(phylofile,c);
	PhyloBlock b=pp.nextBlock();
	System.err.println(b.toString());
    }
}