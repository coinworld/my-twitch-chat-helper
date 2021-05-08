package tv.twitch.hwsnemo.autoreply.cmd.impl;

import tv.twitch.hwsnemo.autoreply.ChatLevel;
import tv.twitch.hwsnemo.autoreply.Main;
import tv.twitch.hwsnemo.autoreply.MainConfig;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdInfo;
import tv.twitch.hwsnemo.autoreply.osu.SendableException;
import tv.twitch.hwsnemo.autoreply.osu.gosu.NowPlaying;

public class NpCmd implements Cmd {

	private String format = getFormat(MainConfig.getString("npformat",
			"{artist} - {song} [{difficulty}] +{mods} (by {mapper} | {sr}* | {bpm}) {url}"));

	private static String getFormat(String form) {
		return form.replace("{artist}", "%1$s").replace("{song}", "%2$s").replace("{difficulty}", "%3$s")
				.replace("{mapper}", "%4$s").replace("{sr}", "%5$.2f").replace("{bpm}", "%6$s")
				.replace("{setid}", "%7$d").replace("{beatmapid}", "%8$d").replace("{mods}", "%9$s")
				.replace("{url}", "%10$s");
	}

	private static String format(NowPlaying np, String format) {
		String bpm;
		if (np.getMinBPM() == np.getMaxBPM()) {
			bpm = np.getMinBPM() + "bpm";
		} else {
			bpm = "BPM: " + np.getMinBPM() + "-" + np.getMaxBPM();
		}
		String url = "<no url available>";
		String path = setorid(np);
		if (path != null) {
			url = "osu.ppy.sh/" + path;
		}
		return String.format(format, np.getArtist(), np.getTitle(), np.getDifficulty(), np.getMapper(), np.getFullSR(),
				bpm, np.getSet(), np.getId(), np.getMods(), url);
	}
	
	private static String setorid(NowPlaying np) {
		if (np.getSet() > 0) {
			return "s/" + np.getSet();
		} else if (np.getId() > 0) {
			return "b/" + np.getId();
		}
		return null;
	}
	
	private volatile NowPlaying np = null;
	
	// %1$d : set id | %2$d : beatmap id
	private static final String CHIMU = "https://api.chimu.moe/v1/download/%1$d?n=1";
	private static final String NERINA = "https://nerina.pw/d/%1$d";
	private static final String SAYOBOT = "https://osu.sayobot.cn/osu.php?s=%1$d";

	@Override
	public boolean go(CmdInfo inf) {
		String mirroraddr = null;
		if (inf.chkPut(ChatLevel.NORMAL, "!np", "!nowplaying", "!map", "!song")) {
			try {
				np = NowPlaying.get();
			} catch (SendableException e) {
				inf.send(e.getMessage());
				return true;
			} catch (Exception e) {
				Main.writeWarn(
						"You must run gosumemory to use !np command. If it is running, please report this issue.");
				inf.send("An unknown exception occurred.");
				return true;
			}

			inf.send(format(np, format));
			return true;
		} else if (inf.chkPut(ChatLevel.NORMAL, "!bloodcat", "!chimu")) {
			mirroraddr = CHIMU;
		} else if (inf.chkPut(ChatLevel.NORMAL, "!nerina")) {
			mirroraddr = NERINA;
		} else if (inf.chkPut(ChatLevel.NORMAL, "!sayobot")) {
			mirroraddr = SAYOBOT;
		}
		if (mirroraddr != null) {
			if (np != null) {
				inf.send(String.format(mirroraddr, np.getSet(), np.getId()));
			} else {
				inf.send("You need to use !np in prior to use this command.");
			}
			return true;
		}
		return false;
	}

}
