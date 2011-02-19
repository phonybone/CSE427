import java.io.*;
import java.util.*;

/*
  Map the HCRs to their (nearest) exons.
 */

class MapHCRs {
    public static void main(String[] argv) {
	String hcr_file="hcrs.ser";
	HashMap<String,HCR[]> chr2HCRs=readHCRs(hcr_file); 
	HCRFeature[] all_hcrs=all_hcrs(chr2HCRs);

	String exonfile="knownGene.txt";
	UCSCExon[] ucsc_exons=UCSCExon.parse(exonfile);
	Arrays.sort(ucsc_exons); // whee!

	for (int i=0; i<all_hcrs.length; i++) {
	    HCRFeature hcr=all_hcrs[i];
	    int insertIndex=Arrays.binarySearch(ucsc_exons,hcr);

	    // Check to see if we overlap; if not, is insertIndex-1 closer?
	}
    }


    // Read the serialized HashMap of HCRs
    public static HashMap readHCRs(String hcr_file) {
	HashMap chr2HCRs=null;
	try {
	    ObjectInputStream ois=new ObjectInputStream(new FileInputStream(hcr_file));
	    chr2HCRs=(HashMap)ois.readObject();
	} catch (IOException ioe) {
	    new Die(ioe);
	} catch (ClassNotFoundException e) {
	    new Die(e);
	}
	return chr2HCRs;
    }

    public static HCRFeature[] all_hcrs(HashMap<String,HCR[]> chr2HCRs) {
	Collection c=chr2HCRs.values();
	HCR[] hcrs=new HCR[c.size()];
	hcrs=(HCR[])c.toArray(hcrs);

	HCRFeature[] hcrfs=new HCRFeature[c.size()];
	for (int i=0; i<c.size(); i++) { hcrfs[i]=new HCRFeature(hcrs[i]); }
	return hcrfs;
    }

}