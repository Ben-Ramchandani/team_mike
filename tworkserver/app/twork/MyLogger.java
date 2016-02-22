package twork;

public class MyLogger {
	
	public static boolean enable = true;
	
	public static void log(String s) {
		if(enable) {
			System.out.println(s);
		}
	}
	
	public static void alwaysLog(String s) {
		System.out.println(s);
	}
	
	public static void info(String s) {
		log("[info] " + s);
	}
	
	public static void warn(String s) {
		log("[warn] " + s);
	}
	
	public static void fatal(String s) {
		alwaysLog("[FATAL] " + s);
	}
	
	public static void critical(String s) {
		alwaysLog("[CRITICAL] - The system may be in an invalid state - " + s);
	}
}
