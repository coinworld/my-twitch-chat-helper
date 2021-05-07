package tv.twitch.hwsnemo.autoreply.cmd;

import tv.twitch.hwsnemo.autoreply.ChatLevel;
import tv.twitch.hwsnemo.autoreply.Main;

public class ConsoleCmdInfo extends CmdInfo {
	public ConsoleCmdInfo(String[] sp) {
		super(sp);
	}

	@Override
	public boolean chkPut(ChatLevel lvl, String maincmd, String... ailas) {
		boolean right = maincmd.equalsIgnoreCase(getCmd());
		if (!right) {
			for (String c : ailas) {
				if (c.equalsIgnoreCase(getCmd())) {
					right = true;
					break;
				}
			}
		}
		return right;
	}

	@Override
	public String getSender() {
		return "CON";
	}

	@Override
	public void send(String msg) {
		Main.write("BOT> " + msg);
	}

}
