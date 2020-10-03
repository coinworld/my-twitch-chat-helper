package tv.twitch.hwsnemo.autoreply.cmd.impl;

import java.util.List;

import org.pircbotx.hooks.events.MessageEvent;

import tv.twitch.hwsnemo.autoreply.Chat;
import tv.twitch.hwsnemo.autoreply.cmd.Cmd;
import tv.twitch.hwsnemo.autoreply.cmd.CmdHistory;
import tv.twitch.hwsnemo.autoreply.cmd.CmdLevel;
import tv.twitch.hwsnemo.autoreply.osu.Match;
import tv.twitch.hwsnemo.autoreply.osu.OsuApi;
import tv.twitch.hwsnemo.autoreply.osu.OsuApiException;
import tv.twitch.hwsnemo.autoreply.osu.UpdatingMatch;
import tv.twitch.hwsnemo.autoreply.osu.result.H2H;
import tv.twitch.hwsnemo.autoreply.osu.result.Result;
import tv.twitch.hwsnemo.autoreply.osu.result.TeamVS;

public class MatchCmd implements Cmd {

	private class Act {
		private final boolean ourteam;
		private final int score;

		private Act(boolean ourteam, int score) {
			this.ourteam = ourteam;
			this.score = score;
		}
	}

	private interface AutoRun<T extends Result> {
		void go(T result);
	}

	private class AutoThread<T extends Result> extends Thread {
		private AutoThread(AutoRun<T> run, Class<T> clazz) {
			super(new Runnable() {

				@Override
				public void run() {
					try {
						// Match m = new Match(mp);
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

	private volatile Act act = null;

	{
		reset();
	}

	private String ourname;

	private volatile int ourscore;

	private String oppname;

	private volatile int oppscore;

	private String desc;

	private volatile boolean ongoing;

	private String getScore() {
		return ourname + " | " + ourscore + " - " + oppscore + " | " + oppname;
	}

	@Override
	public boolean go(String[] sp, MessageEvent event) {
		if (CmdHistory.checkAndPut(sp[0], event, "!start", CmdLevel.MOD)) {
			if (!ongoing) {
				boolean auto = false;
				if (sp.length != 1) {
					String[] args = sp[1].split(" ", 2);
					int mp = Integer.parseInt(args[0]);
					if (args[1].contains(",")) { // 1v1
						String[] players = args[1].split(",", 2);
						String our = players[0];
						String opp = players[1];
						int ourid;
						int oppid;

						try {
							ourid = OsuApi.getUserId(our);
							oppid = OsuApi.getUserId(opp);
							if (ourid == -1 || oppid == -1) {
								throw new Exception();
							}
						} catch (Exception e) {
							Chat.send("Wrong username.");
							e.printStackTrace();
							return true;
						}

						ourname = our;
						oppname = opp;

						ongoing = true;
						auto = true;
						this.mp = mp;

						new AutoThread<H2H>(h2h -> {
							if (h2h.getWinner() == ourid) {
								wewon(1);
							} else if (h2h.getWinner() == oppid) {
								welost(1);
							}
						}, H2H.class).start();
					} else {
						boolean blue;
						if (args[1].equalsIgnoreCase("blue")) {
							blue = true;
						} else if (args[1].equalsIgnoreCase("red")) {
							blue = false;
						} else {
							Chat.send("No team specified.");
							return true;
						}

						ongoing = true;
						auto = true;
						this.mp = mp;

						new AutoThread<TeamVS>(team -> {
							if (team.blueWon()) {
								if (blue)
									wewon(1);
								else
									welost(1);
							} else {
								if (blue)
									welost(1);
								else
									wewon(1);
							}
						}, TeamVS.class).start();
					}
				}
				if (!auto) {
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
			if (sp.length != 1)
				n = Integer.parseInt(sp[1]);
			wewon(n);
			Chat.send("PogChamp " + getScore());
		} else if (CmdHistory.checkAndPut(sp[0], event, "!lose", CmdLevel.MOD)) {
			if (!ongoing)
				return true;

			int n = 1;
			if (sp.length != 1)
				n = Integer.parseInt(sp[1]);
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
		} else if (CmdHistory.checkAndPut(sp[0], event, "!undo", CmdLevel.MOD)) {
			if (!ongoing)
				return true;

			undo();
			Chat.send(getScore());
		} else if (CmdHistory.checkAndPut(sp[0], event, "!mp", CmdLevel.NORMAL)) {
			if (!ongoing)
				return true;

			if (mp >= 0) {
				Chat.send("https://osu.ppy.sh/mp/" + mp);
			}
		} else if (CmdHistory.checkAndPut(sp[0], event, "!reset", CmdLevel.MOD)) {
			if (!ongoing)
				return true;

			ourscore = 0;
			oppscore = 0;
			Chat.send(getScore());
		} else {
			return false;
		}

		return true;
	}

	private void reset() {
		ourname = "Our team";
		ourscore = 0;
		oppname = "Opponent";
		oppscore = 0;
		desc = "Nothing here";
		ongoing = false;
		mp = -1;
	}

	private void undo() {
		if (act != null) {
			if (act.ourteam) {
				ourscore -= act.score;
			} else {
				oppscore -= act.score;
			}
			act = null;
		}
	}

	private void welost(int score) {
		oppscore += score;
		act = new Act(false, score);
	}

	private void wewon(int score) {
		ourscore += score;
		act = new Act(true, score);
	}

}
