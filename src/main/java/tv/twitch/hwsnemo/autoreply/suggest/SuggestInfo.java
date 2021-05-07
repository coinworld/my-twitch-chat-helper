package tv.twitch.hwsnemo.autoreply.suggest;

import org.pircbotx.hooks.events.MessageEvent;

import tv.twitch.hwsnemo.autoreply.ChatLevel;

public class SuggestInfo {
	private final MessageEvent event;
	
	public SuggestInfo(MessageEvent event) {
		this.event = event;
	}
	
	public String getSender() {
		return event.getUser().getNick();
	}
	
	public String getMsg() {
		return event.getMessage();
	}
	
	public boolean isMod() {
		return ChatLevel.MOD.check(event);
	}
	
	public boolean isVIP() {
		return ChatLevel.VIP.check(event);
	}
}
