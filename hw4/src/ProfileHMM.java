import java.util.*;
import java.io.*;
import java.lang.Math.*;

class ProfileHMM {
    public HashMap<String,ProfileHMM_Node> graph; // Graph structure
    public int length;				  // equal to the number of Match states; aka alignment.n_match_cols
    public Alignment alignment;
    public BackgroundProbs bps;
    
    private static boolean debugging=false;

    // length is the length of strings this PHMM can profile; 
    public ProfileHMM() {
	this.graph=new HashMap<String,ProfileHMM_Node>();
    }

    public void initialize(Alignment a, BackgroundProbs bps) {
	this.graph.clear();	

	this.new_node("begin", false);
	this.new_node("end", false);
	this.new_node("I0", false);
	for (int i=1; i<=a.n_match_cols; i++) {
	    boolean last=i==a.n_match_cols;
	    this.new_node("M"+String.valueOf(i), last);
	    this.new_node("I"+String.valueOf(i), last);
	    this.new_node("D"+String.valueOf(i), last);
	}
	this.length=a.n_match_cols;
	this.bps=bps;
    }

    public void new_node(String state, boolean last) {
	ProfileHMM_Node node=new ProfileHMM_Node(state, last);
	this.graph.put(state, node);
    }
	
    public ProfileHMM_Node get_node(String state) throws ProfileHMM_BadStateException {
	ProfileHMM_Node n=(ProfileHMM_Node)this.graph.get(state);
	if (n==null) throw new ProfileHMM_BadStateException(state);
	return n;
    }

    // return the transition probability from state j to state k: fixme: implement
    public double tr_pr(String j, String k) {
	ProfileHMM_Node n=get_node(j);
	
	return 0;
    }

    // return the emission probability of char b from state j: fixme: implement
    public double em_pr(String j, char b) {
	return 0;
    }

    public void train(Alignment align, BackgroundProbs bps) {
	this.initialize(align, bps);
	System.out.println("training on alignment...");
	for (int r=0; r<align.height(); r++) {
	    ProfileHMM_Node cs=get_node("begin"); // cs for "current state"
	    char[] path=align.row(r);
	    for (int c=0; c<path.length; c++) {
		char aa=path[c];
		
		String ns="";
		if (aa != '-') {
		    ns=align.is_match_col(c)? "M":"I";
		} else {	// aa=='-'
		    if (align.is_match_col(c)) ns="D"; 
		    else continue; // ignore '-' chars in non-match cols
		}
	    
		// increment Ajk's and Eb(j)'s
		cs.inc_tr(ns);
		if (aa != '-') cs.inc_em(aa);

		//System.out.println(String.format("a[%2d][%2d]=%c (%s)   %s ->%s", 
		//r, c, aa, (align.is_match_col(c)? '*':' '), cs, cs.nextState(ns)));
		cs=get_node(cs.nextState(ns));
	    }
	    cs.inc_tr("end");

	    //System.out.println(String.format("last state: %s",  cs));
	    //System.out.println(String.format("end state: %s\n",  get_node("end")));
	}

	// Normalize values (sort for purposes of debugging):
	SortedSet<String> sortedKeys=new TreeSet<String>(graph.keySet());
	Iterator it=sortedKeys.iterator();
	System.out.println("normalizing...");
	while (it.hasNext()) {
	    ProfileHMM_Node s=get_node((String)it.next());
	    s.normalize_trs();
	    s.normalize_ems(bps);
	}
    }
    

////////////////////////////////////////////////////////////////////////


    public ArrayList<ViterbiResult> align_genome(String prot_file) {
	System.out.println(String.format("aligning proteins in %s", prot_file));
	ProtStream ps=new ProtStream(prot_file);
	ArrayList<ViterbiResult> results=new ArrayList<ViterbiResult>();
	String prot;
	while ((prot=ps.next())!=null) {
	    Viterbi viterbi=new Viterbi(this, prot, bps);
	    double score=viterbi.score();
	    viterbi.dump();
	    
	    results.add(new ViterbiResult(ps.last_prot_name, prot, score));
	    // System.out.println(String.format("%s: %g", ps.last_prot_name, score));
	    
	    if (score>0) { viterbi.backtrace(); }

	    break;
	}
	return results;
    }

    public ArrayList<ViterbiResult> align_alignment(Alignment a) {
	ArrayList<ViterbiResult> results=new ArrayList<ViterbiResult>();
	for (int i=0; i<a.n_rows; i++) {
	    String prot=a.rowAsString(i).replaceAll("[-]", "");
	    Viterbi viterbi=new Viterbi(this, prot, bps);
	    double score=viterbi.score();
	    results.add(new ViterbiResult(a.names[i], prot, score));
	    // System.out.println(String.format("%s: %g", a.names[i], v));
	}
	return results;
    }

    public static BackgroundProbs get_bgprobs(String bps_file) {
	BackgroundProbs bps=null;
	try {
	    bps=BackgroundProbs.readBps(bps_file);
	} catch (IOException ioe) {
	    new Die(ioe);
	} catch (ClassNotFoundException e) {
	    new Die(e);
	}
	return bps;
    }

////////////////////////////////////////////////////////////////////////

    // argv[0]: name of a file containing proteins, suitable as fodder for ProtStream (default="NC_011660.faa")
    // argv[1]: name of a file containing an alignment (default="hw=muscle17.txt")
    // Also: there must exist a file with the same basename as argv[0], but ending in '.bps.ser'.  This
    // file should contain the serialized version of the background probs.
    public static void main(String[] argv) {
	String prot_file="NC_011660.faa";
	String align_file="hw2-muscle17.txt";
	String bps_file=null;

	boolean align_from_alignment=false;
	try {
	    prot_file=argv[0];
	    align_file=argv[1];
	    bps_file=argv[2];
	    align_from_alignment=argv.length >= 4;
	} catch (ArrayIndexOutOfBoundsException e) {
	    // pass
	    bps_file=prot_file.replaceAll(".faa", ".bps.ser");
	}

	Alignment a=new Alignment(align_file,8).read();
	System.out.println(String.format("using alignment %s from %s", a, align_file));

	BackgroundProbs bps=get_bgprobs(bps_file);
	System.out.println("Using background probs in "+bps_file);
	ProfileHMM hmm=new ProfileHMM();
	hmm.train(a, bps);

	ArrayList<ViterbiResult> results;
	if (align_from_alignment) {
	    results=hmm.align_alignment(a);
	} else {
	    results=hmm.align_genome(prot_file);
	}
	
	System.out.println("Sorted results:");
	ViterbiResult[] sorted=results.toArray(new ViterbiResult[results.size()]);
	Arrays.sort(sorted);
	for (int i=0; i<sorted.length; i++) {
	    System.out.println(String.format("%s %s", sorted[i].header(), (debugging? sorted[i].seq : "")));
	}
    }
}