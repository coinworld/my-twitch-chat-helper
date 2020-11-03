package tv.twitch.hwsnemo.autoreply.cmd;

import org.pircbotx.hooks.events.MessageEvent;

public enum CmdLevel {
	NORMAL(1, ""), VIP(2, "vip"), MOD(3, "moderator"), BROADCASTER(4, "broadcaster");

	private final int LEVEL;
	private final String badge;

	private CmdLevel(int level, String badge) {
		this.LEVEL = level;
		this.badge = badge;
	}
	
	public boolean check(MessageEvent event) {
		String badge = event.getV3Tags().get("badges");
		for (CmdLevel lvl : CmdLevel.values()) {
			if (this.getLevel() <= lvl.getLevel()) {
				if (badge.contains(lvl.getBadgeString())) {
					return true;
				}
			}
		}
		return false;
	}

	public int getLevel() {
		return LEVEL;
	}

	public String getBadgeString() {
		return badge;
	}
}
