package tv.twitch.hwsnemo.autoreply.osu;

public class SendableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static String debug = "Not yet.";

	public SendableException(String string) {
		super(string);
	}
	
	public SendableException(String string, String debugmsg) {
		this(string);
		debug = debugmsg;
	}
	
	public static String getDebugMessage() {
		return debug;
	}

}
