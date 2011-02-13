public class ChromSeq {
    public String org;
    public String chrom;
    public String seq;
    public int seq_start;
    public int seq_end;

    public ChromSeq(String org, String chrom, String seq, int seq_start, int seq_end) {
	this.org=org;
	this.chrom=chrom;
	this.seq=seq;
	this.seq_start=seq_start;
	this.seq_end=seq_end;
    }

    public String toString() {
	return String.format("%10s %5s %10d %s %10d",org, chrom, seq_start, seq, seq_end);
    }
}