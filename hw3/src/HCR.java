/* 
   This class represents a HighlyConservedRegion, "measured" in human chromosome.
 */
import java.io.*;
import java.util.*;


class HCR implements Serializable, Comparable {
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

    public String fullString() {
	StringBuffer buf=new StringBuffer();
	buf.append(toString());
	buf.append("\n");
	for (int i=0; i<length(); i++) {
	    buf.append(String.valueOf(phyloP[i]));
	    buf.append(" ");
	}
	buf.append("\n");
	return new String(buf);
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
    public double avg_phyloP(int start, int stop) {
	double sum=0;
	for (int i=start; i<=stop; i++) { sum+=phyloP[i]; }
	return sum/(stop-start+1);
    }
    public double avg_phyloP(Interval i) {
	return avg_phyloP(i.start,i.stop);
    }

    // Return a String s such that s[i]=c if phyloP[i]>c, s[i]=nc otherwise
    public String plusString(char c, char nc, int start, int stop) { // could probably come up with a better name for that
	int len=stop-start+1;
	StringBuffer buf=new StringBuffer(len);
	for (int i=0; i<len; i++) {
	    buf.setCharAt(i+start, phyloP[i]>c? c:nc);
	}
	return new String(buf);
    }
    public String plusString(Interval i) {
	return plusString('+', ' ', i.start, i.stop);
    }


    public int compareTo(Object o) throws ClassCastException, RuntimeException {
	HCR other=(HCR)o;
	if (this.chrom==null || other.chrom==null) throw new RuntimeException("missing chrom(s)");
	if (this.interval==null || other.interval==null) throw new RuntimeException("missing interval(s)");

	int i=this.chrom.compareTo(other.chrom);
	if (i!=0) return i;
	return this.interval.compareTo(other.interval);
    }

    public String alignment() {
	StringBuffer buf=new StringBuffer();

	// Sort the zBlocks:
	MultiZBlock[] zbs=new MultiZBlock[zBlocks.size()];
	zbs=(MultiZBlock[])zBlocks.toArray(zbs);
	
	for (int i=0; i<zbs.length; i++) {
	    MultiZBlock zb=zbs[i];
	    buf.append(zb.alignment(this));
	    buf.append("\n");
	}

	
	return new String(buf);
    }
}