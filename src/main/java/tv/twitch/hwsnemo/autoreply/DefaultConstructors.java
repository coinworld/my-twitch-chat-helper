package tv.twitch.hwsnemo.autoreply;

import java.util.ArrayList;
import java.util.List;

import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.impl.MatchCmd;
import tv.twitch.hwsnemo.autoreply.cmd.impl.MiscCmd;
import tv.twitch.hwsnemo.autoreply.cmd.impl.NpCmd;
import tv.twitch.hwsnemo.autoreply.cmd.impl.TimeCmd;
import tv.twitch.hwsnemo.autoreply.suggest.Suggest;
import tv.twitch.hwsnemo.autoreply.suggest.impl.LanguageDetect;
import tv.twitch.hwsnemo.autoreply.suggest.impl.LinkDetector;
import tv.twitch.hwsnemo.autoreply.suggest.impl.PredictAnswer;

public class DefaultConstructors {
	private static List<Construction<? extends Cmd>> cmdcons = new ArrayList<>();
	private static List<Construction<? extends Suggest>> suggcons = new ArrayList<>();

	static {
		cmdcons.add(MatchCmd::new);
		cmdcons.add(MiscCmd::new);
		cmdcons.add(NpCmd::new);
		cmdcons.add(TimeCmd::new);

		suggcons.add(LanguageDetect::new);
		suggcons.add(LinkDetector::new);
		suggcons.add(PredictAnswer::new);
	}

	public static List<Cmd> createCmds() {
		List<Cmd> cmds = new ArrayList<>();
		for (Construction<? extends Cmd> c : cmdcons) {
			try {
				cmds.add(c.construct());
			} catch (NotEnabledException e) {

			} catch (Exception e) {
				Main.write("Error while creating cmd instances: " + e.getMessage());
			}
		}
		return cmds;
	}

	public static List<Suggest> createSuggs() {
		List<Suggest> suggs = new ArrayList<>();
		for (Construction<? extends Suggest> s : suggcons) {
			try {
				suggs.add(s.construct());
			} catch (NotEnabledException e) {

			} catch (Exception e) {
				Main.write("Error while creating suggesting instances: " + e.getMessage());
			}
		}
		return suggs;
	}
}
