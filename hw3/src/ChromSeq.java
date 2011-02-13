public class ChromSeq {
    public String org;
    public String chrom;
    public String strand;
    public String seq;
    public int seq_start;
    public int seq_end;
    public int length;

    public ChromSeq(String org, String chrom, String seq, int seq_start) {
	this.org=org;
	this.chrom=chrom;
	this.seq=seq;
	this.seq_start=seq_start;
	this.length=seq.length();
	this.seq_end=seq_start+length-1;
    }

    // fixme: still have to break this up into screen-size pieces
    public String toString() {
	return String.format("%10s %25s %10d %s %10d",org, chrom, seq_start, seq, seq_end);
    }
}