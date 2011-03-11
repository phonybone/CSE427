class ViterbiResult implements Comparable {
    public String name;
    public String seq;
    public double score;
    public String path;
    public String alignment;

    public ViterbiResult(String name, String seq, double score, String path, String alignment) {
	this.name=name;
	this.seq=seq;
	this.score=score;
	this.path=path;
	this.alignment=alignment;
   }

    public String toString() {
	return String.format("%s: %g %s", name, score, seq);
    }

    public String header() {
	return String.format("%s: %g", name, score);
    }

    public String dump() {
	StringBuffer buf=new StringBuffer();
	buf.append(String.format("name: %s\n",      name));
	buf.append(String.format("seq: %s\n",       seq));
	buf.append(String.format("score: %g\n",     score));
	buf.append(String.format("path: %s\n",      path));
	buf.append(String.format("alignment: %s\n", alignment));
	return new String(buf);
    }

    public int compareTo(Object o) {
	ViterbiResult other=(ViterbiResult)o;
	return this.score == other.score? 0 : (this.score > other.score? -1:1);
    }
}