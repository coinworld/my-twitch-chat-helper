package tv.twitch.hwsnemo.autoreply.cmd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.pircbotx.hooks.events.MessageEvent;

public class CmdInfo {
	private final String cmd;
	private final String arg;
	private final MessageEvent event;

	public CmdInfo(String[] sp, MessageEvent event) {
		this.cmd = sp[0];
		if (sp.length == 2)
			this.arg = sp[1];
		else
			this.arg = null;
		this.event = event;
	}

	public boolean chkPut(CmdLevel lvl, String maincmd, String... ailas) {
		return checkPut(cmd, event, lvl, maincmd, ailas);
	}

	public String getCmd() {
		return cmd;
	}

	public String getArg() {
		return arg;
	}
	
	public String getSender() {
		return event.getUser().getNick();
	}
	
	private static final Map<String, Long> lastcmd = new ConcurrentHashMap<>();

	private static boolean checkPut(String input, MessageEvent event, CmdLevel lvl, String maincmd, String... cmd) {
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

	private static long cooldown = 3000L;

	public static void setCooldown(long time) {
		cooldown = time;
	}

	private static boolean isUsedRecently(String cmd) {
		if (lastcmd.containsKey(cmd)) {
			long diff = System.currentTimeMillis() - lastcmd.get(cmd);
			return diff < cooldown;
		}
		return false;
	}
}
