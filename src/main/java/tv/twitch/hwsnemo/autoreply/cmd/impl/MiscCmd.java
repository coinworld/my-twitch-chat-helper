package tv.twitch.hwsnemo.autoreply.cmd.impl;

import java.util.Random;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.Main;
import tv.twitch.hwsnemo.autoreply.NotEnabledException;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;
import tv.twitch.hwsnemo.autoreply.cmd.CmdInfo;

public class MiscCmd implements Cmd {

	public MiscCmd() throws NotEnabledException {
		Main.throwOr("enablemisccmd");
	}

	@Override
	public boolean go(CmdInfo inf) {
		if (inf.chkPut(CmdLevel.VIP, "!roll")) {
			int num = 100;
			if (inf.getArg() != null) {
				num = Integer.parseInt(inf.getArg());
			}

			Random rnd = new Random();
			int res = rnd.nextInt(num) + 1;

			Chat.send("You got " + res + " peepoGlad");
			return true;
		}
		return false;
	}

}
