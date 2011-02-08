import java.util.*;

class PhyloBlock {
    public String chrom;
    public int start;
    public int stop;
    public ArrayList<Double> r;	// not sure we need r
    public ArrayList<Double> q;
    public ArrayList<Double> X;
    public ArrayList<Double> Y;

    public PhyloBlock(String chrom, int start, int stop) {
	this.chrom=chrom;
	this.start=start;
	this.stop=stop;
    }

    public int length() { return stop-start+1; }

    public void qFromR() {
	
    }


}