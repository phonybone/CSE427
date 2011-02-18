class Die {
    Die(Throwable t, String msg) {
	if (msg!=null) System.err.println(msg);
	System.err.println(t.getMessage());
	t.printStackTrace(System.err);
	System.exit(1);
    }
    
    Die(Throwable t) {
	new Die(t,null);
    }

    Die(String msg) {
	System.err.println(msg);
	System.exit(1);
    }
}