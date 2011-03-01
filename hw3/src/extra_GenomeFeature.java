class GenomeFeature implements Comparable {
    public int i_chrom;
    public Interval interval;

    public GenomeFeature() {}
    public GenomeFeature(String chrom, Interval interval) {
	String chr_n=chrom.substring(3);
	int i_chrom;
	if (chrom.length()>5)	{ // some weird chromosome notations in knownGenes.txt
	    this.i_chrom=25;
	} else {
	    try {
		this.i_chrom=Integer.valueOf(chr_n).intValue();
	    } catch (NumberFormatException e) {
		if (chr_n.equals("X")) { this.i_chrom=23; }
		else if (chr_n.equals("Y")) { this.i_chrom=24; }
		else { this.i_chrom=25; } // no idea
	    }
	}

	this.interval=interval;
    }

    public String toString() {
	return String.format("GF: chrom #%d %s", i_chrom, interval);
    }


    public static boolean verbose=false;
    public int compareTo(Object o)  {
	GenomeFeature other=(GenomeFeature)o;
	int answer;
	if (this.i_chrom>other.i_chrom)      { answer=2; }
	else if (this.i_chrom<other.i_chrom) { answer=-2; }
	else                                 { answer=this.interval.compareTo(other.interval); }

	if (verbose) System.err.println(String.format("this: %s\nother %s\nreturning %d", this, other, answer));
	if (answer!=0) {
	    int abs_a=answer<0? -answer:answer; 
	    answer=answer/abs_a;
	}
	if (verbose) System.err.println(String.format("answer now %d\n",answer));
	return answer;
    }

    public int length() { return interval.length(); }
    public int distanceTo(GenomeFeature other) { return this.interval.distanceTo(other.interval); }
}