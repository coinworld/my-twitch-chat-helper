package tv.twitch.hwsnemo.autoreply.cmd.impl;

import java.util.Random;

import org.pircbotx.hooks.events.MessageEvent;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.Check;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;

public class MiscCmd implements Cmd {

	@Override
	public boolean go(String[] sp, MessageEvent event) {
		if (Check.andPut(sp[0], event, CmdLevel.VIP, "!roll")) {
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
