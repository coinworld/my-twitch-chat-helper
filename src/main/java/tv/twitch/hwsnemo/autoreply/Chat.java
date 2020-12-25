package tv.twitch.hwsnemo.autoreply;

import java.util.List;

import javax.net.ssl.SSLSocketFactory;

import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.MessageEvent;

import tv.twitch.hwsnemo.autoreply.cmd.ChatCmdInfo;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdInfo;
import tv.twitch.hwsnemo.autoreply.cmd.ConsoleCmdInfo;
import tv.twitch.hwsnemo.autoreply.suggest.Suggest;
import tv.twitch.hwsnemo.autoreply.suggest.SuggestAction;

public class Chat {

	private static class Listener extends ListenerAdapter {
		private final List<Cmd> cmds = DefaultConstructors.createCmds();

		private final List<Suggest> sugg = DefaultConstructors.createSuggs();
		private final boolean log = MainConfig.isYes("enablechatlog");
		private volatile SuggestAction act = null;

		@Override
		public void onConnect(ConnectEvent event) throws Exception {
			// Main.revert();
			// do not let System.out.println be used
			
			ChatCmdInfo.setCooldown(MainConfig.getLong("cmdcooldown", 3000));

			Main.write("Connected. Ctrl+C to exit.");
			PircBotX bot = event.getBot();

			bot.sendRaw().rawLineNow("CAP REQ :twitch.tv/commands");
			bot.sendRaw().rawLineNow("CAP REQ :twitch.tv/tags");
			bot.sendRaw().rawLineNow("CAP REQ :twitch.tv/membership");
			bot.sendRaw().rawLineNow("CAP REQ :twitch.tv/tags twitch.tv/commands");

			while (true) {
				String c = "";
				try {
					c = Main.readLine();
					if (c.equals("`")) {
						if (act != null) {
							act.run();
							act = null;
							Main.write("Action done.");
							continue;
						}
					} else if (c.startsWith("!")) {
						String[] sp = c.split(" ", 2);
						CmdInfo ci = new ConsoleCmdInfo(sp);
						if (loopCmd(ci)) continue;
					}
					if (!c.isEmpty()) {
						bot.sendIRC().message(Chat.getDefCh(), c);
						Main.write("* " + Chat.getName() + ": " + c);
					}
				} catch (UserInterruptException e) {
					break;
				} catch (EndOfFileException e) {
					break;
				}
			}
			bot.stopBotReconnect();
			bot.close();
		}

		private boolean loopCmd(CmdInfo inf) {
			for (Cmd cmd : cmds) {
				if (cmd.go(inf)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void onMessage(MessageEvent event) throws Exception {
			String msg = event.getMessage();

			if (msg.startsWith("!")) {
				Main.write(event.getUser().getLogin() + "> " + msg);
				String[] sp = msg.split(" ", 2);
				CmdInfo inf = new ChatCmdInfo(sp, event);
				loopCmd(inf);
			} else {
				for (Suggest entry : sugg) {
					SuggestAction sa = entry.hit(event.getUser().getLogin(), msg);
					if (sa != null) {
						this.act = sa;
						Main.writeWarn(event.getUser().getLogin() + ": " + msg + " | " + sa.reason());
						break;
					}
				} // there is a possibility that message can be displayed later than the prior
					// message if finding suggestions takes long because pircbotx is async
				if (act == null && log) {
					Main.write(event.getUser().getLogin() + ": " + msg);
				}
			}
		}
	}

	private static final String SERVER = "irc.chat.twitch.tv";
	private static final int PORT = 6697;
	private static String name = null;
	private static String defch = null;

	private static PircBotX bot = null;

	private static String prefix = MainConfig.getString("chatprefix", "");

	protected static void create(String name, String auth, String defch) throws Exception {
		Chat.name = name;
		Chat.defch = defch;

		bot = new PircBotX(new Configuration.Builder().addServer(SERVER, PORT)
				.setSocketFactory(SSLSocketFactory.getDefault()).setName(name).setServerPassword(auth)
				.addAutoJoinChannel(defch).addListener(new Listener()).buildConfiguration());
	}

	public static PircBotX getBot() {
		return bot;
	}

	public static String getDefCh() {
		return defch;
	}

	public static String getName() {
		return name;
	}

	public static void send(String msg) {
		getBot().sendIRC().message(getDefCh(), ((msg.startsWith("/") || msg.startsWith("!")) ? "" : prefix) + msg);
		Main.write("<SEND> " + msg);
	}
}
