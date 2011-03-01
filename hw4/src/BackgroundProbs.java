import java.io.*;

// Background transition and emission probabilities

class BackgroundProbs {
    private final int n_aas=26;
    public double[] probs=new double[n_aas];

    public BackgroundProbs(ProtStream ps) {
	int[] sums=new int[n_aas];
	int total_aas=0;
	String prot;
	int n_prots=0;
	while ((prot=ps.next())!=null) {
	    n_prots++;
	    char[] chars=prot.toCharArray();
	    total_aas+=chars.length;
	    for (int i=0; i<chars.length; i++) {
		int index=chars[i]-'A';
		sums[index]++;
	    }
	}
	System.out.println(String.format("%d prots (%d total aa's)", n_prots, total_aas));
	
	for (int i=0; i<n_aas; i++) {
	    probs[i]=(double)sums[i]/total_aas;
	}
    }

    public double pr(char aa) {
	int i=aa == '-'? 0 : (int)aa-'A';
	return probs[i];
    }

    public static void main(String[] argv) {
	ProtStream ps=new ProtStream("NC_011660.faa");
	BackgroundProbs bps=new BackgroundProbs(ps);
	
	double d=0;
	for (char aa='A'; aa<='Z'; aa++) {
	    System.out.println(String.format("%c: %6.4f", aa, bps.pr(aa)));
	    d+=bps.pr(aa);
	}
	assert(d==1.0);
	System.out.println(String.format("d=%6.4f",d));
    }
}

