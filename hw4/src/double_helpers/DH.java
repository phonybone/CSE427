package double_helpers;

public class DH {
    public static double log2bE=Math.log(2.0);
    public static double log2(double d) { return Math.log(d)/log2bE; }
    public static double max2(double d1, double d2) { return (d1>d2? d1:d2); }
    public static double max3(double d1, double d2, double d3) { return d1>d2? (d1>d3? d1:d3) : (d2>d3? d2:d3); }

    public static String d1s(double[] r, String header) {
	StringBuffer buf=new StringBuffer();
	if (header!=null) { buf.append(String.format("%s: ", header)); }
	for (int j=0; j<r.length; j++) {
	    buf.append(String.format("%7.3f ", r[j]));
	}
	return new String(buf);
    }
    public static String d1s(double[] r) { return d1s(r,null); }

    public static String d2s(double[][] a, String header) {
	StringBuffer buf=new StringBuffer();
	if (header!=null) {
	    buf.append(header);
	    buf.append("\n");
	}
	for (int i=0; i<a.length; i++) {
	    buf.append(d1s(a[i], header+String.format("[%d]", i)));
	    buf.append("\n");
	}
	return new String(buf);
    }
    public static String d2s(double[][] a) { return d2s(a, null); }

}