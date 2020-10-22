package tv.twitch.hwsnemo.autoreply.cmd;

import org.pircbotx.hooks.events.MessageEvent;

public enum CmdLevel {
	NORMAL(1), VIP(2), MOD(3);

	private final int LEVEL;

	private CmdLevel(int level) {
		this.LEVEL = level;
	}

	public boolean check(MessageEvent event) {
		if (this == NORMAL) {
			return true;
		}
		String badge = event.getV3Tags().get("badges");
		if (this == VIP) {
			return badge.contains("vip") || MOD.check(event);
		} else if (this == MOD) {
			return badge.contains("moderator") || badge.contains("broadcaster");
		}
		return false;
	} // maybe i can make this method abstract and let these enums implement this but
		// yeah

	public int getLevel() {
		return LEVEL;
	}
}
