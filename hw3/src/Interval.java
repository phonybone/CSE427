import java.io.*;

class Interval 
//implements Serializable 
{
    public int start;
    public int stop;

    public Interval(int start, int stop) {
	this.start=start;
	this.stop=stop;
    }

    public String asString() { return String.format("%d-%d",this.start,this.stop); }

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


    public static boolean gt(Interval i1, Interval i2) { return i1.start>i2.stop; }
    public static boolean lt(Interval i1, Interval i2) { return i1.stop<i2.start; }
    public        boolean gt(Interval other) { return gt(this,other); }
    public        boolean lt(Interval other) { return lt(this,other); }

    public static boolean ge(Interval i1, Interval i2) { return i1.start>=i2.start && i1.stop>=i2.stop; }
    public static boolean le(Interval i1, Interval i2) { return i1.start<=i2.start && i1.stop<=i2.stop; } 
    public        boolean ge(Interval other) { return ge(this,other); }
    public        boolean le(Interval other) { return le(this,other); }




    public static int cmp(Interval i1, Interval i2) {
	if (i1.start<i2.start) return -1;
	if (i1.start==i2.start) {
	    return i1.stop<i2.stop? -1:
		i1.stop>i2.stop? 1:0;
	}
	return 1;		// i1.start>i2.start
    }
    public int cmp(Interval other) { return cmp(this,other); }

    public static boolean adjacent(Interval i1, Interval i2) { return i1.stop==i2.start-1 || i2.stop==i1.start-1; }
    public        boolean adjacent(Interval other) { return adjacent(this,other); }

    // join two intervals together, taking the union
    // Do so even if they don't overlap
    public static Interval join(Interval i1, Interval i2) {
	int start=i1.start<i2.start? i1.start : i2.start;
	int stop=i1.stop<i2.stop? i1.stop : i2.stop;
	return new Interval(start,stop);
    }
    public Interval join(Interval other) { return join(this,other); }
}