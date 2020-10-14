package tv.twitch.hwsnemo.autoreply.cmd.impl;

import org.pircbotx.hooks.events.MessageEvent;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.cmd.Check;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;
import tv.twitch.hwsnemo.autoreply.osu.SendableException;
import tv.twitch.hwsnemo.autoreply.osu.gosu.NowPlaying;

public class NpCmd implements Cmd {

	@Override
	public boolean go(String[] sp, MessageEvent event) {
		if (Check.andPut(sp[0], event, CmdLevel.NORMAL, "!np", "!nowplaying")) {
			NowPlaying np;
			try {
				np = NowPlaying.get();
			} catch (SendableException e) {
				Chat.send(e.getMessage());
				return true;
			} catch (Exception e) {
				Chat.send("An unknown exception occurred.");
				return true;
			}
			
			Chat.send(String.format("%s - %s [%s] (by %s) osu.ppy.sh/s/%d", np.getArtist(), np.getTitle(), np.getDifficulty(), np.getMapper(), np.getSet()));
			return true;
		}
		return false;
	}

}
