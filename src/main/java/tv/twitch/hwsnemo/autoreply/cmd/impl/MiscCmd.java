package tv.twitch.hwsnemo.autoreply.cmd.impl;

import java.util.Random;

import org.pircbotx.hooks.events.MessageEvent;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdHistory;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;

public class MiscCmd implements Cmd {

	@Override
	public boolean go(String[] sp, MessageEvent event) {
		if (CmdHistory.checkAndPut(sp[0], event, "!roll", CmdLevel.VIP)) {
			int num = 100;
			if (sp.length != 1) {
				num = Integer.parseInt(sp[1]);
			}

			Random rnd = new Random();
			int res = rnd.nextInt(num) + 1;

			Chat.send("You got " + res + " peepoGlad");
			return true;
		}
		return false;
	}

}
