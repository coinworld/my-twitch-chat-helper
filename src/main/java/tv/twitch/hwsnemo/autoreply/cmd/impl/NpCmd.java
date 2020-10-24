package tv.twitch.hwsnemo.autoreply.cmd.impl;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.NotEnabledException;
import tv.twitch.hwsnemo.autoreply.cmd.Check;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;
import tv.twitch.hwsnemo.autoreply.cmd.MsgInfo;
import tv.twitch.hwsnemo.autoreply.osu.SendableException;
import tv.twitch.hwsnemo.autoreply.osu.gosu.NowPlaying;

public class NpCmd implements Cmd {

	public NpCmd() throws NotEnabledException {
		Check.throwOr("enablenpcmd");
	}

	@Override
	public boolean go(MsgInfo inf) {
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

			StringBuilder sb = new StringBuilder(np.getArtist()).append(" - ").append(np.getTitle()).append(" [")
					.append(np.getDifficulty()).append("] (by ").append(np.getMapper()).append(" | ")
					.append(np.getFullSR()).append('*');
			if (np.getMinBPM() > 0 && np.getMaxBPM() > 0) {
				if (np.getMinBPM() == np.getMaxBPM()) {
					sb.append(" | ").append(np.getMinBPM()).append("bpm");
				} else {
					sb.append(" | BPM: ").append(np.getMinBPM()).append('-').append(np.getMaxBPM());
				}
			}
			sb.append(") osu.ppy.sh/s/").append(np.getSet());

			Chat.send(sb.toString());
			return true;
		}
		return false;
	}

}
