import java.util.*;

class ProfileHMM {
    public HashMap<String,ProfileHMM_Node> graph; // Graph structure


    // length is the length of strings this PHMM can profile; 
    public ProfileHMM() {}

    public void initialize(Alignment a) {
	this.graph=new HashMap<String,ProfileHMM_Node>();

	this.new_node("begin");
	this.new_node("end");
	for (int i=1; i<=a.n_cols; i++) {
	    this.new_node("M"+String.valueOf(i));
	    this.new_node("I"+String.valueOf(i));
	    this.new_node("D"+String.valueOf(i));
	}
    }

    public void new_node(String state) {
	ProfileHMM_Node node=new ProfileHMM_Node(state);
	this.graph.put(state, node);
    }
	
    public ProfileHMM_Node get_node(String state) throws ProfileHMM_BadStateException {
	ProfileHMM_Node n=(ProfileHMM_Node)this.graph.get(state);
	if (n==null) throw new ProfileHMM_BadStateException(state);
	return n;
    }

    // return the transition probability from state j to state k
    public double tr_pr(String j, String k) {
	ProfileHMM_Node n=get_node(j);
	
	return 0;
    }

    // return the emission probability of char b from state j
    public double em_pr(String j, char b) {
	return 0;
    }

    public void train(Alignment align) {
	this.initialize(align);
	for (int r=0; r<align.n_rows; r++) {
	    ProfileHMM_Node cs=get_node("begin"); // cs for "current state"
	    char[] path=align.row(r);
	    for (int c=0; c<path.length; c++) {
		char aa=path[c];

		if (aa != '-') {
		    cs.inc_em(aa); // emit aa
		    if (align.is_match_col(c)) {
			cs.inc_tr("M");
			cs=get_node(cs.nextState("M"));
		    } else {
			cs.inc_tr("I");
			cs=get_node(cs.nextState("I"));
		    }
		} else {	// aa=='-'
		    // Don't emit anything
		    if (align.is_match_col(c)) {
			cs.inc_tr("D");
			cs=get_node(cs.nextState("D"));
		    } // else ignore
		}
		System.out.println(String.format("a[%d][%d]=%c (match=%s)   %s", r, c, aa, align.is_match_col(c), cs));
	    }
	}
	// Need to do something about the end state?

	// Normalize values:
	Iterator it=graph.values().iterator();
	while (it.hasNext()) {
	    ProfileHMM_Node s=(ProfileHMM_Node)it.next();
	    System.out.println(s);
	    System.out.println(s.Ebs());
	    s.normalize_trs();
	    s.normalize_ems();
	    System.out.println(s);
	    System.out.println(s.Ebs());
	    System.out.println(" ");
	}
    }


    public static void main(String[] argv) {
	Alignment a=new Alignment("hw2-muscle17.txt.short",8).read();
	System.out.println(a.dump());
	ProfileHMM hmm=new ProfileHMM();
	hmm.train(a);
    }
}