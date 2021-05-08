package tv.twitch.hwsnemo.autoreply.cmd.impl;

import java.util.List;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.ChatLevel;
import tv.twitch.hwsnemo.autoreply.Main;
import tv.twitch.hwsnemo.autoreply.MainConfig;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdInfo;
import tv.twitch.hwsnemo.autoreply.osu.Match;
import tv.twitch.hwsnemo.autoreply.osu.Match.Names;
import tv.twitch.hwsnemo.autoreply.osu.OsuApi;
import tv.twitch.hwsnemo.autoreply.osu.SendableException;
import tv.twitch.hwsnemo.autoreply.osu.TextFileWrite;
import tv.twitch.hwsnemo.autoreply.osu.result.H2H;
import tv.twitch.hwsnemo.autoreply.osu.result.Result;
import tv.twitch.hwsnemo.autoreply.osu.result.TeamVS;

public class MatchCmd implements Cmd {

	private boolean overlay = MainConfig.isYes("textscore");

	private interface AutoRun<T extends Result> {
		void go(T result);
	}

	private class AutoThread<T extends Result> extends Thread {
		private AutoThread(AutoRun<T> run, Class<T> clazz, MatchCmd mc) {
			super(() -> {
				boolean autoscorechat = MainConfig.isYes("autoscorechat");
				
				try {
					while (mc.ongoing) {
						List<Result> res = mc.m.getNow();
						if (!res.isEmpty()) {
							for (Result r : res) {
								if (r.isDraw())
									continue;
								if (clazz.isInstance(r))
									run.go(clazz.cast(r));
							}
							if (autoscorechat)
								Chat.send("Auto: " + mc.getScore());
							mc.updateOverlay();
						}
						Thread.sleep(5000L);
					}
				} catch (SendableException e) {
					Chat.send("Track Aborted: " + e.getMessage());
				} catch (Exception e) {
					Chat.send("An unknown exception occurred. Please tell the bot runner to check log.");
					Main.writeWarn("Match Tracking Exception: " + e.getMessage());
				}
				// mc.reset();
			});
		}
	}

	{
		reset();
	}

	private String ourname;
	private volatile int ourscore;
	private int oursetscore;

	private String oppname;
	private volatile int oppscore;
	private int oppsetscore;

	private String desc;

	private volatile boolean ongoing;

	private int set;

	private boolean isblue;

	private Match m;
	private TextFileWrite fw;

	private void reset() {
		ourname = "Our team";
		ourscore = 0;
		oursetscore = 0;
		oppname = "Opponent";
		oppscore = 0;
		oppsetscore = 0;
		desc = "No info yet.";
		ongoing = false;
		isblue = true;
		m = null;
		set = -1;

		if (fw != null)
			fw.write(getOverlayScore());
		fw = null;
	}

	private String setformat = getScoreFormat(MainConfig.getString("setscoreformat",
			"{ourname} ({oursetscore}) | {ourscore} - {oppscore} | ({oppsetscore}) {oppname}"));

	private String scoreformat = getScoreFormat(
			MainConfig.getString("scoreformat", "{ourname} | {ourscore} - {oppscore} | {oppname}"));

	private String getScore() {
		if (set > 0 || (oursetscore > 0 || oppsetscore > 0)) {
			return String.format(setformat, ourname, ourscore, oppscore, oppname, desc, oursetscore, oppsetscore);
		}
		return String.format(scoreformat, ourname, ourscore, oppscore, oppname, desc);
	}

	private String overlaysetformat = getScoreFormat(MainConfig.getString("textsetscoreformat",
			"({oursetscore}) | {ourscore} - {oppscore} | ({oppsetscore})"));

	private String overlayscoreformat = getScoreFormat(
			MainConfig.getString("textscoreformat", "{ourscore} - {oppscore}"));

	private String getOverlayScore() {
		if (set > 0 || (oursetscore > 0 || oppsetscore > 0)) {
			return String.format(overlaysetformat, ourname, ourscore, oppscore, oppname, desc, oursetscore, oppsetscore);
		}
		return String.format(overlayscoreformat, ourname, ourscore, oppscore, oppname, desc);
	}

	private static String getScoreFormat(String format) {
		return format.replace("{ourname}", "%1$s").replace("{ourscore}", "%2$d").replace("{oppscore}", "%3$d")
				.replace("{oppname}", "%4$s").replace("{info}", "%5$s").replace("{oursetscore}", "%6$d").replace("{oppsetscore}", "%7$d");
	}

	@Override
	public boolean go(CmdInfo inf) {
		if (inf.chkPut(ChatLevel.MOD, "!start")) {
			if (!ongoing) {
				if (inf.getArg() != null) {
					String[] args = inf.getArg().split(" ");

					int mpid = -1;
					boolean isteam = false;
					String ourpname = null;
					String opppname = null;
					boolean nowarmup = false;
					boolean autoname = false;

					for (String origarg : args) {
						String arg = origarg.toLowerCase();
						if (arg.startsWith("mp:")) {
							mpid = Integer.parseInt(arg.substring(3));
						} else if (arg.startsWith("team:")) {
							String teamstr = arg.substring(5);
							if (teamstr.equals("blue")) {
								isblue = true;
							} else if (teamstr.equals("red")) {
								isblue = false;
							} else {
								continue;
							}
							isteam = true;
						} else if (arg.startsWith("player:")) {
							String[] players = origarg.substring(7).split(",", 2);
							if (players.length == 2) {
								ourpname = players[0].replace('*', ' ');
								opppname = players[1].replace('*', ' ');
							}
						} else if (arg.startsWith("set:")) {
							set = Integer.parseInt(arg.substring(4));
						} else if (arg.equals("nowarmup")) {
							nowarmup = true;
						} else if (arg.equals("autoname")) {
							autoname = true;
						}
					}

					ongoing = true;

					if (mpid >= 0) {
						try {
							m = new Match(mpid, nowarmup);
						} catch (Exception e1) {
							reset();
							inf.send("Something is wrong with tracking");
							return true;
						}
						// this.mp = mpid;
						if (isteam) {
							if (autoname) {
								Names names = m.getNames();
								if (names != null) {
									if (isblue) {
										setName(names.blue(), names.red());
									} else {
										setName(names.red(), names.blue());
									}
								}
							}
							new AutoThread<TeamVS>(team -> {
								if (team.blueWon()) {
									if (isblue)
										win();
									else
										lose();
								} else {
									if (isblue)
										lose();
									else
										win();
								}
							}, TeamVS.class, this).start();
							inf.send("Now the bot will track this TEAM match.");
						} else {
							int ourid;
							int oppid;
							try {
								ourid = OsuApi.getUserId(ourpname);
								oppid = OsuApi.getUserId(opppname);
								if (ourid == -1 || oppid == -1) {
									throw new Exception();
								}
							} catch (Exception e) {
								inf.send("Wrong username.");
								e.printStackTrace();
								return true;
							}
							setName(ourpname, opppname);

							new AutoThread<H2H>(h2h -> {
								if (h2h.getWinner() == ourid) {
									win();
								} else if (h2h.getWinner() == oppid) {
									lose();
								}
							}, H2H.class, this).start();
							inf.send("Now the bot will track this 1V1 match.");
						}
					} else {
						inf.send("Now mods can add score by !win or !lose, but check if you have made some typo.");
					}
				} else {
					ongoing = true;
					inf.send("Now mods can add score by !win or !lose");
				}

				if (ongoing && overlay) {
					fw = new TextFileWrite("score.txt");
				}
			} else {
				inf.send("Match is not over yet.");
			}
		} else if (inf.chkPut(ChatLevel.MOD, "!setinfo")) {
			if (inf.getArg() == null)
				return true;

			desc = inf.getArg();
			inf.send("Info is now set.");
		} else if (inf.chkPut(ChatLevel.NORMAL, "!score")) {
			if (!ongoing)
				return true;

			inf.send(getScore());
		} else if (inf.chkPut(ChatLevel.MOD, "!win")) {
			if (!ongoing)
				return true;

			int n = 1;
			if (inf.getArg() != null) {
				String sc = inf.getArg().toLowerCase();
				if (sc.startsWith("set:")) {
					n = Integer.parseInt(sc.substring(4));
					winSet(n);
					n = -1;
				} else {
					n = Integer.parseInt(sc);
				}
			}
			for (int i = 0; i < n; i++) {
				win();
			}
			inf.send(getScore());
		} else if (inf.chkPut(ChatLevel.MOD, "!lose")) {
			if (!ongoing)
				return true;

			int n = 1;
			if (inf.getArg() != null) {
				String sc = inf.getArg().toLowerCase();
				if (sc.startsWith("set:")) {
					n = Integer.parseInt(sc.substring(4));
					loseSet(n);
					n = -1;
				} else {
					n = Integer.parseInt(sc);
				}
			}
			for (int i = 0; i < n; i++) {
				lose();
			}
			inf.send(getScore());
		} else if (inf.chkPut(ChatLevel.MOD, "!over")) {
			reset();
			inf.send("Now that every information is gone, you can start again.");
		} else if (inf.chkPut(ChatLevel.NORMAL, "!mp")) {
			if (!ongoing)
				return true;

			if (m != null) {
				inf.send("https://osu.ppy.sh/mp/" + m.getMP());
			}
		} else if (inf.chkPut(ChatLevel.MOD, "!reset")) {
			if (!ongoing)
				return true;

			if (inf.getArg() != null && inf.getArg().equalsIgnoreCase("all")) {
				resetScore(true);
			}

			resetScore();
			inf.send(getScore());
		} else if (inf.chkPut(ChatLevel.MOD, "!setname")) {
			if (inf.getArg() == null) {
				return true;
			}
			String[] t = inf.getArg().split(",");
			if (t.length != 2) {
				inf.send("Wrong Name");
				return true;
			}

			setName(t[0].replace('*', ' '), t[1].replace('*', ' '));

			inf.send("Team names are set.");
		} else {
			return false;
		}
		updateOverlay();
		return true;
	}

	private void setName(String our, String opp) {
		ourname = our;
		oppname = opp;
	}

	private void resetScore(boolean set) {
		if (set) {
			oursetscore = 0;
			oppsetscore = 0;
		} else {
			resetScore();
		}
	}

	private void resetScore() {
		ourscore = 0;
		oppscore = 0;
	}

	private void lose() {
		oppscore++;
		if (set > 0 && oppscore >= set) {
			oppscore = 0;
			ourscore = 0;
			oppsetscore++;
		}
	}

	private void loseSet(int set) {
		oppsetscore += set;
		resetScore();
	}

	private void win() {
		ourscore++;
		if (set > 0 && ourscore >= set) {
			oppscore = 0;
			ourscore = 0;
			oursetscore++;
		}
	}

	private void winSet(int set) {
		oursetscore += set;
		resetScore();
	}

	private String overlayScore = "";

	private void updateOverlay() {
		if (overlay) {
			String score = getOverlayScore();
			if (overlayScore.equals(score)) {
				return;
			}
			overlayScore = score;
			if (fw != null) {
				fw.write(score);
			}
		}
	}

}
