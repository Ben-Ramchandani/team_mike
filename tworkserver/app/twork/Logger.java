package twork;

public class Logger {
	
	public static boolean enable = true;
	
	public static void log(String s) {
		if(enable) {
			System.out.println(s);
		}
	}
}
