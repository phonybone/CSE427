/* 
   This class represents a HighlyConservedRegion, measured in human chromosome.
 */
import java.io.*;
import java.util.*;


class HCR implements Serializable {
    public String chrom;		// human
    public Interval interval;		// also human
    public double[] phyloP;		// list of phyloP scores across interval
    public double c;			// what value of c was used to find this HCR
    public ArrayList<MultiZBlock> zBlocks;

    static final long serialVersionUID = 1001L;
    public HCR() {}	// for serializable


    public HCR(String chrom, Interval i, double[] phyloP, double c) {
	this.chrom=chrom;
	this.interval=i;
	this.phyloP=phyloP;
	this.c=c;
	this.zBlocks=new ArrayList<MultiZBlock>();
    }
    
    public String toString() {
	return String.format("%s %d %d %d %g",chrom,interval.start, interval.stop,length(),avg_phyloP());
    }

    // Format for the homework:
    public String report() {
	return String.format("%s:%s\n%d\n%g\n",chrom,interval.toString(),length(), avg_phyloP());
    }

    public int length() { return interval.length(); }
    public double avg_phyloP() {
	double sum=0;
	for (int i=0; i<phyloP.length; i++) { sum+=phyloP[i]; }
	return sum/interval.length();
    }

    // Return a String s such that s[i]=c if phyloP[i]>c, s[i]=nc otherwise
    public String plusString(char c, char nc) { // could probably come up with a better name for that
	int len=length();
	StringBuffer buf=new StringBuffer(len);
	for (int i=0; i<len; i++) {
	    buf.setCharAt(i, phyloP[i]>c? c:nc);
	}
	return new String(buf);
    }
}