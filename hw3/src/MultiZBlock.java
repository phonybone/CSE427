import java.util.*;

class MultiZBlock {
    public HashMap org2seq;
    private final String[] list_order={"hg19", 
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

    public void put(String org, ChromSeq chrseq) {
	org2seq.put(org,chrseq);
    }

    public ChromSeq get(String org) {
	return (ChromSeq)org2seq.get(org);
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

    
}