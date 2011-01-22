import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.IOException;

class ProtStream {
    public BufferedReader reader;
    public String next_prot_name;
    public String this_prot_name;
    public StringBuffer this_prot;

    ProtStream(String prot_file) {
	try {
	    FileInputStream fis=new FileInputStream(prot_file);
	    reader=new BufferedReader(new InputStreamReader(fis));
	    this_prot_name=reader.readLine().trim();
	    this_prot=new StringBuffer();
	    
	    String line=reader.readLine().trim();
	    if (line==null) {
		System.err.println(prot_file+": error getting first line of first protein???");
		System.exit(1);
	    }
	    while (! line.startsWith(">")) {
		this.this_prot.append(line);
		line=reader.readLine();
	    }
	    this.next_prot_name=line;

	} catch (IOException ioe) {
	    System.err.println(ioe.getMessage());
	    System.exit(1);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    public void dump(PrintStream s) {
	s.println("this_prot_name: "+this_prot_name);
	s.println("next_prot_name: "+next_prot_name);
	s.println("this_prot: "+this_prot);
    }


    public String prot_name() {
	return this.this_prot_name;
    }

    public String next() {
	return this.pump_buffer();
    }

    private String pump_buffer() {
	//	this.this_prot_name=this.next_prot_name;
	if (this.next_prot_name==null) return null;

	String prot=new String(this.this_prot);
	this.this_prot=new StringBuffer("");

	String line;
	try {
	    while ((line=this.reader.readLine())!=null) {
		line=line.trim();
		if (line.startsWith(">")) {
		    this.next_prot_name=line;
		    break;
		} else {
		    this.this_prot.append(line);
		}
	    }
	    if (line==null) {
		next_prot_name=null;
	    }
	} catch (IOException e) {
	    return null;
	}
	return prot;
    }


}