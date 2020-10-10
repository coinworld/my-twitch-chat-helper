package tv.twitch.hwsnemo.autoreply.cmd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.pircbotx.hooks.events.MessageEvent;

public class CmdHistory {

	private static final Map<String, Long> lastcmd = new ConcurrentHashMap<>();

	public static boolean checkAndPut(String input, MessageEvent event, CmdLevel lvl, String... cmd) {
		for (String c : cmd) {
			if (c.equalsIgnoreCase(input)) {
				if ((!isUsedRecently(cmd[0]) || lvl.getLevel() > 2) && lvl.check(event)) {
					lastcmd.put(cmd[0], System.currentTimeMillis());
					return true;
				}
				break;
			}
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
