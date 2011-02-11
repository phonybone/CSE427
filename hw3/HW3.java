import system.*;

class HW3 {
    public static void main(String[] argv) {
	Properties props = System.getProperties();
	for (String key : props.keys()) {
	    String value=props.get(key);
	    System.out.printf("%s=%s\n", key, value);
	}
    }
}