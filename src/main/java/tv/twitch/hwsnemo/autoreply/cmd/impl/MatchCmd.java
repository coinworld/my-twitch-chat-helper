package tv.twitch.hwsnemo.autoreply.cmd.impl;

import java.util.List;

import org.pircbotx.hooks.events.MessageEvent;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdHistory;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;
import tv.twitch.hwsnemo.autoreply.osu.OsuApi;
import tv.twitch.hwsnemo.autoreply.osu.OsuApiException;
import tv.twitch.hwsnemo.autoreply.osu.UpdatingMatch;
import tv.twitch.hwsnemo.autoreply.osu.result.H2H;
import tv.twitch.hwsnemo.autoreply.osu.result.Result;
import tv.twitch.hwsnemo.autoreply.osu.result.TeamVS;

public class MatchCmd implements Cmd {

	private interface AutoRun<T extends Result> {
		void go(T result);
	}

	private class AutoThread<T extends Result> extends Thread {
		private AutoThread(AutoRun<T> run, Class<T> clazz) {
			super(new Runnable() {

				@Override
				public void run() {
					try {
						UpdatingMatch m = new UpdatingMatch(mp);
						Chat.send("Now I track the match automatically.");
						while (ongoing) {
							List<Result> res = m.getNow();
							if (!res.isEmpty()) {
								for (Result r : res) {
									if (r.isDraw())
										continue;
									if (clazz.isInstance(r))
										run.go(clazz.cast(r));
								}
								Chat.send("Auto: " + getScore());
							}
							Thread.sleep(3000L);
						}
					} catch (OsuApiException e) {
						Chat.send("Track Aborted: " + e.getMessage());
					} catch (Exception e) {
						Chat.send("An unknown exception occurred. Track is now disabled.");
						e.printStackTrace();
					}
					reset();
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

	private void reset() {
		ourname = "Our team";
		ourscore = 0;
		oursetscore = 0;
		oppname = "Opponent";
		oppscore = 0;
		oppsetscore = 0;
		desc = "Nothing here";
		ongoing = false;
		mp = -1;
		set = -1;
	}

	private String getScore() {
		if (oursetscore > 0 || oppsetscore > 0) {
			return ourname + " (" + oursetscore + ") | " + ourscore + " - " + oppscore + " | (" + oppsetscore + ") "
					+ oppname;
		}
		return ourname + " | " + ourscore + " - " + oppscore + " | " + oppname;
	}

	@Override
	public boolean go(String[] sp, MessageEvent event) {
		if (CmdHistory.checkAndPut(sp[0], event, "!start", CmdLevel.MOD)) {
			if (!ongoing) {
				if (sp.length != 1) {
					String[] args = sp[1].split(" ");

					int mpid = -1;
					boolean isteam = false;
					String ourpname = null;
					String opppname = null;

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
						}
					}
					
					ongoing = true;
					
					if (mpid >= 0) {
						this.mp = mpid;
						if (isteam) {
							new AutoThread<TeamVS>(team -> {
								if (team.blueWon()) {
									if (isblue)
										wewon(1);
									else
										welost(1);
								} else {
									if (isblue)
										welost(1);
									else
										wewon(1);
								}
							}, TeamVS.class).start();
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
								Chat.send("Wrong username.");
								e.printStackTrace();
								return true;
							}
							
							ourname = ourpname;
							oppname = opppname;
							
							new AutoThread<H2H>(h2h -> {
								if (h2h.getWinner() == ourid) {
									wewon(1);
								} else if (h2h.getWinner() == oppid) {
									welost(1);
								}
							}, H2H.class).start();
						}
					} else {
						Chat.send("Now mods can add score by !win or !lose, but check if you have made some typo.");
					}
				} else {
					ongoing = true;
					Chat.send("Now mods can add score by !win or !lose");
				}
			} else {
				Chat.send("Match is not over yet.");
			}
		} else if (CmdHistory.checkAndPut(sp[0], event, "!setinfo", CmdLevel.MOD)) {
			desc = sp[1];
			Chat.send("Info is now set.");
		} else if (CmdHistory.checkAndPut(sp[0], event, "!score", CmdLevel.NORMAL)) {
			if (!ongoing)
				return true;

			Chat.send(getScore());
		} else if (CmdHistory.checkAndPut(sp[0], event, "!win", CmdLevel.MOD)) {
			if (!ongoing)
				return true;

			int n = 1;
			if (sp.length != 1) {
				String sc = sp[1].toLowerCase();
				if (sc.startsWith("set:")) {
					n = Integer.parseInt(sc.substring(4));
					oursetscore += n;
					ourscore = 0;
					oppscore = 0;
					n = 0;
				} else {
					n = Integer.parseInt(sc);
				}
			}
			wewon(n);
			Chat.send("PogChamp " + getScore());
		} else if (CmdHistory.checkAndPut(sp[0], event, "!lose", CmdLevel.MOD)) {
			if (!ongoing)
				return true;

			int n = 1;
			if (sp.length != 1) {
				String sc = sp[1].toLowerCase();
				if (sc.startsWith("set:")) {
					n = Integer.parseInt(sc.substring(4));
					oppsetscore += n;
					ourscore = 0;
					oppscore = 0;
					n = 0;
				} else {
					n = Integer.parseInt(sc);
				}
			}
			welost(n);
			Chat.send("Sadge " + getScore());
		} else if (CmdHistory.checkAndPut(sp[0], event, "!over", CmdLevel.MOD)) {
			if (!ongoing)
				return true;

			Chat.send("Match is over / " + getScore());
			reset();
		} else if (CmdHistory.checkAndPut(sp[0], event, "!info", CmdLevel.NORMAL)) {
			if (!ongoing)
				return true;

			Chat.send(event.getUser().getNick() + " -> " + desc);
		} else if (CmdHistory.checkAndPut(sp[0], event, "!mp", CmdLevel.NORMAL)) {
			if (!ongoing)
				return true;

			if (mp >= 0) {
				Chat.send("https://osu.ppy.sh/mp/" + mp);
			}
		} else if (CmdHistory.checkAndPut(sp[0], event, "!reset", CmdLevel.MOD)) {
			if (!ongoing)
				return true;

			if (sp.length != 1 && sp[1].equalsIgnoreCase("all")) {
				oursetscore = 0;
				oppsetscore = 0;
			}

			ourscore = 0;
			oppscore = 0;
			Chat.send(getScore());
		} else {
			return false;
		}

		return true;
	}

	private void welost(int score) {
		if (score <= 0)
			return;
		oppscore += score;
		int setscore = getSetScore(oppscore);
		if (setscore > 0) {
			oppscore = 0;
			ourscore = 0;
			oppsetscore += setscore;
		}
	}
	
	private int getSetScore(int score) {
		int add = 0;
		if (set > 0 && score >= set) {
			while (true) {
				score -= set;
				if (score >= 0) {
					add++;
				} else {
					break;
				}
			}
		}
		return add;
	}

	private void wewon(int score) {
		if (score <= 0)
			return;
		ourscore += score;
		int setscore = getSetScore(ourscore);
		if (setscore > 0) {
			oppscore = 0;
			ourscore = 0;
			oursetscore += setscore;
		}
	}

}
