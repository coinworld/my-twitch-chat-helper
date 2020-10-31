package tv.twitch.hwsnemo.autoreply.cmd;

public abstract class CmdInfo {

	private final String cmd;
	private final String arg;

	public CmdInfo(String[] sp) {
		this.cmd = sp[0];
		if (sp.length == 2)
			this.arg = sp[1];
		else
			this.arg = null;
	}

	public abstract boolean chkPut(CmdLevel lvl, String maincmd, String... ailas);

	public String getCmd() {
		return cmd;
	}

	public String getArg() {
		return arg;
	}

	public abstract String getSender();

	public abstract void send(String msg);
}