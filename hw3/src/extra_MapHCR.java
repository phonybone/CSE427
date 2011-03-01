import java.io.*;
import java.util.*;

/*
  Map the HCRs to their (nearest) exons.
 */

class MapHCRs {
    public static void main(String[] argv) {
	Date start_time=new Date();

	// Read the serialized hcrs; convert to HCRFeatures
	String hcr_file="hcrs.ser";
	HashMap<String,HCR[]> chr2HCRs=readHCRs(hcr_file); 
	HCRFeature[] all_hcrs=all_hcrs(chr2HCRs);

	// Read and sort the UCSC Exons from knownGenes.txt
	String exonfile="knownGenes.txt";
	System.err.println("parsing known genes in "+exonfile);
	UCSCExon[] ucsc_exons=UCSCExon.parse(exonfile);
	System.err.println("sorting exons");
	Arrays.sort(ucsc_exons); 
	System.err.println(String.format("sorted %d ucsc exons", ucsc_exons.length));
	
	// Load ucsc id -> symbol hash:
	HashMap<String,String> ucsc2sym=loadUCSC2Sym("ucsc2symbol.csv");

	// Map the HCRs:
	HashMap<String,Integer> histo=mapHCRs(all_hcrs, ucsc_exons, ucsc2sym);
	
	// Print the histo:
	System.out.println(String.format("%10s: %d", "exonic", histo.get("overlapping")));
	System.out.println(String.format("%10s: %d", "intronic", histo.get("intronic")));
	System.out.println(String.format("%10s: %d", "intergenic", histo.get("intergenic")));
	

	Date end_time=new Date();
	System.err.println(String.format("execution time: %s",new TimeSpan(start_time,end_time)));

    }

    // Try to find each hcr in the sorted exon array:
    // If the hcr overlaps an exon, report that.  Otherwise, report the closest exon
    public static HashMap<String,Integer> 
	mapHCRs(HCRFeature[] all_hcrs, 
		UCSCExon[] ucsc_exons, 
		HashMap<String,String> ucsc2sym) {

	HashMap<String,Integer> histo=new HashMap<String,Integer>();
	histo.put("overlapping",0);
	histo.put("intronic",0);
	histo.put("intergenic",0);

	for (int i=0; i<all_hcrs.length; i++) {
	    HCRFeature hcr=all_hcrs[i];
	    int insertIndex=Arrays.binarySearch(ucsc_exons,hcr);
	    int index=insertIndex>0? insertIndex : -insertIndex;

	    // Check to see if we overlap; if not, is insertIndex-1 closer?
	    if (insertIndex>0) { // overlaps; but this case unlikely unless hcr exactly matches exon
		UCSCExon exon=ucsc_exons[insertIndex];
		exon.symbol=ucsc2sym.get(exon.ucsc_id);
		System.out.println(String.format("hcr %s overlaps exon=%s", hcr, ucsc_exons[index]));
		incHisto("overlapping", histo);

	    } else if (ucsc_exons[-insertIndex].interval.overlaps(hcr.interval)) { // also overlaps
		UCSCExon exon=ucsc_exons[-insertIndex];
		exon.symbol=ucsc2sym.get(exon.ucsc_id);
		System.out.println(String.format("hcr %s overlaps exon=%s (exonic)", hcr, ucsc_exons[index]));
		incHisto("overlapping", histo);

	    } else {
		insertIndex=-insertIndex;
		UCSCExon before=ucsc_exons[insertIndex];
		UCSCExon after=ucsc_exons[insertIndex+1];

		
		UCSCExon closest=hcr.distanceTo(before) < hcr.distanceTo(after)? before : after;
		UCSCExon bracketing=hcr.distanceTo(before) > hcr.distanceTo(after)? before : after;
		closest.symbol=ucsc2sym.get(closest.ucsc_id);
		if (closest.symbol==null) {
		    System.err.println("no symbol for "+closest.ucsc_id);
		    closest.symbol=closest.ucsc_id;
		}
		bracketing.symbol=ucsc2sym.get(bracketing.ucsc_id);
		if (bracketing.symbol==null) {
		    System.err.println("no symbol for "+bracketing.ucsc_id);
		    bracketing.symbol=bracketing.ucsc_id;
		}

		boolean intronic=bracketing.symbol.equals(closest.symbol);
		String location=intronic? "intronic" : "intergenic";
		System.out.println(String.format("hcr %s: closest feature: %s (%s, distance=%d)",
						 hcr,closest, location, hcr.distanceTo(closest)));
		incHisto(location, histo);
	    }
	}
	return histo;
    }


    public static void incHisto(String key, HashMap<String,Integer> h) {
	h.put(key, (int)h.get(key)+1);
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
	ArrayList<HCRFeature> hcrf_al=new ArrayList<HCRFeature>();
	Collection c=chr2HCRs.values(); 
	Iterator it=c.iterator();
	while (it.hasNext()) {
	    HCR[] hcrs=(HCR[])it.next();
	    for (int i=0; i<hcrs.length; i++) {	hcrf_al.add(new HCRFeature(hcrs[i])); }
	}
	HCRFeature all_hcrs[]=hcrf_al.toArray(new HCRFeature[hcrf_al.size()]);
	return all_hcrs;
    }
    
    public static HashMap<String,String> loadUCSC2Sym(String ucsc2sym_file) {
	System.err.println("Loading "+ucsc2sym_file);
	HashMap<String,String> u2s=new HashMap<String,String>();
	try {
	    BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(ucsc2sym_file)));
	    String line;
	    while ((line=reader.readLine())!=null) {
		if (line.startsWith("#")) continue;
		String[] fields=line.trim().split(",");
		if (fields.length != 2) continue;
		u2s.put(fields[0],fields[1]);
	    }
	} catch (IOException ioe) {
	    new Die(ioe);
	}
	return u2s;
    }
}