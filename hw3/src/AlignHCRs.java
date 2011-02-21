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
	int total_hcrs=dump_chr2HCRs(chr2HCRs,hcr_file,false);
	// System.exit(1);
	
	// Look through multiz files for blocks overlapping one of our HCRs:
	String[] human_chrs={"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
	// String[] human_chrs={"11"};
	for (int i=0; i<human_chrs.length; i++) {
	    String chrom="chr"+human_chrs[i];
	    assignZBlocks(chrom, chr2HCRs);
	}

	// get a sorted list of hcrs; sort by chrom, position
	// Problem is that we're getting an array of arrays; need to flatten
	HCR[] sorted_hcrs=get_sorted_hcrs(chr2HCRs);
	
	// Show the full alignment for each hcr:
	System.out.println("Victor Cassen");
	System.out.println(String.format("%d Extremely Conserved Elements\n", total_hcrs));
	for (int i=0; i<sorted_hcrs.length; i++) {
	    HCR hcr=sorted_hcrs[i];
	    System.out.println(hcr.alignment());
	    //break;		// debugging aid
	}

	// Write length and chrom data
	try {
	    StringHelpers.spitString(lengthData(chr2HCRs),"length.data");
	    StringHelpers.spitString(chromDist(chr2HCRs),"chrom.data");
	} catch (IOException ioe) {
	    new Die(ioe);
	}

	Date end_time=new Date();
	System.err.println(String.format("execution time: %s",new TimeSpan(start_time,end_time)));
    }

    // Read the filtered .maf files, assign each zblock to the proper hcr:
    public static void assignZBlocks(String chrom, HashMap<String,HCR[]> chr2HCRs) {
	String multiZfile=String.format("%s.maf.filtered",chrom);
	MultiZParser parser=new MultiZParser(multiZfile);
	MultiZBlock zBlock=null;

	int n_zbs=0;
	int n_hcrs=0;
	HCR[] hcrs=(HCR[])chr2HCRs.get(chrom);
	n_hcrs=hcrs.length;

	while ((zBlock=parser.next())!=null) {
	    String z_chrom=zBlock.human_chrom;
	    if (z_chrom==null) continue;
	    Interval zb_int=zBlock.human_interval;
	    if (zb_int==null) continue;

	    if (hcrs==null || hcrs.length==0) continue;
	    
	    for (int i=0; i<hcrs.length; i++) {
		HCR hcr=hcrs[i];
		if (zb_int.overlaps(hcr.interval)) {
		    hcr.zBlocks.add(zBlock);
		    n_zbs++;
		} // don't break after adding block; theoretically possible a zBlock goes with more than one hcr
	    }
	}
	System.err.println(String.format("%6s: %3d zBlocks added to %3d hcrs",chrom, n_zbs, n_hcrs));
    }


    public static HCR[] get_sorted_hcrs(HashMap<String,HCR[]> chr2HCRs) {
	Collection v=chr2HCRs.values();
	Iterator v_it=v.iterator();
	ArrayList<HCR>sorted_hcrs_al=new ArrayList<HCR>();
	while (v_it.hasNext()) {
	    HCR[] hcrs=(HCR[])v_it.next();
	    for (int i=0; i<hcrs.length; i++) {
		sorted_hcrs_al.add(hcrs[i]);
	    }
	}
	HCR[] sorted_hcrs=(HCR[])sorted_hcrs_al.toArray(new HCR[sorted_hcrs_al.size()]);
	Arrays.sort(sorted_hcrs);
	return sorted_hcrs;
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
    public static int dump_chr2HCRs(HashMap chr2HCRs, String hcr_file, boolean full) {
	Iterator it=chr2HCRs.values().iterator();
	int n_hcrs=0;
	while (it.hasNext()) { n_hcrs+=((HCR[])it.next()).length;	}
	System.err.println(String.format("read %s: %d hcrs",hcr_file, n_hcrs));;

	if (full) {
	    Iterator i=chr2HCRs.keySet().iterator();
	    while (i.hasNext()) {
		String chrom=(String)i.next();
		System.err.println(chrom);
		HCR[] hcrs=(HCR[])chr2HCRs.get(chrom);
		for (int j=0; j<hcrs.length; j++) {
		    System.err.println("  "+hcrs[j].toString());
		}
	    }
	}
	return n_hcrs;
    }


    // Provide a tab-delimited "file"; each line contains two fields
    public static String lengthData(HashMap<String,HCR[]> chr2HCRs) {
	StringBuffer buf=new StringBuffer();
	Iterator it=chr2HCRs.values().iterator();
	while (it.hasNext()) {
	    HCR[] hcrs=(HCR[])it.next();
	    for (int i=0; i<hcrs.length; i++) {
		HCR hcr=hcrs[i];
		buf.append(String.format("%d\n",hcr.length()));
	    }
	}
	return new String(buf);
    }

    public static String chromDist(HashMap<String,HCR[]> chr2HCRs) {
	StringBuffer buf=new StringBuffer();
	Iterator it=chr2HCRs.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry e=(Map.Entry)it.next();
	    String chrom=(String)e.getKey();
	    HCR[] hcrs=(HCR[])e.getValue();
	    buf.append(String.format("%s\t%d\n",chrom,hcrs.length));
	}
	return new String(buf);
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
	    System.err.println(filtered_multiZfile+" written");
	} catch (IOException ioe) {
	    new Die(ioe);
	}
    }
}