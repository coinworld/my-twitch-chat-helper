package tv.twitch.hwsnemo.autoreply.cmd.impl;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.Main;
import tv.twitch.hwsnemo.autoreply.NotEnabledException;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;
import tv.twitch.hwsnemo.autoreply.cmd.CmdInfo;
import tv.twitch.hwsnemo.autoreply.osu.SendableException;
import tv.twitch.hwsnemo.autoreply.osu.gosu.NowPlaying;

public class NpCmd implements Cmd {

	private static String format = "{artist} - {song} [{difficulty}] (by {mapper} | {sr}* | {bpm}) osu.ppy.sh/s/{setid}";

	public static void setFormat(String format) {
		NpCmd.format = format;
	}

	private static String format(NowPlaying np) {
		String bpm;
		if (np.getMinBPM() == np.getMaxBPM()) {
			bpm = np.getMinBPM() + "bpm";
		} else {
			bpm = "BPM: " + np.getMinBPM() + "-" + np.getMaxBPM();
		}
		return format.replace("{artist}", np.getArtist()).replace("{song}", np.getTitle())
				.replace("{difficulty}", np.getDifficulty()).replace("{mapper}", np.getMapper())
				.replace("{sr}", np.getFullSR() + "").replace("{bpm}", bpm).replace("{setid}", np.getSet() + "")
				.replace("{beatmapid}", np.getId() + "");
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
				Chat.send(e.getMessage());
				return true;
			} catch (Exception e) {
				Chat.send("An unknown exception occurred.");
				return true;
			}

			Chat.send(format(np));
			return true;
		}
		return false;
	}

}
