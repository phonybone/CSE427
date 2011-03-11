import java.io.*;

// Background transition and emission probabilities

class BackgroundProbs implements Serializable {
    private final int n_aas=26;
    public double[] probs=new double[n_aas];

    public BackgroundProbs(ProtStream ps) {
	int[] sums=new int[n_aas];
	int total_aas=0;
	String prot;
	int n_prots=0;
	while ((prot=ps.next())!=null) {
	    //System.out.println("prot is "+prot);
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

    // Serialize bps to file
    public void writeBps(String toFile) throws IOException {
	ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(toFile));
	oos.writeObject(this);
	oos.close();
	System.out.println(toFile+" written");
    }

    // Deserialize bps from file
    public static BackgroundProbs readBps(String fromFile)  throws IOException, ClassNotFoundException {
	BackgroundProbs bps=null;
	ObjectInputStream ois=new ObjectInputStream(new FileInputStream(fromFile));
	bps=(BackgroundProbs)ois.readObject();
	ois.close();
	return bps;
    }

    public static void main(String[] argv) {
	String prot_files[]={"NC_011660.faa", "NC_007618.faa", "NC_007624.faa"};

	for (int i=0; i<prot_files.length; i++) {
	    String prot_file=prot_files[i];
	    ProtStream ps=new ProtStream(prot_file);
	    System.out.println("counting "+prot_file);

	    BackgroundProbs bps=new BackgroundProbs(ps);
	
	    double d=0;
	    for (char aa='A'; aa<='Z'; aa++) {
		System.out.println(String.format("%c: %6.4f", aa, bps.pr(aa)));
		d+=bps.pr(aa);
	    }
	    assert(d==1.0);
	    System.out.println(String.format("d=%6.4f",d));

	    String output_file=prot_file;
	    output_file=output_file.replaceAll(".faa", ".bps.ser");
	    System.out.println("output_file is "+output_file);
	    try {
		bps.writeBps(output_file);
	    } catch (IOException ioe) {
		new Die(ioe);
	    }
	}
    }
}

