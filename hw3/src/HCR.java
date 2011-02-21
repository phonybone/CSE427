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
	return String.format("(%s) %s %d %d %d %g",this.getClass().getName(),chrom,interval.start, interval.stop,length(),avg_phyloP());
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


    public int length() { return interval.length(); }
    public double avg_phyloP(int start, int stop) {
	double sum=0;
	for (int i=start; i<=stop; i++) { sum+=phyloP[i]; }
	return sum/(stop-start+1);
    }
    public double avg_phyloP(Interval i) { return avg_phyloP(i.start,i.stop);}
    public double avg_phyloP() { return avg_phyloP(0,length()-1); }

    // Return a String s such that s[i]=c if phyloP[i]>c, s[i]=nc otherwise
    public String plusString(String seq, int offset) {
	// offset refers to where to start within phyloP
	StringBuffer buf=new StringBuffer();
	char[] chars=new char[seq.length()];
	seq.getChars(0,seq.length(),chars,0);

	int j=0;
	for (int i=0; i<chars.length; i++) {
	    buf.append(chars[i]=='-'? ' ' : phyloP[j++]>c? '+':' ');
	}
	return new String(buf);
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
	buf.append(String.format("%s:%s\n",chrom,interval));
	buf.append(String.format("%d\n",interval.length()));
	buf.append(String.format("%6.4f\n\n", avg_phyloP()));

	// Sort the zBlocks:
	MultiZBlock[] zbs=new MultiZBlock[zBlocks.size()];
	zbs=(MultiZBlock[])zBlocks.toArray(zbs);
	Arrays.sort(zbs);
	
	//	System.err.println(String.format("hcr: %s",interval.fullString()));
	for (int i=0; i<zbs.length; i++) {
	    MultiZBlock zb=zbs[i];
	    //	    System.err.println(String.format("\nHCR:alignment: zb=%s intersection=%s",
	    //zb.human_interval.fullString(),
	    //zb.human_interval.intersection(interval).fullString()));
	    
	    buf.append(zb.alignment(this));
	    buf.append("\n");
	}
	buf.append("\n");
	
	return new String(buf);
    }
}