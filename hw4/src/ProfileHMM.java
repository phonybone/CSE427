import java.util.*;
import java.io.*;
import java.lang.Math.*;

class ProfileHMM {
    public HashMap<String,ProfileHMM_Node> graph; // Graph structure
    public int length;				  // equal to the number of Match states; aka alignment.n_match_cols

    // length is the length of strings this PHMM can profile; 
    public ProfileHMM() {
	this.graph=new HashMap<String,ProfileHMM_Node>();
    }

    public void initialize(Alignment a) {
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
	this.initialize(align);
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
	    //System.out.println(s);
	    //System.out.println(s.Ebs());
	    s.normalize_trs();
	    s.normalize_ems(bps);
	    //System.out.println(s);
	    //System.out.println(s.Ebs());
	    //System.out.println(" ");
	}
    }

    double viterbi(String path, BackgroundProbs bps) {
	// Allocate arrays.  
	// Major index: We need one more element in each because we're 1-based
	// Minor index: Again, 1-based.
	double[][] Vm=new double[this.length+1][path.length()+1];
	double[][] Vi=new double[this.length+1][path.length()+1];
	double[][] Vd=new double[this.length+1][path.length()+1];
	double[] Vend=new double[path.length()+1];
	double[] Vbegin=new double[path.length()+1];;

	// Establish basis:
	for (int j=0; j<this.length; j++) {
	    Vm[j][0]=Double.NaN;
	    Vi[j][0]=Double.NEGATIVE_INFINITY;
	    Vd[j][0]=Double.NaN;

	    Vm[j][1]=Double.NEGATIVE_INFINITY;
	    Vi[j][1]=Double.NEGATIVE_INFINITY;
	    Vd[j][1]=Double.NEGATIVE_INFINITY;
	}

	// begin and end states:
	for (int i=0; i<=path.length(); i++) {
	    Vbegin[i]=Double.NEGATIVE_INFINITY;	// Vbegin gets set to 0, below
	    Vend[i]=Double.NEGATIVE_INFINITY; // these will get overwritten, later
	}
	Vbegin[0]=0;

	
	// Special recurrences that depend on start state: M1, I0, D1
	// fixme: double-check this shit
	for (int i=1; i<=path.length(); i++) { 
	    char Xi=path.charAt(i-1); // strings are still 0-based
	    Vi[0][i]=log2(get_node("I0").get_em(Xi) / bps.pr(Xi)) + 
		max2(Vbegin[i-1] + log2(get_node("begin").get_tr("I")),
		     Vi[0][i-1]  + log2(get_node("I0").get_tr("I")));

	    Vm[1][i]=log2( get_node("M1").get_em(path.charAt(i-1))) + 
		max2( Vbegin[0] + log2(get_node("begin").get_tr("M1")),
		      Vi[0][i] +  log2(get_node("I0").get_tr("M1"))); 
	    
	    Vd[1][i]=max2(Vbegin[0] + get_node("begin").get_tr("D"),
			  Vi[0][i] + get_node("I0").get_tr("D"));
	}
		      
	

	// Basic recurrence:
	for (int j=1; j<this.length; j++) {
	    for (int i=1; i<=path.length(); i++) { // fixme: check indexing for i, given 1-based; 
		char Xi=path.charAt(i-1); // strings are still 0-based
		double eMj=get_node("M"+String.valueOf(j)).get_em(Xi);
		double qXi=bps.pr(Xi);

		Vm[j][i]=log2(eMj/qXi) + 
		    max3(Vm[j-1][i-1]+log2(get_node("M"+String.valueOf(j-1)).get_tr("M")),
			 Vi[j-1][i-1]+log2(get_node("I"+String.valueOf(j-1)).get_tr("M")),
			 Vd[j-1][i-1]+log2(get_node("D"+String.valueOf(j-1)).get_tr("M")));

		double eIj=get_node("I"+String.valueOf(j)).get_em(Xi);
		Vi[j][i]=log2(eIj/qXi) + 
		    max3(Vm[j-1][i-1]+log2(get_node("M"+String.valueOf(j-1)).get_tr("I")),
			 Vm[j-1][i-1]+log2(get_node("I"+String.valueOf(j-1)).get_tr("I")),
			 Vm[j-1][i-1]+log2(get_node("D"+String.valueOf(j-1)).get_tr("I")));

		Vd[j][i]=
		    max3(Vm[j-1][i-1]+log2(get_node("M"+String.valueOf(j-1)).get_tr("D")),
			 Vm[j-1][i-1]+log2(get_node("I"+String.valueOf(j-1)).get_tr("D")),
			 Vm[j-1][i-1]+log2(get_node("D"+String.valueOf(j-1)).get_tr("D")));

	    }

	    // Final recurrence: Vend[j]
	    
	}

	return Vend[path.length()];	// fixme; check index
    }

    public String dump_double2d(double[][] Vm) {
	StringBuffer buf=new StringBuffer();
	for (int i=0; i<Vm.length; i++) {
	    for (int j=0; j<Vm[i].length; j++) {
		buf.append(String.format("%5.3f ", Vm[i][j]));
	    }
	    buf.append("\n");
	}
	return new String(buf);
    }

    private double log2bE=Math.log(2.0);
    double log2(double d) { return Math.log(d)/log2bE; }
    double max2(double d1, double d2) { (d1>d2? d1:d2); } // yes, I know, it's probably defined in Double
    double max3(double d1, double d2, double d3) { return d1>d2? (d1>d3? d1:d3) : (d2>d3? d2:d3); }


////////////////////////////////////////////////////////////////////////

    public static void main(String[] argv) {
	Alignment a=new Alignment("hw2-muscle17.txt.veryshort",8).read();
	System.out.println(a);
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