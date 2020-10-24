package tv.twitch.hwsnemo.autoreply.cmd.impl;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;
import tv.twitch.hwsnemo.autoreply.cmd.MsgInfo;
import tv.twitch.hwsnemo.autoreply.osu.SendableException;

public class DebugCmd implements Cmd {

	@Override
	public boolean go(MsgInfo inf) {
		if (inf.chkPut(CmdLevel.MOD, "!debug")) {
			Chat.send(SendableException.getDebugMessage());
			return true;
		}
		return false;
	}

}
