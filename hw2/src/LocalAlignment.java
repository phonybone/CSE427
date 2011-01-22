import java.lang.String;

class LocalAlignment {
    private String query;
    private String subject;
    private int max_score, max_i, max_j;

    public LocalAlignment() {
    }

    private int[][] matrix;

    public int[][] matrix() { return matrix; } // not going to worry about callers writing into this for the moment.

    public int score(String Q, String S) {
	int Ql=Q.length();
	int Sl=S.length();
	Blosum62 b62=new Blosum62();

	int v[][]=new int[Sl+1][Ql+1];

	// init two borders:
	for (int s=0; s<=Sl; s++) { v[s][0]=0; }
	for (int q=0; q<=Ql; q++) { v[0][q]=0; }

	int max_score=0;
	int max_i=0, max_j=0;

	for (int i=1; i<=Sl; i++) {
	    for (int j=1; j<=Ql; j++) {

		char s=S.charAt(i-1);
		char q=Q.charAt(j-1);

		int v_ij=v[i-1][j-1] + b62.sigma(s,q);

		int b=v[i-1][j] + b62.sigma(s,'-');
		if (b>v_ij) { v_ij=b; }

		int c=v[i][j-1] + b62.sigma('-',q);
		if (c>v_ij) { v_ij=c; }
		
		v[i][j]=v_ij;
		if (v_ij>max_score) {
		    max_score=v_ij;
		    max_i=i;
		    max_j=j;
		}
	    }
	}

	// preserve info for possible traceback:
	this.query=Q;
	this.subject=S;
	this.matrix=v;
	this.max_score=max_score;
	this.max_i=max_i;
	this.max_j=max_j;
	return max_score;
    }

    
    // return a 3-line string describing the alignment found above:
    public String traceback() {
	int[][] v=this.matrix;
	int i=this.max_i;	// indexes s
	int j=this.max_j;	// indexes q
	String S=this.subject;
	String Q=this.query;

	StringBuffer S_align=new StringBuffer();
	StringBuffer Q_align=new StringBuffer();

	Blosum62 b62=new Blosum62();

	while (v[i][j] > 0) {
	    
	    // Determine next i,j
	    int next_i=i;	// java warnings barf if not initialized; 
	    int next_j=j;

	    char s=S.charAt(i-1);
	    char q=Q.charAt(j-1);

	    // Try diagonal first:
	    if (v[i][j] == v[i-1][j-1] + b62.sigma(s,q)) {
		next_i=i-1;
		next_j=j-1;
		S_align.append(s);
		Q_align.append(q);

	    } else if (v[i][j] == v[i-1][j] + b62.sigma(s,'-')) {
		next_i=i-1;
		next_j=j;
		S_align.append(s);
		Q_align.append('-');

	    } else if (v[i][j] == v[i][j-1] + b62.sigma('-',q)) {
		next_i=i;
		next_j=j-1;
		S_align.append('-');
		Q_align.append(q);

	    } else {
		System.err.println("Something's wrong at v["+String.valueOf(i)+"]["+String.valueOf(j)+"]");
		System.exit(1);
	    }
	    i=next_i;
	    j=next_j;
	}

	S_align=S_align.reverse();
	Q_align=Q_align.reverse();
	return new String(Q_align+"\n"+S_align+"\n");
    }
}