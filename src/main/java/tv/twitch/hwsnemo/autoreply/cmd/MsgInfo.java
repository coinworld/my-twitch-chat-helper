package tv.twitch.hwsnemo.autoreply.cmd;

import org.pircbotx.hooks.events.MessageEvent;

public class MsgInfo {
	private final String cmd;
	private final String arg;
	private final MessageEvent event;

	public MsgInfo(String[] sp, MessageEvent event) {
		this.cmd = sp[0];
		if (sp.length == 2)
			this.arg = sp[1];
		else
			this.arg = null;
		this.event = event;
	}

	public boolean chkPut(CmdLevel lvl, String maincmd, String... ailas) {
		return Check.andPut(cmd, event, lvl, maincmd, ailas);
	}

	public String getCmd() {
		return cmd;
	}

	public String getArg() {
		return arg;
	}
}
