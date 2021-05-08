package tv.twitch.hwsnemo.autoreply.cmd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.pircbotx.hooks.events.MessageEvent;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.ChatLevel;
import tv.twitch.hwsnemo.autoreply.MainConfig;

public class ChatCmdInfo extends CmdInfo {
	private final MessageEvent event;

	public ChatCmdInfo(String[] sp, MessageEvent event) {
		super(sp);
		this.event = event;
	}

	@Override
	public boolean chkPut(ChatLevel lvl, String maincmd, String... ailas) {
		return checkPut(getCmd(), event, lvl, maincmd, ailas);
	}
	
	@Override
	public String getSender() {
		return event.getUser().getNick();
	}
	
	private static final Map<String, Long> lastcmd = new ConcurrentHashMap<>();

	private static boolean checkPut(String input, MessageEvent event, ChatLevel lvl, String maincmd, String... cmd) {
		boolean right = maincmd.equalsIgnoreCase(input);
		if (!right) {
			for (String c : cmd) {
				if (c.equalsIgnoreCase(input)) {
					right = true;
					break;
				}
			}
		}

		if (right && (!isUsedRecently(maincmd) || ChatLevel.MOD.check(event)) && lvl.check(event)) {
			lastcmd.put(maincmd, System.currentTimeMillis());
			return true;
		}
		return false;
	}

	private static long cooldown = MainConfig.getLong("cmdcooldown", 3000);

	private static boolean isUsedRecently(String cmd) {
		if (lastcmd.containsKey(cmd)) {
			long diff = System.currentTimeMillis() - lastcmd.get(cmd);
			return diff < cooldown;
		}
		return false;
	}

	@Override
	public void send(String msg) {
		Chat.send(msg);
	}
}
