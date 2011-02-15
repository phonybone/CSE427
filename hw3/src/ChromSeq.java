import java.io.Serializable;

public class ChromSeq implements Serializable, Comparable {
    public String org;
    public String chrom;
    public String strand;
    public String seq;
    public Interval interval;

    static final long serialVersionUID = 1003L;

    public ChromSeq() {}	// for serializable
    public ChromSeq(String org, String chrom, String seq, int seq_start) {
	this.org=org;
	this.chrom=chrom;
	this.seq=seq;
	this.interval=new Interval(seq_start,seq_start+seq.length()-1);
    }

    public int length() { return interval.length(); }

    public String toString() {
	return String.format("%10s %8s %s %10d %s",org, chrom, strand, length(), interval.toString() );
    }

    public int compareTo(Object o) {
	return interval.compareTo(o);
    }
}