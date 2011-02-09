import java.util.*;

class PhyloBlock {
    public String chrom;
    public int start;
    public int stop;
    public int length;
    public double[] q;	// really q-c, but PhyloParser takes care of that
    public double[] r;
    public double[] X;
    public double[] Y;

    public PhyloBlock(String chrom, int start) {
	this.chrom=chrom;
	this.start=start;
	stop=0;
	length=0;
	q=null;
	r=null;
	X=null;
	Y=null;
    }

    public void setQ(ArrayList<Double> Dq) {
	this.length=q.length;
	this.stop=start+length-1; // start and stop are the same when length==1

	q=new double[Dq.size()];
	for (int i=0; i<q.length; i++) { q[i]=Dq.get(i).doubleValue();}
	// there has GOT to be a better way of doing this

	rFromQ();
	XFromR();
	YFromR();
    }

    public String toString(int width) {
	StringBuffer s=new StringBuffer();
	s.append(String.format("%s: %d-%d (%d)\n", chrom, start, stop, length));

	int i=0;

	while (i < q.length) {
	    if (q!=null) {
		s.append("q: ");
		for (int j=i; j<i+width && j<length; j++) { s.append(String.format(" %6.3f",q[i])); }
		s.append("\n");
	    }
	    if (r!=null) {
		s.append("r: ");
		for (int j=i; j<i+width && j<length; j++) { s.append(String.format(" %6.3f",r[i])); }
		s.append("\n");
	    }

	    if (X!=null) {
		s.append("x: ");
		for (int j=i; j<i+width && j<length; j++) { s.append(String.format(" %6.3f",X[i])); }
		s.append("\n");
	    }
	    if (Y!=null) {
		s.append("y: ");
		for (int j=i; j<i+width && j<length; j++) { s.append(String.format(" %6.3f",Y[i])); }
		s.append("\n\n");
	    }
	    i+=width;
	}
	return new String(s);
    }
    public String toString() {	return toString(30);    }

    public void rFromQ() {
	this.r=new double[length];
	r[0]=q[0];
	for (int i=1; i<length; i++) {
	    r[i]=r[i-1]+q[i];
	}
    }

    // X[i] = min(r0, r1, ..., ri) = min(X[i-1],r[i]) (since R is non-increasing)
    public void XFromR() {
	this.X=new double[length];
	X[0]=r[0];
	for (int i=1; i<length; i++) {
	    X[i]=Math.min(X[i-1],r[i]);
	}
    } 
    public void YFromR() {
	this.Y=new double[length];
	Y[length-1]=r[length-1];
	for (int i=length-2; i>=0; i--) {
	    Y[i]=Math.max(Y[i+1],r[i]);
	}
    } 

    //    public ArrayList<Interval> mergeXY() {
    //    }

}