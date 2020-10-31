package tv.twitch.hwsnemo.autoreply.cmd.impl;

import tv.twitch.hwsnemo.autoreply.Main;
import tv.twitch.hwsnemo.autoreply.NotEnabledException;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdInfo;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;
import tv.twitch.hwsnemo.autoreply.osu.SendableException;
import tv.twitch.hwsnemo.autoreply.osu.gosu.NowPlaying;

public class NpCmd implements Cmd {

	private static String format = "%1$s - %2$s [%3$s] +%9$s (by %4$s | %5$f* | %6$s) osu.ppy.sh/s/%7$d";

	// "{artist} - {song} [{difficulty}] (by {mapper} | {sr}* | {bpm})
	// osu.ppy.sh/s/{setid}" {beatmapid} - 8 {mods} - 9

	public static void setFormat(String format) {
		NpCmd.format = format.replace("{artist}", "%1$s").replace("{song}", "%2$s").replace("{difficulty}", "%3$s")
				.replace("{mapper}", "%4$s").replace("{sr}", "%5$.2f").replace("{bpm}", "%6$s").replace("{setid}", "%7$d")
				.replace("{beatmapid}", "%8$d").replace("{mods}", "%9$s");
	}

	private static String format(NowPlaying np) {
		String bpm;
		if (np.getMinBPM() == np.getMaxBPM()) {
			bpm = np.getMinBPM() + "bpm";
		} else {
			bpm = "BPM: " + np.getMinBPM() + "-" + np.getMaxBPM();
		}
		return String.format(format, np.getArtist(), np.getTitle(), np.getDifficulty(), np.getMapper(), np.getFullSR(),
				bpm, np.getSet(), np.getId(), np.getMods());
	}

	public NpCmd() throws NotEnabledException {
		Main.throwOr("enablenpcmd");

		if (Main.getConfig().containsKey("npformat")) {
			setFormat(Main.getConfig().get("npformat"));
		}
	}

	@Override
	public boolean go(CmdInfo inf) {
		if (inf.chkPut(CmdLevel.NORMAL, "!np", "!nowplaying", "!map", "!song")) {
			NowPlaying np;
			try {
				np = NowPlaying.get();
			} catch (SendableException e) {
				inf.send(e.getMessage());
				return true;
			} catch (Exception e) {
				inf.send("An unknown exception occurred.");
				return true;
			}

			inf.send(format(np));
			return true;
		}
		return false;
	}

}
