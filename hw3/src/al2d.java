// Figure out how to convert an ArrayList<Double> to a double[]
import java.util.*;

class al2d {
    public static void main(String[] argv) {
	ArrayList<Double> ar=new ArrayList<Double>();
	ar.add(3.2);
	ar.add(6.9);
	ar.add(2.7);
	ar.add(1.3);
	ar.add(8.6);

	Double[] da=new Double[ar.size()];
	da=ar.toArray(da);
	for (int i=0; i<da.length; i++) {
	    System.out.println(String.format("da[%d]=%g",i,da[i]));
	}
	double d=da[0];
	System.out.println(String.format("d=%g",d));

	double[] da2=new double[ar.size()];
	//	da2=ar.toArray(da2);
	for (int i=0; i<da2.length; i++) {
	    System.out.println(String.format("da2[%d]=%g",i,da2[i]));
	}

    }
}