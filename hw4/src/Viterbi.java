import double_helpers.DH;
import java.util.*;

class Viterbi {
    public ProfileHMM hmm;
    public String path;
    public double score;
    
    protected double[][] Vm;
    protected double[][] Vi;
    protected double[][] Vd;
    protected double[] Vend;
    protected double[] Vbegin;
    public BackgroundProbs bps;


    Viterbi(ProfileHMM hmm, String path, BackgroundProbs bps) {
	this.hmm=hmm;
	this.path=path;

	this.Vm=new double[hmm.length+1][path.length()+1];
	this.Vi=new double[hmm.length+1][path.length()+1];
	this.Vd=new double[hmm.length+1][path.length()+1];
	this.Vend=new double[path.length()+2];
	this.Vbegin=new double[path.length()+1];;
	
	this.bps=bps;
    }

    public double score() {
	establish_basis();
	special_recurrences();
	basic_recurrence();
	score=final_recurrence();
	return score;
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
	for (int i=0; i<path.length()+1; i++) {
	    Vm[0][i]=Double.NaN;
	    Vd[0][i]=Double.NaN;
	}

	// begin and end states:
	for (int i=0; i<=path.length(); i++) {
	    Vbegin[i]=Double.NEGATIVE_INFINITY;	// Vbegin gets set to 0, below
	    Vend[i]=Double.NEGATIVE_INFINITY; // these will get overwritten, later
	}
	Vbegin[0]=0;
	// System.out.println(String.format("%s:\n%s\n", "Vbegin", d1s(Vbegin, "Vbegin")));
    }



	
    // Special recurrences that depend on start state: M1, I0, D1
    // Also, I1, even though it doesn't depend on begin, so that the loop in basic_recurrence can start at 2
    public void special_recurrences() {
	for (int i=1; i<=path.length(); i++) { 
	    char Xi=path.charAt(i-1); // strings are still 0-based
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
	for (int i=1; i<=path.length(); i++) { // fixme: check indexing for i, given 1-based; 
	    char Xi=path.charAt(i-1); // strings are still 0-based
	    double qXi=bps.pr(Xi);
	    //System.out.println(String.format("\n%c: qXi=%g", Xi, qXi));

	    for (int j=2; j<=hmm.length; j++) {
		double eMj=hmm.get_node("M"+String.valueOf(j)).get_em(Xi);
		Vm[j][i]=DH.log2(eMj/qXi) + 
		    DH.max3(Vm[j-1][i-1]+DH.log2(hmm.get_node("M"+String.valueOf(j-1)).get_tr("M")),
			    Vi[j-1][i-1]+DH.log2(hmm.get_node("I"+String.valueOf(j-1)).get_tr("M")),
			    Vd[j-1][i-1]+DH.log2(hmm.get_node("D"+String.valueOf(j-1)).get_tr("M")));

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
	for (int i=1; i<=path.length()+1; i++) {
	    Vend[i]=DH.max3(Vm[L][i-1] + DH.log2(hmm.get_node("M"+String.valueOf(L)).get_tr("end")),
			    Vi[L][i-1] + DH.log2(hmm.get_node("I"+String.valueOf(L)).get_tr("end")),
			    Vd[L][i-1] + DH.log2(hmm.get_node("D"+String.valueOf(L)).get_tr("end")));
	}

	return Vend[path.length()+1];	
    }

////////////////////////////////////////////////////////////////////////

    public String backtrace() {
	ProfileHMM_Node cs=hmm.get_node("end");
	int j=hmm.length;
	int i=path.length()+1;
	// System.out.println(String.format("path is %s (%d)", path, path.length()));
	double epsilon=1E-4;
	StringBuffer buf=new StringBuffer();

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

	i=path.length();
	while (j >= 2) {
	    //System.out.println(String.format("\nj=%d, i=%d, cs is %s", j, i, cs.state));
	    char Xi=path.charAt(i-1); // strings are still 0-based
	    double qXi=bps.pr(Xi);
	    double eMj=hmm.get_node("M"+String.valueOf(j)).get_em(Xi);
	    double eIj=hmm.get_node("I"+String.valueOf(j)).get_em(Xi);
	    //System.out.println(String.format("i=%d, j=%d, eM%d('%c')=%g, eI%d('%c')=%g", i, j, j, Xi, eMj, j, Xi, eIj));

	    // dec i only if we inc'd i during basic recurrence (ie, not a delete state), likewise j
	    if (cs.state_type.equals("M")) {
		from_m = DH.log2(eMj/qXi) + Vm[j-1][i-1] + DH.log2(hmm.get_node("M"+String.valueOf(j-1)).get_tr("M"));
		from_i = DH.log2(eMj/qXi) + Vi[j-1][i-1] + DH.log2(hmm.get_node("I"+String.valueOf(j-1)).get_tr("M"));
		from_d = DH.log2(eMj/qXi) + Vm[j-1][i-1] + DH.log2(hmm.get_node("D"+String.valueOf(j-1)).get_tr("M"));

		if (Math.abs(from_m-Vm[j][i]) < epsilon) {
		    cs=hmm.get_node("M"+String.valueOf(j-1)); j--; i--;
		} 
		else if (Math.abs(from_i-Vm[j][i]) < epsilon) {
		    cs=hmm.get_node("I"+String.valueOf(j-1)); j--; i--;
		} 
		else if (Math.abs(from_i-Vm[j][i]) < epsilon) {
		    cs=hmm.get_node("D"+String.valueOf(j-1)); j--; i--;
		} else {
		    dump(); System.out.println(String.format("M: Vm[%d][%d]=%g, from_m = %g, from_i=%g, from_d=%g", 
							     j, i, Vm[j][i], from_m, from_i, from_d));
		    new Die(String.format("Can't determine backtrace from %s, M", cs.state));
		}
		buf.append(hmm.alignment.is_match_col(j)? Xi:"!");

	    } else if (cs.state_type.equals("I")) {
		from_m = DH.log2(eIj/qXi) + Vm[j][i-1] + DH.log2(hmm.get_node("M"+String.valueOf(j-1)).get_tr("I"));
		// next: should be ...(j).get_tr("I")?
		from_i = DH.log2(eIj/qXi) + Vi[j][i-1] + DH.log2(hmm.get_node("I"+String.valueOf(j-1)).get_tr("I")); 
		from_d = DH.log2(eIj/qXi) + Vd[j][i-1] + DH.log2(hmm.get_node("D"+String.valueOf(j-1)).get_tr("I"));

		if (Math.abs(from_m-Vi[j][i]) < epsilon) {
		    cs=hmm.get_node("M"+String.valueOf(j-1)); i--;
		} 
		else if (Math.abs(from_i-Vi[j][i]) < epsilon) {
		    cs=hmm.get_node("I"+String.valueOf(j)); i--;
		} 
		else if (Math.abs(from_d-Vi[j][i]) < epsilon) {
		    cs=hmm.get_node("D"+String.valueOf(j-1)); i--;
		} else {
		    dump();
		    System.out.println(String.format("I: Vi[%d][%d]=%g, (%g) from_m = %g, from_i=%g, from_d=%g", 
						     j, i, Vi[j][i], DH.log2(eIj/qXi), from_m, from_i, from_d));
		    new Die(String.format("Can't determine backtrace from %s, I", cs.state));
		}
		buf.append(hmm.alignment.is_match_col(j)? Xi:"?");

	    } else if (cs.state_type.equals("D")) {
		from_m =                    Vm[j-1][i] + DH.log2(hmm.get_node("M"+String.valueOf(j-1)).get_tr("D"));
		from_i =                    Vi[j-1][i] + DH.log2(hmm.get_node("I"+String.valueOf(j-1)).get_tr("D"));
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
			System.out.println(String.format("D: Vd[%d][%d]=%g, from_m = %g, from_i=%g, from_d=%g", 
							 j, i, Vd[j][i], from_m, from_i, from_d));
			new Die(String.format("Can't determine backtrace from %s, D", cs.state));
		    }
		}

		String s=hmm.alignment.is_match_col(j)? "-":"";
		buf.append(s);

	    } else {
		new Die(String.format("unknown state_type??? %s", cs.state_type));
	    }
	}
	
	//System.out.println(String.format("now what? i=%d, j=%d", i, j));
	while (i>0) {
	    cs=hmm.get_node("I"+String.valueOf(i-1));
	    //System.out.println(String.format("%c: next cs is %s (countdown)", path.charAt(i-1), cs.state));
	    buf.append(path.charAt(i-1));
	    i--;
	}
	

	return new String(buf.reverse());
    }

////////////////////////////////////////////////////////////////////////



    public void dump() {
	System.out.println(String.format("%s:\n%s\n", "Vm",   DH.d2s(Vm, "Vm")));
	System.out.println(String.format("%s:\n%s\n", "Vi",   DH.d2s(Vi, "Vi")));
	System.out.println(String.format("%s:\n%s\n", "Vd",   DH.d2s(Vd, "Vd")));
	System.out.println(String.format("%s:\n%s\n", "Vend", DH.d1s(Vend, "Vend")));
    }

}