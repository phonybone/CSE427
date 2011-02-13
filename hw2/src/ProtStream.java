import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.IOException;

class ProtStream {
    public BufferedReader reader;
    public String last_prot_name;
    public String next_prot_name;
    public String this_prot_name;
    public String this_prot;
    public StringBuffer next_prot;

    ProtStream(String prot_file) {
	try {
	    FileInputStream fis=new FileInputStream(prot_file);
	    reader=new BufferedReader(new InputStreamReader(fis));
	    this_prot_name=reader.readLine().trim();
	    next_prot=new StringBuffer();
	    
	    String line=reader.readLine().trim();
	    if (line==null) {
		System.err.println(prot_file+": error getting first line of first protein???");
		System.exit(1);
	    }
	    while (line!=null && !line.startsWith(">")) {
		next_prot.append(line);
		line=reader.readLine();
	    }
	    next_prot_name=line;

	} catch (IOException ioe) {
	    System.err.println(ioe.getMessage());
	    System.exit(1);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    public void dump(PrintStream s) {
	s.println("last_prot_name: "+last_prot_name);
	s.println("this_prot_name: "+this_prot_name);
	s.println("next_prot_name: "+next_prot_name);
	s.println("this_prot: "+this_prot);
    }


    public String prot_name() {
	return last_prot_name;
    }

    public String next() {
	return pump_buffer();
    }

    private String pump_buffer() {
	if (this_prot_name==null) {
	    //	    System.out.println("this_prot_name is null, returning null");
	    return null;
	}
	last_prot_name=this_prot_name;
	this_prot_name=next_prot_name;
	this_prot=new String(next_prot);

	next_prot=new StringBuffer("");

	String line;
	try {
	    while ((line=reader.readLine())!=null) {
		line=line.trim();
		if (line.startsWith(">")) {
		    next_prot_name=line;
		    break;
		} else {
		    next_prot.append(line);
		}
	    }
	    if (line==null) {
		//		System.out.println("line is null, returning null");
		next_prot_name=null;
	    }
	} catch (IOException e) {
	    return null;
	}
	return this_prot;
    }


}