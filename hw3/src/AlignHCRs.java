import java.io.*;
import java.util.*;

/*
  This program reads the list of found HCRs (see FindHCR.java) and prints out the 
  corresponding areas of multiple alignments as found in the MULTIZ files located
  in /projects/instr/11wi/cse427/multiz/chr%d.maf.
 */

class AlignHCRs {
    public static void main(String[] argv) {

	String hcr_file="hcrs11.ser";
	HashMap chr2HCRs=readHCRs(hcr_file); // k=chrX, v=PhyloBlock?

	// Look through multiz files for blocks overlapping one of our HCRs:
	// String[] human_chrs={"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
	String[] human_chrs={"11"};
	for (int i=0; i<human_chrs.length; i++) {
	    String chrom="chr"+human_chrs[i];
	    HCR[] hcrs=(HCR[])chr2HCRs.get(chrom);
	    findZBlocks(chrom, hcrs);
	    
	    // assemble zblocks around each hcr:
	    for (i=0; i<hcrs.length; i++) {
		String alignment=alignZBlocks(hcrs[i]);
	    }
	}
    }

    public static void findZBlocks(String chrom, HCR[] hcrs) {
	String multiZfile=String.format("/projects/instr/11wi/cse427/multiz/%s.maf",chrom);
	MultiZParser parser=new MultiZParser(multiZfile);
	MultiZBlock zBlock=null;
	while ((zBlock=parser.next())!=null) {
	    ChromSeq cs=zBlock.get("hg19");
	    for (int j=0; j<hcrs.length; j++) {
		if (cs.interval.overlaps(hcrs[i].interval)) {
		    hcrs[i].zBlocks.add(zBlock);
		}
	    }
	}
    }

    public static String alignZBlocks(HCR hcr) {
	// sort hcr.zBlocks by starting coord
	Arrays.sort(hcr.zBlocks);

	// concat all seqs in zblocks together, by org; fill in gaps with '-'
	// Have to create a String[] for each species; each entry the concat of the appropriate
	// entries from the zblocks.
	// within a multiz block, all seqs are of equal length (yay)
    }

    public static void dump_chr2HCRs(HashMap chr2HCRs) {
	Iterator i=chr2HCRs.keySet().iterator();
	while (i.hasNext()) {
	    String chrom=(String)i.next();
	    System.out.println(chrom);
	    HCR[] hcrs=(HCR[])chr2HCRs.get(chrom);
	    for (int j=0; j<hcrs.length; j++) {
		System.out.println("  "+hcrs[j].toString());
	    }
	}
    }

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
}