import double_helpers.DH;
import java.util.*;

class Viterbi {
    public ProfileHMM hmm;
    public String prot;
    public String name;		// protein name
    public double score;
    public String path;
    
    protected double[][] Vm;
    protected double[][] Vi;
    protected double[][] Vd;
    protected double[] Vend;
    protected double[] Vbegin;
    public BackgroundProbs bps;


    Viterbi(ProfileHMM hmm, String prot, String name, BackgroundProbs bps) {
	this.hmm=hmm;
	this.prot=prot;

	//System.out.println("prot is "+prot);
	//System.out.println(String.format("allocating %d X %d", hmm.length+1, prot.length()+1));
	this.Vm=new double[hmm.length+1][prot.length()+1];
	this.Vi=new double[hmm.length+1][prot.length()+1];
	this.Vd=new double[hmm.length+1][prot.length()+1];
	this.Vend=new double[prot.length()+2];
	this.Vbegin=new double[prot.length()+1];;
	
	this.bps=bps;
	this.name=name;
    }

    // Run the scoring algorithm.  If score>0, backtrace the path and 
    // create an alignment.
    // Returns a ViterbiResult object.
    public ViterbiResult score() {
	establish_basis();
	special_recurrences();
	basic_recurrence();
	score=final_recurrence();
	String alignment=null;

	if (score>0) {
	    ArrayList<ProfileHMM_Node> state_path=backtrace();
	    path=state_path_to_string(state_path);
	    alignment=align(state_path);
	}
	
	return new ViterbiResult(name, prot, score, path, alignment);
    }

    // Establish basis:
    public void establish_basis() {
	for (int j=0; j<hmm.length; j++) {
	    Vm[j][0]=Double.NEGATIVE_INFINITY;
	    Vi[j][0]=Double.NEGATIVE_INFINITY;
	    Vd[j][0]=Double.NEGATIVE_INFINITY;

	    Vm[j][1]=Double.NEGATIVE_INFINITY;
	    Vi[j][1]=Double.NEGATIVE_INFINITY;
	    Vd[j][1]=Double.NEGATIVE_INFINITY;
	}

	// For states that don't exist, but have a place in the arrays due to 1-based indexing, set their values to NaN:
	for (int i=0; i<prot.length()+1; i++) {
	    Vm[0][i]=Double.NaN;
	    Vd[0][i]=Double.NaN;
	}

	// begin and end states:
	for (int i=0; i<=prot.length(); i++) {
	    Vbegin[i]=Double.NEGATIVE_INFINITY;	// Vbegin gets set to 0, below
	    Vend[i]=Double.NEGATIVE_INFINITY;  // these will get overwritten, later
	}
	Vbegin[0]=0;
	// System.out.println(String.format("%s:\n%s\n", "Vbegin", d1s(Vbegin, "Vbegin")));
    }



	
    // Special recurrences that depend on start state: M1, I0, D1
    // Also, I1, even though it doesn't depend on begin, so that the loop in basic_recurrence can start at 2
    public void special_recurrences() {
	for (int i=1; i<=prot.length(); i++) { 
	    char Xi=prot.charAt(i-1); // strings are still 0-based
	    Vm[1][i]=DH.log2( hmm.get_node("M1").get_em(Xi) / bps.pr(Xi)) + 
		DH.max2( Vbegin[0] + DH.log2(hmm.get_node("begin").get_tr("M1")),
			 Vi[0][i] +  DH.log2(hmm.get_node("I0").get_tr("M1"))); 
	    
	    Vi[0][i]=DH.log2(hmm.get_node("I0").get_em(Xi) / bps.pr(Xi)) + 
		DH.max2(Vbegin[i-1] + DH.log2(hmm.get_node("begin").get_tr("I")),
			Vi[0][i-1]  + DH.log2(hmm.get_node("I0").get_tr("I")));

	    Vi[1][i]=DH.log2(hmm.get_node("I1").get_em(Xi) / bps.pr(Xi)) + 
		DH.max3(Vm[1][i-1] + DH.log2(hmm.get_node("M1").get_tr("I")),
			Vi[1][i-1] + DH.log2(hmm.get_node("I0").get_tr("I")),
			Vd[1][i-1] + DH.log2(hmm.get_node("D1").get_tr("I")));

	    Vd[1][i]=DH.max2(Vbegin[0] + hmm.get_node("begin").get_tr("D"),
			     Vi[0][i] + hmm.get_node("I0").get_tr("D"));
	}

	/* 
	   System.out.println(d1s(Vm[1], "Vm[1]"));
	   System.out.println(d1s(Vi[0], "Vi[0]"));
	   System.out.println(d1s(Vi[1], "Vi[1]"));
	   System.out.println(d1s(Vd[1], "Vd[1]"));
	*/
    }


    // Basic recurrence:
    public void basic_recurrence() {
	for (int i=1; i<=prot.length(); i++) { // fixme: check indexing for i, given 1-based; 
	    char Xi=prot.charAt(i-1); // strings are still 0-based
	    double qXi=bps.pr(Xi);
	    //System.out.println(String.format("\n%c: qXi=%g", Xi, qXi));

	    for (int j=2; j<=hmm.length; j++) {
		double eMj=hmm.get_node("M"+String.valueOf(j)).get_em(Xi);
		double part1=DH.log2(eMj/qXi);

		double fm=Vm[j-1][i-1]+DH.log2(hmm.get_node("M"+String.valueOf(j-1)).get_tr("M"));
		double fi=Vi[j-1][i-1]+DH.log2(hmm.get_node("I"+String.valueOf(j-1)).get_tr("M"));
		double fd=Vd[j-1][i-1]+DH.log2(hmm.get_node("D"+String.valueOf(j-1)).get_tr("M"));

		Vm[j][i]=part1+DH.max3(fm, fi, fd);
		//System.out.println(String.format("Vm[%d][%d]=%g; %g+max3(%g,%g,%g) eMj=%g, qXi=%g", 
		//			 i, j, Vm[j][i], part1, fm, fi, fd, eMj, qXi));

		//System.out.println(String.format("eM[%d](%c)=%g\tVm[%d][%d]=%g", j, Xi, eMj, j, i, Vm[j][i]));

		double eIj=hmm.get_node("I"+String.valueOf(j)).get_em(Xi);
		Vi[j][i]=DH.log2(eIj/qXi) + 
		    DH.max3(Vm[j][i-1]+DH.log2(hmm.get_node("M"+String.valueOf(j-1)).get_tr("I")),
			    Vi[j][i-1]+DH.log2(hmm.get_node("I"+String.valueOf(j-1)).get_tr("I")),
			    Vd[j][i-1]+DH.log2(hmm.get_node("D"+String.valueOf(j-1)).get_tr("I")));

		//System.out.println(String.format("eI[%d](%c)=%g\tVi[%d][%d]=%g", j, Xi, eIj, j, i, Vi[j][i]));

		Vd[j][i]=
		    DH.max3(Vm[j-1][i]+DH.log2(hmm.get_node("M"+String.valueOf(j-1)).get_tr("D")),
			    Vi[j-1][i]+DH.log2(hmm.get_node("I"+String.valueOf(j-1)).get_tr("D")),
			    Vd[j-1][i]+DH.log2(hmm.get_node("D"+String.valueOf(j-1)).get_tr("D")));

		//System.out.println(String.format("Vd[%d][%d]=%g", j, i, Vd[j][i]));

	    }
	}
    }


    // Final recurrence:
    public double final_recurrence() {
	// Final recurrence: Vend[j]
	// I don't think it makes sense to build Vend for all of 0<=i<=L+1, only L+1.  But do it anyway...
	Vend[0]=Double.NaN;
	int L=hmm.length;
	for (int i=1; i<=prot.length()+1; i++) {
	    Vend[i]=DH.max3(Vm[L][i-1] + DH.log2(hmm.get_node("M"+String.valueOf(L)).get_tr("end")),
			    Vi[L][i-1] + DH.log2(hmm.get_node("I"+String.valueOf(L)).get_tr("end")),
			    Vd[L][i-1] + DH.log2(hmm.get_node("D"+String.valueOf(L)).get_tr("end")));
	}

	return Vend[prot.length()+1];	
    }

////////////////////////////////////////////////////////////////////////

    public ArrayList<ProfileHMM_Node> backtrace() {
	ProfileHMM_Node cs=hmm.get_node("end");
	int j=hmm.length;
	int i=prot.length()+1;
	//System.out.println(String.format("\nprot is %s (%d)", prot, prot.length()));
	double epsilon=1E-4;

	ArrayList<ProfileHMM_Node> state_path_rev=new ArrayList<ProfileHMM_Node>();
	state_path_rev.add(hmm.get_node("end"));

	double from_m=Vm[j][i-1] + DH.log2(hmm.get_node("M"+String.valueOf(j)).get_tr("end"));
	double from_i=Vi[j][i-1] + DH.log2(hmm.get_node("I"+String.valueOf(j)).get_tr("end"));
	double from_d=Vd[j][i-1] + DH.log2(hmm.get_node("D"+String.valueOf(j)).get_tr("end"));
	// System.out.println(String.format("from_m = %g\nfrom_i=%g\nfrom_d=%g", from_m, from_i, from_d));

	//System.out.println(String.format("Vend[%d] is %g", i, Vend[i]));
	if        (Math.abs(from_m-Vend[i]) < epsilon) {
	    cs=hmm.get_node("M"+String.valueOf(j));
	} else if (Math.abs(from_i-Vend[i]) < epsilon) {
	    cs=hmm.get_node("I"+String.valueOf(j));
	} else if (Math.abs(from_d-Vend[i]) < epsilon) {
	    cs=hmm.get_node("D"+String.valueOf(j));
	} else {
	    new Die("Can't determine backtrace from end state???");
	}
	//System.out.println(String.format("from Vend, cs is %s", cs.state));
	state_path_rev.add(cs);

	i=prot.length();
	while (j >= 2) {
	    //System.out.println(String.format("\nj=%d, i=%d, cs is %s", j, i, cs.state));
	    char Xi=prot.charAt(i-1); // strings are still 0-based
	    double qXi=bps.pr(Xi);
	    double eMj=hmm.get_node("M"+String.valueOf(j)).get_em(Xi);
	    double eIj=hmm.get_node("I"+String.valueOf(j)).get_em(Xi);
	    //System.out.println(String.format("i=%d, j=%d, eM%d('%c')=%g, eI%d('%c')=%g", i, j, j, Xi, eMj, j, Xi, eIj));

	    // dec i only if we inc'd i during basic recurrence (ie, not a delete state), likewise j
	    if (cs.state_type.equals("M")) {
		from_m = DH.log2(eMj/qXi) + Vm[j-1][i-1] + DH.log2(hmm.get_node("M"+String.valueOf(j-1)).get_tr("M"));
		from_i = DH.log2(eMj/qXi) + Vi[j-1][i-1] + DH.log2(hmm.get_node("I"+String.valueOf(j-1)).get_tr("M"));
		from_d = DH.log2(eMj/qXi) + Vd[j-1][i-1] + DH.log2(hmm.get_node("D"+String.valueOf(j-1)).get_tr("M"));

		if (Math.abs(from_m-Vm[j][i]) < epsilon) {
		    cs=hmm.get_node("M"+String.valueOf(j-1)); j--; i--;
		} 
		else if (Math.abs(from_i-Vm[j][i]) < epsilon) {
		    cs=hmm.get_node("I"+String.valueOf(j-1)); j--; i--;
		} 
		else if (Math.abs(from_d-Vm[j][i]) < epsilon) {
		    cs=hmm.get_node("D"+String.valueOf(j-1)); j--; i--;
		} else {
		    dump(); 
		    System.out.println(String.format("%s: Vm[%d][%d]=%g, from_m = %g, from_i=%g, from_d=%g, log2(eMj/qXi)=%g, log2(eIj/qXi)=%g", 
						     cs.state, j, i, Vm[j][i], from_m, from_i, from_d, DH.log2(eMj/qXi), DH.log2(eIj/qXi)));
		    new Die(String.format("Can't determine backtrace from %s, M", cs.state));
		}

	    } else if (cs.state_type.equals("I")) {
		from_m = DH.log2(eIj/qXi) + Vm[j][i-1] + DH.log2(hmm.get_node("M"+String.valueOf(j-1)).get_tr("I"));
		// next: should be ...(j).get_tr("I")?
		from_i = DH.log2(eIj/qXi) + Vi[j][i-1] + DH.log2(hmm.get_node("I"+String.valueOf(j-1)).get_tr("I")); 
		from_d = DH.log2(eIj/qXi) + Vd[j][i-1] + DH.log2(hmm.get_node("D"+String.valueOf(j-1)).get_tr("I"));

		if (Math.abs(from_m-Vi[j][i]) < epsilon) {
		    cs=hmm.get_node("M"+String.valueOf(j)); i--;
		} 
		else if (Math.abs(from_i-Vi[j][i]) < epsilon) {
		    cs=hmm.get_node("I"+String.valueOf(j)); i--;
		} 
		else if (Math.abs(from_d-Vi[j][i]) < epsilon) {
		    cs=hmm.get_node("D"+String.valueOf(j)); i--;
		} else {
		    dump();
		    System.out.println(String.format("%s: Vm[%d][%d]=%g, from_m = %g, from_i=%g, from_d=%g, log2(eMj/qXi)=%g, log2(eIj/qXi)=%g", 
						     cs.state, j, i, Vm[j][i], from_m, from_i, from_d, DH.log2(eMj/qXi), DH.log2(eIj/qXi)));
		    new Die(String.format("Can't determine backtrace from %s, I", cs.state));
		}

	    } else if (cs.state_type.equals("D")) {
		from_m =                  + Vm[j-1][i] + DH.log2(hmm.get_node("M"+String.valueOf(j-1)).get_tr("D"));
		from_i =                  + Vi[j-1][i] + DH.log2(hmm.get_node("I"+String.valueOf(j-1)).get_tr("D"));
		from_d =                    Vd[j-1][i] + DH.log2(hmm.get_node("D"+String.valueOf(j-1)).get_tr("D"));
		if (cs.state_type.equals("D")) {
		    if (Math.abs(from_m-Vd[j][i]) < epsilon) {
			cs=hmm.get_node("M"+String.valueOf(j-1)); j--;
		    } 
		    else if (Math.abs(from_i-Vd[j][i]) < epsilon) {
			cs=hmm.get_node("I"+String.valueOf(j-1)); j--;
		    } 
		    else if (Math.abs(from_d-Vd[j][i]) < epsilon) {
			cs=hmm.get_node("D"+String.valueOf(j-1)); j--;

		    } else {
			dump();
			System.out.println(String.format("%s: Vm[%d][%d]=%g, from_m = %g, from_i=%g, from_d=%g, log2(eMj/qXi)=%g, log2(eIj/qXi)=%g", 
							 cs.state, j, i, Vm[j][i], from_m, from_i, from_d, DH.log2(eMj/qXi), DH.log2(eIj/qXi)));
			new Die(String.format("Can't determine backtrace from %s, D", cs.state));
		    }
		}

	    } else {
		new Die(String.format("unknown state_type??? %s", cs.state_type));
	    }
	    //System.out.println(String.format("%c: next cs is %s", prot.charAt(i-1), cs.state));
	    state_path_rev.add(cs);

	}
	
	
	//System.out.println(String.format("now what? i=%d, j=%d", i, j));
	// fixme!
	// What you really want is to stay in I0...
	// Except maybe what you really want to do is continue to probe, of the states possible, which you came from
	// D1: possible states are I0 and begin
	// I1: I1, D1, M1
	// M1: I0, begin
	// I0: I0, begin
	while (i>1) {
	    cs=hmm.get_node("I0");
	    state_path_rev.add(cs);
	    System.out.println(String.format("%c: next cs is %s (countdown)", prot.charAt(i-1), cs.state));

	    i--;
	}

	state_path_rev.add(hmm.get_node("begin"));
	ArrayList<ProfileHMM_Node> state_path=new ArrayList<ProfileHMM_Node>();
	for (i=state_path_rev.size()-1; i>=0; i--) {
	    state_path.add(state_path_rev.get(i));
	}
	
	return state_path;
    }


    public String state_path_to_string(ArrayList<ProfileHMM_Node> state_path) {
	StringBuffer buf=new StringBuffer();
	Iterator it=state_path.iterator();
	while (it.hasNext()) {
	    ProfileHMM_Node state=(ProfileHMM_Node)it.next();
	    buf.append(state.state); // er...
	    buf.append(' ');
	}
	return new String(buf);
    }

    public String align(ArrayList<ProfileHMM_Node> state_path) {
	// Reconstruct the alignment
	StringBuffer alignment=new StringBuffer();
	int i=0;
	for (int j=0; j<state_path.size(); j++) {
	    ProfileHMM_Node state=state_path.get(j);
	    if (state.state_type.equals("M") || state.state_type.equals("I")) {
		alignment.append(prot.charAt(i++));
	    } else if (state.state_type.equals("D")) {
		alignment.append('-');
	    }
	}
	for (i=1; i<hmm.alignment.n_cols; i++) {
	    if (! hmm.alignment.is_match_col(i)) {
		if (alignment.length() < hmm.alignment.n_cols) {
		    alignment.insert(i,'-');
		}
	    }
	}

	return new String(alignment);
    }

////////////////////////////////////////////////////////////////////////



    public void dump() {
	System.out.println(String.format("%s:\n%s\n", "Vm",   DH.d2s(Vm, "Vm")));
	System.out.println(String.format("%s:\n%s\n", "Vi",   DH.d2s(Vi, "Vi")));
	System.out.println(String.format("%s:\n%s\n", "Vd",   DH.d2s(Vd, "Vd")));
	System.out.println(String.format("%s:\n%s\n", "Vend", DH.d1s(Vend, "Vend")));
    }

}