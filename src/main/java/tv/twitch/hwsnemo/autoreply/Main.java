package tv.twitch.hwsnemo.autoreply;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

import org.fusesource.jansi.AnsiConsole;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;

import tv.twitch.hwsnemo.autoreply.osu.OsuApi;

public class Main {

	public static final boolean RUN = true;

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

		if (RUN) {
			String oauth = null;
			String osuapi = null;
			String defch = null;
			String twitchname = null;

			Map<String, String> map = ConfigFile.get("config.txt");
			for (String key : map.keySet()) {
				if (key.equals("oauth")) {
					oauth = map.get(key);
				} else if (key.equals("osuapi")) {
					osuapi = map.get(key);
				} else if (key.equals("defch")) {
					defch = map.get(key);
				} else if (key.equals("twitchname")) {
					twitchname = map.get(key);
				}
			}

			if (oauth == null || osuapi == null || defch == null || twitchname == null) {
				write("Please check the config file");
				return;
			}

			write("Loaded the config successfully");

			OsuApi.set(osuapi);

			out = System.out;
			err = System.err;
			PrintStream ps = new PrintStream(OutputStream.nullOutputStream());
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

}
