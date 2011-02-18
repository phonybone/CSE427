import java.util.*;
import java.io.Serializable;

class MultiZBlock implements Serializable, Comparable {
    public HashMap org2seq;
    public String src_block;		// taken directly from the .maf file
    public String human_chrom;
    public Interval human_interval;

    static final long serialVersionUID = 1002L;

    public final static String[] list_order={"hg19", 
					      "mm9",
					      "fr2",
					      "rn4",
					      "anoCar1",
					      "bosTau4",
					      "calJac1",
					      "canFam2",
					      "cavPor3",
					      "choHof1",
					      "danRer6",
					      "dasNov2",
					      "dipOrd1",
					      "echTel1",
					      "equCab2",
					      "eriEur1",
					      "felCat3",
					      "galGal3",
					      "gasAcu1",
					      "gorGor1",
					      "loxAfr3",
					      "macEug1",
					      "micMur1",
					      "monDom5",
					      "myoLuc1",
					      "ochPri2",
					      "ornAna1",
					      "oryCun2",
					      "oryLat2",
					      "otoGar1",
					      "panTro2",
					      "papHam1",
					      "petMar1",
					      "ponAbe2",
					      "proCap1",
					      "pteVam1",
					      "rheMac2",
					      "sorAra1",
					      "speTri1",
					      "taeGut1",
					      "tarSyr1",
					      "tetNig2",
					      "tupBel1",
					      "turTru1",
					      "vicPac1",
					      "xenTro2"
    };


    public MultiZBlock() {
	org2seq=new HashMap();
    }

    public MultiZBlock(String src_block) {
	org2seq=new HashMap();
	this.src_block=src_block;
    }

    ////////////////////////////////////////////////////////////////////////

    // Assign a ChromSeq to this zBlock
    public void put(ChromSeq chrseq) {
	org2seq.put(chrseq.org,chrseq);
    }

    // Return the ChromSeq object for a given org
    public ChromSeq get(String org) {
	return (ChromSeq)org2seq.get(org);
    }

    // Return the human ChromSeq object for this zBlock
    public ChromSeq human_cs() {
	return (ChromSeq)(org2seq.get("hg19"));
    }

    // Return the name of the human chromosome for this zBlock
    public String human_chr() {
	ChromSeq cs=(ChromSeq)(org2seq.get("hg19"));
	return cs.chrom;
    }

    public String toString() {
	StringBuffer buf=new StringBuffer();
	for (int i=0; i<list_order.length; i++) {
	    ChromSeq seq=(ChromSeq)org2seq.get(list_order[i]);
	    if (seq==null) continue;
	    buf.append(seq.toString());
	    buf.append("\n");
	}
	return new String(buf);
    }

    public String header() {
	String chrom=human_chrom!=null? human_chrom : "???";
	String intstr=human_interval!=null? human_interval.toString() : "???";
	return String.format("%s:%s",chrom,intstr);
    }

    public int compareTo(Object o) throws ClassCastException {
	MultiZBlock zb=(MultiZBlock)o;
	return human_interval.compareTo(zb.human_interval);
    }

    ////////////////////////////////////////////////////////////////////////
    public void populate_hash() {
	String[] lines=src_block.split("\n");
	for (int i=0; i<lines.length; i++) {
	    String line=lines[i];
	    if (!line.startsWith("s ")) continue;
	    try {

		// Extract info for a ChromSeq: (copied from MultiZParser; should refactor somehow)
		String[] fields=line.split("\\s+");
		String org_chrom=fields[1];
		String f2[]=org_chrom.split("\\.");
		String org=f2[0];
		String chrom=f2[1];
		String start=fields[2];
		// String length=fields[3];
		String strand=fields[4];
		String seq=fields[6];
		int istart=Integer.valueOf(start).intValue();
		// int istop=istart+Integer.valueOf(length).intValue()-1;

		ChromSeq cs=new ChromSeq(org, chrom, seq, istart);
		cs.strand=strand;
		this.put(cs);

	    } catch (ArrayIndexOutOfBoundsException e) {
		new Die(e, "cannot extract chrom and line: "+line);
	    }
	}
    }

    // Return a string for this zBlock as per the homework:
    // 
    public String alignment(HCR hcr) {
	populate_hash();

	Interval hcr_int=hcr.interval;
	int offset=hcr_int.start>human_interval.start? hcr_int.start : human_interval.start;
	Interval substr_int=hcr_int.intersection(human_interval).shiftedBy(-offset);

	StringBuffer buf=new StringBuffer(); 
	buf.append(String.format("%s:%s\n",hcr.chrom,human_interval.intersection(hcr.interval)));
	//buf.append(String.format("\n%d\n",substr_int.length()));
	//buf.append(String.format("%6.4f\n", hcr.avg_phyloP(substr_int)));
	

	//	System.err.println(String.format("hcr_int: %s (%d)", hcr_int.toString(), hcr_int.length()));
	//	System.err.println(String.format("human_interval: %s (%d)", human_interval, human_interval.length()));

	for (int i=0; i<list_order.length; i++) {
	    ChromSeq cs=get(list_order[i]);
	    if (cs==null) { 
		//		buf.append(String.format("%s: empty\n", list_order[i])); // fixme
	    } else {
		String subseq=get(list_order[i]).seq.substring(substr_int.start, substr_int.stop+1);
		buf.append(String.format("%10s %s\n", list_order[i], subseq));
	    }
	}

	// Only want the relevent portion of seq
	String substr=get("hg19").seq.substring(substr_int.start, substr_int.stop+1);
	buf.append(String.format("phyloP:    %s\n", hcr.plusString(substr,substr_int.start)));

	return new String(buf);
    }
}