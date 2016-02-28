package sitehelper;

public abstract class Metadata {
	private static int numberDevices = 0;
	public static int getNumberDevices() {
		return numberDevices;
	}
	
	public static synchronized void increaseNumberDevices() {
		numberDevices++;
	}
	
	public static synchronized void decreaseNumberDevices() {
		if (numberDevices > 0) numberDevices--;
	}
	
}