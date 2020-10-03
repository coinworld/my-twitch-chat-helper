package tv.twitch.hwsnemo.autoreply.cmd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.pircbotx.hooks.events.MessageEvent;

public class CmdHistory {

	private static final Map<String, Long> lastcmd = new ConcurrentHashMap<>();

	public static boolean checkAndPut(String input, MessageEvent event, String cmd, CmdLevel lvl) {
		if (cmd.equalsIgnoreCase(input) && (!isUsedRecently(cmd) || lvl.getLevel() > 1) && lvl.check(event)) {
			lastcmd.put(cmd, System.currentTimeMillis());
			return true;
		}
		return false;
	}

	private static boolean isUsedRecently(String cmd) {
		if (lastcmd.containsKey(cmd)) {
			long diff = System.currentTimeMillis() - lastcmd.get(cmd);
			return diff < 3000L;
		}
		return false;
	}
}
