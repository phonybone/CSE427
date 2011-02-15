import java.io.*;
import java.util.*;

class Interval implements Serializable, Comparable {
    public int start;
    public int stop;

    static final long serialVersionUID = 1004L;

    public Interval(int start, int stop) {
	this.start=start;
	this.stop=stop;
    }
    public Interval() {}	// for serializable

    public Interval shiftedBy(int offset) {
	return new Interval(this.start+offset, this.stop+offset);
    }

    public int length() { return stop-start+1; }

    public String asString() { return String.format("%d-%d",this.start,this.stop); }
    public String toString() { return asString(); }

    public static boolean overlaps(Interval i1, Interval i2) { return i1.start<=i2.stop && i1.stop>=i2.start; }
    public        boolean overlaps(Interval other) { return overlaps(this,other); }

    public static boolean contains(Interval i1, Interval i2) {	return i1.start<=i2.start && i1.stop >= i2.stop; }
    public        boolean contains(Interval other) { return contains(this,other); }
    public        boolean contains(int i) { return this.start<=i && this.stop >=i; }

    // Comparison "operator" semantics:
    // gt() and lt() can be considered as "strictly" greater (less) than, meaning
    // that if there is any overlap between the two intervals than these functions return false.
    // ge() and le() allow overlap, but both extremities must satisfy the inequality
    // eg       ------------       or   -------------
    //       --------                       -------------
    // return true, but not 
    //          -------------
    //       --------------------
    // Question/fixme: how to implement the Comparator interface?


    public static boolean equals(Interval i1, Interval i2) { return i1.start==i2.start && i1.stop==i2.stop; }
    public        boolean equals(Interval other) { return equals(this,other); }
    public static boolean ne(Interval i1, Interval i2) { return i1.start!=i2.start || i1.stop!=i2.stop; }
    public        boolean ne(Interval other) { return ne(this,other); }

    public static boolean gt(Interval i1, Interval i2) { return i1.start>i2.stop; }
    public static boolean lt(Interval i1, Interval i2) { return i1.stop<i2.start; }
    public        boolean gt(Interval other) { return gt(this,other); }
    public        boolean lt(Interval other) { return lt(this,other); }

    public static boolean ge(Interval i1, Interval i2) { return i1.start>=i2.start && i1.stop>=i2.stop; }
    public static boolean le(Interval i1, Interval i2) { return i1.start<=i2.start && i1.stop<=i2.stop; } 
    public        boolean ge(Interval other) { return ge(this,other); }
    public        boolean le(Interval other) { return le(this,other); }




    public int compareTo(Object o) 
	throws ClassCastException {
	Interval i=(Interval)o;
	if (this.start<i.start) return -1;
	if (this.start==i.start) {
	    return this.stop<i.stop? -1:
		this.stop>i.stop? 1:0;
	}
	return 1;		// this.start>i.start
    }

    public static boolean adjacent(Interval i1, Interval i2) { return i1.stop==i2.start-1 || i2.stop==i1.start-1; }
    public        boolean adjacent(Interval other) { return adjacent(this,other); }

    // join two intervals together, taking the union
    // Do so even if they don't overlap
    public static Interval join(Interval i1, Interval i2) {
	int start=i1.start<i2.start? i1.start : i2.start;
	int stop=i1.stop>i2.stop? i1.stop : i2.stop;
	return new Interval(start,stop);
    }
    public Interval join(Interval other) { return join(this,other); }


    public static Interval[] merge_overlaps(Interval[] sorted) {
	Interval current=sorted[0];
	ArrayList<Interval> merged=new ArrayList<Interval>();
	boolean add_last=false;
	for (int i=1; i<sorted.length; i++) {
	    System.out.println(String.format("next: %s",sorted[i].toString()));
	    if (sorted[i].overlaps(current)) {
		current=current.join(sorted[i]);
		add_last=true;
	    } else {
		merged.add(current);
		System.out.println(String.format("added %s",current.toString()));
		current=sorted[i];
		add_last=false;
	    }
	    System.out.println(String.format("current: %s",current.toString()));
	}
	if (add_last) {
	    merged.add(current);
	}
	return merged.toArray(new Interval[merged.size()]);
    }
}