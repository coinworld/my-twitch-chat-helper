package tv.twitch.hwsnemo.autoreply.cmd.impl;

import java.util.Random;

import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdInfo;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;

public class MiscCmd implements Cmd {

	@Override
	public boolean go(CmdInfo inf) {
		if (inf.chkPut(CmdLevel.VIP, "!roll")) {
			int num = 100;
			if (inf.getArg() != null) {
				num = Integer.parseInt(inf.getArg());
			}

			Random rnd = new Random();
			int res = rnd.nextInt(num) + 1;

			inf.send(inf.getSender() + " got " + res);
			return true;
		}
		return false;
	}

}
