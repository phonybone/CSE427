import java.util.*;
import java.io.*;

class ProfileHMM {
    public HashMap<String,ProfileHMM_Node> graph; // Graph structure


    // length is the length of strings this PHMM can profile; 
    public ProfileHMM() {
	this.graph=new HashMap<String,ProfileHMM_Node>();
    }

    public void initialize(Alignment a, BackgroundProbs bps) {
	this.graph.clear();	

	this.new_node("begin", false, bps);
	this.new_node("end", false, bps);
	this.new_node("I0", false, bps);
	System.out.println(String.format("allocating %d HMM cols",a.n_match_cols));
	for (int i=1; i<=a.n_match_cols; i++) {
	    boolean last=i==a.n_match_cols;
	    this.new_node("M"+String.valueOf(i), last, bps);
	    this.new_node("I"+String.valueOf(i), last, bps);
	    this.new_node("D"+String.valueOf(i), last, bps);
	}
    }

    public void new_node(String state, boolean last, BackgroundProbs bps) {
	ProfileHMM_Node node=new ProfileHMM_Node(state, last).set_pseudocounts(bps);
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
	System.out.println("processing alignment...");
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
	    
		cs.inc_tr(ns);
		if (aa != '-') cs.inc_em(aa);

		System.out.println(String.format("a[%2d][%2d]=%c (%s)   %s ->%s", 
						 r, c, aa, (align.is_match_col(c)? '*':' '), cs, cs.nextState(ns)));
		cs=get_node(cs.nextState(ns));
	    }
	    cs.inc_tr("end");
	    System.out.println(String.format("last state: %s",  cs));

	    // end state:
	    System.out.println(String.format("end state: %s\n",  get_node("end")));
	}

	// Normalize values (sort for purposes of debugging):
	SortedSet<String> sortedKeys=new TreeSet<String>(graph.keySet());
	Iterator it=sortedKeys.iterator();
	System.out.println("normalizing...");

	while (it.hasNext()) {
	    ProfileHMM_Node s=get_node((String)it.next());
	    System.out.println(s);
	    System.out.println(s.Ebs());
	    s.normalize_trs();
	    s.normalize_ems();
	    System.out.println(s);
	    System.out.println(s.Ebs());
	    System.out.println(" ");
	}
    }

////////////////////////////////////////////////////////////////////////

    public static void main(String[] argv) {
	Alignment a=new Alignment("hw2-muscle17.txt",8).read();
	System.out.println(a.dump());
	ProfileHMM hmm=new ProfileHMM();
	BackgroundProbs bps=null;
	try {
	    bps=BackgroundProbs.readBps("NC_011660.bps.ser");
	} catch (IOException ioe) {
	    new Die(ioe);
	} catch (ClassNotFoundException e) {
	    new Die(e);
	}

	hmm.train(a, bps);
    }
}