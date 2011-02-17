import java.io.*;
import java.util.*;
import java.lang.*;

/*
  This program reads the list of found HCRs (see FindHCR.java) and prints out the 
  corresponding areas of multiple alignments as found in the MULTIZ files located
  in /projects/instr/11wi/cse427/multiz/chr%d.maf.
 */

class AlignHCRs {
    public static void main(String[] argv) {
	Date start_time=new Date();

	// Read in serialized HashMap of HCRs
	String hcr_file="hcrs.ser";
	HashMap<String,HCR[]> chr2HCRs=readHCRs(hcr_file); // k=chrX, v=PhyloBlock?
	dump_chr2HCRs(chr2HCRs,hcr_file,false);
	// System.exit(1);
	
	// Look through multiz files for blocks overlapping one of our HCRs:
	String[] human_chrs={"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
	// String[] human_chrs={"11"};
	for (int i=0; i<human_chrs.length; i++) {
	    String chrom="chr"+human_chrs[i];
	    System.out.println("assigning zBlocks from "+chrom);
	    assignZBlocks(chrom, chr2HCRs);
	}

	// get a sorted list of hcrs; sort by chrom, position
	HCR[] hcrs=(HCR[])chr2HCRs.values().toArray();
	Arrays.sort(hcrs);
	
	// Show the full alignment for each hcr:
	for (int i=0; i<hcrs.length; i++) {
	    HCR hcr=hcrs[i];
	    System.out.println(hcr.alignment());
	    break;		// fixme; debugging aid
	}

	Date end_time=new Date();
	System.out.println(String.format("execution time: %s",new TimeSpan(start_time,end_time)));
    }

    // Read the filtered .maf files, assign each zblock to the proper hcr:
    public static void assignZBlocks(String chrom, HashMap<String,HCR[]> chr2HCRs) {
	String multiZfile=String.format("%s.maf.filtered",chrom);
	MultiZParser parser=new MultiZParser(multiZfile);
	MultiZBlock zBlock=null;

	while ((zBlock=parser.next())!=null) {
	    String z_chrom=zBlock.human_chrom;
	    if (z_chrom==null) continue;
	    Interval zb_int=zBlock.human_interval;
	    if (zb_int==null) continue;
	    HCR[] hcrs=(HCR[])chr2HCRs.get(z_chrom);
	    if (hcrs==null || hcrs.length==0) continue;
	    
	    for (int i=0; i<hcrs.length; i++) {
		HCR hcr=hcrs[i];
		if (zb_int.overlaps(hcr.interval)) {
		    hcr.zBlocks.add(zBlock);
		    break;
		}
	    }
	}
    }


////////////////////////////////////////////////////////////////////////



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

    // See if we read in everything from the .ser file sucessfully
    public static void dump_chr2HCRs(HashMap chr2HCRs, String hcr_file, boolean full) {
	Iterator it=chr2HCRs.values().iterator();
	int n_hcrs=0;
	while (it.hasNext()) { n_hcrs+=((HCR[])it.next()).length;	}
	System.out.println(String.format("read %s: %d hcrs",hcr_file, n_hcrs));;

	if (full) {
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
    }


////////////////////////////////////////////////////////////////////////

    // Read the .maf files, filter to output only zBlocks we're interested in
    public static void filterZBlocks(String chrom, HashMap chr2HCRs) {
	String multiZfile=String.format("/projects/instr/11wi/cse427/multiz/%s.maf",chrom);
	// String multiZfile=String.format("/projects/instr/11wi/cse427/multiz/%s.maf.10K",chrom);
	MultiZParser parser=new MultiZParser(multiZfile);

	try {
	    String filtered_multiZfile=String.format("%s.maf.filtered",chrom);
	    FileWriter writer=new FileWriter(filtered_multiZfile);

	    MultiZBlock zBlock=null;
	    while ((zBlock=parser.next())!=null) {
		String z_chrom=zBlock.human_chrom;
		if (z_chrom==null) continue;
		Interval zb_int=zBlock.human_interval;
		if (zb_int==null) continue;
		HCR[] hcrs=(HCR[])chr2HCRs.get(z_chrom);
		if (hcrs==null || hcrs.length==0) continue;

		for (int i=0; i<hcrs.length; i++) {
		    Interval hcr_int=hcrs[i].interval;
		    if (zb_int.overlaps(hcr_int)) {
			writer.write(zBlock.src_block);
			writer.write("\n");
			System.out.println(zBlock.header());
		    }
		}
	    }
	    writer.flush();	// close() probably does this for us
	    writer.close();
	    System.out.println(filtered_multiZfile+" written");
	} catch (IOException ioe) {
	    new Die(ioe);
	}
    }
}