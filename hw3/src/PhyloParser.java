import java.io.*;
import java.lang.*;
import java.util.*;

class PhyloParser {
    public BufferedReader reader;
    public PhyloBlock currentBlock;
    public double c;
    public double max_score;
    public ArrayList<Double> q;

    PhyloParser(String phylofile, double c) {
	try {
	    reader=new BufferedReader(new InputStreamReader(new FileInputStream(phylofile)));
	    String line=reader.readLine();
	    if (line==null) throw new IOException(phylofile+": empty file???");
	    currentBlock=parseFixedLine(line); // "prime pump" (ooh!  ahh!  ooh!  ahh!)

	} catch (IOException ioe) {
	    System.err.println(ioe.getMessage());
	    System.exit(1);
	}
	q=new ArrayList<Double>();
	this.c=c;
	this.max_score=0;
    }

    // Parse lines that look like this:
    // fixedStep chrom=chr10 start=60001 step=1
    // Retuns a partially-filled in PhyloBlock
    private PhyloBlock parseFixedLine(String line) 
	throws IOException 
    {
	int start=0;
	String chrom=null;
	if (!line.startsWith("fixed")) 
	    throw new IOException(line+": doesn't start with 'fixed'");
	String[] matches=line.split("\\s+");
	for (int i=0; i<matches.length; i++) {
	    String[] m2=matches[i].split("=");
	    if (m2[0].equals("chrom")) chrom=m2[0];
	    if (m2[0].equals("start")) {
		try { start=Integer.parseInt(m2[1]); }
		catch (NumberFormatException nfe) { new Die(nfe, "trying to convert "+m2[1]+" to integer"); }
	    }
	}
	
	return new PhyloBlock(chrom,start);
    }
    
    // Return the next block, as defined by 'fixedStep' lines (or null when done)
    // Leave parsing after the next "fixedStep" line
   public PhyloBlock nextBlock() {
       PhyloBlock block=null;
       String line;

       try {
	   while ((line=reader.readLine())!=null) {
	       if (line.startsWith("fixedStep")) {
		   // Create and return current block:
		   block=currentBlock;
		   block.setQ(q);
		   block.max_score=max_score;
		   max_score=0;
		   assert block.length==q.size();
		   //		   System.err.println(String.format("new block: length=%d",q.size()));
		   currentBlock=parseFixedLine(line);
		   break;
	       }
	       try {
		   Double v=Double.valueOf(line.trim());
		   if (v<0) v=0.0;
		   q.add(new Double(v-c));
		   if (v>max_score) max_score=v;
	       } catch (NumberFormatException nfe) {
		   new Die(nfe);
	       }
	   }
       } catch (IOException ioe) {
	   new Die(ioe);
       }
       return block; 		// is null if done
   }
}