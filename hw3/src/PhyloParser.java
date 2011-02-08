import java.io.*;

class PhyloParser {
    public BufferedReader reader;
    public int start;
    public int stop;
    public String chrom;


    PhyloParser(String phylofile, double c) {
	try {
	    reader=new BufferedReader(new InputStreamReader(new FileInputStream(phylofile)));
	    String line=reader.readLine();
	    if (line==null) throw new IOException(phylofile+": empty file???");
	    parseFixedLine(line);

	} catch (IOException ioe) {
	    System.err.println(ioe.getMessage());
	    System.exit(1);
	}
    }

    // Parse lines that look like this:
    // fixedStep chrom=chr10 start=60001 step=1

    private void parseFixedLine(String line) {
	if (!line.startsWith("fixed")) 
	    throw new IOExceptions(line+": doesn't start with 'fixed'");
	String[] matches=line.matches("\\s+");
	for (int i=0; i<matches.length; i++) {
	    String[] m2=matches[i].split("=");
	    if (m2[0].equals("chrom")) this.chrom=m2[0];
	    if (m2[0].equals("start")) {
		this.start=Integer.parseInt(m2[0]);
		this.stop=this.start;
	    }
	}
    }
    
    public PhyloBlock next() {
	
    }
}