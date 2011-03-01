import java.util.regex.*;

class ProfileHMM_Node {
    public String state;	// must match /^[MID]\d+$/, 'begin', 'end', or I0
    public String state_type;	// must be one of M, I, D, begin, end
    public int state_num;	// must be >0, unless state_type=="I"

    public double to_m;
    public double to_i;
    public double to_d;

    public double Eb[];	   // emission probabilities out of this state
    private final int Eb_size=26;
    public  final int n_aas=20;
    private final String skip_aas="[BJOUXZ]";

    private String valid_statename="^[MID]\\d+|begin|end$"; 


    public ProfileHMM_Node(String state) 
	throws ProfileHMM_BadStateException 
    {
	if (state.matches(valid_statename)) {
	    this.state=state;
	    if (state.matches("[MID]\\d+")) {
		this.state_type=state.substring(0,1);
		this.state_num=Integer.valueOf(state.substring(1)).intValue();
	    } else {
		state_type=state;
	    }
	    this.to_m=1.0/3.0;	// 1/3 is prior probability
	    this.to_i=1.0/3.0;
	    this.to_d=1.0/3.0;
	    
	    this.Eb=new double[Eb_size];
	    for (int i=0; i<Eb_size; i++) { this.Eb[i]=1.0/n_aas; } // set prior probability

	} else {
	    // throw exception?
	    throw new ProfileHMM_BadStateException("Bad state name: "+state);
	} 

    }

    public String toString() {
	return String.format("%s (%d): %s=%6.4f %s=%6.4f %s=%6.4f", state, state_num,
			     nextState("M"), to_m, nextState("I"), to_i, nextState("D"), to_d);
    }
    
    public String Ebs() {
	StringBuffer buf=new StringBuffer();
	for (int i=0; i<Eb_size; i++) {
	    int aa='A'+i;
	    char[] aaa=new char[1]; aaa[0]=(char)aa; // String(char) doesn't exist, but String(char[]) does
	    if (new String(aaa).matches(skip_aas)) continue;
	    buf.append(String.format("%c: %.3f ", aa, this.Eb[i]));
	}
	return new String(buf);
    }



    // Generate the name of the kth state following this state, taking into account
    // the structure of the HMM.  This routine is used as a self-check and in toString.
    public String nextState(String k_type) {
	if (k_type.equals("I")) {
	    return String.format("I%d",state_num);
	} else {
	    return String.format("%s%d", k_type, state_num+1);
	}
	
    }


    // 
    public void set_tr(String k, double d) throws ProfileHMM_BadStateException {
	
    }

    public double get_tr(String k) throws ProfileHMM_BadStateException {
	String k_type=k.substring(0,1);
	if      (k_type.equals("M")) return to_m;
	else if (k_type.equals("I")) return to_i;
	else if (k_type.equals("D")) return to_d;
	else throw new ProfileHMM_BadStateException(k);
    }

    public boolean valid_state(String j) {
	return true;
    }


    // Training methods:
    public void inc_tr(String j, double d) {
	if      (j.equals("M")) this.to_m+=d;
	else if (j.equals("I")) this.to_i+=d;
	else if (j.equals("D")) this.to_d+=d;
	else throw new ProfileHMM_BadStateException(j);
    }
    public void inc_tr(String j) { inc_tr(j,1); }

    public void normalize_trs() {
	double sum=to_m+to_i+to_d;
	to_m /= sum;
	to_i /= sum;
	to_d /= sum;
    }

    public void inc_em(char aa, double d) {
	int index=aa-'A';
	Eb[index]+=d;
    }
    public void inc_em(char aa) { inc_em(aa,1); }

    public void normalize_ems() {
	double sum=0;
	for (int i=0; i<n_aas; i++) {sum+=Eb[i];}

	// have to subtract out the elements that were never indexed:
	sum -= (Eb_size-n_aas)/n_aas; // should be 6/20
	for (int i=0; i<n_aas; i++) {Eb[i] /= sum;}
    }

    public static void main(String[] argv) {
	ProfileHMM_Node n=new ProfileHMM_Node("M13");
	n.inc_tr("M");
	n.inc_tr("D");
	n.inc_tr("M");
	n.inc_tr("I");
	n.inc_tr("I");
	n.inc_tr("D");
	n.inc_tr("D");
	n.inc_tr("M");
	n.inc_tr("M");
	n.inc_tr("I");
	n.inc_tr("D");
	System.out.println(String.format("n is %s",n));
 
	System.out.println(String.format("next M state: %s", n.nextState("M")));
	System.out.println(String.format("next I state: %s", n.nextState("I")));
	System.out.println(String.format("next D state: %s", n.nextState("D")));
   }
    
}