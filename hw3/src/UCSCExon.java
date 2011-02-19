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
    public UCSCExon(String id, String chrom, int i_chrom, String symbol, int exon_num, Interval interval, String strand) {
	super(i_chrom, interval);

	this.ucsc_id=id;
	this.chrom=chrom;
	this.symbol=symbol;
	this.exon_num=exon_num;
	this.interval=interval;
	this.strand=strand;

    }
    
    // Parse a line from knownGene.txt; generally contains several exons
    public static UCSCExon[] parseLine(String knownGene) {
	try {
	    String[] fields=knownGene.split("//s+");
	    String ucsc_id=fields[0];
	    String chrom=fields[1];
	    String strand=fields[2];
	    int n_exons=Integer.valueOf(fields[7]).intValue();
	    String exon_starts_str=fields[8];
	    String exon_stops_str=fields[9];


	    String[] starts=exon_starts_str.split(",");
	    String[] stops=exon_stops_str.split(",");
	    UCSCExon[] exons=new UCSCExon[starts.length];

	    String chr_n=chrom.substring(3);
	    int i_chrom;
	    try {
		i_chrom=Integer.valueOf(chr_n).intValue();
	    } catch (NumberFormatException e) {
		if (chr_n.equals("X")) { i_chrom=23; }
		else if (chr_n.equals("Y")) { i_chrom=24; }
		else { i_chrom=25; } // no idea
	    }



	    assert(starts.length==stops.length);
	    for (int i=0; i<starts.length; i++) {
		int start=Integer.valueOf(starts[i]).intValue();
		int stop=Integer.valueOf(stops[i]).intValue();
		exons[i]=new UCSCExon(ucsc_id, chrom, i_chrom, null, i, new Interval(start,stop), strand);
	    }
	    return exons;

	} catch (ArrayIndexOutOfBoundsException e) {
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
	    while ((line=reader.readLine())!=null) {
		UCSCExon[] line_exons=parseLine(line.trim());
		for (int i=0; i<line_exons.length; i++) {
		    exon_list.add(line_exons[i]);
		}
	    }
	    exons=(UCSCExon[])exon_list.toArray(new UCSCExon[exon_list.size()]);

	} catch (IOException ioe) {
	    new Die(ioe);
	}
	return exons;
    }

}