package tv.twitch.hwsnemo.autoreply;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.MessageEvent;

import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.impl.MatchCmd;
import tv.twitch.hwsnemo.autoreply.cmd.impl.MiscCmd;
import tv.twitch.hwsnemo.autoreply.cmd.impl.NpCmd;
import tv.twitch.hwsnemo.autoreply.cmd.impl.TimeCmd;
import tv.twitch.hwsnemo.autoreply.suggest.Suggest;
import tv.twitch.hwsnemo.autoreply.suggest.SuggestAction;

public class Chat {

	private static class Listener extends ListenerAdapter {
		private final List<Cmd> cmds = new ArrayList<>();

		private final List<Suggest> sugg = new ArrayList<>();
		private volatile SuggestAction act = null;

		@Override
		public void onConnect(ConnectEvent event) throws Exception {
			Main.revert();

			Main.write("Connected. Ctrl+C to exit.");
			PircBotX bot = event.getBot();

			bot.sendRaw().rawLineNow("CAP REQ :twitch.tv/commands");
			bot.sendRaw().rawLineNow("CAP REQ :twitch.tv/tags");
			bot.sendRaw().rawLineNow("CAP REQ :twitch.tv/membership");
			bot.sendRaw().rawLineNow("CAP REQ :twitch.tv/tags twitch.tv/commands");

			// bot.sendIRC().message(Chat.getDefCh(), "Bot is now connected.");
			Map<String, String> conf = Main.getConfig();
			if (!conf.containsKey("enablegosu") || conf.get("enablegosu").equals("yes")) {
				cmds.add(new NpCmd());
			}
			if (!(!conf.containsKey("onlynp") || conf.get("onlynp").equals("yes"))) {
				cmds.add(new MatchCmd());
				cmds.add(new MiscCmd());
				cmds.add(new TimeCmd());
			}

			// sugg.add(new PredictAnswer());
			// sugg.add(new LinkDetector());
			// sugg.add(new LanguageDetect());

			while (true) {
				String c = "";
				try {
					c = Main.readLine();
					if (c.equals("`")) {
						if (act != null) {
							act.run();
							act = null;
							Main.write("Action done.");
						}
					} else if (!c.isEmpty()) {
						bot.sendIRC().message(Chat.getDefCh(), c);
						Main.write(Chat.getName() + ": " + c);
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

		@Override
		public void onMessage(MessageEvent event) throws Exception {
			String msg = event.getMessage();

			if (msg.startsWith("!")) {
				Main.write(event.getUser().getLogin() + "> " + msg);
				String[] sp = msg.split(" ", 2);
				for (Cmd cmd : cmds) {
					if (cmd.go(sp, event)) {
						break;
					}
				}
			} else {
				for (Suggest entry : sugg) {
					SuggestAction sa = entry.hit(event.getUser().getLogin(), msg);
					if (sa != null) {
						this.act = sa;
						Main.writeWarn(event.getUser().getLogin() + ": " + msg + " | " + sa.reason());
						break;
					}
				} // there is a possibility that message can be displayed later than the prior
					// message because pircbotx is async
				if (act == null) {
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
		getBot().sendIRC().message(getDefCh(), ((msg.startsWith("/") || msg.startsWith("!")) ? "" : "[BOT] ") + msg);
		Main.write("<SEND> " + msg);
	}
}
