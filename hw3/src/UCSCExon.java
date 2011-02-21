import java.io.*;
import java.util.*;

class UCSCExon extends GenomeFeature {
    public String ucsc_id;	// actually a gene id
    public String chrom;
    public String symbol;
    public int exon_num;	// which exon in the gene is this
    public String strand;

    static HashMap<String,String> ucsc2sym;

    public UCSCExon() {}
    public UCSCExon(String id, String chrom, String symbol, int exon_num, Interval interval, String strand) {
	super(chrom, interval);

	this.ucsc_id=id;
	this.chrom=chrom;
	this.symbol=symbol;
	this.exon_num=exon_num;
	this.interval=interval;
	this.strand=strand;

    }

    public String toString() {
	StringBuffer buf=new StringBuffer();
	buf.append(String.format("%s %s %s exon_num=%d %s %s", 
				 ucsc_id, chrom, (symbol==null? "sym=?":symbol), exon_num, interval, strand));
	return new String(buf);	//
    }

    
    // Parse a line from knownGene.txt; generally contains several exons
    public static UCSCExon[] parseLine(String knownGene) {
	try {
	    String[] fields=knownGene.split("\\s+");
	    String ucsc_id=fields[0];
	    String chrom=fields[1];
	    String strand=fields[2];
	    int n_exons=Integer.valueOf(fields[7]).intValue();
	    String exon_starts_str=fields[8];
	    String exon_stops_str=fields[9];


	    String[] starts=exon_starts_str.split(",");
	    String[] stops=exon_stops_str.split(",");
	    UCSCExon[] exons=new UCSCExon[starts.length];

	    assert(starts.length==stops.length);
	    for (int i=0; i<starts.length; i++) {
		int start=Integer.valueOf(starts[i]).intValue();
		int stop=Integer.valueOf(stops[i]).intValue();
		exons[i]=new UCSCExon(ucsc_id, chrom, null, i, new Interval(start,stop), strand);
	    }
	    return exons;

	} catch (ArrayIndexOutOfBoundsException e) {
	    System.err.println("about to return null on line="+knownGene);
	    System.err.println(e.getMessage());
	    e.printStackTrace(System.err);
	    System.exit(1);
	    return null;
	}
    }

    // Return a big ole array of all the exons in the exonfile
    public static UCSCExon[] parse(String exonfile) {
	ArrayList<UCSCExon> exon_list=new ArrayList<UCSCExon>();
	UCSCExon[] exons=null;
	try {
	    BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(exonfile)));
	    String line;
	    int fuse=-1;
	    while ((line=reader.readLine())!=null) {
		UCSCExon[] line_exons=parseLine(line.trim());
		if (line_exons!=null) {
		    for (int i=0; i<line_exons.length; i++) {
			exon_list.add(line_exons[i]);
		    }
		}
		if (fuse--==0) break;
	    }
	    exons=(UCSCExon[])exon_list.toArray(new UCSCExon[exon_list.size()]);

	} catch (IOException ioe) {
	    new Die(ioe);
	}
	return exons;
    }

}