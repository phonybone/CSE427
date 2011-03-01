class HW4 {
    public static void main(String[] argv) {
	readAlignment();
    }

    public static void readAlignment() {
	Alignment alignment=new Alignment("hw2-muscle17.txt",8).read();
	System.out.println(alignment);

	
    }

}

