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
    public double max_score;		// number of phyloP scores that are higher than c
    public int min_interval = 100;
    

    public PhyloBlock(String chrom, int start) {
	this.chrom=chrom;
	this.start=start;
	stop=0;
	length=0;
	max_score=-10000;	// whatever; something large and negative
	q=null;
	r=null;
	X=null;
	Y=null;
    }

    public void setQ(ArrayList<Double> Dq) {
	this.length=Dq.size();
	this.stop=start+length-1; // start and stop are the same when length==1
	//System.out.println(String.format("seqQ: start=%d, length=%d, stop=%d",this.start, this.length, this.stop));
	q=new double[Dq.size()];
	for (int i=0; i<q.length; i++) { q[i]=Dq.get(i).doubleValue();}
	// there has GOT to be a better way of doing this
	// Could use ArrayList.toArray(Double[]), but would still have to 
	// convert Double to double for each element.
    }


    public String headerString() {
	return String.format("%s: %d-%d (l=%d) max_score=%g", chrom, start, stop, length, max_score);
    }


    public String toString(int width) {
	StringBuffer s=new StringBuffer();
	s.append(this.headerString());

	int i=0;

	while (i < q.length) {
	    if (q!=null) {
		s.append("q: ");
		for (int j=i; j<i+width && j<length; j++) { s.append(String.format(" %6.3f",q[j])); }
		s.append("\n");
	    }
	    if (r!=null) {
		s.append("r: ");
		for (int j=i; j<i+width && j<length; j++) { s.append(String.format(" %6.3f",r[j])); }
		s.append("\n");
	    }

	    if (X!=null) {
		s.append("x: ");
		for (int j=i; j<i+width && j<length; j++) { s.append(String.format(" %6.3f",X[j])); }
		s.append("\n");
	    }
	    if (Y!=null) {
		s.append("y: ");
		for (int j=i; j<i+width && j<length; j++) { s.append(String.format(" %6.3f",Y[j])); }
		s.append("\n\n");
	    }
	    i+=width;
	}
	return new String(s);
    }
    public String toString() {	return toString(20);    }

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

    public ArrayList<Interval> mergeXY() {
	int xi=0;
	int yj=0;
	boolean in_y=Y[0]<=X[0];
	ArrayList<Interval> iList=new ArrayList<Interval>();

	while (xi<X.length && yj<Y.length) {
	    // char p=Y[yj]>X[xi]? '<':'>';
	    //	    System.out.println(String.format("X[%d]=%5.2f %c Y[%d]=%5.2f",xi, X[xi], p, yj,Y[yj]));

	    if (Y[yj]>=X[xi]) {
		yj++;	
		in_y=true;
	    } else {
		xi++;
		if (in_y && (yj-xi > min_interval)) {
		    Interval i=new Interval(xi,yj-1).shiftedBy(this.start);
		    iList.add(i);
		}
		in_y=false;
	    }
	}


	if ((xi<X.length) && (yj-xi > min_interval)) {
	    Interval i=new Interval(xi+1,yj-1).shiftedBy(this.start); // I *think* these indices are right
	    iList.add(i);
	    System.out.println("added final "+i.toString());
	}

	return iList;
    }

    // Return the average phyloP score from a portion of the block specified
    // by the interval.  Get the phyloP scores from Q, but it's necessary to
    // re-add in the value of c.  
    public double avg_phylop(Interval i, double c) {
	double sum=0;
	for (int j=i.start; j<=i.stop; j++) {
	    sum+=q[j-start]+c;
	}
	return sum/i.length();
    }

    double[] phyloPOver(Interval i, double c) {
	double[] p = new double[i.length()];
	Interval i2=i.shiftedBy(-this.start);
	for (int j=i2.start; j<=i2.stop; j++) {
	    p[j-i2.start]=q[j]+c;
	}
	return p;
    }
}