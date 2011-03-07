class ViterbiResult implements Comparable {
    public String name;
    public String seq;
    public double score;

    public ViterbiResult(String name, String seq, double score) {
	this.name=name;
	this.seq=seq;
	this.score=score;
    }

    public String toString() {
	return String.format("%s: %g %s", name, score, seq);
    }

    public String header() {
	return String.format("%s: %g", name, score);
    }

    public int compareTo(Object o) {
	ViterbiResult other=(ViterbiResult)o;
	return this.score == other.score? 0 : (this.score > other.score? -1:1);
    }
}