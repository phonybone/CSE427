
public class TestBlosum62 {
    public static void main(String argv[]) {
	Blosum62 b62=new Blosum62();

	char a,b;
	int s,S;

	a='A'; b='A'; s=4;              	S=b62.sigma(a,b);
	if (S != s) { 
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: s="+String.valueOf(s)+", S="+String.valueOf(S)); 
	} else {
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: ok"); 
	}

	a='K'; b='C'; s=-3;              	S=b62.sigma(a,b);
	if (S != s) { 
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: s="+String.valueOf(s)+", S="+String.valueOf(S)); 
	} else {
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: ok"); 
	}


	a='G'; b='K'; s=-2;              	S=b62.sigma(a,b);
	if (S != s) { 
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: s="+String.valueOf(s)+", S="+String.valueOf(S)); 
	} else {
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: ok"); 
	}


	a='S'; b='V'; s=-2;              	S=b62.sigma(a,b);
	if (S != s) { 
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: s="+String.valueOf(s)+", S="+String.valueOf(S)); 
	} else {
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: ok"); 
	}


	a='H'; b='B'; s=0;              	S=b62.sigma(a,b);
	if (S != s) { 
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: s="+String.valueOf(s)+", S="+String.valueOf(S)); 
	} else {
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: ok"); 
	}


	a='N'; b='V'; s=-3;              	S=b62.sigma(a,b);
	if (S != s) { 
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: s="+String.valueOf(s)+", S="+String.valueOf(S)); 
	} else {
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: ok"); 
	}


	a='K'; b='Q'; s=1;              	S=b62.sigma(a,b);
	if (S != s) { 
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: s="+String.valueOf(s)+", S="+String.valueOf(S)); 
	} else {
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: ok"); 
	}


	a='L'; b='P'; s=-3;              	S=b62.sigma(a,b);
	if (S != s) { 
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: s="+String.valueOf(s)+", S="+String.valueOf(S)); 
	} else {
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: ok"); 
	}


	a='E'; b='C'; s=-4;              	S=b62.sigma(a,b);
	if (S != s) { 
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: s="+String.valueOf(s)+", S="+String.valueOf(S)); 
	} else {
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: ok"); 
	}


	a='H'; b='L'; s=-3;              	S=b62.sigma(a,b);
	if (S != s) { 
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: s="+String.valueOf(s)+", S="+String.valueOf(S)); 
	} else {
	    System.out.println("["+String.valueOf(a)+"]["+String.valueOf(b)+"]: ok"); 
	}


	System.out.println("Done");
    }
}