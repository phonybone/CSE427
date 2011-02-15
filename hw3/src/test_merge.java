import java.util.*;

class test_merge {
    public static void main(String[] argv) {
	double[] X={0.0,  -.8,  -.8,  -.8,  -.8, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.2, -1.2, -1.2, -1.2, -1.4, -1.4, -1.4, -1.8, -1.8};  
	double[] Y={0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, -.4, -.4, -.4, -.6, -.6, -.6, -.6, -1.0, -1.0, -1.0, -1.6, -1.6};
	assert X.length==Y.length;
	
	ArrayList<Interval> m=mergeXY(X,Y);
	System.out.println("All intervals");
	for (int i=0; i<m.size(); i++) {
	    System.out.println(m.get(i).toString());
	}

	// assume m is sorted; combine overlapping intervals
	Interval[] ma=m.toArray(new Interval[m.size()]);
	Interval current=ma[0];
	ArrayList<Interval> merged=new ArrayList<Interval>();
	for (int i=1; i<ma.length; i++) {
	    if (ma[i].overlaps(current)) {
		current=current.join(ma[i]);
	    } else {
		merged.add(current);
		current=ma[i];
	    }
	}
	if (!current.ne(ma[ma.length-1])) {
	    merged.add(current);
	}
	System.out.println("Merged");
	for (int i=0; i<merged.size(); i++) {
	    System.out.println(merged.get(i).toString());
	}

    }

    public static ArrayList<Interval> mergeXY(double[] X, double [] Y) {
	int xi=0;
	int yj=0;
	boolean in_y=Y[0]<=X[0];
	ArrayList<Interval> iList=new ArrayList<Interval>();

	while (xi<X.length && yj<Y.length) {
	    // char p=Y[yj]>X[xi]? '<':'>';
	    // System.out.println(String.format("X[%d]=%5.2f %c Y[%d]=%5.2f",xi, X[xi], p, yj,Y[yj]));

	    if (Y[yj]>=X[xi]) {
		yj++;
		in_y=true;
	    } else {
		xi++;
		if (in_y) {
		    Interval i=new Interval(xi,yj-1);
		    iList.add(i);
		}
		in_y=false;
	    }
	}
	System.out.println(String.format("end: xi=%d, yj=%d, in_y=%s",xi,yj,(in_y? "true":"false")));
	if (xi<X.length) {
	    Interval i=new Interval(xi+1,yj-1); // I *think* these indices are right
	    iList.add(i);
	    System.out.println("added final "+i.toString());
	}

	return iList;
    }
}