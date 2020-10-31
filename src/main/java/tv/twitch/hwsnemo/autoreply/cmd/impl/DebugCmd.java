package tv.twitch.hwsnemo.autoreply.cmd.impl;

import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdInfo;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;
import tv.twitch.hwsnemo.autoreply.osu.SendableException;

public class DebugCmd implements Cmd {

	@Override
	public boolean go(CmdInfo inf) {
		if (inf.chkPut(CmdLevel.MOD, "!debug")) {
			inf.send(SendableException.getDebugMessage());
			return true;
		}
		return false;
	}

}
