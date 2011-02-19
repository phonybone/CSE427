class GenomeFeature implements Comparable {
    public int i_chrom;
    public Interval interval;

    public GenomeFeature() {}
    public GenomeFeature(int i_chrom, Interval interval) {
	this.i_chrom=i_chrom;
	this.interval=interval;
    }

    public int compareTo(Object o)  {
	GenomeFeature other=(GenomeFeature)o;
	if (this.i_chrom>other.i_chrom) return 1;
	if (this.i_chrom<other.i_chrom) return -1;
	return this.interval.compareTo(other.interval);
    }

    public int length() { return interval.length(); }
}