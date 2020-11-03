package tv.twitch.hwsnemo.autoreply.cmd.impl;

import java.util.List;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.Main;
import tv.twitch.hwsnemo.autoreply.NotEnabledException;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdInfo;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;
import tv.twitch.hwsnemo.autoreply.osu.Match;
import tv.twitch.hwsnemo.autoreply.osu.Match.Names;
import tv.twitch.hwsnemo.autoreply.osu.OsuApi;
import tv.twitch.hwsnemo.autoreply.osu.SendableException;
import tv.twitch.hwsnemo.autoreply.osu.result.H2H;
import tv.twitch.hwsnemo.autoreply.osu.result.Result;
import tv.twitch.hwsnemo.autoreply.osu.result.TeamVS;

public class MatchCmd implements Cmd {

	public MatchCmd() throws NotEnabledException {
		Main.throwOr("enablematchcmd");
		
		if (Main.getConfig().containsKey("scoreformat")) {
			scoreformat = getScoreFormat(Main.getConfig().get("scoreformat"));
		}
		
		if (Main.getConfig().containsKey("setscoreformat")) {
			setformat = getScoreFormat(Main.getConfig().get("setscoreformat"));
		}
	}

	private static interface AutoRun<T extends Result> {
		void go(T result);
	}

	private static class AutoThread<T extends Result> extends Thread {
		private AutoThread(AutoRun<T> run, Class<T> clazz, MatchCmd mc, Match m) {
			super(new Runnable() {

				@Override
				public void run() {
					try {
						Chat.send("Now I track the match automatically.");
						while (mc.ongoing) {
							List<Result> res = m.getNow();
							if (!res.isEmpty()) {
								for (Result r : res) {
									if (r.isDraw())
										continue;
									if (clazz.isInstance(r))
										run.go(clazz.cast(r));
								}
								Chat.send("Auto: " + mc.getScore());
							}
							Thread.sleep(3000L);
						}
					} catch (SendableException e) {
						Chat.send("Track Aborted: " + e.getMessage());
					} catch (Exception e) {
						Chat.send("An unknown exception occurred. Track is now disabled.");
						e.printStackTrace();
					}
					mc.reset();
				}

			});
		}
	}

	private int mp;

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

	private void reset() {
		ourname = "Our team";
		ourscore = 0;
		oursetscore = 0;
		oppname = "Opponent";
		oppscore = 0;
		oppsetscore = 0;
		desc = null;
		ongoing = false;
		isblue = true;
		m = null;
		mp = -1;
		set = -1;
	}
	
	private static String setformat = "%1$s (%5$d) | %2$d - %3$d | (%6$d) %4$s";
	// {ourname} ({oursetscore}) | {ourscore} - {oppscore} | ({oppsetscore}) {oppname}
	
	private static String scoreformat = "%1$s | %2$d - %3$d | %4$s";
	// {ourname} | {ourscore} - {oppscore} | {oppname}

	private String getScore() {
		if (set > 0 || (oursetscore > 0 || oppsetscore > 0)) {
			return String.format(setformat, ourname, oursetscore, ourscore, oppscore, oppsetscore, oppname);
		}
		return String.format(scoreformat, ourname, ourscore, oppscore, oppname);
	}
	
	private static String getScoreFormat(String format) {
		return format.replace("{ourname}", "%1$s").replace("{ourscore}", "%2$d").replace("{oppscore}", "%3$d")
				.replace("{oppname}", "%4$s").replace("{oursetscore}", "%5$d").replace("{oppsetscore}", "%6$d");
	}

	@Override
	public boolean go(CmdInfo inf) {
		if (inf.chkPut(CmdLevel.MOD, "!start")) {
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
						this.mp = mpid;
						if (isteam) {
							if (autoname) {
								Names names = m.getNames();
								if (names != null) {
									if (isblue) {
										ourname = names.blue();
										oppname = names.red();
									} else {
										ourname = names.red();
										oppname = names.blue();
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
							}, TeamVS.class, this, m).start();
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

							ourname = ourpname;
							oppname = opppname;

							new AutoThread<H2H>(h2h -> {
								if (h2h.getWinner() == ourid) {
									win();
								} else if (h2h.getWinner() == oppid) {
									lose();
								}
							}, H2H.class, this, m).start();
						}
					} else {
						inf.send("Now mods can add score by !win or !lose, but check if you have made some typo.");
					}
				} else {
					ongoing = true;
					inf.send("Now mods can add score by !win or !lose");
				}
			} else {
				inf.send("Match is not over yet.");
			}
		} else if (inf.chkPut(CmdLevel.MOD, "!setinfo")) {
			if (inf.getArg() == null)
				return true;
			
			desc = inf.getArg();
			inf.send("Info is now set.");
		} else if (inf.chkPut(CmdLevel.NORMAL, "!score")) {
			if (!ongoing)
				return true;

			inf.send(getScore() + (desc != null ? (" / " + desc) : ""));
		} else if (inf.chkPut(CmdLevel.MOD, "!win")) {
			if (!ongoing)
				return true;

			int n = 1;
			if (inf.getArg() != null) {
				String sc = inf.getArg().toLowerCase();
				if (sc.startsWith("set:")) {
					n = Integer.parseInt(sc.substring(4));
					oursetscore += n;
					resetScore();
					n = -1;
				} else {
					n = Integer.parseInt(sc);
				}
			}
			for (int i = 0; i < n; i++) {
				win();
			}
			inf.send(getScore());
		} else if (inf.chkPut(CmdLevel.MOD, "!lose")) {
			if (!ongoing)
				return true;

			int n = 1;
			if (inf.getArg() != null) {
				String sc = inf.getArg().toLowerCase();
				if (sc.startsWith("set:")) {
					n = Integer.parseInt(sc.substring(4));
					oppsetscore += n;
					resetScore();
					n = -1;
				} else {
					n = Integer.parseInt(sc);
				}
			}
			for (int i = 0; i < n; i++) {
				lose();
			}
			inf.send(getScore());
		} else if (inf.chkPut(CmdLevel.MOD, "!over")) {
			reset();
			inf.send("Now that every information is gone, you can start again.");
		} else if (inf.chkPut(CmdLevel.NORMAL, "!mp")) {
			if (!ongoing)
				return true;

			if (mp >= 0) {
				inf.send("https://osu.ppy.sh/mp/" + mp);
			}
		} else if (inf.chkPut(CmdLevel.MOD, "!reset")) {
			if (!ongoing)
				return true;

			if (inf.getArg() != null && inf.getArg().equalsIgnoreCase("all")) {
				resetScore(true);
			}

			resetScore();
			inf.send(getScore());
		} else if (inf.chkPut(CmdLevel.MOD, "!setname")) {
			if (inf.getArg() == null) {
				return true;
			}
			String[] t = inf.getArg().split(",");
			if (t.length != 2) {
				inf.send("Wrong Name");
				return true;
			}
			
			ourname = t[0].replace('*', ' ');
			oppname = t[1].replace('*', ' ');
			
			inf.send("Team names are set.");
		} else {
			return false;
		}

		return true;
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

	private void win() {
		ourscore++;
		if (set > 0 && ourscore >= set) {
			oppscore = 0;
			ourscore = 0;
			oursetscore++;
		}
	}

}
