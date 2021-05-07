package tv.twitch.hwsnemo.autoreply;

import java.util.Iterator;

import javax.net.ssl.SSLSocketFactory;

import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.MessageEvent;

import tv.twitch.hwsnemo.autoreply.cmd.ChatCmdInfo;
import tv.twitch.hwsnemo.autoreply.cmd.CmdInfo;
import tv.twitch.hwsnemo.autoreply.cmd.CmdProcessor;
import tv.twitch.hwsnemo.autoreply.cmd.ConsoleCmdInfo;
import tv.twitch.hwsnemo.autoreply.suggest.SuggestInfo;
import tv.twitch.hwsnemo.autoreply.suggest.SuggestProcessor;

public class Chat {

	private static class Listener extends ListenerAdapter {
		private CmdProcessor cmds;
		private SuggestProcessor suggs;

		private final boolean log = MainConfig.isYes("enablechatlog");

		@Override
		public void onConnect(ConnectEvent event) throws Exception {
			cmds = new CmdProcessor();
			suggs = new SuggestProcessor();

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
				String c = startNext();
				try {
					if (c == null) c = Main.readLine();
					if (c.equals("`")) {
						if (!SuggestProcessor.isEmpty()) {
							SuggestProcessor.run();
							Main.write("Action done.");
							continue;
						}
					} else if (c.startsWith("!")) {
						String[] sp = c.split(" ", 2);
						CmdInfo ci = new ConsoleCmdInfo(sp);
						if (loopCmd(ci))
							continue;
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
			return cmds.loop(inf);
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
				if (suggs.loop(new SuggestInfo(event))) {
					Main.write("##" + event.getUser().getLogin() + ": " + msg + " | " + SuggestProcessor.reason());
				}
				// there is a possibility that message can be displayed later than the prior
				// message if finding suggestions takes long because pircbotx is async
				else if (log) {
					Main.write(event.getUser().getLogin() + ": " + msg);
				}
			}
		}
	}
	
	private static Iterator<String> startcmd = null;
	
	private static String startNext() {
		if (startcmd != null && startcmd.hasNext())
			return startcmd.next();
		return null;
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
		startcmd = ConfigFile.getLines("startup.txt").iterator();

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
