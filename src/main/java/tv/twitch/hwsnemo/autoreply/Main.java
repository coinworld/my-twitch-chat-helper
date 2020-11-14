package tv.twitch.hwsnemo.autoreply;

import java.io.OutputStream;
import java.io.PrintStream;

import org.fusesource.jansi.AnsiConsole;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;

import tv.twitch.hwsnemo.autoreply.osu.OsuApi;

public class Main {

	public static final boolean RUN = true; // debug stuff

	private static LineReader reader;
	private static Terminal term;
	private static volatile boolean read = false;

	private static final String prompt = "> ";

	private static PrintStream out;

	private static PrintStream err;

	public static void main(String[] args) throws Exception {
		AnsiConsole.systemInstall();

		reader = LineReaderBuilder.builder().build();
		term = reader.getTerminal();
		if (args.length != 0) {
			MainConfig.setConfigName(args[0]);
		}

		if (RUN && MainConfig.getConfig() != null) {
			String oauth = null;
			String osuapi = null;
			String defch = null;
			String twitchname = null;

			for (String key : MainConfig.getConfig().keySet()) {
				if (key.equals("oauth")) {
					oauth = MainConfig.getConfig().get(key);
				} else if (key.equals("osuapi")) {
					osuapi = MainConfig.getConfig().get(key);
				} else if (key.equals("defch")) {
					defch = MainConfig.getConfig().get(key);
				} else if (key.equals("twitchname")) {
					twitchname = MainConfig.getConfig().get(key);
				}
			}

			if (oauth == null || defch == null || twitchname == null) {
				write("Please check the config file");
				return;
			}

			OsuApi.set(osuapi);

			out = System.out;
			err = System.err;
			PrintStream ps = new PrintStream(new OutputStream() {
				@Override
				public void write(int b) {
				}
			});
			System.setOut(ps);
			System.setErr(ps); // i don't want to see slf4j errors.

			Chat.create(twitchname, oauth, defch);
			Chat.getBot().startBot();
		} else {
		}
	}

	public static String readLine() {
		read = true;
		String r = reader.readLine(prompt);
		read = false;
		return r;
	}

	public static void revert() {
		System.setOut(Main.out);
		System.setErr(Main.err);
	}

	public static void write(String m) {
		if (!read) {
			term.writer().println(m);
			return;
		}
		reader.callWidget(LineReader.CLEAR);
		term.writer().println(m);
		reader.callWidget(LineReader.REDRAW_LINE);
		reader.callWidget(LineReader.REDISPLAY);
		term.writer().flush();
	}

	public static void writeWarn(String m) {
		write("\033[93;1m" + m + "\033[0m");
	}

	public static void throwOr(String key) throws NotEnabledException {
		if (!MainConfig.isYes(key)) {
			throw new NotEnabledException();
		}
	}

}
