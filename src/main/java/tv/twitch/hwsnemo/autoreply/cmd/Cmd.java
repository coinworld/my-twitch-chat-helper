package tv.twitch.hwsnemo.autoreply.cmd;

import org.pircbotx.hooks.events.MessageEvent;

public interface Cmd {
	public boolean go(String[] sp, MessageEvent event);
}
