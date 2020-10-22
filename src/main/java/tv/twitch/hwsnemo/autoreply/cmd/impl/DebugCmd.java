package tv.twitch.hwsnemo.autoreply.cmd.impl;

import org.pircbotx.hooks.events.MessageEvent;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.cmd.Check;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;
import tv.twitch.hwsnemo.autoreply.osu.SendableException;

public class DebugCmd implements Cmd {

	@Override
	public boolean go(String[] sp, MessageEvent event) {
		if (Check.andPut(sp[0], event, CmdLevel.MOD, "!debug")) {
			Chat.send(SendableException.getDebugMessage());
			return true;
		}
		return false;
	}

}
