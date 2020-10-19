package tv.twitch.hwsnemo.autoreply.cmd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.pircbotx.hooks.events.MessageEvent;

public class Check {

	private static final Map<String, Long> lastcmd = new ConcurrentHashMap<>();

	public static boolean andPut(String input, MessageEvent event, CmdLevel lvl, String maincmd, String... cmd) {
		boolean right = maincmd.equalsIgnoreCase(input);
		if (!right) {
			for (String c : cmd) {
				if (c.equalsIgnoreCase(input)) {
					right = true;
					break;
				}
			}
		}
		
		if (right && (!isUsedRecently(maincmd) || CmdLevel.MOD.check(event)) && lvl.check(event)) {
			lastcmd.put(maincmd, System.currentTimeMillis());
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
